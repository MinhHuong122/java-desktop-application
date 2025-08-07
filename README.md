# 🍜 Phần Mềm Quản Lý Cửa Hàng Ăn Vặt - Totoro Shop



<p align="center">

&nbsp; <img src="./assets/img/1.png" alt="Totoro Shop Banner" width="700"/>

</p>



<p align="center">

&nbsp; <img src="https://img.shields.io/badge/Java-11 | 17-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white" alt="Java Version">

&nbsp; <img src="https://img.shields.io/badge/JavaFX-17-007396?style=for-the-badge\&logo=java\&logoColor=white" alt="JavaFX Version">

&nbsp; <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge\&logo=mysql\&logoColor=white" alt="MySQL Version">

&nbsp; <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge" alt="License: MIT">

</p>



Đây là một ứng dụng Desktop được phát triển để quản lý toàn diện các hoạt động của một cửa hàng ăn vặt. Dự án sử dụng ngôn ngữ lập trình Java, giao diện được thiết kế bằng JavaFX và Scene Builder, cùng hệ quản trị cơ sở dữ liệu MySQL để lưu trữ dữ liệu.



## ✨ Tính Năng Nổi Bật



-   **📈 Quản lý Doanh thu:** Theo dõi và thống kê doanh thu theo ngày, tháng, năm.

-   **📦 Quản lý Sản phẩm:** Thêm, xóa, sửa thông tin các món ăn, đồ uống. Phân loại sản phẩm theo danh mục.

-   **🧾 Quản lý Hóa đơn:** Tạo và quản lý hóa đơn bán hàng, xem lại lịch sử giao dịch.

-   **👥 Quản lý Nhân viên:** Quản lý thông tin nhân viên, phân quyền truy cập.

-   **📊 Thống kê & Báo cáo:** Tạo các báo cáo trực quan về tình hình kinh doanh, sản phẩm bán chạy.

-   **🔐 Đăng nhập & Phân quyền:** Hệ thống đăng nhập an toàn, phân quyền chức năng cho quản lý và nhân viên.
-   
## 🚀 Tải Về \& Cài Đặt Nhanh
Bạn có thể trải nghiệm ứng dụng ngay lập tức bằng cách tải về file cài đặt cho Windows dưới đây.
<p align="center">
&nbsp; <a href="https://github.com/MinhHuong122/java-desktop-application/releases/download/v1.0/TotoroShopSetup.exe" style="text-decoration:none;">
&nbsp;   <img src="https://img.shields.io/badge/Tải Về Ngay (.exe)-4CAF50?style=for-the-badge\&logo=windows\&logoColor=white" alt="Download .exe">
&nbsp; </a>
</p>



*Lưu ý: File cài đặt này chỉ dành cho hệ điều hành Windows.*



## 🛠️ Công Nghệ Sử Dụng



-   **Ngôn ngữ lập trình:** [Java](https://www.java.com/)

-   **Framework giao diện:** [JavaFX](https://openjfx.io/)

-   **Công cụ thiết kế UI:** [Scene Builder](https://gluonhq.com/products/scene-builder/)

-   **Hệ quản trị CSDL:** [MySQL](https://www.mysql.com/)

-   **IDE:** IntelliJ IDEA / Apache NetBeans

-   **Quản lý thư viện:** Maven / Gradle (Tùy chọn)



## 📸 Giao Diện Ứng Dụng (Screenshots)



*Dưới đây là một vài hình ảnh về ứng dụng. Bạn hãy thay thế bằng ảnh chụp màn hình thực tế của mình.*



<table align="center">

&nbsp;<tr>

&nbsp;   <td align="center"><strong>Giao diện Đăng nhập</strong></td>

&nbsp;   <td align="center"><strong>Trang tổng quan</strong></td>

&nbsp;</tr>

&nbsp;<tr>

&nbsp;   <td><img src="https://i.imgur.com/uH3gZ9H.png" alt="Login Screen" width="400"></td>

&nbsp;   <td><img src="https://i.imgur.com/gOqGq7B.png" alt="Dashboard" width="400"></td>

&nbsp;</tr>

&nbsp;<tr>

&nbsp;   <td align="center"><strong>Quản lý Sản phẩm</strong></td>

&nbsp;   <td align="center"><strong>Quản lý Hóa đơn</strong></td>

&nbsp;</tr>

&nbsp;<tr>

&nbsp;   <td><img src="https://i.imgur.com/sC4tW6C.png" alt="Product Management" width="400"></td>

&nbsp;   <td><img src="https://i.imgur.com/3fL5kYJ.png" alt="Invoice Management" width="400"></td>

&nbsp;</tr>

</table>



## 👨‍💻 Hướng Dẫn Cài Đặt Từ Mã Nguồn (Dành cho Lập trình viên)



Để chạy dự án từ mã nguồn, bạn cần thực hiện các bước sau:



### 1. Yêu cầu cần có:



-   [JDK](https://www.oracle.com/java/technologies/downloads/) (Phiên bản 11 hoặc mới hơn)

-   [MySQL Server](https://dev.mysql.com/downloads/mysql/) (Phiên bản 8.0 hoặc mới hơn)

-   Một IDE hỗ trợ Java/JavaFX như [IntelliJ IDEA](https://www.jetbrains.com/idea/) hoặc [Apache NetBeans](https://netbeans.apache.org/).



### 2. Các bước thực hiện:



1.  **Clone repository về máy:**

&nbsp;   ```bash

&nbsp;   git clone [https://github.com/MinhHuong122/java-desktop-application.git](https://github.com/MinhHuong122/java-desktop-application.git)

&nbsp;   ```

2.  **Tạo cơ sở dữ liệu:**

&nbsp;   -   Mở MySQL Workbench hoặc một công cụ quản trị MySQL khác.

&nbsp;   -   Tạo một database mới (ví dụ: `totoroshop\_db`).

&nbsp;   -   Import file `.sql` đi kèm trong thư mục `database` của dự án để tạo các bảng cần thiết.



3.  **Cấu hình kết nối CSDL:**

&nbsp;   -   Tìm đến file cấu hình kết nối database trong mã nguồn (ví dụ: `src/com/config/DatabaseConnector.java`).

&nbsp;   -   Thay đổi các thông tin `DB\_URL`, `USER`, `PASSWORD` cho phù hợp với môi trường của bạn.



4.  **Chạy ứng dụng:**

&nbsp;   -   Mở dự án bằng IDE của bạn.

&nbsp;   -   Build dự án để tải các thư viện cần thiết.

&nbsp;   -   Tìm đến file `Main.java` (hoặc file chứa hàm `main`) và chạy ứng dụng.



## 🤝 Đóng Góp



Mọi sự đóng góp để cải thiện dự án đều được hoan nghênh. Nếu bạn có ý tưởng, vui lòng tạo một `issue` để chúng ta có thể thảo luận.



1.  Fork dự án

2.  Tạo branch mới (`git checkout -b feature/AmazingFeature`)

3.  Commit thay đổi của bạn (`git commit -m 'Add some AmazingFeature'`)

4.  Push lên branch (`git push origin feature/AmazingFeature`)

5.  Mở một Pull Request



## 📜 Giấy Phép (License)



Dự án này được cấp phép theo Giấy phép MIT. Xem file `LICENSE` để biết thêm chi tiết.



---



<p align="center">

&nbsp; Made with ❤️ by MinhHuong122

</p>

