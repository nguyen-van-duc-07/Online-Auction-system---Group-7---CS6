package com.auction.client.network;

import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.network.NetworkConfig;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ServerConnection {
  private static Socket socket;
  private static ObjectOutputStream out;
  private static ObjectInputStream in;

  public static void connect() {
    try {
      socket = new Socket(NetworkConfig.DEFAULT_HOST, NetworkConfig.SERVER_PORT);

      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(socket.getInputStream());

      new Thread(() -> listenForData()).start();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void listenForData() {
    try {
      Object response;
      while ((response = in.readObject()) != null) {
        if (response instanceof String) {
          // Phân loại phản hồi
          if ("LOGIN_RESPONSE".equals(response)) {
            String msg = (String) in.readObject();
            ResponseHandler.login(msg);

          } else if ("SIGN_UP_RESPONSE".equals(response)) {
            String msg = (String) in.readObject();
            ResponseHandler.signup(msg);
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void sendData(Object obj) {
    try {
      if (out != null) {
        out.writeObject(obj);
        out.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void closeConnection() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null) socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
