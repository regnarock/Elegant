package elegant;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Elegant extends Application {

    @Getter() @Setter(AccessLevel.PRIVATE)
    private static String title = "Elegant v0.1";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("elegant.fxml"));

        Parent root = loader.load();
        primaryStage.setTitle(title);
        primaryStage.setScene(new Scene(root, 800, 640));
        primaryStage.show();
        Controller controller = loader.<Controller>getController();
        controller.initData(primaryStage);
    }
}
