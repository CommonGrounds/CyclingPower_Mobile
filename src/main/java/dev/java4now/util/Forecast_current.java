package dev.java4now.util;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import dev.java4now.App;
import dev.java4now.View.MainPage;
import dev.java4now.View.SecondPage;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static dev.java4now.View.MainPage.wind_rotation;

public class Forecast_current {

    private static final Logger LOGGER = LoggerFactory.getLogger(Forecast_current.class);

    public static BigDecimal temperature, wind_speed, wind_direction, relative_humidity, elevation, surface_pressure, longitude, latitude, utc_offset_seconds,
             weather_code= BigDecimal.valueOf(100);
    public BigDecimal is_day;
    public String temperature_unit, wind_speed_unit, wind_direction_unit, relative_humidity_unit, surface_pressure_unit, timezone, time,
            weather_code_description, timezone_abbreviation, WMO_image_name;
    public static String Background_image_name, Old_Background_image_name,Background_audio_name;
    public boolean image_switch = false;
    /*JSONValue*/ String error, reason;
    public static Rotate rotate = new Rotate();

    public static final StringProperty temp_text = new SimpleStringProperty("---");
    public static final IntegerProperty temperature_int = new SimpleIntegerProperty(0);
    public static final StringProperty wind_speed_text = new SimpleStringProperty("0.0 km/h");
    public static final StringProperty wind_direction_text = new SimpleStringProperty("---");
    public static final StringProperty relative_humidity_text = new SimpleStringProperty("---");
    public static final StringProperty surface_pressure_text = new SimpleStringProperty("---");
    public static final StringProperty wind_dir_short = new SimpleStringProperty("?");
    public static final StringProperty weather_code_text = new SimpleStringProperty("Weather");

    public boolean parseData(String text) throws URISyntaxException, IOException, JsonException {

        JsonObject json = (JsonObject) Jsoner.deserialize(IOUtils.toString(new java.net.URI(text), StandardCharsets.UTF_8)); // string to json from url

        error = (String) json.get("error");
        if(error != null){
            LOGGER.error("error: " + error );
            reason = (String) json.get("reason");
            LOGGER.error("error reason: " + reason );
            return false;
        }

        JsonObject  current = (JsonObject) json.get("current");
        time = (String) current.get("time");
//            time = Json.parseObject(current.getString("time")).toString();
            temperature = (BigDecimal) current.get("temperature_2m");
            wind_speed = (BigDecimal) current.get("wind_speed_10m");
            wind_direction = (BigDecimal) current.get("wind_direction_10m");
            relative_humidity = (BigDecimal) current.get("relative_humidity_2m");
            weather_code = (BigDecimal) current.get("weather_code");
            surface_pressure = (BigDecimal) current.get("surface_pressure");
            is_day = (BigDecimal) current.get("is_day");

            weather_code_description = Weather_Code_Description(weather_code.doubleValue()); // za trenutni weather_code


        SecondPage.weather_icon.setImage(new Image(Objects.requireNonNull(App.class.getResource("mm_api_symbols/" + WMO_image_name )).toExternalForm()));
        rotate.setAngle(wind_direction.doubleValue());

        temp_text.set(/*"Temperature: " + */temperature + "  °C");
        temperature_int.set(temperature.intValue());
        wind_speed_text.set(wind_speed + " km/h");
        wind_direction_text.set(/*"Wind Direction: " + */wind_direction + " °");
        relative_humidity_text.set("Humidity: " + relative_humidity + " %");
        surface_pressure_text.set("Pressure: " + surface_pressure + " hPa");
        wind_dir_short.set(wind_direction_description(wind_direction.intValue()));
        weather_code_text.set(weather_code_description);
        wind_rotation.set(wind_direction.doubleValue());

//        LOGGER.debug("JsonObject Success: \n(" + temperature + ")\n" + "(" + wind_speed + ")\n" + "(" + wind_direction + ")\n" + "(" + weather_code + ")\n" +
//		"(" + surface_pressure + ")\n" + "(" + time + ")\n" + "(" + is_day + ")\n" + "(" + weather_code_description + ")\n" + "(" + WMO_image_name + ")");
        return true;
    }

