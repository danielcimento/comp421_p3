import db.DatabaseInterface
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control.{Alert, TextInputDialog}
import javafx.stage.{Stage, WindowEvent}
import ui.{LoginPrompt, OperationsTabPane}

object Main extends App {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[COMP421_P3], args: _*)
  }

  class COMP421_P3 extends Application {
    override def start(primaryStage: Stage): Unit = {
      Platform.setImplicitExit(true)
      var dbInterface: Option[DatabaseInterface] = None

      primaryStage.setTitle("COMP421 P3")
      primaryStage.setResizable(false)
      primaryStage.setOnCloseRequest(_ => {
        dbInterface.foreach(_.close())
        Platform.exit()
      })

      // TODO: Retry on failed login
      val prompt = new LoginPrompt()
      prompt.showAndWait().ifPresent(x => x match {
        case Some((db, username)) =>
          val dbi = new DatabaseInterface(db)
          dbInterface = Some(dbi)

          dbi.getUserId(username) match {
            case Some(userId) =>
              primaryStage.setScene(new Scene(new OperationsTabPane(dbi, userId), 640, 480))
              primaryStage.show()
            case _ =>
              dbi.close()
              Platform.exit()
          }



        case None => Platform.exit()
      })
    }
  }
}