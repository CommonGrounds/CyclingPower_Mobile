package dev.java4now.util;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import dev.java4now.View.SecondPage;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javafx.scene.paint.Color;

public class AQI_Index {

    public BigDecimal european_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,dust;
    public static  BigDecimal uv_index = BigDecimal.valueOf(1);
    public String european_aqi_unit,pm10_unit,pm2_5_unit,carbon_monoxide_unit,nitrogen_dioxide_unit,sulphur_dioxide_unit,dust_unit,
            european_aqi_def,pm10_def,pm2_5_def,carbon_monoxide_def,nitrogen_dioxide_def,sulphur_dioxide_def;
    public int color_index_aqi,color_index_pm10,color_index_pm2_5,color_index_carbon_monoxide,color_index_nitrogen_dioxide,color_index_sulphur_dioxide;

    public static String[] color_definition = {"#50F0E6","#50CCAA","#F0E641","#FF5050","#960032","#7D2181"};
    public static String[] uv_color_definition = {"#00FF00","yellow","orange","red","#9400D3"};

    public static final StringProperty european_aqi_text = new SimpleStringProperty("---");
    public static final StringProperty pm10_text = new SimpleStringProperty("0.0 km/h");
    public static final StringProperty pm2_5_text = new SimpleStringProperty("---");
    public static final StringProperty dust_text = new SimpleStringProperty("---");
    public static final StringProperty uv_index_text = new SimpleStringProperty("UV: 1");

    public void parseData(String openAQIData) throws URISyntaxException, IOException, JsonException {

        JsonObject json = (JsonObject) Jsoner.deserialize(IOUtils.toString(new java.net.URI(openAQIData), StandardCharsets.UTF_8));

        JsonObject  current = (JsonObject) json.get("current");
        european_aqi = (BigDecimal) current.get("european_aqi");
        pm10 = (BigDecimal) current.get("pm10");
        pm2_5 = (BigDecimal) current.get("pm2_5");
//        carbon_monoxide = (BigDecimal) current.get("carbon_monoxide");
//        nitrogen_dioxide = (BigDecimal) current.get("nitrogen_dioxide");
//        sulphur_dioxide = (BigDecimal) current.get("sulphur_dioxide");
        dust = (BigDecimal) current.get("dust");
        uv_index = (BigDecimal) current.get("uv_index");
//        uv_index = uv_index.add(BigDecimal.valueOf(5.0)); // samo kao test
//        System.out.println("european_aqi : " + european_aqi + "\npm10 : " + pm10 + "\npm2_5 : " + pm2_5 + "\nuv : " + uv_index.get());

        String str = european_aqi_definition();
//        System.out.println("color_index_aqi: " + color_index_aqi);
        european_aqi_text.set(str);
        pm10_text.set(pm10_definition());
        pm2_5_text.set(pm2_5_definition());
        dust_text.set(dust.toString());
        uv_index_text.set("UV: %.1f".formatted(uv_index.doubleValue()));

//        carbon_monoxide_def = carbon_monoxide_definition();
//        nitrogen_dioxide_def = nitrogen_dioxide_definition();
//        sulphur_dioxide_def = sulphur_dioxide_definition();
    }

    //---------------------------------------------------------------
    private String european_aqi_definition() {
        String def = "unknown";
        if(european_aqi.intValue() < 20) {
            def = "Good";
            color_index_aqi = 0;
        }else if(european_aqi.intValue()  < 40) {
            def = "Fair";
            color_index_aqi = 1;
        }else if(european_aqi.intValue()  < 60) {
            def = "Moderate";
            color_index_aqi = 2;
        }else if(european_aqi.intValue()  < 80) {
            def = "Poor";
            color_index_aqi = 3;
        }else if(european_aqi.intValue()  < 100) {
            def = "Very poor";
            color_index_aqi = 4;
        }else {
            def = "Extremely poor";
            color_index_aqi = 5;
        }
        return def;
    }

