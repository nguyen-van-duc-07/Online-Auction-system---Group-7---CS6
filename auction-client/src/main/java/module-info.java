module com.example.auctionclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    opens com.example.auctionclient to javafx.fxml;
    exports com.example.auctionclient;

}