package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

/**
 *
 */
public class VlcjJavaFxApplication extends Application {
	private static Stage primaryStage; // **Declare static Stage**

    private void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    static public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    private static MediaPlayerFactory mediaPlayerFactory;

    private static EmbeddedMediaPlayer embeddedMediaPlayer;

    private static ImageView videoImageView;

    public VlcjJavaFxApplication() {
        mediaPlayerFactory = new MediaPlayerFactory();
        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
    	setPrimaryStage(primaryStage);
        videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);
        embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(videoImageView));
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        videoImageView.fitWidthProperty().bind(root.widthProperty());
        videoImageView.fitHeightProperty().bind(root.heightProperty());

        root.setCenter(videoImageView);

        Scene scene = new Scene(root, 1200, 675, Color.BLACK);
        primaryStage.setTitle("vlcj JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        Main m = new Main();
        m.showWindow();
        
    }
    
    public static void play(String mrl) {
    	embeddedMediaPlayer.media().play(mrl);
    }
    
    public static void play(String mrl,String[] options) {
    	embeddedMediaPlayer.media().play(mrl,options);
    }
    
    public static ImageView getImageView() {
        return videoImageView;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

