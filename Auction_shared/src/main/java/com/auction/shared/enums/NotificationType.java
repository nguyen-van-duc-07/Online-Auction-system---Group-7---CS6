package com.auction.shared.enums;

public enum NotificationType {
  // Buyer
  AUCTION_WON,           // Thắng phiên → navigate sang OrderDetail
  OUTBID,                // Bị vượt giá → navigate sang ItemAuction
  ORDER_CANCELLED,       // Đơn hàng bị hủy tự động

  // Seller
  AUCTION_ENDED,         // Phiên của seller kết thúc
  ORDER_CONFIRMED,       // Buyer xác nhận thanh toán
  ORDER_CANCELLED_BY_BUYER, // Buyer hủy đơn
  SELLER_APPROVED,       // Tài khoản seller được duyệt
  SELLER_REJECTED,       // Tài khoản seller bị từ chối
  AUCTION_CANCELLED,     // Phiên bị hủy

  // System
  SYSTEM                 // Thông báo hệ thống chung
}