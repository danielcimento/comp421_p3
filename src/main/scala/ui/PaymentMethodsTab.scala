package ui

import java.sql.Date
import java.util.UUID

import db.DatabaseInterface
import javafx.beans.property.StringProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority}

class PaymentMethodsTab(dbInterface: DatabaseInterface, userId: UUID) extends Tab {
  setText("Payment Methods")
  setOnSelectionChanged(_ => {
    refreshPaymentMethods()
  })

  val rootPane = new GridPane

  val paypalLabel = new Label("Paypal Accounts:")
  GridPane.setMargin(paypalLabel, new Insets(10, 0, 5, 10))

  val paypalAccounts = new ListView[String]()
  GridPane.setColumnSpan(paypalAccounts, 5)
  GridPane.setMargin(paypalAccounts, new Insets(0, 10, 5, 10))

  val emailLabel = new Label("Email Address:")
  GridPane.setMargin(emailLabel, new Insets(0, 0, 0, 10))
  val paypalEntry = new TextField()
  GridPane.setColumnSpan(paypalEntry, 2)

  val paypalAdd = new Button("Add")
  GridPane.setHgrow(paypalAdd, Priority.ALWAYS)
  GridPane.setMargin(paypalAdd, new Insets(0, 5, 0, 5))
  paypalAdd.setOnAction(_ => {
    dbInterface.addPaypal(userId, paypalEntry.getText)
    refreshPaymentMethods()
  })

  val paypalRemove = new Button("Delete")
  GridPane.setHgrow(paypalRemove, Priority.ALWAYS)
  GridPane.setMargin(paypalRemove, new Insets(0, 10, 0, 0))
  paypalRemove.setDisable(true)
  paypalAccounts.getSelectionModel.selectedItemProperty().addListener((_, _, newVal) => paypalRemove.setDisable(newVal == null))
  paypalRemove.setOnAction(_ => {
    dbInterface.deletePaypal(paypalAccounts.getSelectionModel.getSelectedItem)
    refreshPaymentMethods()
  })

  val cardsLabel = new Label("Credit/Debit Cards")
  GridPane.setMargin(cardsLabel, new Insets(5, 0, 0, 10))

  val cards = new ListView[String]()
  GridPane.setColumnSpan(cards, 5)
  GridPane.setMargin(cards, new Insets(0, 10, 5, 10))

  val cardNumberField = new TextField()
  cardNumberField.textProperty().addListener(_ => new ChangeListener[String] {
    override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      if (cardNumberField.getText().length() > 12) {
        val s = cardNumberField.getText().substring(0, 12)
        cardNumberField.setText(s)
      }
    }
  })
  cardNumberField.textProperty().addListener((observable, oldValue, newValue) =>
    if (newValue.length > 12) observable.asInstanceOf[StringProperty].setValue(oldValue)
  )

  cardNumberField.setPromptText("New Card Number")

  GridPane.setMargin(cardNumberField, new Insets(0, 5, 5, 10))
  val expiration = new DatePicker()
  expiration.setPromptText("Expiration Date")
  GridPane.setMargin(expiration, new Insets(0, 0, 5, 0))
  val cardType = new ComboBox[String]()
  GridPane.setMargin(cardType, new Insets(0, 5, 5, 0))
  cardType.getItems.setAll("Debit", "Credit")
  cardType.setPromptText("Debit/Credit")

  val cardAdd = new Button("Add")
  GridPane.setHgrow(cardAdd, Priority.ALWAYS)
  GridPane.setMargin(cardAdd, new Insets(0, 10, 5, 0))
  cardAdd.setOnAction(_ => {
    val (cardNum, exp, cardTy) = (cardNumberField.getText, expiration.getValue, cardType.getValue)
    dbInterface.addCard(userId, cardNum, Date.valueOf(exp), cardTy)
    refreshPaymentMethods()
  })

  val cardDelete = new Button("Delete")
  cardDelete.setDisable(true)
  GridPane.setHgrow(cardDelete, Priority.ALWAYS)
  GridPane.setMargin(cardDelete, new Insets(0, 10, 5, 0))
  cards.getSelectionModel.selectedItemProperty().addListener((_, _, newVal) => cardDelete.setDisable(newVal == null))
  cardDelete.setOnAction(_ => {
    dbInterface.deleteCard(cards.getSelectionModel.getSelectedItem.substring(0, 12))
    refreshPaymentMethods()
  })

  rootPane.addRow(0, paypalLabel)
  rootPane.addRow(1, paypalAccounts)
  rootPane.addRow(2, emailLabel, paypalEntry)
  rootPane.add(paypalAdd, 3, 2)
  rootPane.add(paypalRemove, 4, 2)
  rootPane.addRow(3, cardsLabel)
  rootPane.addRow(4, cards)
  rootPane.addRow(5, cardNumberField, expiration, cardType, cardAdd, cardDelete)

  val cc1 = new ColumnConstraints()
  cc1.setPercentWidth(30)
  val cc2 = new ColumnConstraints()
  cc2.setPercentWidth(30)
  val cc3 = new ColumnConstraints()
  cc3.setPercentWidth(20)
  val cc4 = new ColumnConstraints()
  cc4.setPercentWidth(10)
  val cc5 = new ColumnConstraints()
  cc5.setPercentWidth(10)

  rootPane.getColumnConstraints.addAll(cc1, cc2, cc3, cc4, cc5)

  setContent(rootPane)

  private def refreshPaymentMethods(): Unit = {
    paypalAccounts.getItems.setAll(dbInterface.getPaypalAccounts(userId): _*)
    cards.getItems.setAll(dbInterface.getCards(userId): _*)
  }
}
