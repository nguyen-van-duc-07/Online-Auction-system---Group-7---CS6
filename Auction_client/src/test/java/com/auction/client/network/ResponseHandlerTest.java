package com.auction.client.network;

import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.GetBalanceRequestDTO;
import com.auction.shared.response.LoginResponseDTO;
import com.auction.shared.response.SignUpResponseDTO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResponseHandlerTest {

    private MockedStatic<Platform> mockedPlatform;
    private MockedStatic<ScreenController> mockedScreenController;
    private MockedStatic<ServerConnection> mockedServerConnection;

    @BeforeEach
    void setUp() {
        // Setup static mocks
        mockedPlatform = mockStatic(Platform.class);
        mockedPlatform.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });

        mockedScreenController = mockStatic(ScreenController.class);
        mockedServerConnection = mockStatic(ServerConnection.class);

        // Reset SessionManager
        SessionManager.setCurrentUser(null);
    }

    @AfterEach
    void tearDown() {
        if (mockedPlatform != null) mockedPlatform.close();
        if (mockedScreenController != null) mockedScreenController.close();
        if (mockedServerConnection != null) mockedServerConnection.close();
    }

    @Test
    void login_thanhCong_vaiTroBidder_luuUserVaChuyenTrang() {
        // Sắp đặt
        UserDTO user = new UserDTO();
        user.setId("user123");
        user.setRole(UserRole.BIDDER);
        user.setAccountName("Nguyen Van Bidder");

        LoginResponseDTO loginRes = new LoginResponseDTO(true, "Đăng nhập thành công!", user);

        // Thực thi
        ResponseHandler.login(loginRes);

        // Xác minh
        assertEquals(user, SessionManager.getCurrentUser());
        
        // Xác minh gửi request số dư lên server
        mockedServerConnection.verify(() -> ServerConnection.sendData(any(GetBalanceRequestDTO.class)));
        
        // Xác minh chuyển màn hình sang MainLayout
        mockedScreenController.verify(() -> ScreenController.switchScreen("MainLayout.fxml", "Trang chủ"));
    }

    @Test
    void login_thanhCong_vaiTroAdmin_luuUserVaChuyenTrangAdmin() {
        // Sắp đặt
        UserDTO user = new UserDTO();
        user.setId("admin123");
        user.setRole(UserRole.ADMIN);
        user.setAccountName("Nguyen Van Admin");

        LoginResponseDTO loginRes = new LoginResponseDTO(true, "Đăng nhập thành công!", user);

        // Thực thi
        ResponseHandler.login(loginRes);

        // Xác minh
        assertEquals(user, SessionManager.getCurrentUser());
        
        // Admin không lấy số dư ví, xác minh không gửi GetBalanceRequestDTO
        mockedServerConnection.verify(() -> ServerConnection.sendData(any(GetBalanceRequestDTO.class)), never());
        
        // Xác minh chuyển màn hình sang AdminScreen
        mockedScreenController.verify(() -> ScreenController.switchScreen("Admin/AdminScreen.fxml", "Trang chủ"));
    }

    @Test
    void login_thatBai_hienThiAlertLoi() {
        // Sắp đặt
        LoginResponseDTO loginRes = new LoginResponseDTO(false, "Sai tài khoản hoặc mật khẩu", null);

        // Thực thi
        ResponseHandler.login(loginRes);

        // Xác minh
        assertNull(SessionManager.getCurrentUser());
        
        // Xác minh gọi showAlert hiển thị lỗi
        mockedScreenController.verify(() -> ScreenController.showAlert(
            eq(Alert.AlertType.ERROR), eq("Lỗi đăng nhập"), eq("Sai tài khoản hoặc mật khẩu")
        ));
    }

    @Test
    void signUp_thatBai_hienThiAlertLoi() {
        // Sắp đặt
        SignUpResponseDTO signUpRes = new SignUpResponseDTO(false, "Số điện thoại đã tồn tại!");

        // Thực thi
        ResponseHandler.signUp(signUpRes);

        // Xác minh gọi showAlert hiển thị lỗi
        mockedScreenController.verify(() -> ScreenController.showAlert(
            eq(Alert.AlertType.ERROR), eq("Lỗi đăng kí"), eq("Số điện thoại đã tồn tại!")
        ));
    }
}
