package com.auction.client.screenhandler;

import javafx.scene.Parent;
import lombok.Getter;

@Getter
public class ScreenState {
  private final Parent root;
  private final String title;
  private final String fxmlFile;

  public ScreenState(Parent root, String title, String fxmlFile) {
    this.root = root;
    this.title = title;
    this.fxmlFile = fxmlFile;
  }
}
