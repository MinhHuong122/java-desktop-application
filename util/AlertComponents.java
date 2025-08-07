package util;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AlertComponents {
    // Phương thức tạo Alert có icon
    private static Alert createAlert(AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Đặt icon cho Alert
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("/assets/img/LOGO.png"));

        return alert;
    }

    // Phương thức hiển thị Alert thông báo với kiểu tùy chọn
    public static void showAlert(AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = createAlert(alertType, title, headerText, contentText);
        alert.showAndWait();
    }

    // Phương thức hiển thị Alert thông báo với kiểu ERROR
    public static void showError(String title, String headerText, String contentText) {
        showAlert(AlertType.ERROR, title, headerText, contentText);
    }

    // Phương thức hiển thị Alert thông báo với kiểu WARNING
    public static void showWarning(String title, String headerText, String contentText) {
        showAlert(AlertType.WARNING, title, headerText, contentText);
    }

    // Phương thức hiển thị Alert thông báo với kiểu INFORMATION
    public static void showInformation(String title, String headerText, String contentText) {
        showAlert(AlertType.INFORMATION, title, headerText, contentText);
    }

    // Phương thức hiển thị Alert thông báo với kiểu CONFIRMATION (dùng để hỏi người dùng)
    public static boolean showConfirmation(String title, String headerText, String contentText) {
        Alert alert = createAlert(AlertType.CONFIRMATION, title, headerText, contentText);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
