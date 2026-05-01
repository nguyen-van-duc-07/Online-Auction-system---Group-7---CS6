module com.auction.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.auction.shared;
    requires static lombok; // Gọi sang Shared

    opens com.auction.client to javafx.fxml;
    exports com.auction.client;
}