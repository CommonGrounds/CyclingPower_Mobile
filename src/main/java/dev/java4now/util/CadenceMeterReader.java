package dev.java4now.util;

import com.gluonhq.attach.ble.*;
import com.gluonhq.attach.util.Platform;
import dev.java4now.util.Filters.ExponentialSmoothing;
import dev.java4now.util.Filters.LowPassFilter;
import dev.java4now.util.Filters.MovingAverageFilter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CadenceMeterReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CadenceMeterReader.class);

    public static boolean CADENCE_STARTED = false;
    static byte[] ENABLE_NOTIFICATION_VALUE = {0x01, 0x00};
    static final byte[] DISABLE_NOTIFICATION_VALUE = {0x00, 0x00};
    static final byte[] ENABLE_INDICATION_VALUE = {0x02, 0x00};
    private static final UUID CYCLING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    private static final UUID CADENCE_CHARACTERISTIC_UUID = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb");
    private static final UUID CUSTOM_CADENCE_SERVICE_UUID = UUID.fromString("8ce5cc01-0a4d-11e9-ab14-d663bd873d93");
    private static long lastProcessedTime = 0;
    private static long lastValidReadingTime = 0;
    private static final long CADENCE_TIMEOUT_MS = 3000; // 2 seconds without data = stopped
    private static final long CADENCE_RECONNECT_TIME = 15000;
    private static long currentTime = Long.MAX_VALUE;
    private static Timeline waiting_Timeline;
    private static long lastValidSystemTime = 0;

    static LowPassFilter filter_lowPass = new LowPassFilter(0.2); // Smoothing factor
    static MovingAverageFilter filter_avg = new MovingAverageFilter(3); // Average over 5 readings
    static ExponentialSmoothing filter_exp = new ExponentialSmoothing(0.5); // Smoothing factor
    static BleService ble_service;
    static BleProfile profile_service;
    static BleDevice ble_device;
    static BleDescriptor Batt_BleDescriptor;
    public static final ObjectProperty state_changed = new SimpleObjectProperty();
    public static final StringProperty cadence_data = new SimpleStringProperty("---");
    public static final StringProperty cadence_data_avg = new SimpleStringProperty("---");
    private static boolean ONLY_ONE = true;
    private static boolean BATT_ONLY_ONE = true;
    private static int previousCrankRevolutions = -1;
    private static long previousCrankEventTime = -1;
    private static int divider = 1;
    private static boolean scanning = true;
    public static ArrayList<Double> all_cadence = new ArrayList<>();
    public static IntegerProperty icon_code = new SimpleIntegerProperty(Integer.MAX_VALUE);


