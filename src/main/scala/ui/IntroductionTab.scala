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
    new Text("Looking for More")
  )
  titles.foreach(GridPane.setHalignment(_, HPos.CENTER))

  val tabDescription = new Text("Description")
  tabDescription.setFont(Font.font(null, FontWeight.BOLD, -1))

  val descriptions = List(
    tabDescription,
    new Text("Allows you to find all your friends who own a certain game.")
  )
  descriptions.foreach(GridPane.setHalignment(_, HPos.CENTER))

  val titleColumn = new ColumnConstraints()
  titleColumn.setPercentWidth(30.0)
  val descriptionColumn = new ColumnConstraints()
  descriptionColumn.setPercentWidth(70.0)
  mainPane.getColumnConstraints.addAll(titleColumn, descriptionColumn)

  mainPane.addColumn(0, titles: _*)
  mainPane.addColumn(1, descriptions: _*)

  mainPane.getChildren.forEach(GridPane.setMargin(_, new Insets(10, 0, 10, 0)))

  setContent(mainPane)
}
