package dev.java4now.http;

//import com.gluonhq.charm.down.Services;
//import com.gluonhq.charm.down.plugins.StorageService;

import dev.java4now.System_Info;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.function.Consumer;

import static dev.java4now.System_Info.FIT_UPLOAD_URL;
import static dev.java4now.System_Info.IMAGE_UPLOAD_URL;

public class SendToServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendToServer.class);

// ip addr ( inet )

//    public static final String USERNAME = "test"; // Replace with actual username
    private static final String PASSWORD = "test"; // Replace with actual password

    public static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void sendFitFileToServer(File localFile, Consumer<String> jsonFileCallback) {
            // Get StorageService for mobile storage
    //        StorageService storageService = Services.get(StorageService.class).orElseThrow(() -> new RuntimeException("StorageService not available"));
    //        String fileName = "cycling_activity_" + System.currentTimeMillis() + ".fit";
    //        File localFile = new File(STORAGE_DIR + "/" +fileName);
/*
            // Ensure directory exists
            localFile.getParentFile().mkdirs();

            // Copy the encoded .fit file to local storage
            try (FileInputStream fis = new FileInputStream(new File("cycling_activity_" + System.currentTimeMillis() + ".fit"));
                 FileOutputStream fos = new FileOutputStream(localFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
*/

        try {
            String auth = System_Info.user_name.get() + ":" + PASSWORD;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + encodedAuth;

            String boundary = "----" + Long.toHexString(System.currentTimeMillis());
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(FIT_UPLOAD_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", authHeader);

            StringBuilder body = new StringBuilder();
            body.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(localFile.getName()).append("\"\r\n")
                    .append("Content-Type: application/octet-stream\r\n\r\n");

            byte[] fileContent = Files.readAllBytes(localFile.toPath());
            String bodyStart = body.toString();
            String bodyEnd = "\r\n--" + boundary + "--\r\n";

            byte[] bodyStartBytes = bodyStart.getBytes(StandardCharsets.UTF_8);
            byte[] bodyEndBytes = bodyEnd.getBytes(StandardCharsets.UTF_8);
            byte[] requestBody = new byte[bodyStartBytes.length + fileContent.length + bodyEndBytes.length];
            System.arraycopy(bodyStartBytes, 0, requestBody, 0, bodyStartBytes.length);
            System.arraycopy(fileContent, 0, requestBody, bodyStartBytes.length, fileContent.length);
            System.arraycopy(bodyEndBytes, 0, requestBody, bodyStartBytes.length + fileContent.length, bodyEndBytes.length);

            HttpRequest request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                String responseBody = response.body();
                                LOGGER.debug("FIT file uploaded successfully: " + responseBody);
                                // Extract JSON filename from response (e.g., "JSON generated: test_...")
                                String jsonFileName = responseBody.replace("File processed successfully. JSON generated: ", "").trim();
                                jsonFileCallback.accept(jsonFileName); // Pass to callback
                            } else {
                                LOGGER.error("Failed to upload FIT file. Status: " + response.statusCode() + ", Body: " + response.body());
                                jsonFileCallback.accept(null); // Signal failure
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> LOGGER.error("Error uploading FIT file: " + throwable.getMessage()));
                        jsonFileCallback.accept(null);
                        return null;
                    });

            localFile.delete();

        } catch (IOException e) {
            Platform.runLater(() -> LOGGER.error("Error sending FIT file to server: " + e.getMessage()));
            jsonFileCallback.accept(null);
        }

    }


    //---------------------------------------------------------
    // Consumer<Boolean> callback parameter to report success/failure.
    public static void sendImageToServer(File imageFile, String jsonFile, Consumer<Boolean> callback) {
        try {
            String auth = System_Info.user_name.get() + ":" + PASSWORD;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + encodedAuth;

            String boundary = "----" + Long.toHexString(System.currentTimeMillis());
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(IMAGE_UPLOAD_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", authHeader);

            StringBuilder body = new StringBuilder();
            body.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"jsonFile\"\r\n\r\n")
                    .append(jsonFile).append("\r\n");
            body.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(imageFile.getName()).append("\"\r\n")
                    .append("Content-Type: ").append(Files.probeContentType(imageFile.toPath())).append("\r\n\r\n");

            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            String bodyStart = body.toString();
            String bodyEnd = "\r\n--" + boundary + "--\r\n";

            byte[] bodyStartBytes = bodyStart.getBytes(StandardCharsets.UTF_8);
            byte[] bodyEndBytes = bodyEnd.getBytes(StandardCharsets.UTF_8);
            byte[] requestBody = new byte[bodyStartBytes.length + fileContent.length + bodyEndBytes.length];
            System.arraycopy(bodyStartBytes, 0, requestBody, 0, bodyStartBytes.length);
            System.arraycopy(fileContent, 0, requestBody, bodyStartBytes.length, fileContent.length);
            System.arraycopy(bodyEndBytes, 0, requestBody, bodyStartBytes.length + fileContent.length, bodyEndBytes.length);

            HttpRequest request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                LOGGER.debug("Image uploaded successfully: " + response.body());
                                callback.accept(true); // Signal success
                            } else {
                                LOGGER.error("Failed to upload image. Status: " + response.statusCode() + ", Body: " + response.body());
                                callback.accept(false); // Signal failure
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            LOGGER.error("Error sending image to server: " + throwable.getMessage());
                            callback.accept(false); // Signal failure on exception
                        });
                        return null;
                    });

        } catch (IOException e) {
            Platform.runLater(() -> {
                LOGGER.error("Error preparing image upload: " + e.getMessage());
                callback.accept(false);
            });
        }
    }
}
