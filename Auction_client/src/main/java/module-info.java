module com.auction.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.auction.shared.user; // Gọi sang Shared

    opens com.auction.client to javafx.fxml;
    exports com.auction.client;
    exports com.auction.client.screenhandler;
    opens com.auction.client.screenhandler to javafx.fxml;
}