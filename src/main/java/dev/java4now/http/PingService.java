package dev.java4now.http;

import dev.java4now.System_Info;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingService {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static void startPing() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(System_Info.SERVER_URL + "/ping"))
                        .GET()
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Ping: " + response.statusCode());
            } catch (Exception e) {
                System.err.println("Ping failed: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.MINUTES);
    }
}