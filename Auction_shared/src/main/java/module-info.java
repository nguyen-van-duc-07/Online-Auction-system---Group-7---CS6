module com.auction.shared{
    requires jakarta.validation;
    requires jakarta.persistence;
    requires static lombok;
    exports com.auction.shared.enums;
    exports com.auction.shared.model.auction;
    exports com.auction.shared.model.core;
    exports com.auction.shared.model.factory;
    exports com.auction.shared.model.item;
    exports com.auction.shared.model.transaction;
    exports com.auction.shared.model.user;
    // Export thêm bất kỳ package nào khác mà Client cần dùng
}