package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static HikariDataSource dataSource;

    static {
        // Cấu hình HikariCP
        HikariConfig config = new HikariConfig();

        // Thông tin kết nối từ Aiven
        String host = "java-app-donguyenminhhuong.i.aivencloud.com";
        String port = "15492";
        String databaseName = "fastfood_database";
        String user = "avnadmin";

        // URL kết nối với SSL
        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?ssl-mode=REQUIRED";
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        // Cấu hình tối ưu hóa hiệu suất
        config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Driver MySQL
        config.setMaximumPoolSize(10); // Số kết nối tối đa trong pool (tùy chỉnh theo nhu cầu)
        config.setMinimumIdle(5);      // Số kết nối tối thiểu luôn sẵn sàng
        config.setIdleTimeout(300000); // Thời gian chờ tối đa (5 phút)
        config.setMaxLifetime(1800000); // Thời gian sống tối đa của kết nối (30 phút)
        config.setConnectionTimeout(30000); // Timeout khi lấy kết nối (30 giây)

        // Cấu hình SSL (không kiểm tra chứng chỉ)
        config.addDataSourceProperty("useSSL", "true");
        config.addDataSourceProperty("verifyServerCertificate", "false");

        // Tối ưu hiệu suất truy vấn
        config.addDataSourceProperty("cachePrepStmts", "true"); // Cache PreparedStatements
        config.addDataSourceProperty("prepStmtCacheSize", "250"); // Kích thước cache
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // Giới hạn SQL cache

        // Khởi tạo DataSource
        dataSource = new HikariDataSource(config);
    }

    // Lấy kết nối từ pool
    public static Connection connectDB() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Đóng kết nối (trả lại pool)
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close(); // Không thực sự đóng mà trả về pool
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Đóng pool khi ứng dụng tắt (tùy chọn)
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
