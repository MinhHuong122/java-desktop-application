package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

// Import util
import util.*;

public class mainController {
    // Internal properties
    private String position = "";
    private String iconType = "/assets/img/LOGO.png";

    @FXML private MediaView mediaView;
    private MediaPlayer mediaPlayer;

    // FXML Property
    @FXML
    private Button btnDangNhap;

    @FXML
    private Button btnDangKy;

    @FXML private ImageView imageView;
    
    private String[] imagePaths = {
        "/assets/img/1.png",
        "/assets/img/2.png",
        "/assets/img/3.png"
    };
    
    private int currentIndex = 0;
    private PauseTransition autoPlay;

    @FXML
    // Initialize method
    public void initialize() 
    {
        imageView.setImage(new Image(getClass().getResourceAsStream(imagePaths[currentIndex])));
        autoPlay = new PauseTransition(Duration.seconds(3));
        autoPlay.setOnFinished(e -> nextImage());
        autoPlay.play();

        Media media = new Media(getClass().getResource("/assets/vid/iLoveYt.net_YouTube_Ghibli-food-compilation-Best-food-scenes_Media_yRO1XV1D1Os_004_360p.mp4").toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();

        // SetOnAction for button
        btnDangNhap.setOnAction(e -> {
            try {
                // Dừng video khi bấm Đăng nhập
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
        
                position = "/view/loginPage.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(position));
                Parent root = loader.load();
                Stage modalStage = new Stage();
                modalStage.setResizable(false);
                modalStage.setTitle("ĐĂNG NHẬP");
                modalStage.getIcons().add(new Image(contantPropertyUtil.IMG_PATH + contantPropertyUtil.IMG_LOGO));
        
                modalStage.setScene(new Scene(root));
                modalStage.initModality(Modality.APPLICATION_MODAL);
                modalStage.show();
        
                // 🔥 Đóng cửa sổ MainPage
                Stage mainStage = (Stage) btnDangNhap.getScene().getWindow();
                mainStage.close();
        
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        
        btnDangKy.setOnAction(e -> {
            try {
                // Dừng video khi bấm Đăng ký
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
        
                position = "/view/registerPage.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(position));
                Parent root = loader.load();
                Stage modalStage = new Stage();
                modalStage.setResizable(false);
                modalStage.setTitle("ĐĂNG KÝ");
                modalStage.getIcons().add(new Image(contantPropertyUtil.IMG_PATH + contantPropertyUtil.IMG_LOGO));
        
                modalStage.setScene(new Scene(root));
                modalStage.initModality(Modality.APPLICATION_MODAL);
                modalStage.show();
        
                // 🔥 Đóng cửa sổ MainPage
                Stage mainStage = (Stage) btnDangKy.getScene().getWindow();
                mainStage.close();
        
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });   
    }
    
    @FXML
    private void nextImage() {
        currentIndex = (currentIndex + 1) % imagePaths.length;
        updateImage();
    }

    @FXML
    private void prevImage() {
        currentIndex = (currentIndex - 1 + imagePaths.length) % imagePaths.length;
        updateImage();
    }

    private void updateImage() {
        imageView.setImage(new Image(getClass().getResourceAsStream(imagePaths[currentIndex])));
        autoPlay.stop();  // Dừng chạy tự động khi bấm nút
        autoPlay.play();  // Restart lại sau khi đổi ảnh
    }

}