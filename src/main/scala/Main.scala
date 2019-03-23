import db.DatabaseInterface
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control.{Alert, TextInputDialog}
import javafx.stage.{Stage, WindowEvent}
import ui.OperationsTabPane

object Main extends App {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[COMP421_P3], args: _*)
  }

  class COMP421_P3 extends Application {
    override def start(primaryStage: Stage): Unit = {
      primaryStage.setTitle("COMP421 P3")

      // TODO: See if we can mask the password when typing (low priority)
      val passwordPrompt = new TextInputDialog()
      passwordPrompt.setContentText("Please type password for user 'cs421g51': ")
      passwordPrompt.setHeaderText("Database Password")
      passwordPrompt.setTitle("Password")

      passwordPrompt
        .showAndWait()
          .ifPresent(pw => {
            primaryStage.setScene(new Scene(new OperationsTabPane(new DatabaseInterface(pw)), 640, 480))
            primaryStage.show()
          })
    }
  }
}