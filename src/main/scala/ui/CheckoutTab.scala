package ui

import java.text.NumberFormat
import java.util.UUID

import db.DatabaseInterface
import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout._

class CheckoutTab(dbInterface: DatabaseInterface, userId: UUID) extends Tab {
  setText("Checkout")
  var cartItems: Option[List[(String, java.math.BigDecimal)]] = None

  val rootPane = new GridPane

  setOnSelectionChanged(_ => {
    refreshCart()
    getPaymentMethods()
  })

  val cartLabel = new Label("Shopping Cart:")
  val cartContents = new ListView[String]()
  cartContents.setEditable(false)
  cartContents.getSelectionModel.setSelectionMode(SelectionMode.SINGLE)

  GridPane.setColumnSpan(cartContents, 2)
  GridPane.setHgrow(cartContents, Priority.ALWAYS)

  // TODO: Add total if I can get the listview custom cells to work
  val totalLabel = new Label()
//  GridPane.setColumnSpan(totalLabel, 2)
  val removeButton = new Button("Remove")
  GridPane.setHalignment(removeButton, HPos.RIGHT)
//  removeButton.disableProperty().bind(cartContents.selectionModelProperty())
  removeButton.setOnAction(_ => {
    // TODO: switch to custom cells instead of trimming string
    dbInterface.removeFromCart(userId, cartContents.getSelectionModel.getSelectedItem.split('(').head.trim)
    refreshCart()
  })

  val paymentMethodsLabel = new Label("Payment Methods")
  val paymentMethods = new ListView[String]()
  paymentMethods.setEditable(false)
  paymentMethods.getSelectionModel.selectedItemProperty().addListener(_ => {
    validateCheckout()
  })
  GridPane.setHgrow(paymentMethods, Priority.ALWAYS)
  GridPane.setColumnSpan(paymentMethods, 2)

  val giftLabel = new Label("Gift?")
  val giftCheckbox = new CheckBox()
  val giftSection = new HBox(10)
  giftSection.getChildren.addAll(giftLabel, giftCheckbox)

  val recipientField = new TextField()
  recipientField.setPromptText("Recipient")
  recipientField.visibleProperty().bind(giftCheckbox.selectedProperty())
  recipientField.textProperty().addListener(_ => {
    validateCheckout()
  })

  val checkoutButton = new Button("Checkout")
  GridPane.setHalignment(checkoutButton, HPos.RIGHT)
  checkoutButton.setOnAction(_ => {
      dbInterface.checkout(
        userId,
        UUID.fromString(paymentMethods.getSelectionModel.getSelectedItem),
        if(giftCheckbox.isSelected) Some(recipientField.getText) else None)
      refreshCart()
    }
  )

  rootPane.addRow(0, cartLabel)
  rootPane.addRow(1, cartContents)
  rootPane.addRow(2, totalLabel, removeButton)
  rootPane.addRow(3, paymentMethodsLabel)
  rootPane.addRow(4, paymentMethods)
  rootPane.addRow(5, giftSection, recipientField)
  rootPane.add(checkoutButton, 1, 6)

  val rc1 = new RowConstraints()
  val rc2 = new RowConstraints()
  rc2.setPercentHeight(50)
  val rc3 = new RowConstraints()
  val rc4 = new RowConstraints()
  val rc5 = new RowConstraints()
  rc5.setPercentHeight(20)
  val rc6 = new RowConstraints()
  val rc7 = new RowConstraints()

  val cc1 = new ColumnConstraints()
  cc1.setPercentWidth(20)
  val cc2 = new ColumnConstraints()
  cc2.setPercentWidth(80)

  rootPane.getRowConstraints.addAll(rc1, rc2, rc3, rc4, rc5, rc6, rc7)
  rootPane.getColumnConstraints.addAll(cc1, cc2)

  GridPane.setMargin(cartLabel, new Insets(5, 0, 3, 10))
  GridPane.setMargin(cartContents, new Insets(0, 10, 5, 10))
  GridPane.setMargin(totalLabel, new Insets(0, 0, 0, 10))
  GridPane.setMargin(removeButton, new Insets(0, 10, 0, 0))
  GridPane.setMargin(paymentMethodsLabel, new Insets(5, 0, 3, 10))
  GridPane.setMargin(paymentMethods, new Insets(0, 10, 0, 10))
  GridPane.setMargin(giftSection, new Insets(5, 0, 3, 10))
  GridPane.setMargin(recipientField, new Insets(5, 10, 3, 0))
  GridPane.setMargin(checkoutButton, new Insets(0, 10, 0, 0))

  setContent(rootPane)

  private def formatCart(contents: List[(String, java.math.BigDecimal)]): Unit = {
    cartContents.getItems.setAll(contents.map({
      case (name, price) =>
        s"$name (${formatCurrency(price, "USD")})"
    }): _*)
  }

  private def formatCurrency(value: java.math.BigDecimal, currencyCode: String): String = {
    NumberFormat.getAvailableLocales.toList
      .map(NumberFormat.getCurrencyInstance)
      .filter(_.getCurrency.getCurrencyCode == currencyCode) match {
      case nf :: _ => s"${nf.format(value)} ($currencyCode)"
      case _ => s"$value ($currencyCode)"
    }
  }

  private def getPaymentMethods(): Unit = {
    paymentMethods.getItems.setAll(dbInterface.getPaymentMethods(userId).toList: _*)
  }

  private def refreshCart(): Unit = {
    cartItems = Some(dbInterface.getShoppingCart(userId))
    cartItems.foreach(formatCart)

    validateCheckout()
  }

  private def validateCheckout(): Unit = {
    if(giftCheckbox.isSelected) {
      checkoutButton.setDisable(recipientField.getText.isEmpty || dbInterface.ownsAnyGamesInCart(userId, Some(recipientField.getText)))
    } else {
      checkoutButton.setDisable(dbInterface.ownsAnyGamesInCart(userId, None))
    }

    if(paymentMethods.getSelectionModel.isEmpty || cartContents.getItems.isEmpty) {
      checkoutButton.setDisable(true)
    }
  }
}