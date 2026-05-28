package com.auction.client.screenhandler;

import com.auction.shared.enums.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UploadItemControllerTest {

    private UploadItemController controller;

    @BeforeEach
    void setUp() {
        controller = new UploadItemController();
    }

    @Test
    void mapCategoryToEnum_dungChuoiTiengViet_traVeEnumChinhXac() {
        assertEquals(ItemType.ELECTRONICS, controller.mapCategoryToEnum("Thiết bị điện tử"));
        assertEquals(ItemType.VEHICLES, controller.mapCategoryToEnum("Phương tiện di chuyển"));
        assertEquals(ItemType.FASHION, controller.mapCategoryToEnum("Thời trang"));
        assertEquals(ItemType.COLLECTIBLES, controller.mapCategoryToEnum("Đồ sưu tầm"));
        assertEquals(ItemType.SPORTS, controller.mapCategoryToEnum("Thể thao"));
        assertEquals(ItemType.ARTS, controller.mapCategoryToEnum("Nghệ thuật"));
    }

    @Test
    void mapCategoryToEnum_chuoiKhongHopLe_traVeOther() {
        assertEquals(ItemType.OTHER, controller.mapCategoryToEnum("Khác"));
        assertEquals(ItemType.OTHER, controller.mapCategoryToEnum(null));
        assertEquals(ItemType.OTHER, controller.mapCategoryToEnum("random_category"));
    }
}
