package dev.java4now;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.*;
import atlantafx.base.util.Animations;
import com.github.cliftonlabs.json_simple.JsonException;
import com.gluonhq.attach.battery.BatteryService;
import com.gluonhq.attach.compass.CompassService;
import com.gluonhq.attach.connectivity.ConnectivityService;
import com.gluonhq.attach.device.DeviceService;
import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.magnetometer.MagnetometerService;
import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.attach.pictures.PicturesService;
import com.gluonhq.attach.position.Parameters;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.attach.util.Services;
import com.gluonhq.attach.settings.SettingsService;
import dev.java4now.View.SecondPage;
import dev.java4now.http.SendToServer;
import dev.java4now.local_json.CityService_json;
import dev.java4now.model.CyclingRecorder;
import dev.java4now.util.*;
import dev.java4now.util.Filters.ExponentialSmoothing;
import dev.java4now.util.Filters.LowPassFilter;
import dev.java4now.util.Filters.MovingAverageFilter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.gluonhq.attach.position.PositionService.DEFAULT_PARAMETERS;
import static dev.java4now.App.camera_btn;
import static dev.java4now.App.modalPane;
import static dev.java4now.util.ImageUtils.deleteFile;


public class System_Info {

    public static class Location_int {
        public double latitude;
        public double longitude;

