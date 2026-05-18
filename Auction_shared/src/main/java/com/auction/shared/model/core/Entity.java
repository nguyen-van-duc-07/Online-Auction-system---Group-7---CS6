package com.auction.shared.model.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class Entity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID) // Uỷ quyền cho JPA tự sinh UUID
  @Column(length = 36, updatable = false, nullable = false)
  protected String id;

  @Column(updatable = false) // Đảm bảo thời gian tạo không bị update nhầm sau này
  protected LocalDateTime createdAt;

  protected Entity() {
  }

  public Entity(String id, LocalDateTime createdAt) {
    this.id = id;
    this.createdAt = createdAt;
  }

  /**
   * Hàm này được JPA tự động gọi (callback) NGAY TRƯỚC KHI thực hiện lệnh INSERT vào Database.
   * Đây là thời điểm hoàn hảo để gán ngày tạo.
   */
  @PrePersist
  protected void onPrePersist() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }
}