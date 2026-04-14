package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class ScreenController
{
    // Dùng để chuyển sang trang bất kì hiệu quả hơn
    public static void switchScreen(ActionEvent event, String fxmlFile, String title)
    {
        try
        {
            Parent root = FXMLLoader.load(ScreenController.class.getResource(fxmlFile));
            Scene scene = new Scene(root);
            Stage stage = null;
            if (event.getSource() instanceof Node) // Nếu phần vừa ấn là thể hiện của Button là lớp con của Node
            {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }
            else if (event.getSource() instanceof MenuItem) // Nếu phần vừa ấn là thể hiện của MenuItem
            {
                stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            }
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi hệ thống");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải màn hình: " + fxmlFile);
            alert.showAndWait();
        }
    }

    public static Optional<ButtonType> showAlert(Alert.AlertType type, String title, String content, ActionEvent event)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        if (event != null)
        {
            Stage stage = null;
            if (event.getSource() instanceof Node)
            {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }
            else if (event.getSource() instanceof MenuItem)
            {
                stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            }
            if (stage != null)
            {
                alert.initOwner(stage);
            }
        }
        return alert.showAndWait();
    }
}