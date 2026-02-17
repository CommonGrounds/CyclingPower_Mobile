package dev.java4now.Maps;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import com.gluonhq.maps.tile.TileRetriever;

import java.util.concurrent.CompletableFuture;

import static dev.java4now.View.SecondPage.actual_zoom;
import static dev.java4now.View.SecondPage.mapPoint;

public class CustomTileLayer extends MapLayer {
    // private final String tileUrl = "https://tiles.stadiamaps.com/tiles/osm_bright/{z}/{x}/{y}{r}.png?api_key=64f61ab5-083e-4fff-82c1-c764879f3fe4";
    private final String tileUrl = "https://tile.openstreetmap.de/{z}/{x}/{y}.png";
//    private final String tileUrl = "https://tile.opentopomap.org/{z}/{x}/{y}.png";
    private final TileRetriever tileRetriever;
    private final MapView mapView;
    private int lastZoom = -1;
    private double lastLat = -1;
    private double lastLon = -1;

    public CustomTileLayer(MapView mapView) {
        this.mapView = mapView;
        this.tileRetriever = (zoom, x, y) -> {
            String url = tileUrl.replace("{z}", String.valueOf(zoom))
                    .replace("{x}", String.valueOf(x))
                    .replace("{y}", String.valueOf(y))
                    .replace("{s}", getSubdomain(x)); // Cycle through subdomains (a, b, c)
            return CompletableFuture.supplyAsync(() -> {
                try {
//                    System.out.println("Attempting to load tile from: " + url);
                    Image image = new Image(url, 256, 256, true, true); // Fixed size, background loading
                    if (image.isError()) {
                        System.err.println("Tile loading error for URL: " + url + " - " + image.getException());
                    }
                    return image;
                } catch (Exception e) {
                    System.err.println("Failed to load tile: " + url + " - " + e.getMessage());
                    return null;
                }
            });
        };

        // Start a polling timer to check map position/zoom every 200ms
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), event -> checkAndUpdateTiles()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void checkAndUpdateTiles() {
        MapPoint center = mapPoint[0];
        int zoom = actual_zoom.get();

        if (center == null) {
            return;
        }

        double lat = center.getLatitude();
        double lon = center.getLongitude();

        // Update tiles if position or zoom has changed
        if (lat != lastLat || lon != lastLon || zoom != lastZoom) {
            lastLat = lat;
            lastLon = lon;
            lastZoom = zoom;
            layoutLayer();
        }
    }

    @Override
    protected void layoutLayer() {
        MapPoint center = mapView.getCenter();
        if (center == null) {
            return;
        }

        double latitude = center.getLatitude();
        double longitude = center.getLongitude();
        int zoom = (int) mapView.getZoom();

        double width = mapView.getWidth();
        double height = mapView.getHeight();
        int tilesX = (int) Math.ceil(width / 256) + 2;  // 256 is tile size, +2 for extra coverage
        int tilesY = (int) Math.ceil(height / 256) + 2;

        int centerTileX = lonToTileX(longitude, zoom);
        int centerTileY = latToTileY(latitude, zoom);

        getChildren().clear(); // Clear any existing content immediately

        for (int dx = -tilesX / 2; dx <= tilesX / 2; dx++) {
            for (int dy = -tilesY / 2; dy <= tilesY / 2; dy++) {
                int tileX = centerTileX + dx;
                int tileY = centerTileY + dy;

                CompletableFuture<Image> tileFuture = tileRetriever.loadTile(zoom, tileX, tileY);
                tileFuture.thenAccept(tileImage -> {
                    if (tileImage != null && !tileImage.isError()) {
                        javafx.application.Platform.runLater(() -> {
                            ImageView imageView = new ImageView(tileImage);
                            imageView.setTranslateX((tileX - centerTileX) * 256);
                            imageView.setTranslateY((tileY - centerTileY) * 256);
                            getChildren().add(imageView);
                        });
                    } else {
                        System.err.println("Tile failed to load for " + tileX + "," + tileY + " at zoom " + zoom);
                    }
                }).exceptionally(throwable -> {
                    System.err.println("Error loading tile: " + throwable.getMessage());
                    return null;
                });
            }
        }
    }

    private String getSubdomain(long x) {
        // Cycle through subdomains (a, b, c) to balance load
        return (x % 3 == 0) ? "a" : (x % 3 == 1) ? "b" : "c";
    }

    private int lonToTileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180) / 360 * Math.pow(2, zoom));
    }

    private int latToTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (int) Math.floor((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * Math.pow(2, zoom));
    }
}