package ui

import java.util.UUID

import db.DatabaseInterface
import javafx.beans.binding.BooleanBinding
import javafx.collections.ListChangeListener
import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout.{ColumnConstraints, GridPane}

// Each of our query will be a Tab object that contains the visual elements we want to interact with
class LookingForMoreTab(dbInterface: DatabaseInterface, userId: UUID) extends Tab {
  setText("Looking for More")

  val rootPane = new GridPane()

  val gameNameLabel = new Label("Game Name: ")
  GridPane.setMargin(gameNameLabel, new Insets(10, 0, 0, 10))

  val gameNameField = new TextField()
  GridPane.setMargin(gameNameField, new Insets(0, 0, 0, 10))
  val searchButton = new Button("Search")
  GridPane.setHalignment(searchButton, HPos.RIGHT)
  GridPane.setMargin(searchButton, new Insets(0, 10, 0, 0))

  val resultsList = new ListView[String]()
  resultsList.setEditable(false)
  resultsList.getItems.addListener(_ => resultsList.setDisable(resultsList.getItems.isEmpty))

  GridPane.setColumnSpan(resultsList, 2)
  GridPane.setMargin(resultsList, new Insets(10))

  searchButton.setOnAction(_ => {
    resultsList.getItems.clear()
    dbInterface.getFriendsWhoOwn(userId, gameNameField.getText).foreach(addToList)
  })

  val cc1 = new ColumnConstraints()
  cc1.setPercentWidth(60)
  val cc2 = new ColumnConstraints()
  cc2.setPercentWidth(40)

  rootPane.getColumnConstraints.addAll(cc1, cc2)
  rootPane.setHgap(20)
  rootPane.setVgap(10)

  rootPane.addRow(0, gameNameLabel)
  rootPane.addRow(1, gameNameField, searchButton)
  rootPane.addRow(2, resultsList)

  setContent(rootPane)

  private def addToList(s: String) = {
    resultsList.getItems.add(s)
  }
}
