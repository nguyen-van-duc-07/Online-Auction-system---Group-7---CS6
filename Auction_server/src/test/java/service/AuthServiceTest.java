package service;

import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.Admin;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.Wallet;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.ChangePasswordRequestDTO;
import com.auction.shared.request.CreateAdminRequestDTO;
import com.auction.shared.request.LoginRequestDTO;
import com.auction.shared.request.SignUpRequestDTO;
import com.auction.shared.request.UpdateProfileRequestDTO;
import com.auction.shared.response.ChangePasswordResponseDTO;
import config.ConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;
import repository.WalletRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private WalletRepository walletRepo;

    @Mock
    private NotificationService notifService;

    @Mock
    private ConnectionProvider connectionProvider;

    @Mock
    private Connection mockConnection;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepo,
            walletRepo,
            notifService,
            connectionProvider
        );
    }

    // ==========================================
    // TEST CASES FOR login
    // ==========================================

    @Test
    void login_correctBidderCredentials_returnsBidderWithWallet() {
        // Arrange
        String phone = "0987654321";
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        LoginRequestDTO req = new LoginRequestDTO(phone, password);

        Bidder bidder = new Bidder();
        bidder.setId("bidder123");
        bidder.setRole(UserRole.BIDDER);
        bidder.setPhoneNumber(phone);

        Wallet wallet = new Wallet("bidder123");

        when(userRepo.getPasswordByPhoneNumber(phone)).thenReturn(hashedPassword);
        when(userRepo.getUserByPhoneNumberNameOrId(phone, null)).thenReturn(bidder);
        when(walletRepo.getWalletByUserId("bidder123")).thenReturn(wallet);

        // Act
        User result = authService.login(req);

        // Assert
        assertNotNull(result);
        assertEquals(UserRole.BIDDER, result.getRole());
        assertEquals("bidder123", result.getId());
        assertEquals(wallet, ((Bidder) result).getWallet());
        verify(userRepo).getPasswordByPhoneNumber(phone);
        verify(userRepo).getUserByPhoneNumberNameOrId(phone, null);
        verify(walletRepo).getWalletByUserId("bidder123");
    }

    @Test
    void login_correctAdminCredentials_returnsAdmin() {
        // Arrange
        String phone = "0999999999";
        String password = "adminPassword";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        LoginRequestDTO req = new LoginRequestDTO(phone, password);

        Admin admin = new Admin();
        admin.setId("admin123");
        admin.setRole(UserRole.ADMIN);
        admin.setPhoneNumber(phone);

        when(userRepo.getPasswordByPhoneNumber(phone)).thenReturn(hashedPassword);
        when(userRepo.getUserByPhoneNumberNameOrId(phone, null)).thenReturn(admin);

        // Act
        User result = authService.login(req);

        // Assert
        assertNotNull(result);
        assertEquals(UserRole.ADMIN, result.getRole());
        assertEquals("admin123", result.getId());
        verify(userRepo).getPasswordByPhoneNumber(phone);
        verify(userRepo).getUserByPhoneNumberNameOrId(phone, null);
        verify(walletRepo, never()).getWalletByUserId(anyString());
    }

    @Test
    void login_incorrectPassword_returnsNull() {
        // Arrange
        String phone = "0987654321";
        String correctPassword = "password123";
        String incorrectPassword = "wrongPassword";
        String hashedPassword = BCrypt.hashpw(correctPassword, BCrypt.gensalt());
        LoginRequestDTO req = new LoginRequestDTO(phone, incorrectPassword);

        when(userRepo.getPasswordByPhoneNumber(phone)).thenReturn(hashedPassword);

        // Act
        User result = authService.login(req);

        // Assert
        nullResultCheck(result);
    }

    @Test
    void login_userNotFound_returnsNull() {
        // Arrange
        String phone = "0900000000";
        LoginRequestDTO req = new LoginRequestDTO(phone, "anyPassword");

        when(userRepo.getPasswordByPhoneNumber(phone)).thenReturn(null);

        // Act
        User result = authService.login(req);

        // Assert
        nullResultCheck(result);
    }

    private void nullResultCheck(User result) {
        assertNull(result);
        verify(userRepo, never()).getUserByPhoneNumberNameOrId(anyString(), anyString());
    }

    // ==========================================
    // TEST CASES FOR signUp
    // ==========================================

    @Test
    void signUp_usernameExists_returnsFalse() {
        // Arrange
        String phone = "0987654321";
        SignUpRequestDTO req = new SignUpRequestDTO(phone, "password123");

        when(userRepo.isAccountExist(phone)).thenReturn(true);

        // Act
        boolean result = authService.signUp(req);

        // Assert
        assertFalse(result);
        verify(userRepo).isAccountExist(phone);
        verifyNoInteractions(connectionProvider, walletRepo, notifService);
    }

    @Test
    void signUp_validRequest_createsUserAndWalletAndSendsNotification() throws Exception {
        // Arrange
        String phone = "0987654321";
        SignUpRequestDTO req = new SignUpRequestDTO(phone, "password123");

        when(userRepo.isAccountExist(phone)).thenReturn(false);
        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(userRepo.createUser(eq(mockConnection), any(User.class))).thenReturn(true);
        when(walletRepo.createWallet(eq(mockConnection), any(Wallet.class))).thenReturn(true);

        // Act
        boolean result = authService.signUp(req);

        // Assert
        assertTrue(result);
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
        verify(mockConnection).close();
        verify(notifService).sendFromNotification(any());
    }

    @Test
    void signUp_userCreationFails_rollsBackAndReturnsFalse() throws Exception {
        // Arrange
        String phone = "0987654321";
        SignUpRequestDTO req = new SignUpRequestDTO(phone, "password123");

        when(userRepo.isAccountExist(phone)).thenReturn(false);
        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(userRepo.createUser(eq(mockConnection), any(User.class))).thenReturn(false);

        // Act
        boolean result = authService.signUp(req);

        // Assert
        assertFalse(result);
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).close();
        verify(walletRepo, never()).createWallet(any(), any());
        verify(notifService, never()).sendFromNotification(any());
    }

    @Test
    void signUp_walletCreationFails_rollsBackAndReturnsFalse() throws Exception {
        // Arrange
        String phone = "0987654321";
        SignUpRequestDTO req = new SignUpRequestDTO(phone, "password123");

        when(userRepo.isAccountExist(phone)).thenReturn(false);
        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(userRepo.createUser(eq(mockConnection), any(User.class))).thenReturn(true);
        when(walletRepo.createWallet(eq(mockConnection), any(Wallet.class))).thenReturn(false);

        // Act
        boolean result = authService.signUp(req);

        // Assert
        assertFalse(result);
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).close();
        verify(notifService, never()).sendFromNotification(any());
    }

    @Test
    void signUp_exceptionThrown_rollsBackAndReturnsFalse() throws Exception {
        // Arrange
        String phone = "0987654321";
        SignUpRequestDTO req = new SignUpRequestDTO(phone, "password123");

        when(userRepo.isAccountExist(phone)).thenReturn(false);
        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        doThrow(new RuntimeException("DB Error")).when(mockConnection).setAutoCommit(false);

        // Act
        boolean result = authService.signUp(req);

        // Assert
        assertFalse(result);
        verify(mockConnection).close();
    }

    // ==========================================
    // TEST CASES FOR updateProfile
    // ==========================================

    @Test
    void updateProfile_success_returnsUpdatedUser() {
        // Arrange
        String userId = "user123";
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            userId,
            "New Name",
            "newemail@example.com",
            LocalDate.of(1990, 1, 1),
            "New Address"
        );

        Bidder updatedUser = new Bidder();
        updatedUser.setId(userId);
        updatedUser.setAccountName("New Name");

        when(userRepo.updateProfile(any(User.class))).thenReturn(true);
        when(userRepo.getUserByPhoneNumberNameOrId(null, userId)).thenReturn(updatedUser);

        // Act
        User result = authService.updateProfile(req);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getAccountName());
        verify(userRepo).updateProfile(any(User.class));
        verify(userRepo).getUserByPhoneNumberNameOrId(null, userId);
    }

    @Test
    void updateProfile_failure_returnsNull() {
        // Arrange
        String userId = "user123";
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            userId,
            null,
            null,
            null,
            null
        );

        when(userRepo.updateProfile(any(User.class))).thenReturn(false);

        // Act
        User result = authService.updateProfile(req);

        // Assert
        assertNull(result);
        verify(userRepo).updateProfile(any(User.class));
        verify(userRepo, never()).getUserByPhoneNumberNameOrId(anyString(), anyString());
    }

    // ==========================================
    // TEST CASES FOR createAdmin
    // ==========================================

    @Test
    void createAdmin_usernameExists_returnsFalse() {
        // Arrange
        CreateAdminRequestDTO req = new CreateAdminRequestDTO();
        req.setPhoneNumber("0999999999");

        when(userRepo.isAccountExist("0999999999")).thenReturn(true);

        // Act
        boolean result = authService.createAdmin(req);

        // Assert
        assertFalse(result);
        verify(userRepo).isAccountExist("0999999999");
        verify(userRepo, never()).saveAdminAccount(any());
    }

    @Test
    void createAdmin_success_returnsTrue() {
        // Arrange
        CreateAdminRequestDTO req = new CreateAdminRequestDTO();
        req.setPhoneNumber("0999999999");
        req.setPassword("adminPass");
        req.setAccountName("Super Admin");
        req.setEmail("admin@example.com");
        req.setDob(LocalDate.of(1985, 5, 5));
        req.setAddress("Admin HQ");

        when(userRepo.isAccountExist("0999999999")).thenReturn(false);
        when(userRepo.saveAdminAccount(any(Admin.class))).thenReturn(true);

        // Act
        boolean result = authService.createAdmin(req);

        // Assert
        assertTrue(result);
        verify(userRepo).isAccountExist("0999999999");
        verify(userRepo).saveAdminAccount(any(Admin.class));
    }

    @Test
    void createAdmin_emptyAccountName_setsDefaultNameAndSucceeds() {
        // Arrange
        CreateAdminRequestDTO req = new CreateAdminRequestDTO();
        req.setPhoneNumber("0999999999");
        req.setPassword("adminPass");
        req.setAccountName("");

        when(userRepo.isAccountExist("0999999999")).thenReturn(false);
        when(userRepo.saveAdminAccount(any(Admin.class))).thenReturn(true);

        // Act
        boolean result = authService.createAdmin(req);

        // Assert
        assertTrue(result);
        verify(userRepo).saveAdminAccount(argThat(admin -> 
            admin.getAccountName() != null && !admin.getAccountName().isEmpty()
        ));
    }

    // ==========================================
    // TEST CASES FOR changePassword
    // ==========================================

    @Test
    void changePassword_userNotFound_returnsFailure() {
        // Arrange
        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO("user123", "oldPass", "newPass");
        when(userRepo.getPasswordByUserId("user123")).thenReturn(null);

        // Act
        ChangePasswordResponseDTO result = authService.changePassword(req);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Không tìm thấy thông tin tài khoản người dùng!", result.getMessage());
        verify(userRepo, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void changePassword_incorrectOldPassword_returnsFailure() {
        // Arrange
        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO("user123", "wrongOldPass", "newPass");
        String currentHashedPassword = BCrypt.hashpw("correctOldPass", BCrypt.gensalt());
        when(userRepo.getPasswordByUserId("user123")).thenReturn(currentHashedPassword);

        // Act
        ChangePasswordResponseDTO result = authService.changePassword(req);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Mật khẩu hiện tại không chính xác!", result.getMessage());
        verify(userRepo, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void changePassword_correctOldPasswordSuccess_returnsSuccess() {
        // Arrange
        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO("user123", "correctOldPass", "newPass");
        String currentHashedPassword = BCrypt.hashpw("correctOldPass", BCrypt.gensalt());
        when(userRepo.getPasswordByUserId("user123")).thenReturn(currentHashedPassword);
        when(userRepo.updatePassword(eq("user123"), anyString())).thenReturn(true);

        // Act
        ChangePasswordResponseDTO result = authService.changePassword(req);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Cập nhật mật khẩu mới thành công!", result.getMessage());
        verify(userRepo).updatePassword(eq("user123"), anyString());
    }

    @Test
    void changePassword_correctOldPasswordFailure_returnsSystemFailure() {
        // Arrange
        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO("user123", "correctOldPass", "newPass");
        String currentHashedPassword = BCrypt.hashpw("correctOldPass", BCrypt.gensalt());
        when(userRepo.getPasswordByUserId("user123")).thenReturn(currentHashedPassword);
        when(userRepo.updatePassword(eq("user123"), anyString())).thenReturn(false);

        // Act
        ChangePasswordResponseDTO result = authService.changePassword(req);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Lỗi hệ thống khi cập nhật mật khẩu mới!", result.getMessage());
        verify(userRepo).updatePassword(eq("user123"), anyString());
    }

    // ==========================================
    // TEST CASES FOR getAllUsers
    // ==========================================

    @Test
    void getAllUsers_returnsList() {
        // Arrange
        List<UserDTO> expectedUsers = new ArrayList<>();
        expectedUsers.add(UserDTO.builder().id("1").accountName("User One").build());
        expectedUsers.add(UserDTO.builder().id("2").accountName("User Two").build());

        when(userRepo.getAllUsers()).thenReturn(expectedUsers);

        // Act
        List<UserDTO> actualUsers = authService.getAllUsers();

        // Assert
        assertNotNull(actualUsers);
        assertEquals(2, actualUsers.size());
        assertEquals(expectedUsers, actualUsers);
        verify(userRepo).getAllUsers();
    }
}
