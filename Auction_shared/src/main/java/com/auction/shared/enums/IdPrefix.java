package com.auction.shared.enums;

public enum IdPrefix {
  BIDDER("BD"),
  ADMIN("AD"),
  AUCTION("AU"),
  ITEM("IT"),
  SELLER("SL");

  private final String value;

  IdPrefix(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