        public Location_int(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static DisplayService displayService;
    public static Dimension2D dimension = new Dimension2D(800, 600);
    public static String platform = "Unknown";
    public  static Notification msg;
    public static double lat,lon;
    public static final StringProperty alt = new SimpleStringProperty(Double.toString(Double.NaN));
    static long old_time = 0;
    public static CityService_json cityService;
    private static double previous_alt = 0.0;
    private static Double alt_tmp = null;
    private static double gain_up = 0.0;
    private static double gain_down = 0.0;
    private static Location_int previousLocation;
    public static java.time.Duration duration = java.time.Duration.ZERO;
    private static double power_awg_old;
    public static File fitFile;
    public static ProgressBar progressBar;
    private static boolean previous_zero = false;

    private static final ArrayList<Double> all_speed = new ArrayList<>();

    public static String javaVersion() {
        return System.getProperty("java.version");
    }
    public static String javafxVersion() {
        return System.getProperty("javafx.version");
    }


    public static final DoubleProperty total_elapsed_time = new SimpleDoubleProperty(0.0);
    public static final BooleanProperty gps_exist = new SimpleBooleanProperty(false);
    public static final BooleanProperty location_flag = new SimpleBooleanProperty(false);
    public static BooleanProperty net_exist = new SimpleBooleanProperty(false);
    static boolean net_service = false;

    public static final DoubleProperty display_width = new SimpleDoubleProperty(0.0);
    public static final DoubleProperty display_width_half = new SimpleDoubleProperty(0.0);
    public static final DoubleProperty display_height = new SimpleDoubleProperty(600.0);
    public static final DoubleProperty center_height = new SimpleDoubleProperty(0.0);

    public static final StringProperty city_country_text = new SimpleStringProperty("No Signal");
    public static final StringProperty suburb_text = new SimpleStringProperty("---");
    public static final StringProperty road_house_number_text = new SimpleStringProperty("---");

    public static final BooleanProperty speed_flag = new SimpleBooleanProperty(false);
    public static final StringProperty speed = new SimpleStringProperty("0.0");
    public static final StringProperty speed_average = new SimpleStringProperty("0.0");
    public static final StringProperty alt_gain_up = new SimpleStringProperty("0.0");
    public static final StringProperty alt_gain_down = new SimpleStringProperty("0.0");
    public static final BooleanProperty forecast_flag = new SimpleBooleanProperty(false);
    static int count = 0;

    public static final DoubleProperty compass = new SimpleDoubleProperty(0.0);
    public static final StringProperty heading_dir_short = new SimpleStringProperty("?");
    public static final StringProperty slope = new SimpleStringProperty("0.0");
    public static final StringProperty power = new SimpleStringProperty("0");
    public static final StringProperty power_max = new SimpleStringProperty("0");
    public static final StringProperty power_avg = new SimpleStringProperty("0");
    public static final BooleanProperty power_avg_up = new SimpleBooleanProperty(false);
    public static final StringProperty calories = new SimpleStringProperty("0");
    public static final StringProperty progress_msg = new SimpleStringProperty("Upload 0 %");

    public static final DoubleProperty battery_level = new SimpleDoubleProperty(80.0);
    public static final BooleanProperty battery_plugged = new SimpleBooleanProperty(false);
    public static final IntegerProperty b_weight = new SimpleIntegerProperty(10);
    public static final IntegerProperty r_weight = new SimpleIntegerProperty(60);
    public static final StringProperty user_name = new SimpleStringProperty(null);
    public static VBox progress_d;

    private static final ArrayList<Double> power_avg_list = new ArrayList<>();

    private static LowPassFilter filter_lowPass = new LowPassFilter(0.2); // Smoothing factor
    static ExponentialSmoothing filter_exp = new ExponentialSmoothing(0.3); // Smoothing factor
    private static final MovingAverageFilter filter_avg = new MovingAverageFilter(5); // Average over 5 readings

    static SpeedCalculator calculator = new SpeedCalculator();
    static CyclingRecorder recorder;

    public static String coffee_url = "https://buymeacoffee.com/PoorCyclist";

//------------------------------- Menjati po potrebi ---------------------------------

//    private static final String SERVER_URL = "http://localhost:8080/api";                    // desktop to localhost
//    public static final String SERVER_URL = "http://10.0.2.2:8080/api";                     // emulator to localhost
//    public static final String SERVER_URL = "http://192.168.0.102:8080/api";                 // mobile to localhost
    public static final String SERVER_URL = "https://cyclingpower-server-1.onrender.com/api";  // mobile to render

//    public static final String FIT_UPLOAD_URL = "http://localhost:8080/api/upload-fit";                          // Update with your server URL - desktop
//    public static final String IMAGE_UPLOAD_URL = "http://localhost:8080/api/upload-image";                      // Image endpoint              - desktop
//    public static final String FIT_UPLOAD_URL = "http://10.0.2.2:8080/api/upload-fit";                           // emulator to localhost
//    public static final String IMAGE_UPLOAD_URL = "http://10.0.2.2:8080/api/upload-image";                       // emulator to localhost
//    public static final String FIT_UPLOAD_URL = "http://192.168.0.102:8080/api/upload-fit";                      // mobile to localhost
//    public static final String IMAGE_UPLOAD_URL = "http://192.168.0.102:8080/api/upload-image";                  // mobile to localhost
    public static final String FIT_UPLOAD_URL = "https://cyclingpower-server-1.onrender.com/api/upload-fit";       // mobile to render
    public static final String IMAGE_UPLOAD_URL = "https://cyclingpower-server-1.onrender.com/api/upload-image";   // mobile to render


    static {
        try {
            recorder = new CyclingRecorder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<File> images_view = new LinkedList<>();



    //------------------------------------------------------------------
    public static void capture_image(){
        // The picture service allows the developer to load a picture from the device's local file system or from a picture taken directly using the device's camera.
        AtomicInteger image_counter = new AtomicInteger();
        PicturesService.create().ifPresentOrElse(service -> {
            System.out.println("Camera OK");
            service.imageProperty().addListener((obs, ov, image) -> {
                if (image != null) {
                    // Resize and save the captured image
                    File resizedFile = ImageUtils.resizeCameraImage(image, user_name.get() + "_" + image_counter.getAndIncrement() + "_",
                            800, 600);
                    System.out.println("Resized camera image: " + resizedFile.getPath() + ", " + resizedFile.length() + " bytes");
                    images_view.add(resizedFile);
                }
            });
            camera_btn.setOnAction(e -> {
                service.asyncTakePhoto(true);
            });
        }, () -> System.out.println("Camera not available"));
    }



    //------------------------------------------------------------------
    public static int get_platform(){
        AtomicInteger result = new AtomicInteger();
        DeviceService.create().map(deviceService -> {
                    platform = deviceService.getPlatform();
                    System.out.println("platform: " + platform);
                    result.set(1);
                    return result.get();
                })
                .orElseGet(() -> {
                    System.out.println("DeviceService nema - Desktop");
                    platform = "Desktop";
                    result.set(0);
                    return result.get();
                });

        return result.get();
    }




    //------------------------------------------------------------------
    public static void get_display(){
        displayService = DisplayService.create().orElse(null);
        if (displayService == null) {
            System.out.println("WARNING: Unable to load Gluon Display Service");
        } else {
            dimension = displayService.getDefaultDimensions();
            System.out.println("Gluon Display Service created");
        }
    }


    //--------------------------------------------------------------------
    public static boolean net_available(){
        AtomicBoolean connected = new AtomicBoolean(false);
        ConnectivityService.create().ifPresent(service -> {
            connected.set(service.isConnected());
//            System.out.println("Network connectivity available? " + String.valueOf(connected));
//            service.connectedProperty().addListener((obs, ov, nv) -> {
//            });
            service.connectedProperty().addListener((obs, ov, nv) -> {
                net_exist.set(nv);
                System.out.println("connected: " + System_Info.net_exist.get());
            });
        });
        return connected.get();
    }


    //----------------------------------------------------
    public static void get_orientation(){
        // With the orientation service you can detect whether the device is currently oriented horizontally or vertically.
        OrientationService.create().ifPresent(service -> {
            Orientation orientation = service.getOrientation().get();
            System.out.println("Current orientation: " + orientation.name());
            service.orientationProperty().addListener((obs, ov, nv) -> {
                System.out.println("Current orientation: " + nv.name());
//                get_display();
            });
        });
    }



    //-------------------------------------------------
    public static void get_gps() {

        if(gps_exist.get()){
            return;
        }

        Services.get(PositionService.class).ifPresent(service -> {
            System.out.println("Waiting for GPS signal");

            service.positionProperty().addListener((obs, ov, nv) -> {
                gps_exist.set(true);
//                System.out.println("Latest known GPS coordinates:\n" + nv.getLatitude() + ", " + nv.getLongitude() + "\nAlt: " + nv.getAltitude() + " m");
                lat = nv.getLatitude();
                lon = nv.getLongitude();
                location_flag.set(true);
                speed_flag.set(true);
                alt.set("%.1f".formatted(nv.getAltitude()));

                if((System.currentTimeMillis() - old_time) < 5000){
                    return;
                }

                calculate_slope(Double.parseDouble(alt.get()),lat,lon);
                // TODO - normalizacija gain-a
                if(alt_tmp != null && !speed.get().equals("0.0")){
                    if(Double.parseDouble(alt.get()) > alt_tmp + 5 || Double.parseDouble(alt.get()) < alt_tmp - 5){
                        alt_tmp = Double.parseDouble(alt.get());    // Velike brze promene ignorisemo
                    }
                    if(Double.parseDouble(alt.get()) >= alt_tmp){
                        gain_up += Double.parseDouble(alt.get()) - alt_tmp;
                        alt_gain_up.set("%.0f".formatted(gain_up));
                    }else{
                        gain_down +=  alt_tmp - Double.parseDouble(alt.get());
                        alt_gain_down.set("%.0f".formatted(gain_down));
                    }
                    power.set("%.0f".formatted(estimatePower(b_weight.get() + r_weight.get(),Double.parseDouble(slope.get()),Double.parseDouble(speed.get()))));
//                    System.out.println("Debug_power: alt: " + alt.get() + ", speed: " + speed.get() + ", slope: " + slope.get() + ", power: " + power.get());
                }else{
                    power.set("0");
                }
                power_max.set("%.0f".formatted(Double.max(Double.parseDouble(power_max.get()),Double.parseDouble(power.get()))));
                if(Double.parseDouble(power.get()) > 0.0){
                    power_avg_list.add(Double.parseDouble(power.get()));
                    OptionalDouble average = power_avg_list
                            .stream()
                            .mapToDouble(a -> a)
                            .average();
                    power_avg.set("%.0f".formatted(average.isPresent() ? average.getAsDouble() : Double.NaN));
                    if(average.getAsDouble() > power_awg_old){
                        power_avg_up.set(true);
                    }else{
                        power_avg_up.set(false);
                    }
                    power_awg_old = average.isPresent() ? average.getAsDouble() : Double.NaN;
                }
                calories.set("%.0f".formatted(calculateCalories(Double.parseDouble(power_avg.get()), duration.toSeconds())));
                alt_tmp = Double.parseDouble(alt.get());
                old_time = System.currentTimeMillis();
                // https://nominatim.openstreetmap.org/reverse?format=json&lat=-34.44076&lon=-58.70521
                // https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=44.91&lon=20.63
                String GPS_Reverse_url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + lat + "&lon=" + lon;

                try {
                    if(net_exist.get()){
                        gps_service.parseData(GPS_Reverse_url);
                        if(count++ == 0 || count >= (300/5)){ // pauza 300 ( 5 min ) / 5 sec manje zbog wait
                            System.out.println("new address data");
                            forecast_flag.set(true);
                            count=1;
                        }
                    }else{
                        cityService.findByLatLong(lat, lon);
                    }
                } catch (URISyntaxException | IOException | JsonException e) {
                    throw new RuntimeException(e);
                }
            });
//            service.start();
            /*
            accuracy - Desired accuracy in location updates
            timeInterval - minimum number of milliseconds between location updates.
            distanceFilter - minimum number of meters between location updates.
            backgroundModeEnabled - allows position updates when the app is running in background
            DEFAULT_PARAMETERS - accuracy=MEDIUM, timeInterval=5000, distanceFilter=15.0, backgroundModeEnabled=false
             */
            // Isprobavati - za sada radi sa 1ms interval i 1m distance na 100ms se GPS gasi - TODO
            com.gluonhq.attach.position.Parameters parameters = new com.gluonhq.attach.position.Parameters(Parameters.Accuracy.LOW, 1L, 1.0F, true);
            service.start(parameters);
//            service.positionProperty().removeListener((obs, ov, nv) ->{}); // TODO
        });
    }



    //------------------------------------------------------------------
    public static void get_compass(){
//################### COMPASS I MAGNETOMETER MORAJU ZAJEDNO DA SE STARTUJU #########################

        MagnetometerService.create().ifPresent(service -> {
            service.readingProperty().addListener((obs, ov, nv) -> {
//                System.out.println(String.format("Magnetic field: %.4f, %.4f, %.4f. Magnitude: %.4f",nv.getX(), nv.getY(), nv.getZ(), nv.getMagnitude()));
//                lbl_magnetic.setText(String.format("Magnetic field: (x)%.1f, (y)%.1f, (z)%.1f: Magnitude: %.2f (µT)",nv.getX(), nv.getY(), nv.getZ(), nv.getMagnitude()));
            });
            service.start(); // 10 Hz default
            //        service.start(new com.gluonhq.attach.magnetometer.Parameters(10)); // 10 Hz default
//            public Parameters(double frequency) // frequency - the rate with which to update the service

//            MagnetometerReading reading = service.getReading();
//            System.out.printf("Magnetic field: %.4f, %.4f, %.4f. Magnitude: %.4f",
//                    reading.getX(), reading.getY(), reading.getZ(), reading.getMagnitude());
        });

        CompassService.create().ifPresent(service -> {
            service.headingProperty().addListener((obs, ov, nv) -> {
//                System.out.println("Current heading: " + nv.doubleValue());
                compass.set(nv.doubleValue());
                heading_dir_short.set(heading_direction_description((int) compass.get()));
            });
            service.start();
//            double heading = service.getHeading();
//            System.out.printf("Current heading: %.2f", heading);
//            label.setText("Current heading: ", + heading );
        });
    }



    //----------------------------------------------------
    public static void get_battery(){
        BatteryService.create().ifPresent(service -> {
            battery_level.set(service.getBatteryLevel());
            battery_plugged.set(service.isPluggedIn());
//            lbl.setText("BatteryLevel: " + batteryLevel + "\nPlugged: " + pluggedIn);

            service.batteryLevelProperty().addListener((obs, ov, nv) -> {
                battery_level.set(nv.doubleValue() * 100);          // 0.0 - 1.0
                System.out.println("BatteryLevel: " + battery_level.get());
            });

            service.pluggedInProperty().addListener((obs, ov, nv) -> {
                battery_plugged.set(nv);
            });
        });
    }




    // ----------------- MAIN SCREEN NOTIFICATION ----------------------
    public static Notification notification() {

        msg = new Notification(
                "",
                new FontIcon(Material2OutlinedAL.HELP_OUTLINE)
        );
        msg.getStyleClass().addAll(
                Styles.ELEVATED_1 , Styles.ACCENT                   // .message css je default
        );
        msg.setPrefHeight(20/*Region.USE_PREF_SIZE*/);
        msg.setMaxHeight(20/*Region.USE_PREF_SIZE*/);
        StackPane.setAlignment(msg, Pos.TOP_CENTER);              // Vazno
        msg.setVisible(false);

        return msg;
    }


//-------------------------------------------------------------
    public static void show_notification(String message,String style){
        if(!message.isEmpty() && msg != null){
            msg.setMessage(message);
            if(message.contains("coffee")){
                var coffee_font = new FontIcon(Feather.COFFEE);
 //               coffee_font.getStyleClass().addAll(Styles.ACCENT);   // Za graphic node uvek ce biti boja notification style-a
                msg.setGraphic(coffee_font);
            }
            msg.getStyleClass().remove(msg.getStyleClass().size()-1);
            msg.getStyleClass().add(style);
            msg.setVisible(true);
            var in = Animations.slideInDown(msg, Duration.millis(250));
            in.playFromStart();

            msg.setOnClose(e -> {
                var out = Animations.slideOutUp(msg, Duration.millis(250));
                out.setOnFinished(f -> msg.setVisible(false));
                out.playFromStart();
//            modalPane.hide();
            });
        }
    }



    //-----------------------------------------------------
    public static VBox progress_dialog(){
        Label msg = new Label();
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().addAll(Styles.SMALL,"custom-progress-bar");
        msg.textProperty().bind(progress_msg);
        var box = new VBox(10,msg,progressBar);
        box.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1), null, null)));
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(250);
        box.setMaxHeight(50);

