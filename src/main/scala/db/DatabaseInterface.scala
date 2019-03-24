package db

import java.sql._
import java.net.InetAddress
import java.util.UUID

import com.jcraft.jsch._

// This class will hold all the logic that connects to the database and executes our queries
// We aren't using encryption for our password in the app, because we aren't storing it anywhere
class DatabaseInterface(val password: String) {
  val connection: Connection = establishConnection

  def getFriendsWhoOwn(userId: UUID, gameName: String): Iterator[String] = {
    val statement = connection.prepareStatement(
      """
          | select username from
          | (select u.user_id, username from users u
          | join friends f on u.user_id = f.user_id where f.friend_id = (?)
          | union
          | select friend_id as user_id, username from users u
          | join friends f on u.user_id = f.friend_id where f.user_id = (?)) my_friends
          | join
          | (select name, user_id from games g
          | join owns o on g.game_id = o.game_id) owned_games
          | on owned_games.user_id = my_friends.user_id
          | where owned_games.name = (?);
      """.stripMargin)
    statement.setObject(1, userId)
    statement.setObject(2, userId)
    statement.setString(3, gameName)


    val rs = statement.executeQuery()
    new Iterator[String] {
      def hasNext = rs.next()
      def next() = rs.getString("username")
    }
  }

  def getPayments(userId: UUID): List[RefundableInvoice] = {
    val statement = connection.prepareStatement(
      """
        | select iid, payment_date, username, sell_price, currency, name from
        | (((select invoice_id as iid, payer_id, payment_date, recipient_id, refunded from invoices i) invcs join contains c
        | on c.invoice_id = invcs.iid) k join games g on k.game_id = g.game_id) l join users u on u.user_id = l.recipient_id
        | where refunded = false and payer_id = (?);
      """.stripMargin)
    statement.setObject(1, userId)

    val rs = statement.executeQuery()
    val columns = 1 to rs.getMetaData.getColumnCount map rs.getMetaData.getColumnName
    val rawResults = Iterator.continually(rs).takeWhile(_.next()).map { rs =>
      columns.map(rs.getObject)
    }

    // Group all our records by the invoice id
    rawResults.toList.groupBy(_(0).asInstanceOf[UUID]).map {
      case (iid, lineItemData) =>
        RefundableInvoice(
          iid,
          lineItemData.head(1).asInstanceOf[Date],
          lineItemData.head(2).asInstanceOf[String],
          lineItemData.map(li => GamePurchase(li(5).asInstanceOf[String], li(3).asInstanceOf[java.math.BigDecimal], li(4).asInstanceOf[String]))
        )
    }.toList
  }

  def refundInvoice(invoice: RefundableInvoice): Unit = {
    val refundStatement = connection.prepareStatement(
      """
        | update invoices set refunded = true
        | where invoice_id = (?);
        |
        | delete from owns
        | where user_id = (select recipient_id from invoices where invoice_id = (?))
        | and game_id in (select game_id from contains where invoice_id = (?));
      """.stripMargin)

    refundStatement.setObject(1, invoice.invoiceId)
    refundStatement.setObject(2, invoice.invoiceId)
    refundStatement.setObject(3, invoice.invoiceId)

    refundStatement.executeUpdate()
  }

  def getAllCategories(): Iterator[String] = {
    val select = connection.prepareStatement(
      """
        | select name from categories;
      """.stripMargin)

    val rs = select.executeQuery()
    new Iterator[String] {
      def hasNext = rs.next()
      def next() = rs.getString("name")
    }
  }

  def getGamesForCategory(category: String): Iterator[(String, String)] = {
    val select = connection.prepareStatement(
      """
        | select name, developer from games
        | join belongs_to b on games.game_id = b.game_id
        | where category_name = (?);
      """.stripMargin)

    select.setString(1, category)

    val rs = select.executeQuery()
    new Iterator[(String, String)] {
      def hasNext: Boolean = rs.next()
      def next() = (rs.getString("name"), rs.getString("developer"))
    }
  }

  def addPaymentMethod(user: UUID, paymentMethod: PaymentMethod): Unit = {
    val newId = UUID.randomUUID()
    val statement = connection.prepareStatement(
      s"""
        | insert into payment_methods values ((?), ${paymentMethod.isInstanceOf[Paypal]}, (?));
      """.stripMargin)
    statement.setObject(1, newId)
    statement.setObject(2, user)
    statement.executeUpdate()

    paymentMethod match {
      case Paypal(email) =>
        val p = connection.prepareStatement(
          s"""
            | insert into paypal_accounts values ((?), (?));
          """.stripMargin)
        p.setObject(1, newId)
        p.setString(2, email)

        p.executeUpdate()
      case Card(num, exp, typ) =>
        val c = connection.prepareStatement(
          """
            | insert into cards values ((?), (?), (?), (?));
          """.stripMargin)
        c.setObject(1, newId)
        c.setString(2, num)
        c.setDate(3, exp)
        c.setString(4, typ)

        c.executeUpdate()
    }
  }

  def addToCart(user: UUID, gameName: String): Unit = {
    val statement = connection.prepareStatement(
      """
        | insert into shopping_cart (user_id, game_id)
        | select (?), game_id from games where name = (?)
        | on conflict do nothing;
      """.stripMargin)

    statement.setObject(1, user)
    statement.setString(2, gameName)

    statement.executeUpdate()
  }

