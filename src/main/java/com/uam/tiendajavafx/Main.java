package com.uam.tiendajavafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uam/tiendajavafx/main-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Tienda JavaFX - PostgreSQL + JDBC");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
