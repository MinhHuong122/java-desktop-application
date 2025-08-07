package util;

import java.util.regex.Pattern;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class ValidationUtil {
    
    // Biểu thức chính quy kiểm tra email hợp lệ, yêu cầu kết thúc bằng @gmail.com
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@gmail\\.com$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Kiểm tra một TextField có trống hay không
     * @param textField TextField cần kiểm tra
     * @param errorBox HBox chứa thông báo lỗi
     * @return true nếu hợp lệ, false nếu trống
     */
    public static boolean isNotEmpty(TextField textField, HBox errorBox) {
        boolean valid = !textField.getText().trim().isEmpty();
        showError(errorBox, !valid, "Bạn cần nhập trường này!");
        return valid;
    }

    /**
     * Kiểm tra email có đúng định dạng không (dành cho TextField)
     * @param textField TextField email cần kiểm tra
     * @param errorBox HBox chứa thông báo lỗi email
     * @return true nếu email hợp lệ, false nếu sai định dạng
     */
    public static boolean isValidEmail(TextField textField, HBox errorBox) {
        String email = textField.getText().trim();
        boolean isValid = true;

        // Ẩn lỗi cũ
        errorBox.setVisible(false);
        errorBox.setManaged(false);

        if (email.isEmpty()) {
            showError(errorBox, true, "Email không được để trống!");
            return false;
        }

        // Kiểm tra định dạng email, yêu cầu kết thúc bằng @gmail.com
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(errorBox, true, "Email phải có định dạng hợp lệ!");
            return false;
        }

        // Kiểm tra ký tự không hợp lệ (ví dụ: backslash)
        if (email.contains("\\")) {
            showError(errorBox, true, "Email không được chứa ký tự \\!");
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra email có đúng định dạng không (dành cho String)
     * @param email Email cần kiểm tra
     * @return true nếu email hợp lệ, false nếu sai định dạng
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Kiểm tra định dạng email, yêu cầu kết thúc bằng @gmail.com
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return false;
        }

        // Kiểm tra ký tự không hợp lệ (ví dụ: backslash)
        if (email.contains("\\")) {
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra mật khẩu có đủ dài không (dành cho TextField)
     * @param passwordField TextField mật khẩu
     * @param errorBox HBox chứa lỗi
     * @return true nếu hợp lệ, false nếu quá ngắn
     */
    public static boolean isValidPassword(TextField passwordField, HBox errorBox) {
        boolean valid = passwordField.getText().trim().length() >= 6;
        showError(errorBox, !valid, "Mật khẩu phải có ít nhất 6 ký tự!");
        return valid;
    }

    /**
     * Kiểm tra mật khẩu có đủ dài không (dành cho String)
     * @param password Mật khẩu cần kiểm tra
     * @return true nếu hợp lệ, false nếu quá ngắn
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.trim().length() >= 6;
    }

    /**
     * Kiểm tra confirm password có khớp với password không (dành cho TextField)
     * @param passwordField TextField mật khẩu
     * @param confirmField TextField xác nhận mật khẩu
     * @param errorBox HBox chứa lỗi
     * @return true nếu khớp, false nếu không khớp
     */
    public static boolean isPasswordMatch(TextField passwordField, TextField confirmField, HBox errorBox) {
        boolean valid = passwordField.getText().trim().equals(confirmField.getText().trim());
        showError(errorBox, !valid, "Mật khẩu không trùng khớp!");
        return valid; // Fixed: Return the correct boolean value
    }

    /**
     * Kiểm tra confirm password có khớp với password không (dành cho String)
     * @param password Mật khẩu
     * @param confirmPassword Xác nhận mật khẩu
     * @return true nếu khớp, false nếu không khớp
     */
    public static boolean isPasswordMatch(String password, String confirmPassword) {
        return password != null && confirmPassword != null && password.trim().equals(confirmPassword.trim());
    }

    /**
     * Hiển thị hoặc ẩn lỗi
     * @param errorBox HBox lỗi
     * @param show true để hiển thị, false để ẩn
     * @param message Thông báo lỗi
     */
    private static void showError(HBox errorBox, boolean show, String message) {
        if (errorBox.getChildren().isEmpty()) {
            // Nếu HBox rỗng, tạo và thêm Text mới
            Text text = new Text(message);
            text.setFill(javafx.scene.paint.Color.RED);
            errorBox.getChildren().add(text);
        } else {
            // Lấy phần tử đầu tiên và kiểm tra kiểu
            Object firstChild = errorBox.getChildren().get(0);
            if (firstChild instanceof Text) {
                Text text = (Text) firstChild;
                if (show) {
                    text.setText(message); // Cập nhật thông báo lỗi
                }
            } else {
                // Nếu không phải Text, thay thế bằng Text mới
                Text text = new Text(message);
                text.setFill(javafx.scene.paint.Color.RED);
                errorBox.getChildren().set(0, text);
            }
        }
        errorBox.setVisible(show);
        errorBox.setManaged(show);
    }
}