    private String pm10_definition() {
        String def = "unknown";
        if(pm10.intValue()  < 20) {
            def = "Good";
            color_index_pm10 = 0;
        }else if(pm10.intValue()  < 40) {
            def = "Fair";
            color_index_pm10 = 1;
        }else if(pm10.intValue()  < 50) {
            def = "Moderate";
            color_index_pm10 = 2;
        }else if(pm10.intValue()  < 100) {
            def = "Poor";
            color_index_pm10 = 3;
        }else if(pm10.intValue()  < 150) {
            def = "Very poor";
            color_index_pm10 = 4;
        }else {
            def = "Extremely poor";
            color_index_pm10 = 5;
        }
        return def;
    }

    private String pm2_5_definition() {
        String def = "unknown";
        if(pm2_5.intValue()  < 10) {
            def = "Good";
            color_index_pm2_5 = 0;
        }else if(pm2_5.intValue()  < 20) {
            def = "Fair";
            color_index_pm2_5 = 1;
        }else if(pm2_5.intValue()  < 25) {
            def = "Moderate";
            color_index_pm2_5 = 2;
        }else if(pm2_5.intValue()  < 50) {
            def = "Poor";
            color_index_pm2_5 = 3;
        }else if(pm2_5.intValue()  < 75) {
            def = "Very poor";
            color_index_pm2_5 = 4;
        }else {
            def = "Extremely poor";
            color_index_pm2_5 = 5;
        }
        return def;
    }

    private String carbon_monoxide_definition() {
        String def = "unknown";
// concentration (ppm) = 24.45 x concentration (mg/m3) ÷ molecular weight ( 28.01 g/mol za CO )
        double ppm = 24.45 * ((carbon_monoxide.doubleValue() / 1000) / 28);
        if(ppm < 4) {
            def = "Good";
            color_index_carbon_monoxide = 0;
        }else if(ppm < 9) {
            def = "Fair";
            color_index_carbon_monoxide = 1;
        }else if(ppm < 12) {
            def = "Moderate";
            color_index_carbon_monoxide = 2;
        }else if(ppm < 15) {
            def = "Poor";
            color_index_carbon_monoxide = 3;
        }else if(ppm < 30) {
            def = "Very poor";
            color_index_carbon_monoxide = 4;
        }else {
            def = "Extremely poor";
            color_index_carbon_monoxide = 5;
        }
        return def;
    }

    private String nitrogen_dioxide_definition() {
        String def = "unknown";
        if(nitrogen_dioxide.intValue()  < 40) {
            def = "Good";
            color_index_nitrogen_dioxide = 0;
        }else if(nitrogen_dioxide.intValue()  < 90) {
            def = "Fair";
            color_index_nitrogen_dioxide = 1;
        }else if(nitrogen_dioxide.intValue()  < 120) {
            def = "Moderate";
            color_index_nitrogen_dioxide = 2;
        }else if(nitrogen_dioxide.intValue()  < 230) {
            def = "Poor";
            color_index_nitrogen_dioxide = 3;
        }else if(nitrogen_dioxide.intValue()  < 340) {
            def = "Very poor";
            color_index_nitrogen_dioxide = 4;
        }else {
            def = "Extremely poor";
            color_index_nitrogen_dioxide = 5;
        }
        return def;
    }

    private String sulphur_dioxide_definition() {
        String def = "unknown";
        if(sulphur_dioxide.intValue()  < 100) {
            def = "Good";
            color_index_sulphur_dioxide = 0;
        }else if(sulphur_dioxide.intValue()  < 200) {
            def = "Fair";
            color_index_sulphur_dioxide = 1;
        }else if(sulphur_dioxide.intValue()  < 350) {
            def = "Moderate";
            color_index_sulphur_dioxide = 2;
        }else if(sulphur_dioxide.intValue()  < 500) {
            def = "Poor";
            color_index_sulphur_dioxide = 3;
        }else if(sulphur_dioxide.intValue()  < 750) {
            def = "Very poor";
            color_index_sulphur_dioxide = 4;
        }else {
            def = "Extremely poor";
            color_index_sulphur_dioxide = 5;
        }
        return def;
    }



    public static int get_uv_color(double uv){
        if(uv < 3) {
            return 0;
        }else if(uv < 6) {
            return 1;
        }else if(uv < 8) {
            return 2;
        }else if(uv < 11) {
            return 3;
        }else {
            return 4;
        }
    }

}
