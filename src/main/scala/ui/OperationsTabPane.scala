package ui

import java.util.UUID

import db.DatabaseInterface
import javafx.scene.control.TabPane
import javafx.scene.control.TabPane.TabClosingPolicy

class OperationsTabPane(databaseInterface: DatabaseInterface, userId: UUID) extends TabPane {
  // All our 5 queries will be separate tabs in the tab pane
  val introPane = new IntroductionTab(this)
  val lfmTab = new LookingForMoreTab(databaseInterface, userId)
  val refundTab = new RefundTab(databaseInterface, userId)
  val categoriesTab = new CategoriesTab(databaseInterface, userId)
  val paymentMethodsTab = new PaymentMethodsTab(databaseInterface, userId)
  val checkoutTab = new CheckoutTab(databaseInterface, userId)

  setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE)
  getTabs.addAll(introPane, categoriesTab, lfmTab, refundTab, paymentMethodsTab, checkoutTab)
}
