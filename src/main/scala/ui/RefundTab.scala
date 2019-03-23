package ui

import db.{DatabaseInterface, RefundableInvoice}
import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout.{ColumnConstraints, GridPane}

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
    pane.setText(refundableInvoice.invoiceId.toString)
    println(refundableInvoice)
    pane
  }
}
