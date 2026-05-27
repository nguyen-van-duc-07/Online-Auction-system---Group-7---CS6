package com.auction.shared.enums;

public enum NotificationType {
  // Buyer
  AUCTION_WON,           // Thắng phiên → navigate sang OrderDetail
  ORDER_CANCELLED,       // Đơn hàng bị hủy tự động

  // Seller
  AUCTION_ENDED,         // Phiên của seller kết thúc
  ORDER_CONFIRMED,       // Buyer xác nhận thanh toán
  ORDER_CANCELLED_BY_BUYER, // Buyer hủy đơn
  AUCTION_CANCELLED,     // Phiên bị hủy

  // Request
  REQUEST_SUBMITTED,
  REQUEST_APPROVED,
  REQUEST_REJECTED,

  // System
  SYSTEM                 // Thông báo hệ thống chung
}