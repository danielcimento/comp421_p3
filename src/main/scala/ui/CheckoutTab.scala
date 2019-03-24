package ui

import java.util.UUID

import db.DatabaseInterface
import javafx.scene.control.Tab

class CheckoutTab(dbInterface: DatabaseInterface, userId: UUID) extends Tab {
  setText("Checkout")
}
