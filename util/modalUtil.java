package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import util.*;

public class modalUtil {

    /**
     
     * 
     * @param title Tiêu đề của modal 
     */
    public static void showModal(String position, String title, Stage currentStage) {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(modalUtil.class.getResource(position));
            Parent root = loader.load();

            // Get controller
            Stage modalStage = new Stage();
            
            // Set title for modal
            modalStage.setTitle(title);

            // Set icon for modal
            modalStage.getIcons().add(new Image(contantPropertyUtil.IMG_PATH + contantPropertyUtil.IMG_LOGO));

            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác ngoài modal
            modalStage.show();
            
            if (currentStage != null) currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
