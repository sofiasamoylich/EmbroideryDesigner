package ua.kma.vyshyvka;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VyshyvkaApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 800);

        scene.getStylesheets().add(
                getClass().getResource("/css/eco-pixel.css").toExternalForm()
        );

        stage.setTitle("Vyshyvka Designer");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}