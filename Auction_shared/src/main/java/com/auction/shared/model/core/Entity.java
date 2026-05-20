package com.auction.shared.model.core;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
@MappedSuperclass
@Getter
@Setter
abstract public class Entity implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id // Đánh dấu đây là Khóa chính (BẮT BUỘC)
  @Column(length = 36, updatable = false, nullable = false) // Chỉ định độ dài chuỗi UUID
  protected String id;
  protected LocalDateTime createdAt;
  // Chỉ cho phép JPA và các lớp con sử dụng (không dùng public)
  protected Entity(){
    this.id = UUID.randomUUID().toString();
    this.createdAt = LocalDateTime.now();
  }
  public Entity(String id, LocalDateTime createdAt){
    this.id = id;
    this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
  }
}