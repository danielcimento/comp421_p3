package ui

import javafx.scene.control.TabPane

class OperationsTabPane() extends TabPane {
  // All our 5 queries will be separate tabs in the tab pane
  val lfmTab = new LookingForMoreTab()

  getTabs.addAll(lfmTab)
}
