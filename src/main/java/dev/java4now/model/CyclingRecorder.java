package dev.java4now.model;

import atlantafx.base.theme.Styles;
import com.garmin.fit.*;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import dev.java4now.System_Info;
import dev.java4now.View.MainPage;
import dev.java4now.http.SendToServer;
import dev.java4now.util.CadenceMeterReader;
import dev.java4now.util.Forecast_current;
import dev.java4now.util.ImageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.java4now.System_Info.*;
import static dev.java4now.View.MainPage.wind_rotation;

public class CyclingRecorder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CadenceMeterReader.class);

    public static final BooleanProperty isRecording = new SimpleBooleanProperty(false);
    private final List<RecordMesg> records = new ArrayList<>();
    private long startTime;
    private int recordCount = 0;
    private double currentLat = 44.8706; // Starting latitude (Zagreb, Croatia)
    private double currentLon = 20.6518; // Starting longitude
    private float distanceTraveled = 0.0f; // Track distance in meters
    private int totalCalories = 0; // Total calories burned for the session
    public static String FileName;
    private String lastUploadedJsonFile;
    java.io.File file;
    public static java.io.File localDir;
    // FIT epoch: 1989-12-31 00:00:00 UTC (631065600 seconds since Unix epoch)
    private static final long FIT_EPOCH_SECONDS = 631065600L;

    // Developer data fields for custom weather data
    private DeveloperDataIdMesg devId;
    private FieldDescriptionMesg windRotationDesc;
    private FieldDescriptionMesg windDirDesc;
    private FieldDescriptionMesg headingFieldDesc;
    private FieldDescriptionMesg windSpeedDesc;
    private FieldDescriptionMesg weatherFieldDesc;


    public CyclingRecorder() throws IOException {
        FileName = "cycling_activity_" + System.currentTimeMillis() + ".fit";
        localDir = new java.io.File(Services.get(StorageService.class)
                .flatMap(StorageService::getPrivateStorage)
                .orElseThrow(() -> new IOException("No storage available"))
                .getPath());
        file = new java.io.File(localDir,FileName);
    }

    public void startRecording() {
        if (!is_Recording()) {
            isRecording.set(true);;
// Convert current Unix timestamp to FIT timestamp
            long unixTime = System.currentTimeMillis() / 1000; // Seconds since 1970
            startTime = unixTime - FIT_EPOCH_SECONDS; // Seconds since 1989-12-31
            records.clear();
            recordCount = 0;
            distanceTraveled = 0.0f;
            totalCalories = 0;
            currentLat = 44.8706; // Reset to starting position
            currentLon = 20.6501;

            // Initialize developer data ID (unique app identifier)
            devId = new DeveloperDataIdMesg();
            byte[] appId = new byte[16];
            for (int i = 0; i < 16; i++) {
                appId[i] = (byte) i; // Simple example UUID; replace with a real unique UUID in production
            }
            for (int i = 0; i < appId.length; i++) {
                devId.setApplicationId(i, appId[i]);
            }
            devId.setDeveloperDataIndex((short) 0);
            devId.setApplicationVersion(1L);

            // Field description for wind speed (custom field)
            windSpeedDesc = new FieldDescriptionMesg();
            windSpeedDesc.setDeveloperDataIndex((short) 0);
            windSpeedDesc.setFieldDefinitionNumber((short) 0);
            windSpeedDesc.setFitBaseTypeId(FitBaseType.FLOAT32);
            windSpeedDesc.setFieldName(0, "WindSpeed");
            windSpeedDesc.setUnits(0, "m/s");

            // Field description for rotation (custom field)
            windRotationDesc = new FieldDescriptionMesg();
            windRotationDesc.setDeveloperDataIndex((short) 0);
            windRotationDesc.setFieldDefinitionNumber((short) 1);
            windRotationDesc.setFitBaseTypeId(FitBaseType.SINT16);   // signed integer because minus values
            windRotationDesc.setFieldName(0, "Rotation");
            windRotationDesc.setUnits(0, "°");  // if problem use - windRotationDesc.setUnits(0, "deg");

            windDirDesc = new FieldDescriptionMesg();
            windDirDesc.setDeveloperDataIndex((short) 0);
            windDirDesc.setFieldDefinitionNumber((short) 3);
            windDirDesc.setFitBaseTypeId(FitBaseType.STRING);
            windDirDesc.setFieldName(0, "Direction");
            windDirDesc.setUnits(0, "");

            headingFieldDesc = new FieldDescriptionMesg();
            headingFieldDesc.setDeveloperDataIndex((short) 0);
            headingFieldDesc.setFieldDefinitionNumber((short) 4);
            headingFieldDesc.setFitBaseTypeId(FitBaseType.STRING);
            headingFieldDesc.setFieldName(0, "Heading");
            headingFieldDesc.setUnits(0, "");

            weatherFieldDesc = new FieldDescriptionMesg();
            weatherFieldDesc.setDeveloperDataIndex((short) 0);
            weatherFieldDesc.setFieldDefinitionNumber((short) 2);
            weatherFieldDesc.setFitBaseTypeId(FitBaseType.UINT8);
            weatherFieldDesc.setFieldName(0, "Weather");
            weatherFieldDesc.setUnits(0, "");
        }
    }

    public void stopRecording() {
        if (is_Recording()) {
            isRecording.set(false);
            encodeFitFile();
        }
    }

    public boolean is_Recording() {
        return isRecording.get();
    }

    public void updatePosition() {
        if (!is_Recording()) {
            return;
        }

        RecordMesg record = new RecordMesg();
        long timestamp = startTime + (System.currentTimeMillis() / 1000); // Simulate 1-second intervals

        // Set timestamp (seconds since UTC 1989-12-31 00:00:00)
        record.setTimestamp(new DateTime(timestamp));

        // Set latitude and longitude (semicircles: degrees * (2^31 / 180))
        double lat = System_Info.lat;
        double lon = System_Info.lon;
        record.setPositionLat((int) (lat * (Math.pow(2, 31) / 180)));
        record.setPositionLong((int) (lon * (Math.pow(2, 31) / 180)));

        // Simulated cycling data (based on distance traveled and time)
        float speed = Float.parseFloat(System_Info.speed.get()) / 3.6f; // m/s
        float power = Float.parseFloat(System_Info.power.get());
        short cadence;
        if(CadenceMeterReader.cadence_data.get().equals("---")){
            cadence = 0;
        }else{
            cadence = Short.parseShort(CadenceMeterReader.cadence_data.get());
        }
        float altitude = Float.parseFloat(System_Info.alt.get());
        float slope = Float.parseFloat(System_Info.slope.get());
        short batteryStatus = BatteryStatus.GOOD;


        record.setSpeed(speed); // m/s * 1000 for FIT units
        record.setPower((int) power); // watts
        record.setCadence(cadence); // rpm
        record.setAltitude(altitude); // m
        record.setGrade((float) (slope)); // slope in percent (e.g., 5% = 5.0f)
        record.setBatterySoc((float) batteryStatus); // battery state of charge in percent
        record.setTemperature((byte) (Forecast_current.temperature_int.get()));

        record.setDistance(Float.valueOf(MainPage.total_distance.get()) * 1000); // Meters in FIT units

// Estimate calories burned for this interval (simple formula: power * time / 4.184 / rider weight)
        // FIT expects calories in kcal, and RecordMesg.setCalories() takes an integer (kcal)
        float caloriesThisSecond = (power * (1.0f / 3600)) / 0.239f; // Watts to kcal (1 watt-hour = 0.239 kcal)
        int caloriesInt = (int) Math.round(caloriesThisSecond); // Ensure integer for FIT
        if (caloriesInt > 0) { // Ensure no zero values
            record.setCalories(caloriesInt); // kcal per second
            totalCalories += caloriesInt;
        } else {
            record.setCalories(1); // Minimum non-zero value to avoid 0
        }

        // Add developer field for wind speed
        DeveloperField windField = new DeveloperField(windSpeedDesc, devId);
        windField.setValue(Forecast_current.wind_speed.floatValue());
        record.addDeveloperField(windField);

        // Add developer field for rotation
        int rotation = wind_rotation.intValue() - compass.intValue();
        DeveloperField rotationField = new DeveloperField(windRotationDesc, devId);
        rotationField.setValue((int)rotation); // Cast to int for SINT16 ( tj. int )
        record.addDeveloperField(rotationField);

        DeveloperField windDirField = new DeveloperField(windDirDesc, devId);
        windDirField.setValue(Forecast_current.wind_dir_short.getValue());
        record.addDeveloperField(windDirField);

        DeveloperField headingField = new DeveloperField(headingFieldDesc, devId);
        headingField.setValue(System_Info.heading_dir_short.getValue());
        record.addDeveloperField(headingField);

//        System.out.println("Debug Wind: wind_speed: " + Forecast_current.wind_speed.floatValue() + " : " + "rotation: " + rotation + " : " + "wind_dir: " + Forecast_current.wind_dir_short.getValue() + " : " + "heading: " + System_Info.heading_dir_short.getValue());

        records.add(record);
        recordCount++;
//        System.out.println("MyDebug: " + recordCount + "," + records.size());
    }

    private void encodeFitFile() {
        try {
            FileEncoder encoder = new FileEncoder(file, Fit.ProtocolVersion.V2_0);

            // Write developer data ID and field descriptions
            encoder.write(devId);             // mora za sve ( session, record etc )
            encoder.write(windSpeedDesc);     // record
            encoder.write(windRotationDesc);  // record
            encoder.write(windDirDesc);       // record
            encoder.write(headingFieldDesc);  // record

            // File ID message (required)
            FileIdMesg fileId = new FileIdMesg();
            fileId.setType(File.ACTIVITY);
            fileId.setManufacturer(Manufacturer.DEVELOPMENT);
            fileId.setProduct(1); // Placeholder product ID
            fileId.setSerialNumber(123456L); // Placeholder serial number
            fileId.setTimeCreated(new DateTime(startTime));
            encoder.write(fileId);

// Session message (optional but recommended)
            SessionMesg session = new SessionMesg();
            session.setTimestamp(new DateTime(startTime + records.size() - 1));
            session.setStartTime(new DateTime(startTime));
            session.setTotalElapsedTime((float) total_elapsed_time.get());      // Seconds
            session.setTotalMovingTime((float) records.size());                 // Seconds
            session.setTotalDistance(Float.valueOf(MainPage.total_distance.get()) * 1000); // Meters
            session.setTotalCalories(Integer.valueOf(System_Info.calories.get())); // Total calories for the session
            // IMPORTANT Dole sve Automatski se preracunava u dekoderu na serveru
            /*
            session.setAvgCadence((short) 77);
            session.setAvgPower(151);
            session.setAvgSpeed(21.2F);
            session.setMaxAltitude(150F);
            session.setMinAltitude(78F);
             */

//            int weather = Forecast_current.weather_code.intValue();
            // Using Optional with default value
            int weather = Optional.ofNullable(Forecast_current.weather_code)
                    .map(BigDecimal::intValue)
                    .orElse(100); // or any other default value
            DeveloperField weatherField = new DeveloperField(weatherFieldDesc, devId);
            weatherField.setValue(weather);
            session.addDeveloperField(weatherField);
            encoder.write(weatherFieldDesc);  // developer data za session

            encoder.write(session);

            // Write all record messages
            for (RecordMesg record : records) {
                encoder.write(record);
            }

            DeviceInfoMesg device = new DeviceInfoMesg();
            device.setTimestamp(new DateTime(startTime + records.size() - 1)); // startTime - integer representing milliseconds since epoch
            device.setBatteryStatus(BatteryStatus.GOOD);
            device.setProductName("CyclePower");
            encoder.write(device);


            // Event message to mark activity end
            EventMesg event = new EventMesg();
            event.setTimestamp(new DateTime(startTime + records.size() - 1));
            event.setEvent(Event.TIMER);
            event.setEventType(EventType.STOP_ALL);
            encoder.write(event);

            encoder.close();
            LOGGER.debug("FIT file saved to: " + file.getAbsolutePath());
            System_Info.fitFile = file;//new java.io.File(file.getAbsolutePath());

            SendToServer.sendFitFileToServer(System_Info.fitFile, jsonFileName -> {
//                progress_dialog_show();
//                progressBar.setProgress(0.8);
                if (jsonFileName != null) {
                    progress_dialog_show();
                    lastUploadedJsonFile = jsonFileName;
                    LOGGER.debug("JSON file from FIT upload: " + lastUploadedJsonFile);

                    if (!images_view.isEmpty()) {
                        AtomicInteger uploadedFiles = new AtomicInteger(0);
                        AtomicInteger totalFiles = new AtomicInteger(images_view.size());
                        images_view.forEach(view -> {
                            if (view != null) {
                                LOGGER.debug("DebugIMAGES: -" + view.getPath() + " --- " + lastUploadedJsonFile);
                                SendToServer.sendImageToServer(view, lastUploadedJsonFile, success -> {
                                    if (success) {
                                        ImageUtils.deleteFile(view); // Delete resized file on success
                                        images_view.remove(view);       // Remove from list
                                        LOGGER.debug("Removed and deleted: " + view.getPath());
                                        updateProgress(uploadedFiles.incrementAndGet(),totalFiles.get());
                                    } else {
                                        LOGGER.error("Upload failed, keeping: " + view.getPath());
                                    }
                                });
                            } else {
                                LOGGER.error("Skipping null image file");
                            }
                        });
                    }else{
                        LOGGER.debug("DebugIMAGES: no images");
                        updateProgress(1,1);  // no images, so finish
                    }
                } else {
                    LOGGER.error("FIT upload failed, no JSON file available.");
                }
            });

        } catch (Exception e) {
            LOGGER.error("Error encoding FIT file: " + e.getMessage());
        }
    }


    //----------------------------------------------------------------------
    private void updateProgress(int completed, int totalFiles) {
        double totalProgress = (double) completed / totalFiles;
        Platform.runLater(() -> {
            progressBar.setProgress(totalProgress);
            progress_msg.set("Upload: " + Math.round(totalProgress *100) + " %");
            if (totalProgress >= 1.0) {
                Timeline hide_pause = new Timeline(new KeyFrame(Duration.seconds(3), evt -> {
                    System_Info.progress_dialog_hide();
                }));
                hide_pause.play(); // only play once after 3 seconds
            }
        });
    }
}
