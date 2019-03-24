import com.jcraft.jsch.JSchException
import db.DatabaseInterface
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control.{Alert, TextInputDialog}
import javafx.stage.{Stage, WindowEvent}
import ui.{LoginPrompt, OperationsTabPane}

import scala.annotation.tailrec

object Main extends App {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[COMP421_P3], args: _*)
  }

  class COMP421_P3 extends Application {
    var dbInterface: Option[DatabaseInterface] = None

    override def start(primaryStage: Stage): Unit = {
      Platform.setImplicitExit(true)

      primaryStage.setTitle("COMP421 P3")
      primaryStage.setResizable(false)
      primaryStage.setOnCloseRequest(_ => {
        dbInterface.foreach(_.close())
        Platform.exit()
      })

      promptLogin(primaryStage)
    }

    @tailrec
    private def promptLogin(primaryStage: Stage, error: Boolean = false): Unit = {
      try {
        val prompt = new LoginPrompt(error)
        prompt.showAndWait().ifPresent(x => x match {
          case Some((db, username)) =>
            val dbi = new DatabaseInterface(db)
            dbInterface = Some(dbi)

            val uid = dbi.getUserId(username)
            uid match {
              case Some(userId) =>
                primaryStage.setScene(new Scene(new OperationsTabPane(dbi, userId), 640, 480))
                primaryStage.show()
              case _ =>
                dbInterface.foreach(_.close())
                throw new RuntimeException()
            }
          case None => Platform.exit()
        })
      } catch {
        case e: JSchException => promptLogin(primaryStage, error = true)
        case _: Throwable => promptLogin(primaryStage, error = true)
      }
    }
  }
}