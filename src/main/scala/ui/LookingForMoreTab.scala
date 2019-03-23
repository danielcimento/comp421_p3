package ui

import db.DatabaseInterface
import javafx.beans.binding.BooleanBinding
import javafx.collections.ListChangeListener
import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout.{ColumnConstraints, GridPane}

// Each of our query will be a Tab object that contains the visual elements we want to interact with
class LookingForMoreTab(dbInterface: DatabaseInterface) extends Tab {
  setText("Looking for More")

  val rootPane = new GridPane()

  val usernameLabel = new Label("My Username: ")
  GridPane.setMargin(usernameLabel, new Insets(10, 0, 0, 10))
  val gameNameLabel = new Label("Game Name: ")
  GridPane.setMargin(gameNameLabel, new Insets(10, 0, 0, 10))

  val usernameField = new TextField()
  GridPane.setMargin(usernameField, new Insets(0, 0, 0, 10))
  val gameNameField = new TextField()
  GridPane.setMargin(gameNameField, new Insets(0, 0, 0, 10))
  val searchButton = new Button("Search")
  GridPane.setHalignment(searchButton, HPos.RIGHT)
  GridPane.setMargin(searchButton, new Insets(0, 10, 0, 0))

  val resultsList = new ListView[String]()
  resultsList.setEditable(false)
  resultsList.getItems.addListener(_ => resultsList.setDisable(resultsList.getItems.isEmpty))

  GridPane.setColumnSpan(resultsList, 3)
  GridPane.setMargin(resultsList, new Insets(10, 10, 10, 10))

  searchButton.setOnAction(_ => {
    resultsList.getItems.clear()
    dbInterface.getFriendsWhoOwn(usernameField.getText, gameNameField.getText).foreach(addToList)
  })

  val unameCC = new ColumnConstraints()
  unameCC.setPercentWidth(40)
  val gnameCC = new ColumnConstraints()
  gnameCC.setPercentWidth(40)
  val buttonCC = new ColumnConstraints()
  buttonCC.setPercentWidth(20)

  rootPane.getColumnConstraints.addAll(unameCC, gnameCC, buttonCC)
  rootPane.setHgap(20)
  rootPane.setVgap(10)

  rootPane.addRow(0, usernameLabel, gameNameLabel)
  rootPane.addRow(1, usernameField, gameNameField, searchButton)
  rootPane.addRow(2, resultsList)

  setContent(rootPane)

  private def addToList(s: String) = {
    resultsList.getItems.add(s)
  }
}
