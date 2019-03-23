package ui

import db.DatabaseInterface
import javafx.scene.control.TabPane

class OperationsTabPane(databaseInterface: DatabaseInterface) extends TabPane {
  // All our 5 queries will be separate tabs in the tab pane
  val lfmTab = new LookingForMoreTab()

  getTabs.addAll(lfmTab)
}