        return box;
    }



//-----------------------------------------------
    public static void progress_dialog_show(){
        modalPane.setAlignment(Pos.TOP_CENTER);
        modalPane.usePredefinedTransitionFactories(Side.TOP);
        Platform.runLater(() -> modalPane.show(progress_d));
    }


//-----------------------------------------------
    public static void progress_dialog_hide(){
//        modalPane.setAlignment(Pos.TOP_CENTER);
//        modalPane.usePredefinedTransitionFactories(Side.TOP);
        if(modalPane.isVisible()) {
            Platform.runLater(() -> modalPane.hide(true));
        }
    }


    //-----------------------------------------------------
    private static void calculate_slope(double alt,double currentLatitude, double currentLongitude){
        Location_int currentLocationInt = new Location_int(currentLatitude, currentLongitude);
        if (previousLocation == null ) {
            // First reading, no speed can be calculated
            previousLocation = currentLocationInt;
        }
        double distance = HaversineUtils.getDistance(previousLocation.latitude,previousLocation.longitude,currentLatitude,currentLongitude);
        previousLocation = currentLocationInt;

        double rise = alt - previous_alt;

        System.out.println("rise / distance: " + rise + " / " + distance);
        double slope_ratio = rise / distance;
        double slope_percentage = slope_ratio * 100;

        // TODO - prevent big slope jumps
        if(slope_percentage > Double.parseDouble(slope.get()) + 5 ){
            slope_percentage = Double.parseDouble(slope.get()) + 2;
        } else if (slope_percentage < Double.parseDouble(slope.get()) - 5) {
            slope_percentage = Double.parseDouble(slope.get()) - 2;
        }

        if(slope_percentage < 1.0 || speed.get().equals("0.0")){
            slope.set("0.0");
        }else{
            slope.set("%.1f".formatted(slope_percentage));
        }

        previous_alt = alt;
    }



