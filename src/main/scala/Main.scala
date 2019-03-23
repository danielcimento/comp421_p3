import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import ui.OperationsTabPane

object Main extends App {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[COMP421_P3], args: _*)
  }

  class COMP421_P3 extends Application {
    override def start(primaryStage: Stage): Unit = {
      primaryStage.setTitle("COMP421 P3")

      primaryStage.setScene(new Scene(new OperationsTabPane(), 640, 480))

      primaryStage.show()
    }
  }
}