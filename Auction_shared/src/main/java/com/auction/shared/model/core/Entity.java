package com.auction.shared.model.core;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

abstract public class Entity implements Serializable {
  private static final long serialVersionUID = 1L;
  protected String id;
  protected LocalDateTime createdAt;

  public Entity() {
    this.id = UUID.randomUUID().toString();
    this.createdAt = LocalDateTime.now();
  }

  public Entity(String id, LocalDateTime createdAt) {
    this.id = id;
    this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
  }

  public String getId() {
    return id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

}