    //-----------------------------------------------------
    // bikeWeight - Total weight of bike and rider in kilograms
        public static double estimatePower(double bikeWeight, double slopePercent, double speedKmh) {
            // Convert speed from km/h to m/s
            double speedMs = speedKmh * 1000 / 3600;

            double effectiveSpeedMs;

            if(Forecast_current.wind_speed == null){
                Forecast_current.wind_speed = BigDecimal.valueOf(0.0);
            }

            if(Forecast_current.wind_speed.doubleValue() > 0.0){
                double windSpeedMs =  Forecast_current.wind_speed.doubleValue() * 1000 / 3600;
                // Calculate the relative wind speed in the direction of the bike
                double windDirectionRad = Math.toRadians(Forecast_current.wind_direction.intValue());
                double bikeDirectionRad = Math.toRadians((int) compass.get());
                double relativeWindSpeedMs = windSpeedMs * Math.cos(windDirectionRad - bikeDirectionRad);
                // Effective speed for air resistance calculation
                effectiveSpeedMs = speedMs + relativeWindSpeedMs;
            }else{
                effectiveSpeedMs = speedMs;
            }

            // Calculate the force of gravity acting on the bike
            double gravityForce = bikeWeight * 9.81 * Math.sin(Math.toRadians(slopePercent));

            // Calculate the air resistance force (simplified model)
            double airResistanceCoefficient = 0.5; // Adjust based on bike and rider aerodynamics
            double airDensity = 1.225; // kg/m^3 (approximate air density at sea level)
            double frontalArea = 0.5; // m^2 (approximate frontal area of bike and rider)
//            double airResistanceForce = 0.5 * airDensity * speedMs * speedMs * frontalArea * airResistanceCoefficient;
            double airResistanceForce = 0.5 * airDensity * effectiveSpeedMs * effectiveSpeedMs * frontalArea * airResistanceCoefficient;

            // Calculate the rolling resistance force
            double rollingResistanceCoefficient = 0.005; // Typical value for road tires
            double rollingResistanceForce = bikeWeight * 9.81 * rollingResistanceCoefficient;

            // Calculate the total force required to maintain speed
            double totalForce = gravityForce + airResistanceForce + rollingResistanceForce;

            // Calculate the power required
            double powerWatts = totalForce * speedMs;   // Power = Force * Velocity.

            return powerWatts;
        }




