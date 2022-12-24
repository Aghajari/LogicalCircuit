package com.aghajari;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXMain extends Application  {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("board.fxml"));
        primaryStage.setTitle("LogicalCircuit");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }

    public static void launchLogicalCircuit(String[] args) {
        launch(args);
    }
}
