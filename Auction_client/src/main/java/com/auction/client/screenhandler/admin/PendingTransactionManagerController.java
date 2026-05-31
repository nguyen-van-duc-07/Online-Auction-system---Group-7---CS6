package com.auction.client.screenhandler.admin;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.model.transaction.WalletTransaction;
import com.auction.shared.request.GetPendingTransactionsRequestDTO;
import com.auction.shared.request.ProcessTransactionRequestDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller quản lý màn hình duyệt giao dịch nạp/rút tiền của người dùng dành cho Admin.
 * <p>
 * Lớp này cho phép Admin theo dõi danh sách các giao dịch đang chờ duyệt (PENDING),
 * thực hiện phê duyệt (APPROVE) hoặc từ chối (REJECT) giao dịch.
 * </p>
 */
public class PendingTransactionManagerController implements Initializable {
    private static PendingTransactionManagerController instance;

    @FXML private TableView<WalletTransaction> tableView;
    @FXML private TableColumn<WalletTransaction, String> colId;
    @FXML private TableColumn<WalletTransaction, String> colWalletId;
    @FXML private TableColumn<WalletTransaction, String> colType;
    @FXML private TableColumn<WalletTransaction, BigDecimal> colAmount;
    @FXML private TableColumn<WalletTransaction, Void> colAction;

    public PendingTransactionManagerController() {
        instance = this;
    }

    public static PendingTransactionManagerController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colWalletId.setCellValueFactory(new PropertyValueFactory<>("walletId"));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().name()));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnApprove = new Button("Duyệt");
            private final Button btnReject = new Button("Từ chối");
            private final HBox pane = new HBox(5, btnApprove, btnReject);

            {
                btnApprove.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
                btnReject.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");

                btnApprove.setOnAction(event -> {
                    WalletTransaction tx = getTableView().getItems().get(getIndex());
                    processTransaction(tx.getId(), WalletTransactionStatus.APPROVE);
                });

                btnReject.setOnAction(event -> {
                    WalletTransaction tx = getTableView().getItems().get(getIndex());
                    processTransaction(tx.getId(), WalletTransactionStatus.REJECT);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadData() {
        ServerConnection.sendData(new GetPendingTransactionsRequestDTO());
    }

    /**
     * Đẩy danh sách các giao dịch chờ duyệt lên bảng hiển thị.
     *
     * @param pendingTransactions danh sách các giao dịch nạp rút chờ xử lý
     */
    public void loadDataToTable(List<WalletTransaction> pendingTransactions) {
        if (pendingTransactions != null) {
            ObservableList<WalletTransaction> list = FXCollections.observableArrayList(pendingTransactions);
            tableView.setItems(list);
        }
    }

    private void processTransaction(String transactionId, WalletTransactionStatus status) {
        ProcessTransactionRequestDTO req = ProcessTransactionRequestDTO.builder()
                .transactionId(transactionId)
                .actionStatus(status)
                .referenceId(SessionManager.getCurrentUser().getId())
                .build();
        ServerConnection.sendData(req);
    }

    @FXML
    public void handleReload() {
        loadData();
    }
}