    //----------------------------------------------------
        public static double calculateCalories(double powerWatts, double durationSeconds) {
            // Convert power (watts) to energy (joules)
            double energyJoules = powerWatts * durationSeconds;

            // Convert energy (joules) to calories (kcal)
            double caloriesBurned = energyJoules / 4184;

            return caloriesBurned;
        }



    //---------------------------------------------------
    public static void speed_timer() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE); // repeat over and over again ( samo 1. ako se ne definise )
        var runtime = Runtime.getRuntime();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), evt -> {
            total_elapsed_time.set(total_elapsed_time.get() + 1);
            if(System_Info.speed_flag.get()){
                System_Info.speed_flag.set(false);
                double spd = ((calculator.calculateSpeed(System_Info.lat, System_Info.lon)*3600)/1000);
                // TODO - prevent big speed jumps
                if(spd > Double.parseDouble(System_Info.speed.get()) + 10 ){
                    spd = Double.parseDouble(System_Info.speed.get()) + 10;
                } else if (spd < Double.parseDouble(System_Info.speed.get()) - 10) {
                    spd = Double.parseDouble(System_Info.speed.get()) - 10;
                }
//                double lowPassSpeed = filter_lowPass.filter(spd);
                double expSpeed = filter_exp.filter(spd);
                double smoothedSpeed = filter_avg.filter(expSpeed);
                if(previous_zero){
                    if(smoothedSpeed > 6.0){
                        previous_zero = false;
                        App.sound_click.play();
                    }else{
                        return;
                    }
                }
                all_speed.add(smoothedSpeed);
                OptionalDouble average = all_speed
                        .stream()
                        .mapToDouble(a -> a)
                        .average();
                speed_average.set("%.1f".formatted(average.isPresent() ? average.getAsDouble() : Double.NaN));
                System_Info.speed.set("%.1f".formatted(smoothedSpeed));

                duration = duration.plusSeconds(1);

                // Custom format
                // ####################################Java-8####################################
//        String formattedElapsedTime = String.format("%02d:%02d:%02d", duration.toHours() % 24,
//                duration.toMinutes() % 60, duration.toSeconds() % 60);
//        System.out.println("The total of the 5 times is: " + formattedElapsedTime);
                // ##############################################################################

                // ####################################Java-9####################################
                SecondPage.total_time.set(String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())); // Ako je manje od 2 nula ispred
//        System.out.println("Total time is: " + SecondPage.total_time.get());
                // ##############################################################################

                recorder.updatePosition();

            }else{
                System_Info.speed.set("0.0");
                if (!previous_zero){
                    App.sound_click.play();
                }
                previous_zero = true;
            }
        }));
        timeline.play();
    }



    //--------------------------------------------------
    static void save_theme_settings(String value){
        SettingsService.create().ifPresent(service -> {
            service.store("LightTheme", value);
//            service.remove("key");
        });
    }


    //--------------------------------------------------
    static String retrieve_theme_settings(){
        AtomicReference<String> value = new AtomicReference<>();
        SettingsService.create().ifPresent(service -> {
            value.set(service.retrieve("LightTheme"));
//            service.remove("key");
        });

        return value.get();
    }



    //--------------------------------------------------
    static void save_screen_settings(String value){
        SettingsService.create().ifPresent(service -> {
            service.store("Screen", value);
//            service.remove("key");
        });
    }



    //--------------------------------------------------
    static String retrieve_screen_settings(){
        AtomicReference<String> value = new AtomicReference<>();
        SettingsService.create().ifPresent(service -> {
            value.set(service.retrieve("Screen"));
//            service.remove("key");
        });

        return value.get();
    }



    //--------------------------------------------------
    static public void save_all_settings(String theme_value,String screen_value,int bike_weight,int rider_weight){
        SettingsService.create().ifPresent(service -> {
            service.store("LightTheme", theme_value);
            service.store("Screen", screen_value);
            service.store("BikeWeight", String.valueOf(bike_weight));
            service.store("RiderWeight", String.valueOf(rider_weight));
//            service.remove("key");
        });
    }


    //--------------------------------------------------
    static public void retrieve_all_settings(){
        SettingsService.create().ifPresent(service -> {
            if(service.retrieve("BikeWeight") != null){
                b_weight.set(Integer.parseInt(service.retrieve("BikeWeight")));
                r_weight.set(Integer.parseInt(service.retrieve("RiderWeight")));
            }
            if (service.retrieve("User") != null){
                user_name.set(service.retrieve("User"));
            }
//            service.remove("key");
        });
    }


    //--------------------------------------------------
    static void save_user_name(String value){
        SettingsService.create().ifPresent(service -> {
            service.store("User", value);
//            service.remove("key");
        });
    }


    //-----------------------------------------------------
    public static String heading_direction_description(int head_direction) {
        String description = "?";
        if (head_direction < 20) {
            description = "N";
        } else if (head_direction < 70) {
            description = "NE";
        } else if (head_direction < 110) {
            description = "E";
        } else if (head_direction < 160) {
            description = "SE";
        } else if (head_direction < 200) {
            description = "S";
        } else if (head_direction < 250) {
            description = "SW";
        } else if (head_direction < 290) {
            description = "W";
        } else if (head_direction < 340) {
            description = "NW";
        } else {
            description = "N";
        }
        return description;
    }
}