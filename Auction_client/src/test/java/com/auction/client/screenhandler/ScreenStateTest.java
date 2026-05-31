package com.auction.client.screenhandler;

import javafx.scene.Parent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenStateTest {

    @Test
    @DisplayName("Khởi tạo ScreenState với giá trị hợp lệ")
    void testConstructor_ValidArgs_ShouldStoreValues() {
        Parent dummyRoot = null;
        String title = "Trang chủ";
        String fxml = "Home.fxml";

        ScreenState state = new ScreenState(dummyRoot, title, fxml);

        assertNull(state.getRoot()); // Vì ta truyền null
        assertEquals(title, state.getTitle());
        assertEquals(fxml, state.getFxmlFile());
    }

    @Test
    @DisplayName("Khởi tạo ScreenState với giá trị null vẫn hoạt động")
    void testConstructor_NullValues_ShouldAllowNulls() {
        ScreenState state = new ScreenState(null, null, null);

        assertNull(state.getRoot());
        assertNull(state.getTitle());
        assertNull(state.getFxmlFile());
    }
}
