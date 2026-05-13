module com.auction.shared{
  requires jakarta.validation;
  requires jakarta.persistence;
  requires static lombok;
  requires org.controlsfx.controls;
  requires javafx.controls;
  exports com.auction.shared.enums;
  exports com.auction.shared.model.auction;
  exports com.auction.shared.model.core;
  exports com.auction.shared.model.item;
  exports com.auction.shared.model.transaction;
  exports com.auction.shared.model.user;
  exports com.auction.shared.network;
  exports com.auction.shared.request;
  exports com.auction.shared.response;
  // Export thêm bất kỳ package nào khác mà Client cần dùng
}