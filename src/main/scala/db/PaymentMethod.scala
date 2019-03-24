package db

import java.sql.Date

sealed trait PaymentMethod

case class Paypal(emailAddress: String) extends PaymentMethod
case class Card(cardNumber: String, expDate: Date, cardType: String) extends PaymentMethod
