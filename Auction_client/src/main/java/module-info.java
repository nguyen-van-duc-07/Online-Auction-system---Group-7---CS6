module com.auction.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.auction.shared; // Gọi sang Shared

    opens com.auction.client to javafx.fxml;
    exports com.auction.client;
}