package dev.java4now.View;

import atlantafx.base.util.Animations;
import com.github.cliftonlabs.json_simple.JsonException;
import dev.java4now.System_Info;
import dev.java4now.util.AQI_Index;
import dev.java4now.util.CadenceMeterReader;
import dev.java4now.util.Forecast_current;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.java4now.App.*;

public class Board extends StackPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(Board.class);

    static AboutPage about_page;
    static GridPane main_page;
    static GridPane second_page;
    static ImageView imageView2;
    static Forecast_current forecast = new Forecast_current();
    static CadenceMeterReader cadence = new CadenceMeterReader();
    static AQI_Index aqi_index = new AQI_Index();
    static Timeline cadence_started;
    static int scan_counter = 0;


    public Board() throws JsonException, URISyntaxException, IOException {

        System_Info.forecast_flag.addListener((obs, ov, nv) -> {
            if (nv) {
                //  in javaFX only the FX thread can modify the UI elements
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String Meteo_url = "https://api.open-meteo.com/v1/forecast?latitude=" + System_Info.lat + "&longitude=" + System_Info.lon + "&current=temperature_2m,is_day,wind_speed_10m,wind_direction_10m,"
                                + "relative_humidity_2m,weather_code,surface_pressure&hourly=temperature_2m,rain,snowfall&daily=weather_code,wind_speed_10m_max,"
                                + "wind_direction_10m_dominant&timezone=auto";
                        String AQI_url = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=" + System_Info.lat + "&longitude=" + System_Info.lon + "&current=european_aqi,pm10,pm2_5,carbon_monoxide,"
                                + "nitrogen_dioxide,sulphur_dioxide,dust,uv_index&hourly=uv_index&timezone=auto&forecast_days=7";

                        try {
                            forecast.parseData(Meteo_url);
                            aqi_index.parseData(AQI_url);
                            // IMPORTANT - Samo da bi inicirao promenu style-a za label text ( mora biti drugacija od inicijalne ) da bi se aktivirao styleProperty() change
                            SecondPage.style_text.set("-fx-text-fill:-color-fg-default;");
                            SecondPage.style_text_left.set("-fx-text-fill:-color-fg-default;");   // Samo da bi inicirao promenu style-a za label text
                        } catch (JsonException | URISyntaxException | IOException e) {
                            throw new RuntimeException(e);
                        }
                        System_Info.forecast_flag.set(false);
                    }
                });
            }
        });

        main_page = MainPage.set(System_Info.dimension.getHeight() - app_top_bar.getHeight());
        main_page.setAlignment(Pos.BASELINE_CENTER);

        second_page = SecondPage.set();
        second_page.setAlignment(Pos.BASELINE_CENTER);

        about_page = new AboutPage(main_page);
        getChildren().addAll(main_page,second_page, about_page);
        main_page.toFront();
//        main_page.setVisible(false);
        about_page.setVisible(false);   // samo zbog provere za swipe - inace ne treba
//        second_page.toFront();
        second_page.setVisible(false);
        addSwipeHandlers();
//        SecondPage.preventMapHandlers();
//        SecondPage.preventMapHandlers_2();
//        SecondPage.preventMapHandlers_3();

        // Periodična provera da li je cadence sensor startovan
        cadence_started = new Timeline(new KeyFrame(Duration.seconds(60), evt -> {
            LOGGER.info("Cadence: Cadence started flag: " + CadenceMeterReader.CADENCE_STARTED);
            if (!CadenceMeterReader.CADENCE_STARTED) {
                scan_counter++;
                if (scan_counter > 10) {
                    LOGGER.warn("Cadence: No cadence sensor found after 10 scans, stopping scan");
                    CadenceMeterReader.CADENCE_STARTED = true;
                    stop_find_sensor_timer();
                    return;
                }
                cadence.start();
            }else{
                stop_find_sensor_timer();
            }
        }));
        cadence_started.setCycleCount(Animation.INDEFINITE);
        cadence_started.play();

        cadence.start(); // initial start
    }



    //--------------------------------------------------
    public static void stop_find_sensor_timer(){
        cadence_started.stop();
    }



    //------------------------------------------------
    public static void showAbout(){
        about_page.toFront();
        about_page.setVisible(true); // samo zbog provere za swipe - inace ne treba
        var in = Animations.slideInRight(about_page, Duration.seconds(.5));
        in.setOnFinished(f -> {
//                stackPane.getChildren().remove(page1);
//                stackPane.getChildren().add(page2);
//                page1.setVisible(false);
        });
        var out = Animations.slideOutLeft(main_page, Duration.seconds(.5));
        out.setOnFinished(f -> {
            main_page.setVisible(false);
            if(about_page.timer != null && !about_page.timer_started){
                about_page.timer.start();
            }
        });

        in.playFromStart();
        out.playFromStart();
    }




    //------------------------------------
    private void addSwipeHandlers(){
        setOnSwipeLeft(evt -> {
            evt.consume();
            LOGGER.debug("SwipeLeft");
            if(!second_page.isVisible()){
                second_page.setVisible(true);
                second_page.toFront();
                var in = Animations.slideInRight(second_page, Duration.seconds(.4));
                in.setOnFinished(f -> {});
                var out = Animations.slideOutLeft(main_page, Duration.seconds(0.4));
                out.setOnFinished(f -> main_page.setVisible(false));

                in.playFromStart();
                out.playFromStart();
            }
        });
        setOnSwipeRight(evt -> {
            evt.consume();
            LOGGER.debug("SwipeRight");
            if(!main_page.isVisible()){
                main_page.setVisible(true);
                main_page.toFront();
                var in = Animations.slideInLeft(main_page, Duration.seconds(0.4));
                in.setOnFinished(f -> {});
                var out = Animations.slideOutRight(second_page, Duration.seconds(0.4));
                out.setOnFinished(f -> second_page.setVisible(false));

                in.playFromStart();
                out.playFromStart();
            }
        });
    }
}
