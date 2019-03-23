package db

import java.sql._
import java.net.InetAddress
import java.util.UUID

import com.jcraft.jsch._

// This class will hold all the logic that connects to the database and executes our queries
// We aren't using encryption for our password in the app, because we aren't storing it anywhere
class DatabaseInterface(val password: String) {
  val connection: Connection = establishConnection


  // TODO: Change user_id to username
  def getFriendsWhoOwn(username: String, gameName: String): Iterator[String] = {
    val statement = connection.prepareStatement(
      """
          | select username from
          | (select u.user_id, username from users u
          | join friends f on u.user_id = f.user_id where f.friend_id = (select user_id from users where username = (?))
          | union
          | select friend_id as user_id, username from users u
          | join friends f on u.user_id = f.friend_id where f.user_id = (select user_id from users where username = (?))) my_friends
          | join
          | (select name, user_id from games g
          | join owns o on g.game_id = o.game_id) owned_games
          | on owned_games.user_id = my_friends.user_id
          | where owned_games.name = (?);
      """.stripMargin)
    statement.setString(1, username)
    statement.setString(2, username)
    statement.setString(3, gameName)


    val rs = statement.executeQuery()
    new Iterator[String] {
      def hasNext = rs.next()
      def next() = rs.getString("username")
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
  }
}
