package com.auction.shared.model.item;

import com.auction.shared.enums.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenericItem extends Item {

    public GenericItem(String name, ItemType type, String description, java.util.Map<String, String> attributes) {
        super(name, type != null ? type : ItemType.OTHER, description, attributes);
    }

    @Override
    public String getDisplayCategory() {
        return "Sản phẩm khác";
    }

    @Override
    public String printInfo() {
        return "Tên: " + getName() + " | Mô tả: " + (getDescription() != null ? getDescription() : "Không có");
    }
}
