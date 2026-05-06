package com.auction.shared.enums;

public enum ItemType {
  ELECTRONICS("Thiết bị điện tử"),
  VEHICLES("Phương tiện di chuyển"),
  COLLECTIBLES("Đồ sưu tầm"),
  FASHION("Thời trang"),
  SPORTS("Thể thao"),
  OTHER("Khác");

  private final String value;

  ItemType(String value) {
    this.value = value;
  }
  public String getValue() {
    return value;
  }
}
