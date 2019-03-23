package ui

import java.text.NumberFormat

import db.{DatabaseInterface, RefundableInvoice}
import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout.{ColumnConstraints, GridPane, VBox}
import javafx.scene.text.{Font, FontWeight, Text}

class RefundTab(dbInterface: DatabaseInterface) extends Tab {
  setText("Refunds")

  val rootPane = new GridPane()

  val unameLabel = new Label("My Username: ")
  GridPane.setMargin(unameLabel, new Insets(10, 0, 0, 10))
  val unameField = new TextField()
  GridPane.setMargin(unameField, new Insets(10, 0, 0, 10))
  val searchButton = new Button("Search Invoices")
  GridPane.setMargin(searchButton, new Insets(10, 10, 0, 0))
  GridPane.setHalignment(searchButton, HPos.RIGHT)

  searchButton.setOnAction(_ => {
    invoices.getPanes.setAll(
      dbInterface.getPayments(unameField.getText) map formatInvoice: _*
    )
  })

  val invoicesLabel = new Label("My Invoices: ")
  GridPane.setMargin(invoicesLabel, new Insets(10, 0, 0, 10))

  val invoices = new Accordion()
  GridPane.setColumnSpan(invoices, 3)
  GridPane.setMargin(invoices, new Insets(10, 10, 10, 10))

  val cc1 = new ColumnConstraints()
  cc1.setPercentWidth(20)
  val cc2 = new ColumnConstraints()
  cc2.setPercentWidth(60)
  val cc3 = new ColumnConstraints()
  cc3.setPercentWidth(20)
  rootPane.getColumnConstraints.addAll(cc1, cc2, cc3)

  rootPane.addRow(0, unameLabel, unameField, searchButton)
  rootPane.addRow(1, invoicesLabel)
  rootPane.addRow(2, invoices)

  setContent(rootPane)

  private def formatInvoice(refundableInvoice: RefundableInvoice): TitledPane = {
    val pane = new TitledPane()
    pane.setText(s"Purchase for ${refundableInvoice.recipientName} on ${refundableInvoice.paymentDate.toString}")

    val paneContent = new GridPane()

    val gameLabel = new Label("Game")
    gameLabel.setFont(Font.font(null, FontWeight.BOLD, -1))
    val sellPriceLabel = new Label("Sell Price")
    sellPriceLabel.setFont(Font.font(null, FontWeight.BOLD, -1))
    GridPane.setHalignment(sellPriceLabel, HPos.RIGHT)

    paneContent.addRow(0, gameLabel, sellPriceLabel)

    refundableInvoice.games.zipWithIndex foreach {
      case (g, i) =>
        val gameName = new Text(s"\t${g.gameName}")
        val gPrice = new Text(formatCurrency(g.sellPrice, g.currencyCode))
        GridPane.setHalignment(gPrice, HPos.RIGHT)

        paneContent.addRow(i + 1, gameName, gPrice)
    }

    val totalLabel = new Label("Total")
    totalLabel.setFont(Font.font(null, FontWeight.BOLD, -1))
    val total = new Text(formatCurrency(refundableInvoice.games.map(_.sellPrice).reduceRight(_.add(_)), refundableInvoice.games.head.currencyCode))
    GridPane.setHalignment(total, HPos.RIGHT)
    paneContent.addRow(refundableInvoice.games.size + 1, totalLabel, total)

    val refundButton = new Button("Refund")
    GridPane.setHalignment(refundButton, HPos.RIGHT)
    paneContent.add(refundButton, 1, refundableInvoice.games.size + 2)
    refundButton.setOnAction(_ => {
      refundButton.setDisable(true)
      refundButton.setText("Refunding...")
      dbInterface.refundInvoice(refundableInvoice)
      refundButton.setText("Refunded!")
    })

    val cc1 = new ColumnConstraints()
    val cc2 = new ColumnConstraints()

    cc1.setPercentWidth(80)
    cc2.setPercentWidth(20)
    paneContent.getColumnConstraints.setAll(cc1, cc2)
    paneContent.setVgap(3.0)

    pane.setContent(paneContent)
    pane
  }

  def formatCurrency(value: java.math.BigDecimal, currencyCode: String): String = {
    NumberFormat.getAvailableLocales.toList
      .map(NumberFormat.getCurrencyInstance)
      .filter(_.getCurrency.getCurrencyCode == currencyCode) match {
      case nf :: _ => s"${nf.format(value)} ($currencyCode)"
      case _ => s"$value ($currencyCode)"
      }
  }
}
