package ui

import java.util.UUID

import db.DatabaseInterface
import javafx.scene.control.Tab

class PaymentMethodsTab(dbInterface: DatabaseInterface, userId: UUID) extends Tab {
  setText("Payment Methods")
}
