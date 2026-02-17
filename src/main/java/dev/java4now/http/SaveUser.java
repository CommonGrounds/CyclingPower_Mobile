package dev.java4now.http;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.java4now.System_Info.SERVER_URL;
import static dev.java4now.http.SendToServer.httpClient;

public class SaveUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveUser.class);

// ip addr ( inet )
//    private static final String SERVER_URL = "http://localhost:8880/api/endpoint";
//    private static final String SERVER_URL = "http://10.0.2.2:8880/api/endpoint";    // emulator to localhost
//    private static final String SERVER_URL = "http://192.168.0.102:8880/api/endpoint";     // mobile to localhost
    public static final StringProperty response_txt = new SimpleStringProperty("---"); // Example UI element

    public static void sendUserToServer(String jsonBody, Consumer<String> successCallback, Consumer<String> errorCallback) {

        LOGGER.debug("json body: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL+"/endpoint"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            successCallback.accept(response.body()); // e.g., "User created successfully!"
                        } else {
                            errorCallback.accept("Failed to send user. Status: " + response.statusCode() + ", Body: " + response.body());
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> errorCallback.accept("Error sending user: " + throwable.getMessage()));
                    return null;
                });

    }
}