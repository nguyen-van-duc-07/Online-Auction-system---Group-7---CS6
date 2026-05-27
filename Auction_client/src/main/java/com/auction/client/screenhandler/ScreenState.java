package com.auction.client.screenhandler;

import javafx.scene.Parent;

/**
 * Lưu trữ trạng thái của màn hình để phục vụ cơ chế quay lại (back).
 * Trạng thái bao gồm nút gốc giao diện (Parent), tiêu đề trang và đường dẫn FXML.
 */
public class ScreenState {
  private final Parent root;
  private final String title;
  private final String fxmlFile;

  /**
   * Khởi tạo đối tượng lưu trữ trạng thái màn hình.
   *
   * @param root nút gốc giao diện của màn hình
   * @param title tiêu đề của màn hình
   * @param fxmlFile đường dẫn tệp FXML tương ứng của màn hình
   */
  public ScreenState(Parent root, String title, String fxmlFile) {
    this.root = root;
    this.title = title;
    this.fxmlFile = fxmlFile;
  }

  /**
   * Lấy nút gốc giao diện của màn hình.
   *
   * @return nút gốc (Parent) giao diện màn hình
   */
  public Parent getRoot() {
    return root;
  }

  /**
   * Lấy tiêu đề của màn hình.
   *
   * @return tiêu đề hiển thị dạng chuỗi
   */
  public String getTitle() {
    return title;
  }

  /**
   * Lấy đường dẫn tệp FXML tương ứng của màn hình.
   *
   * @return đường dẫn tệp FXML
   */
  public String getFxmlFile() {
    return fxmlFile;
  }
}
