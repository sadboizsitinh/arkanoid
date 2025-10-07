module Arkanoid {
    // Các module JavaFX cần thiết
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // Nếu có phần âm thanh, thêm:
    // requires javafx.media;

    // Cho phép JavaFX truy cập các controller và model
    opens arkanoid to javafx.fxml;
    opens arkanoid.controller to javafx.fxml;

    // Xuất các package để các class khác có thể sử dụng
    exports arkanoid;
    exports arkanoid.controller;
}
