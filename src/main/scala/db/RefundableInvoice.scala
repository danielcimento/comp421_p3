package db

import java.sql.Date
import java.util.UUID

case class RefundableInvoice(invoiceId: UUID, paymentDate: Date, recipientName: String, games: List[GamePurchase])

case class GamePurchase(gameName: String, sellPrice: java.math.BigDecimal, currencyCode: String)