package ui

import javafx.geometry.HPos
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.effect.DropShadow
import javafx.scene.layout.{ColumnConstraints, GridPane}
import javafx.scene.paint.Color

class LoginPrompt(error: Boolean) extends Dialog[Option[(String, String)]] {
  setTitle("Login")
  setHeaderText("Please type in the database password for cs421g51 and the user you wish to identify as.")

  val loginButtonType = new ButtonType("Login", ButtonData.OK_DONE)
  getDialogPane.getButtonTypes.addAll(loginButtonType, ButtonType.CANCEL)

  val rootPane = new GridPane()

  val dbPasswordLabel = new Label("Database Password:")
  val usernameLabel = new Label("Username:")
  GridPane.setHalignment(dbPasswordLabel, HPos.RIGHT)
  GridPane.setHalignment(usernameLabel, HPos.RIGHT)

  val dbPassword = new PasswordField
  dbPassword.setPromptText("Database Password (for cs421g51)")
  val username = new TextField("alfred")
  username.setPromptText("Username (e.g. Alfred)")

  private val loginButton = getDialogPane.lookupButton(loginButtonType)
  loginButton.setDisable(true)

  private val buttonDisabler = () => loginButton.setDisable(dbPassword.getText.isEmpty || username.getText.isEmpty)
  username.textProperty().addListener(_ => buttonDisabler())
  dbPassword.textProperty().addListener(_ => buttonDisabler())

  rootPane.addRow(0, dbPasswordLabel, dbPassword)
  rootPane.addRow(1, usernameLabel, username)

  val cc1 = new ColumnConstraints()
  cc1.setPercentWidth(30)
  val cc2 = new ColumnConstraints()
  cc2.setPercentWidth(70)

  rootPane.setVgap(20)
  rootPane.setHgap(20)
  rootPane.getColumnConstraints.addAll(cc1, cc2)

  getDialogPane.setContent(rootPane)

  setResultConverter(btn => {
    if(btn == loginButtonType) {
      Some((dbPassword.getText, username.getText))
    } else {
      None
    }
  })

  val depth = 70 //Setting the uniform variable for the glow width and height

  val borderGlow= new DropShadow()
  borderGlow.setOffsetY(0f)
  borderGlow.setOffsetX(0f)
  borderGlow.setColor(Color.RED)
  borderGlow.setWidth(depth)
  borderGlow.setHeight(depth)

  if(error) {
    dbPassword.setEffect(borderGlow) //Apply the borderGlow effect to the JavaFX node
  }
}
