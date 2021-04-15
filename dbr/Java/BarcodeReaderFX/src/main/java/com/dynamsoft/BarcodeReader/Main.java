package com.dynamsoft.BarcodeReader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {
	
	private static Stage primaryStage; // **Declare static Stage**

    private void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    static public Stage getPrimaryStage() {
        return primaryStage;
    }
    
	@Override
	public void start(Stage primaryStage) {
		setPrimaryStage(primaryStage);
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/dynamsoft/BarcodeReader/fxml/Main.fxml"));
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
            primaryStage.setTitle("Barcode Reader");
            primaryStage.setScene(scene);
            primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
