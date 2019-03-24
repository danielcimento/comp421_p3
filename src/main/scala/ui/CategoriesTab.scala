package ui

import java.util.UUID

import db.DatabaseInterface
import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority}

class CategoriesTab(dbInterface: DatabaseInterface, userId: UUID) extends Tab {
  setText("Browse by Category")

  val rootPane = new GridPane()

  val categoriesLabel = new Label("Category: ")
  GridPane.setMargin(categoriesLabel, new Insets(10, 0, 0, 10))
  val categoriesDropdown = new ComboBox[String]()
  GridPane.setHgrow(categoriesDropdown, Priority.ALWAYS)
  GridPane.setMargin(categoriesDropdown, new Insets(10, 0, 0, 10))
  val searchButton = new Button("Search")
  GridPane.setHalignment(searchButton, HPos.RIGHT)
  GridPane.setMargin(searchButton, new Insets(10, 10, 0, 0))

  setOnSelectionChanged(_ => categoriesDropdown.getItems.setAll(dbInterface.getAllCategories().toList: _*))

  searchButton.setOnAction(_ => {
    results.getItems.clear()
    dbInterface.getGamesForCategory(categoriesDropdown.getValue) foreach {
      case (name, dev) => results.getItems.add(s"$name (made by $dev)")
    }
  })

  rootPane.addRow(0, categoriesLabel, categoriesDropdown, searchButton)

  // TODO: Add to cart option
  val results = new ListView[String]()
  results.setEditable(false)
  results.getItems.addListener(_ => results.setDisable(results.getItems.isEmpty))

  GridPane.setColumnSpan(results, 3)
  GridPane.setMargin(results, new Insets(10))

  rootPane.addRow(1, results)

  val cc1 = new ColumnConstraints()
  cc1.setPercentWidth(20)
  val cc2 = new ColumnConstraints()
  cc2.setPercentWidth(60)
  val cc3 = new ColumnConstraints()
  cc3.setPercentWidth(20)

  rootPane.getColumnConstraints.addAll(cc1, cc2, cc3)

  setContent(rootPane)
}
