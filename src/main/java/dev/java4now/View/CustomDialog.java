package dev.java4now.View;

//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONObject;
//import com.google.gson.Gson;
import io.github.wycst.wast.json.JSON;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
import dev.java4now.http.SaveUser;
import dev.java4now.local_json.City_json;
import dev.java4now.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.function.Consumer;

import static dev.java4now.App.theme;
import static dev.java4now.System_Info.SERVER_URL;
import static dev.java4now.http.SendToServer.httpClient;

public class CustomDialog {
    private final String message;
    private final EventHandler<ActionEvent> onResult;
    private VBox dialogBox;
    private Pane overlay;
    Label messageLabel;


    public CustomDialog(String message, EventHandler<ActionEvent> onResult) {
        this.message = message;
        this.onResult = onResult;
    }

    public void show(Pane parent) {
        // Overlay (semi-transparent background)
        overlay = new Pane();
//        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Gray overlay
        overlay.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100, 0.5), null, null)));
        overlay.setPrefSize(parent.getWidth(), parent.getHeight()); // zacrnjen screen

        // Dialog content
        messageLabel = new Label(message);
        Label lbl0 = new Label("Enter Name");
        TextField name = new TextField();
        Button okButton = new Button("OK");
//        okButton.getStyleClass().add("button");

        dialogBox = new VBox(10, messageLabel, lbl0, name, okButton);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(20));
//        dialogBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");
//        dialogBox.getStylesheets().add(getClass().getResource("root.css").toExternalForm());
        if (theme.isDarkMode()) {
            dialogBox.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1), null, null)));
        } else {
            dialogBox.setBackground(new Background(new BackgroundFill(Color.rgb(235, 235, 235, 1), null, null)));
        }
        dialogBox.setBorder(new Border(new BorderStroke(Color.DARKGREEN,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        dialogBox.setMaxWidth(Math.max(300, parent.getWidth() / 6));
        dialogBox.setMaxHeight(parent.getHeight() / 3);
//        dialogBox.setMaxHeight(Region.USE_COMPUTED_SIZE);

        // Center the dialog
        StackPane dialogPane = new StackPane(overlay, dialogBox);
        dialogPane.setAlignment(Pos.CENTER);

        // Button action
        okButton.setOnAction(e -> {
            create_user(name.getText().toLowerCase(Locale.ROOT), "test", ok -> {
                if (ok) {
//                    Gson gson = new Gson();
//                    ObjectMapper MAPPER = new ObjectMapper();  // jakson
                    User user = new User(name.getText().toLowerCase(Locale.ROOT), "test", "test@gmail.com");
                    String json = JSON.toJsonString(user);       // wast
//                    String json = JSON.toJSONString(user);          // fastjson2
//                    String json= gson.toJson(user);                 // Gson
//                    json = MAPPER.writeValueAsString(user);         // jakson
//                    System.out.println(json);
                    SaveUser.sendUserToServer(json,
                            success -> {
                                onResult.handle(new ActionEvent(name.getText().toLowerCase(Locale.ROOT), null));
                                close(parent);
                            },
                            error -> {
                                System.out.println("error: " + error);
                                messageLabel.setText(error);
                                messageLabel.setTextFill(Color.RED);
                            });
                } else {
                    messageLabel.setText("Choose another name");
                    messageLabel.setTextFill(Color.RED);
                }
            });
        });

        // Add to parent and block interaction
        parent.getChildren().add(dialogPane);
        overlay.setOnMouseClicked(e -> e.consume()); // Prevent clicks on overlay
    }

    private void close(Pane parent) {
        parent.getChildren().remove(overlay.getParent());
    }


    //------------------------------------------------------------------
    private void create_user(String username, String pass, Consumer<Boolean> Callback) {
        checkUsernameExists(username, pass,
                exists -> {
                    if (exists) {
//                        statusLabel.setText("Username '" + username + "' already exists.");
                        Callback.accept(false);
                    } else {
//                        statusLabel.setText("Username '" + username + "' is available.");
                        Callback.accept(true);
                    }
                },
                error -> {
                    messageLabel.setText(error);
                    messageLabel.setTextFill(Color.RED);
//                    Callback.accept(false);
                });
    }


    //-----------------------------------------------------------------------
    public static void checkUsernameExists(String username, String pass, Consumer<Boolean> existsCallback, Consumer<String> errorCallback) {
        String url = SERVER_URL + "/check-username?username=" + username;
        System.out.println("Checking username at: " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
//                .header("Authorization", getBasicAuthHeader(username,pass)) // Optional, if your server requires auth here
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            boolean exists = Boolean.parseBoolean(response.body());
                            existsCallback.accept(exists);
                        } else {
                            errorCallback.accept("Failed to check username. Status: " + response.statusCode());
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> errorCallback.accept("Error checking username: " + throwable.getMessage()));
                    return null;
                });
    }

    // Helper method for Basic Auth (if needed)
    private static String getBasicAuthHeader(String USERNAME, String PASSWORD) {
        String auth = USERNAME + ":" + PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }
}
