package ui

import db.DatabaseInterface
import javafx.scene.control.TabPane
import javafx.scene.control.TabPane.TabClosingPolicy

class OperationsTabPane(databaseInterface: DatabaseInterface) extends TabPane {
  // All our 5 queries will be separate tabs in the tab pane
  val introPane = new IntroductionTab(this)
  val lfmTab = new LookingForMoreTab(databaseInterface)

  setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE)
  getTabs.addAll(introPane, lfmTab)
}
