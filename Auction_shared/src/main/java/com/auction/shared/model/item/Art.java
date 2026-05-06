package com.auction.shared.model.item;

// Nhớ import ArtDTO nếu nó nằm ở package khác nhé
// import com.auction.shared.dto.ArtDTO;

public class Art extends Item {

    private String artistName;
    private String material;
    private int yearCreated;
    private boolean hasCertificate;

    // Gộp 2 constructor rườm rà thành 1 constructor duy nhất dùng DTO
    public Art(ArtDTO dto) {
        // Đẩy phần thông tin chung (id, name, price,...) lên cho class Item xử lý
        super(dto);

        // Xử lý các thông tin riêng biệt của Art
        this.artistName = dto.getArtistName();
        this.material = dto.getMaterial();
        this.yearCreated = dto.getYearCreated();

        // Lưu ý: Thường getter của kiểu boolean sẽ có tiền tố là 'is' thay vì 'get'
        this.hasCertificate = dto.isHasCertificate();
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Tác giả: %s | Chất liệu: %s | Năm ra mắt: %d | Chứng nhận: %s",
                artistName, material, yearCreated, hasCertificate ? "Có" : "Không");
    }

    // --- Các Getter và Setter ---

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public int getYearCreated() { return yearCreated; }
    public void setYearCreated(int yearCreated) { this.yearCreated = yearCreated; }

    // Sửa lại tên hàm này thành isHasCertificate cho đúng chuẩn Naming Convention của Java nhé
    public boolean isHasCertificate() { return hasCertificate; }
    public void setHasCertificate(boolean hasCertificate) { this.hasCertificate = hasCertificate; }
}