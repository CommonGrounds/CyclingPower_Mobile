package dev.java4now.util;

import dev.java4now.System_Info;
import dev.java4now.View.MainPage;
import dev.java4now.View.SecondPage;
import dev.java4now.util.Filters.LowPassFilter;
import dev.java4now.util.Filters.MovingAverageFilter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.OptionalDouble;

public class SpeedCalculator {

    private static final double EARTH_RADIUS = 6371000; // Radius of the earth in meters
    private static ArrayList<Double> last_km_all_speed = new ArrayList<>();
    public static final StringProperty pace = new SimpleStringProperty("00:00:00");

    private static Location previousLocation;
    private static Instant previousTime;
    private static double distance_meters;
    private static double last_km;
    private static double tmp;

    public static class Location {
        public double latitude;
        public double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public double calculateSpeed(double currentLatitude, double currentLongitude) {
        Location currentLocation = new Location(currentLatitude, currentLongitude);
        Instant currentTime = Instant.now();

        if (previousLocation == null || previousTime == null) {
            // First reading, no speed can be calculated
            previousLocation = currentLocation;
            previousTime = currentTime;
            return 0.0;
        }

        double distance = calculateDistance(previousLocation, currentLocation);
        Duration timeElapsed = Duration.between(previousTime, currentTime);
        double timeInSeconds = timeElapsed.toMillis() / 1000.0;

        previousLocation = currentLocation;
        previousTime = currentTime;

        if (timeInSeconds == 0) {
            return 0; // Avoid division by zero
        }
        distance_meters += distance/1000;
        MainPage.total_distance.set("%.1f".formatted(distance_meters));
//        tmp = distance_meters;
//        last_km_all_speed.add(Double.parseDouble(System_Info.speed.get()));
//        if( (tmp - last_km) >= 1.0){
        /*
            OptionalDouble average = last_km_all_speed
                    .stream()
                    .mapToDouble(a -> a)
                    .average();

         */
            var speed_tmp = Double.parseDouble(System_Info.speed.get());
            if(speed_tmp > 3.0){
                var pace_dur = Duration.ofSeconds((long) ((60 / speed_tmp)*60)); // average.getAsDouble()
                pace.set(String.format("%02d:%02d:%02d", pace_dur.toHoursPart(), pace_dur.toMinutesPart(), pace_dur.toSecondsPart()));
            }else{
                pace.set("00:00:00");
            }
//            last_km = tmp;
//            last_km_all_speed.clear();
//        }
//        System.out.println("total_distance: " + SecondPage.total_distance);

        return distance / timeInSeconds; // Speed in meters per second
    }


    // Zameniti sa istim metodom u HaversineUtils
    private static double calculateDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in meters
    }

/*
    public static void main(String[] args) {
        SpeedCalculator calculator = new SpeedCalculator();

        // Example usage (replace with actual GPS data)
        double lat1 = 37.7749; // Example San Francisco
        double lon1 = -122.4194;

        double lat2 = 37.7750;
        double lon2 = -122.4195;

        double speed1 = calculator.calculateSpeed(lat1, lon1);
        System.out.println("Speed 1: " + speed1 + " m/s");

        try {
            Thread.sleep(1000); // Simulate 1 second delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double speed2 = calculator.calculateSpeed(lat2, lon2);
        System.out.println("Speed 2: " + speed2 + " m/s");
    }
 */
}