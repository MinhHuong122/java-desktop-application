package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;


public class Main extends Application {
    // Create start application with primary stage
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load fxml file with fxml loader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mainPage.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 500);
            primaryStage.setScene(scene);
            //tắt tính năng mở rộng cửa sổ
            primaryStage.setResizable(false);

            // Set title
            primaryStage.setTitle("Tiệm ăn vặt Totoro");

            // Set app logo
            primaryStage.getIcons().add(new Image("/assets/img/LOGO.png"));

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Main method
    public static void main(String[] args) 
    {
        // Launch the application
        launch(args);
    }
}
