package ui

import javafx.geometry.{HPos, Insets, Pos}
import javafx.scene.control.{Button, Tab, TabPane}
import javafx.scene.layout._
import javafx.scene.text.{Font, FontWeight, Text}

class IntroductionTab(parent: TabPane) extends Tab {
  setText("Overview")

  private val mainPane = new GridPane()
  mainPane.prefHeightProperty().bind(parent.prefHeightProperty())
  mainPane.prefWidthProperty().bind(parent.prefWidthProperty())

  val tabName = new Text("Tab Name")
  tabName.setFont(Font.font(null, FontWeight.BOLD, -1))

  val titles = List(
    tabName,
    new Text("Browse by Category"),
    new Text("Looking for More"),
    new Text("Refunds"),
    new Text("Payment Methods"),
    new Text("Checkout")
  )
  titles.foreach(GridPane.setMargin(_, new Insets(10, 0, 0, 10)))

  val tabDescription = new Text("Description")
  tabDescription.setFont(Font.font(null, FontWeight.BOLD, -1))

  val descriptions = List(
    tabDescription,
    new Text("Shows all game categories and allows you to browse games by category."),
    new Text("Allows you to find all your friends who own a certain game."),
    new Text("Allows you to view all of your paid invoices and request a refund."),
    new Text("Allows you to manage your payment methods."),
    new Text("Allows you to purchase the games in your shopping cart with a registered payment method.")
  )
  descriptions.foreach(GridPane.setMargin(_, new Insets(10, 0, 0, 0)))

  val titleColumn = new ColumnConstraints()
  titleColumn.setPercentWidth(20.0)
  val descriptionColumn = new ColumnConstraints()
  descriptionColumn.setPercentWidth(80.0)
  mainPane.getColumnConstraints.addAll(titleColumn, descriptionColumn)

  mainPane.addColumn(0, titles: _*)
  mainPane.addColumn(1, descriptions: _*)

  setContent(mainPane)
}