//-------------------------------------------------------------------
    public void start() {
        if (Platform.isDesktop()) {
            LOGGER.info("Bluetooth is not supported on desktop.");
            return;
        }

        LOGGER.debug("---------------- DEBUG IS ON ---------------");
        BleService.create().ifPresent(ble -> {
            ble_service = ble;
            ObservableList<BleDevice> ble_list_device = ble.startScanningDevices();

            ble_list_device.addListener((ListChangeListener<BleDevice>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (BleDevice device : c.getAddedSubList()) {
                            // Ako je već pronađen cadence senzor, ignoriši nove uređaje
                            if (ble_device != null && CADENCE_STARTED) {
                                LOGGER.debug("Cadence: Cadence sensor already found, ignoring device: {}",
                                        device.getName() != null ? device.getName() : "N/A");
                                continue;
                            }

                            LOGGER.debug("Cadence: device.getName(): {} (Address: {})",
                                    device.getName() != null ? device.getName() : "N/A",
                                    device.getAddress());
                            checkForCadenceService(device);
                        }
                    }
                }
            });

            // Zaustavi skeniranje nakon 20 sekundi ako nijedan uređaj nije pronađen
            Timeline scanTimeout = new Timeline(new KeyFrame(Duration.seconds(20), evt -> {
                if (ble_device == null) {
                    ble.stopScanning();
                    LOGGER.info("Cadence: No cadence sensor found after 20 seconds, stopping scan");
                } else {
                    // Cadence senzor je pronađen, zaustavi skeniranje
                    ble.stopScanning();
                    LOGGER.info("Cadence: Cadence sensor found, stopping further scanning");
                }
            }));
            scanTimeout.play();
        });
    }




    //-------------------------------------------------------------------
    private static void checkForCadenceService(BleDevice device) {
        BleService.create().ifPresent(ble -> {
            if (device.stateProperty().get() == BleDevice.State.STATE_CONNECTED) {
                LOGGER.debug("Cadence: Device {} already connected, checking profiles",
                        device.getName() != null ? device.getName() : "N/A");
                checkProfilesForCadence(device, ble);
                return;
            }

            LOGGER.debug("Cadence: Connecting to device {} to check for cadence service",
                    device.getName() != null ? device.getName() : "N/A");

            ble.connect(device);

            InvalidationListener stateListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (device.stateProperty().get() == BleDevice.State.STATE_CONNECTED) {
                        LOGGER.debug("Cadence: Device {} connected, waiting for profiles",
                                device.getName() != null ? device.getName() : "N/A");

                        // Sačekaj malo pre nego što proveriš profile
                        Timeline waitForProfiles = new Timeline(new KeyFrame(Duration.seconds(3), evt -> {
                            checkProfilesForCadence(device, ble);
                        }));
                        waitForProfiles.play();

                        device.stateProperty().removeListener(this);
                    } else if (device.stateProperty().get() == BleDevice.State.STATE_DISCONNECTED) {
                        LOGGER.debug("Cadence: Device {} disconnected during service discovery",
                                device.getName() != null ? device.getName() : "N/A");
                        device.stateProperty().removeListener(this);
                    }
                }
            };

            device.stateProperty().addListener(stateListener);

            // Automatski odspajanje ako se ne pronađe cadence servis nakon 10 sekundi
            Timeline disconnectTimeout = new Timeline(new KeyFrame(Duration.seconds(10), evt -> {
                if (ble_device == null || ble_device != device) {
                    LOGGER.debug("Cadence: No cadence service found on device {}, disconnecting",
                            device.getName() != null ? device.getName() : "N/A");
                    ble.disconnect(device);
                    device.stateProperty().removeListener(stateListener);
                }
            }));
            disconnectTimeout.play();
        });
    }



    //-------------------------------------------------------------------
    private static void checkProfilesForCadence(BleDevice device, BleService ble) {
        LOGGER.debug("Cadence: Checking profiles for device {}",
                device.getName() != null ? device.getName() : "N/A");

        InvalidationListener profilesListener = new InvalidationListener() {
            private boolean foundCadence = false;

            @Override
            public void invalidated(Observable observable) {
                if (foundCadence) return;

                ObservableList<BleProfile> profiles = FXCollections.observableArrayList((List<BleProfile>) observable);
                LOGGER.debug("Cadence: Checking {} profiles for device {}",
                        profiles.size(), device.getName() != null ? device.getName() : "N/A");

                for (BleProfile profile : profiles) {
                    LOGGER.debug("Cadence: Profile UUID: {}", profile.getUuid());

                    // Proveri standardne i custom cadence servise
                    if (profile.getUuid().equals(CYCLING_SPEED_AND_CADENCE_SERVICE_UUID) ||
                            profile.getUuid().equals(CUSTOM_CADENCE_SERVICE_UUID)) {

                        LOGGER.debug("Cadence: Found cadence service {} on device {}",
                                profile.getUuid(), device.getName() != null ? device.getName() : "N/A");
                        foundCadence = true;
                        ble_device = device;
                        ble.stopScanning(); // ZAUSTAVI SKENIRANJE
                        LOGGER.info("Cadence: Cadence sensor found, scanning stopped");
                        device.getProfiles().removeListener(this);
                        connectToDevice(device);
                        return;
                    }

                    // Proveri karakteristike unutar svakog profila
                    checkCharacteristicsForCadence(profile, device, ble);
                }

                // Ako nema profila ili nije pronađen cadence, proveri ponovo za 2 sekunde
                if (profiles.isEmpty() && !foundCadence) {
                    LOGGER.debug("Cadence: No profiles found yet, waiting...");
                    Timeline retry = new Timeline(new KeyFrame(Duration.seconds(2), evt -> {
                        checkProfilesForCadence(device, ble);
                    }));
                    retry.play();
                } else if (!profiles.isEmpty() && !foundCadence) {
                    LOGGER.debug("Cadence: No cadence service found in {} profiles, disconnecting", profiles.size());
                    ble.disconnect(device);
                    device.getProfiles().removeListener(this);
                }
            }
        };

        device.getProfiles().addListener(profilesListener);

        // Pokreni inicijalnu proveru
        ObservableList<BleProfile> currentProfiles = device.getProfiles();
        if (!currentProfiles.isEmpty()) {
            profilesListener.invalidated(currentProfiles);
        }
    }



    //-------------------------------------------------------------------
    private static void checkCharacteristicsForCadence(BleProfile profile, BleDevice device, BleService ble) {
        InvalidationListener characteristicsListener = new InvalidationListener() {
            private boolean foundCadence = false;

            @Override
            public void invalidated(Observable observable) {
                if (foundCadence) return;

                ObservableList<BleCharacteristic> characteristics = (ObservableList<BleCharacteristic>) observable;
                LOGGER.debug("Cadence: Checking {} characteristics in profile {}",
                        characteristics.size(), profile.getUuid());

                for (BleCharacteristic characteristic : characteristics) {
                    LOGGER.debug("Cadence: Characteristic UUID: {}", characteristic.getUuid());

                    if (characteristic.getUuid().equals(CADENCE_CHARACTERISTIC_UUID)) {
                        LOGGER.debug("Cadence: Found cadence characteristic {} in profile {} on device {}",
                                characteristic.getUuid(), profile.getUuid(),
                                device.getName() != null ? device.getName() : "N/A");

                        foundCadence = true;
                        ble_device = device;
                        ble.stopScanning();
                        profile.getCharacteristics().removeListener(this);
                        connectToDevice(device);
                        return;
                    }
                }
            }
        };

        profile.getCharacteristics().addListener(characteristicsListener);

        // Pokreni inicijalnu proveru
        ObservableList<BleCharacteristic> currentCharacteristics = profile.getCharacteristics();
        if (!currentCharacteristics.isEmpty()) {
            characteristicsListener.invalidated(currentCharacteristics);
        }
    }



    //--------------------------------------------------
    private static void connectToDevice(BleDevice device) {
        BleService.create().ifPresent(ble2 -> {
            ble2.connect(device);
            state_changed.bind(device.stateProperty());
            state_changed.addListener((obs, ov, nv) -> {
                javafx.application.Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (BleDevice.State.STATE_CONNECTED.equals(nv)) {
                            resetCadenceState();
                            divider = 1;
                            ONLY_ONE = true;
                            BATT_ONLY_ONE = true;
                            discoverServices(device);
                        } else if (BleDevice.State.STATE_DISCONNECTED.equals(nv)) {
                            resetCadenceState();
//                            all_cadence.clear();
                            cadence_data.set("Reset");
//                            cadence_data_avg.set("0");
                            ble2.connect(device);
                        }
                    }
                });
            });
        });
    }





    //-------------------------------------------------------------------
    private static void discoverServices(BleDevice device) {
        device.getProfiles().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                List<BleProfile> list_of_profiles = (List<BleProfile>) observable;//device.getProfiles();
                LOGGER.debug("MyDebug info: discoverServices-size: " + list_of_profiles.size() + " : " + list_of_profiles);
                for (BleProfile service : list_of_profiles) {
//                    LOGGER.debug("MyDebug info: service.getUuid(): " + service.getUuid() );
                    if (service.getUuid().toString().equals("00001816-0000-1000-8000-00805f9b34fb")) { // Cadence Service UUID
                        LOGGER.debug("MyDebug info: Cadence Service UUID equal");
                        discoverCadenceCharacteristics(service);
                    }
                    if (service.getUuid().equals(BATTERY_SERVICE_UUID)) {
                        discoverBatteryCharacteristics(service);   // TODO ali onda problem sa kadencom ( moze biti samo 1 servis tj. ovde koji se 1. inicij. )
                    }
                }
            }
        });
    }





    //-------------------------------------------------------------------
    private static void discoverCadenceCharacteristics(BleProfile profile) {
        profile.getCharacteristics().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                ObservableList<BleCharacteristic> characteristics = (ObservableList<BleCharacteristic>) observable;

                for (BleCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().equals(CADENCE_CHARACTERISTIC_UUID)) {
                        if (ONLY_ONE) {
                            ONLY_ONE = false;
                            LOGGER.debug("Cadence: Found cadence characteristic, discovering descriptors...");

                            // Discover and configure descriptors
                            characteristic.getDescriptors().addListener(new InvalidationListener() {
                                private boolean configured = false;

                                @Override
                                public void invalidated(Observable obs) {
                                    if (configured) return;

                                    ObservableList<BleDescriptor> descriptors = (ObservableList<BleDescriptor>) obs;
                                    LOGGER.debug("Cadence: Discovered {} descriptors", descriptors.size());

                                    for (BleDescriptor descriptor : descriptors) {
                                        LOGGER.debug("Cadence: Descriptor UUID: {}", descriptor.getUuid());

                                        if (descriptor.getUuid().equals(CLIENT_CHARACTERISTIC_CONFIG_UUID)) {
                                            configured = true;
                                            LOGGER.debug("Cadence: Configuring CCCD for notifications");

                                            // Subscribe to characteristic
                                            try {
                                                ble_service.subscribeCharacteristic(ble_device, profile.getUuid(), characteristic.getUuid());
                                                descriptor.setValue(ENABLE_NOTIFICATION_VALUE);
                                                LOGGER.debug("Cadence notifications enabled");
                                                CADENCE_STARTED = true;
                                            } catch (Exception e) {
                                                LOGGER.error("Cadence: Error enabling notifications: {}", e.getMessage());
                                            }

                                            // Listen for value changes
                                            characteristic.valueProperty().addListener((o, ov, nv) -> {
                                                if (nv != null && !Arrays.equals(ov, nv)) {
                                                    LOGGER.debug("Cadence: Notification received - Raw data: {}", bytesToHex(nv));
                                                    read_cadence(nv);
                                                } else {
                                                    // Check for timeout
                                                    if (System.currentTimeMillis() - lastValidReadingTime > CADENCE_TIMEOUT_MS) {
                                                        if (!cadence_data.get().equals("0")) {
                                                            cadence_data.set("0");
                                                            filter_exp = new ExponentialSmoothing(0.5);
                                                            filter_avg = new MovingAverageFilter(3);
                                                            LOGGER.debug("Cadence: Timeout detected - resetting to 0");
                                                        }
                                                    }
                                                }
                                            });
/*
                                            // Periodična provera da li notifikacije stižu , efikasno ali nije potrebno ako stavimo delay kod 1. citanja nivoa baterije
                                            Timeline cadenceCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(15), evt -> {
                                                if (System.currentTimeMillis() - lastValidReadingTime > CADENCE_TIMEOUT_MS * 2) {
                                                    LOGGER.warn("Cadence: No notifications received, attempting to re-subscribe");
                                                    try {
                                                        ble_service.subscribeCharacteristic(ble_device, profile.getUuid(), characteristic.getUuid());
                                                        descriptor.setValue(ENABLE_NOTIFICATION_VALUE);
                                                        LOGGER.debug("Cadence: Re-subscribed to notifications");
                                                    } catch (Exception e) {
                                                        LOGGER.error("Cadence: Error re-subscribing: {}", e.getMessage());
                                                    }
                                                }
                                            }));
                                            cadenceCheckTimeline.setCycleCount(Animation.INDEFINITE);
                                            cadenceCheckTimeline.play();
*/
                                            break;
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }


    //-------------------------------------------------------------------
    private static void discoverBatteryCharacteristics(BleProfile profile) {
        if (BATT_ONLY_ONE) {
            BATT_ONLY_ONE = false;
            profile_service = profile;
            profile.getCharacteristics().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    ObservableList<BleCharacteristic> characteristics = (ObservableList<BleCharacteristic>) observable;
                    LOGGER.debug("Cadence Batt: Characteristics discovered: {} : {}", characteristics.size(), characteristics);

                    for (BleCharacteristic characteristic : characteristics) {
                        if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                            LOGGER.debug("Cadence Batt: Found battery characteristic: {}", characteristic.getUuid());

                            // Dodaj listener za promene vrednosti karakteristike
                            characteristic.valueProperty().addListener((obs, oldValue, newValue) -> {
                                if (newValue != null && newValue.length > 0 && !Arrays.equals(oldValue, newValue)) {
                                    LOGGER.debug("Cadence Batt: Value updated - Raw data: {}", bytesToHex(newValue));
                                    processBatteryLevel(newValue);
                                } else {
                                    LOGGER.warn("Cadence Batt: Value update invalid (null, empty, or unchanged)");
                                }
                            });

                            // Pokreni periodično čitanje sa većim intervalom
                            Timeline batteryTimeline = new Timeline(new KeyFrame(Duration.seconds(120), evt -> {
                                try {
                                    // Pokreni čitanje vrednosti karakteristike
                                    ble_service.readCharacteristic(ble_device, profile_service.getUuid(), characteristic.getUuid());
                                    LOGGER.debug("Cadence Batt: Initiated periodic read for battery level");
                                } catch (Exception e) {
                                    LOGGER.error("Cadence Batt: Error initiating battery read: {}", e.getMessage());
                                }
                            }));
                            batteryTimeline.setCycleCount(Animation.INDEFINITE);
                            batteryTimeline.play();
                            LOGGER.debug("Cadence Batt: Started periodic battery reading every 120s");

                            // IMPORTANT Inicijalno čitanje odmah ali delay pre čitanja baterije da ne bi iskljucio kadencu ( unsubscribe ) :
                            Timeline initialBatteryRead = new Timeline(new KeyFrame(Duration.seconds(2), evt -> {
                                try {
                                    ble_service.readCharacteristic(ble_device, profile_service.getUuid(), characteristic.getUuid());
                                    LOGGER.debug("Cadence Batt: Initiated initial read for battery level");
                                } catch (Exception e) {
                                    LOGGER.error("Cadence Batt: Error during initial battery read: {}", e.getMessage());
                                }
                            }));
                            initialBatteryRead.play();  // By default, a Timeline's cycleCount is set to 1

                            break; // Pronašli smo karakteristiku, izlazimo iz petlje
                        }
                    }
                }
            });
        }
    }


    //-------------------------------------------------------------
    private static void processBatteryLevel(byte[] data) {
        if (data == null || data.length == 0) {
            LOGGER.debug("Cadence Batt: Battery data is null or empty");
            return;
        }

        LOGGER.debug("Cadence Batt: Processing battery data: {}", bytesToHex(data));

        int batteryLevel = data[0] & 0xFF; // Convert byte to unsigned integer (0-255)

        // Battery level should be 0-100%, but handle out-of-range values
        if (batteryLevel > 100) {
            LOGGER.warn("Cadence Batt: Battery level out of range: {}%, capping to 100", batteryLevel);
            batteryLevel = 100;
        }

        if(scanning){ // Ako se citanje kadence ne pokrene odmah prikazati animaciju
            final AtomicInteger[] count = {new AtomicInteger()};
            String [] str = {"-","--","---"};
            waiting_Timeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
//                LOGGER.info("count{ " +count[0].get() + " )");
                if(!scanning){
//                    LOGGER.info("count stop");
                    waiting_Timeline.stop();
                    return;
                }
                cadence_data.set(str[count[0].get() %3]);
                count[0].getAndIncrement();
                if(count[0].get() == 3){
                    count[0].set(0);}
            }));
            waiting_Timeline.setCycleCount(Animation.INDEFINITE);
            waiting_Timeline.play();
        }
        LOGGER.info("Cadence Batt: Battery Level: {}%", batteryLevel);
        icon_code.set(batteryLevel);
    }



    //--------------------------------------------------------
    private static void resetCadenceState() {
        previousCrankRevolutions = -1;
        previousCrankEventTime = -1;
        lastProcessedTime = 0;
        LOGGER.debug("Cadence: State reset complete");
    }




    private static final long MIN_NOTIFICATION_INTERVAL_MS = 200; // Ignore notifications closer than 100ms
    private static int sameRevolutionCount = 0;
    private static final int MAX_SAME_REVOLUTIONS = 3;
    //-----------------------------------------------------------------
    private static void read_cadence(byte[] nv) {
        LOGGER.debug("Cadence: Raw bytes: {}", bytesToHex(nv));
        scanning = false;

        currentTime = System.currentTimeMillis();

        if (currentTime - lastProcessedTime < MIN_NOTIFICATION_INTERVAL_MS) {
            LOGGER.debug("Cadence: Skipping rapid notification ({}ms)", currentTime - lastProcessedTime);
            return;
        }
        lastProcessedTime = currentTime;

        double rawRpm = calculateRPMUsingSystemTime(nv);

        if (rawRpm == -1) {
            sameRevolutionCount++;
            if (sameRevolutionCount > MAX_SAME_REVOLUTIONS) {
                LOGGER.debug("Cadence: Too many same-rev readings, checking timeout");
            }
            return;
        }

        // Valid reading received - update timestamp and reset counters
        lastValidReadingTime = currentTime;
        sameRevolutionCount = 0;

        if (Double.isNaN(rawRpm) || rawRpm < 0) {
            LOGGER.debug("Cadence: Skipping invalid RPM: {}", rawRpm);
            return;
        }

        // Apply filtering
        double expCadence = filter_exp.filter(rawRpm);
        double smoothedCadence = filter_avg.filter(expCadence);

        LOGGER.debug("Cadence: Raw RPM: {} -> Filtered RPM: {}", rawRpm, smoothedCadence);

        if (smoothedCadence >= 0) {
            cadence_data.set(String.format("%.0f", smoothedCadence));
            all_cadence.add(smoothedCadence);

            OptionalDouble average = all_cadence
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
            cadence_data_avg.set("%.0f".formatted(average.isPresent() ? average.getAsDouble() : Double.NaN));
        } else {
            cadence_data.set("0");
        }
    }



    //-----------------------------------------------------------------
    public static double calculateRPMUsingSystemTime(byte[] data) {
        if (data == null || data.length < 5) {
            return Double.NaN;
        }

        int crankRevolutions = data[1] & 0xFF;
        long currentSystemTime = System.currentTimeMillis();

        if (previousCrankRevolutions == -1) {
            previousCrankRevolutions = crankRevolutions;
            lastValidSystemTime = currentSystemTime;
            return -1;
        }

        int revDiff = crankRevolutions - previousCrankRevolutions;
        if (revDiff < 0) {
            revDiff += 256;
        }

        if (revDiff == 0) {
            return -1; // Same revolution
        }

        double timeSeconds = (currentSystemTime - lastValidSystemTime) / 1000.0;

        // Validate time range
        if (timeSeconds <= 0.1 || timeSeconds > 10.0) {
            LOGGER.warn("Cadence: Invalid system time range: {}s for {} revs", timeSeconds, revDiff);
            previousCrankRevolutions = crankRevolutions;
            lastValidSystemTime = currentSystemTime;
            return -1;
        }

        double rpm = (revDiff * 60.0) / timeSeconds;

        previousCrankRevolutions = crankRevolutions;
        lastValidSystemTime = currentSystemTime;

        return rpm > 180 ? 120.0 : rpm;
    }



    //--------------------------------------------------------
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}