    // ------------------ WMO CODE DESCRIPTION -----------------
    private String Weather_Code_Description(double weather_code) {
        String description;
        int is_day_now = is_day.intValue();
        if ((int) weather_code < 2) {
            Background_image_name = is_day_now > 0 ? "Background/sunny" : "Background/night";
            Background_audio_name = "nature-birds-singing-217212.mp3";
        } else if ((int) weather_code < 50) { // bilo 60
            Background_image_name = is_day_now > 0 ? "Background/sunny" : "Background/night";
            Background_audio_name = "nature-birds-singing-217212.mp3";
        } else if ((int) weather_code < 95) {
            if ((int) weather_code > 70 && (int) weather_code < 80) {
                Background_image_name = "Background/snow";
                Background_audio_name = "mixkit-bad-weather-heavy-rain-and-thunder-1261.wav";
            } else if ((int) weather_code > 82 && (int) weather_code < 90) {
                Background_image_name = "Background/snow";
                Background_audio_name = "mixkit-bad-weather-heavy-rain-and-thunder-1261.wav";
            } else {
                Background_image_name = "Background/rain";
                Background_audio_name = "mixkit-bad-weather-heavy-rain-and-thunder-1261.wav";
            }
        } else {
            Background_image_name = "Background/thunder";
            Background_audio_name = "mixkit-bad-weather-heavy-rain-and-thunder-1261.wav";
        }

        switch ((int) weather_code) {
            case 0:
                description = "Clear";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0001_sunny.png" : "wsymbol_0008_clear_sky_night.png";
                break;
            case 1:
                description = "Clear";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0001_sunny.png" : "wsymbol_0041_partly_cloudy_night.png";
                break;
            case 2:
                description = "Cloudy";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0002_sunny_intervals.png" : "wsymbol_0044_mostly_cloudy_night.png";
                break;
            case 3:
                description = "Overcast";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0003_white_cloud.png" : "wsymbol_0042_cloudy_night.png";
                break;
            case 45:
                description = "Fog";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0007_fog.png" : "wsymbol_0064_fog_night.png";
                break;
            case 48:
                description = "Fog"; // magla,talozenje inja
                WMO_image_name = is_day_now > 0 ? "wsymbol_0007_fog.png" : "wsymbol_0064_fog_night.png";
                break;
            case 51:
                description = "Drizzle";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0009_light_rain_showers.png" : "wsymbol_0025_light_rain_showers_night.png";
                break;
            case 53:
                description = "Drizzle";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0048_drizzle.png" : "wsymbol_0066_drizzle_night.png";
                break;
            case 55:
                description = "Drizzle";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0018_cloudy_with_heavy_rain.png" : "wsymbol_0034_cloudy_with_heavy_rain_night.png";
                break;
            case 56:
                description = "Drizzle";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0013_sleet_showers.png" : "wsymbol_0029_sleet_showers_night.png";
                break;
            case 57:
                description = "Drizzle";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0050_freezing_rain.png" : "wsymbol_0068_freezing_rain_night.png";
                break;
            case 61:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0009_light_rain_showers.png" : "wsymbol_0025_light_rain_showers_night.png";
                break;
            case 63:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0048_drizzle.png" : "wsymbol_0066_drizzle_night.png";
                break;
            case 65:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0018_cloudy_with_heavy_rain.png" : "wsymbol_0034_cloudy_with_heavy_rain_night.png";
                break;
            case 66:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0013_sleet_showers.png" : "wsymbol_0029_sleet_showers_night.png";
                break;
            case 67:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0050_freezing_rain.png" : "wsymbol_0068_freezing_rain_night.png";
                break;
            case 71:
                description = "Snow";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0011_light_snow_showers.png" : "wsymbol_0027_light_snow_showers_night.png";
                break;
            case 73:
                description = "Snow";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0020_cloudy_with_heavy_snow.png" : "wsymbol_0036_cloudy_with_heavy_snow_night.png";
                break;
            case 75:
                description = "Snow";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0020_cloudy_with_heavy_snow.png" : "wsymbol_0036_cloudy_with_heavy_snow_night.png";
                break;
            case 77:
                description = "Snow";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0020_cloudy_with_heavy_snow.png" : "wsymbol_0036_cloudy_with_heavy_snow_night.png";
                break;
            case 80:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0009_light_rain_showers.png" : "wsymbol_0025_light_rain_showers_night.png";
                break;
            case 81:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0048_drizzle.png" : "wsymbol_0066_drizzle_night.png";
                break;
            case 82:
                description = "Rain";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0018_cloudy_with_heavy_rain.png" : "wsymbol_0034_cloudy_with_heavy_rain_night.png";
                break;
            case 85:
                description = "Snow";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0011_light_snow_showers.png" : "wsymbol_0027_light_snow_showers_night.png";
                break;
            case 86:
                description = "Snow";
                WMO_image_name = is_day_now > 0 ? "wsymbol_0020_cloudy_with_heavy_snow.png" : "wsymbol_0036_cloudy_with_heavy_snow_night.png";
                break;
            case 95:
                description = "Thunderstorm";
                WMO_image_name = "wsymbol_0024_thunderstorms.png";
                break;
            case 96:
                description = "Thunderstorm";
                WMO_image_name = "wsymbol_0024_thunderstorms.png";
                break;
            case 99:
                description = "Thunderstorm";
                WMO_image_name = "wsymbol_0024_thunderstorms.png";
                break;
// Thunderstorm forecast with hail is only available in Central Europe
            default:
                description = "Unknown";
                WMO_image_name = "wsymbol_0999_unknown.png";
                break;
        }
        return description;

    }


    //-----------------------------------------------------
    public String wind_direction_description(int wind_direction) {
        String description = "?";
        if (wind_direction < 20) {
            description = "N";
        } else if (wind_direction < 70) {
            description = "NE";
        } else if (wind_direction < 110) {
            description = "E";
        } else if (wind_direction < 160) {
            description = "SE";
        } else if (wind_direction < 200) {
            description = "S";
        } else if (wind_direction < 250) {
            description = "SW";
        } else if (wind_direction < 290) {
            description = "W";
        } else if (wind_direction < 340) {
            description = "NW";
        } else {
            description = "N";
        }
        return description;
    }
}
