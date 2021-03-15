package application;
	
import com.sun.jna.NativeLibrary;

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
		NativeLibrary.addSearchPath("vlc", "C:\\Program Files\\VideoLAN\\VLC");
		try {
			Parent root = FXMLLoader.load(getClass()
	                    .getResource("/application/Main.fxml"));
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
	
    public void showWindow() throws Exception {
    	primaryStage = new Stage();
		start(primaryStage);
	}
}