  def removeFromCart(user: UUID, gameName: String): Unit = {
    val statement = connection.prepareStatement(
      """
        | delete from shopping_cart
        | where user_id = (?)
        | and game_id = (select game_id from games where name = (?));
      """.stripMargin)

    statement.setObject(1, user)
    statement.setString(2, gameName)

    statement.executeUpdate()
  }

  def checkout(user: UUID, paymentId: UUID, recipient: Option[String]): Unit = {
    // TODO: Create a new invoice object
    // TODO: Associate all the games in the user's shopping cart with the invoice at their current price
    // TODO: Add all the games to a user's owned games
    val recipId = recipient map {
      recipName => val recip = connection.prepareStatement(
        """
          | select user_id from users where username = (?) limit 1;
        """.stripMargin)

        recip.setString(1, recipName)
        val rs = recip.executeQuery()

        if(!rs.next()) return

        rs.getObject("user_id").asInstanceOf[UUID]
    }

    val newId = UUID.randomUUID()

    val createInvoice = connection.prepareStatement(
      """
        | insert into invoices values ((?), (?), (?), (?), NOW(), false);
      """.stripMargin)
    createInvoice.setObject(1, newId)
    createInvoice.setObject(2, user)
    createInvoice.setObject(3, recipId.getOrElse(user))
    createInvoice.setObject(4, paymentId)
    createInvoice.executeUpdate()

    val associateItems = connection.prepareStatement(
      """
        | insert into contains (invoice_id, game_id, sell_price, currency)
        | select (?), s.game_id, value, 'USD' from shopping_cart s, pricings p
        | where p.game_id = s.game_id and p.currency = 'USD' and s.user_id = (?);
      """.stripMargin)
    associateItems.setObject(1, newId)
    associateItems.setObject(2, user)
    associateItems.executeUpdate()

    val giveGames = connection.prepareStatement(
      """
        | insert into owns (user_id, game_id)
        | select (?), game_id from shopping_cart s
        | where s.user_id = (?)
        | on conflict do nothing;
      """.stripMargin)
    giveGames.setObject(1, recipId.getOrElse(user))
    giveGames.setObject(2, user)

    giveGames.executeUpdate()

    val cleanCart = connection.prepareStatement(
      """
        | delete from shopping_cart where user_id = (?);
      """.stripMargin)
    cleanCart.setObject(1, user)

    cleanCart.executeUpdate()
  }

  def getShoppingCart(user: UUID): List[(String, java.math.BigDecimal)] = {
    val select = connection.prepareStatement(
      """
        | select name, value from shopping_cart s, games g, pricings p
        |	where currency = 'USD' and s.user_id = (?)
        |	and s.game_id = g.game_id and s.game_id = p.game_id;
      """.stripMargin)
    select.setObject(1, user)

    val rs = select.executeQuery()
    val columns = 1 to rs.getMetaData.getColumnCount map rs.getMetaData.getColumnName
    val rawResults = Iterator.continually(rs).takeWhile(_.next()).map { rs =>
      columns.map(rs.getObject)
    }
    rawResults.map(is => (is(0).asInstanceOf[String], is(1).asInstanceOf[java.math.BigDecimal])).toList
  }

  def getPaymentMethods(userId: UUID): Iterator[String] = {
    val select = connection.prepareStatement(
      """
        | select payment_id from payment_methods where user_id = (?);
      """.stripMargin)
    select.setObject(1, userId)

    val rs = select.executeQuery()
    new Iterator[String] {
      def hasNext: Boolean = rs.next()
      def next() = rs.getString("payment_id")
    }
  }

  def ownsAnyGamesInCart(cartOwner: UUID, recipient: Option[String]): Boolean = {
    val select = recipient match {
      case None => connection.prepareStatement(
        """
          | select * from shopping_cart s join owns o on o.game_id = s.game_id and o.user_id = s.user_id where s.user_id = (?);
        """.stripMargin)
      case Some(_) => connection.prepareStatement(
        """
          | select s.game_id from shopping_cart s
          | where s.user_id = (?)
          | and s.game_id in (select o.game_id from owns o
          | where o.user_id = (select user_id from users where username = (?)));
        """.stripMargin)
    }
    select.setObject(1, cartOwner)
    recipient.foreach(name => select.setString(2, name))

    select.executeQuery().next()
  }

  def getUserId(username: String): Option[UUID] = {
    val select = connection.prepareStatement(
      """
        | select user_id from users where username = (?) limit 1;
      """.stripMargin)
    select.setString(1, username)

    val rs = select.executeQuery()
    if(rs.next()) {
      Some(rs.getObject("user_id").asInstanceOf[UUID])
    } else {
      None
    }
  }

  private def establishConnection: Connection = {
    val lport = 5432
    val rhost = "localhost"
    val rport = 5432

    val username = "cs421g51"

    try {
      val jsch = new JSch()
      val session = jsch.getSession(username, "comp421.cs.mcgill.ca", 22)
      session.setPassword(password)
      session.setConfig("StrictHostKeyChecking", "no")
      session.connect()
      session.setPortForwardingL(lport, rhost, rport)
    } catch {
      case e: Exception => e.printStackTrace()
    }

    // Register the driver.  You must register the driver before you can use it.
    try {
      DriverManager.registerDriver (new org.postgresql.Driver())
    } catch{
      case cnfe: ClassNotFoundException => println("Class not found")
    }

    val url: String = s"jdbc:postgresql://$rhost:$lport/cs421"
    DriverManager.getConnection(url, username, password)

    // TODO: Make sure we close connection on termination.
  }

  def close() = {
    connection.close()
  }
}
