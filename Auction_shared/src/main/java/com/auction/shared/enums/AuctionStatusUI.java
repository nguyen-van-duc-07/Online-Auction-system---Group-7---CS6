package com.auction.shared.enums;

import lombok.Getter;

@Getter
public enum AuctionStatusUI {
    ACTIVE("Đang diễn ra", "#27ae60"),
    FINISHED("Đã kết thúc", "#7f8c8d"),
    PENDING("Sắp diễn ra", "#f39c12");

    private final String displayName;
    private final String color;

    AuctionStatusUI(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
    // Hàm tiện ích để convert từ Enum Shared sang Enum UI
    public static AuctionStatusUI fromShared(AuctionStatus sharedStatus) {
        return AuctionStatusUI.valueOf(sharedStatus.name());
    }
}