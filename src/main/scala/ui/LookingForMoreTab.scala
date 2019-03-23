package ui

import javafx.scene.control.{Button, Tab}
import javafx.scene.layout.GridPane

// Each of our query will be a Tab object that contains the visual elements we want to interact with
class LookingForMoreTab extends Tab {
  setText("Looking for More")

  val mainPane = new GridPane()

  val searchButton = new Button("Search")
  searchButton.setOnAction(e => {
    println("Test")
  })

  mainPane.add(searchButton, 1, 0)

  setContent(mainPane)
}
