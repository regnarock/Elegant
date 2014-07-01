package elegant;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Elegant extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("elegant.fxml"));

        Parent root = loader.load();
        primaryStage.setTitle("Elegant");
        primaryStage.setScene(new Scene(root, 800, 640));
        primaryStage.show();
        Controller controller = loader.<Controller>getController();
        controller.initData(primaryStage);
    }
/*
    /////////////////////////////////////start event handlers for navigation///////////////////////////////
    private EventHandler<ActionEvent> reload = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            webEngine.reload();
        }
    };

    private EventHandler<ActionEvent> go = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            System.out.println(addressField.getText());
            webEngine.load("http://"+addressField.getText());
        }
    };
*/
}
