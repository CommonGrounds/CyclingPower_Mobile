package dev.java4now.View;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import com.github.cliftonlabs.json_simple.JsonException;
import dev.java4now.App;
import dev.java4now.System_Info;
import dev.java4now.util.Forecast_current;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.java4now.App.bike_img;
import static dev.java4now.App.theme;

public class MainPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainPage.class);

    static Card card1;
    static Card card2;
    static Card card3;
    static Card card4;
    static Card card5;
    static Card card6;
    static Card card7;
    static Card card8;

    static Label City_country = new Label(), suburb = new Label(), road_house_number = new Label(), altitude = new Label();

    static GridPane root;
    static Arc wind_arc;
    static Rotate rotate;

    public static final DoubleProperty wind_rotation = new SimpleDoubleProperty(0.0);
    public static final StringProperty total_distance = new SimpleStringProperty("0.0");


    //---------------------------------------------------------
    public static GridPane set(double MaxHeight) throws JsonException, URISyntaxException, IOException {

        root = new GridPane();
//        root.setGridLinesVisible(true);
//        root.getStyleClass().add("grid");
        /*
        root.prefWidthProperty().bind(System_Info.display_width);
        root.prefHeightProperty().bind(System_Info.center_height);
        root.minHeightProperty().bind(System_Info.center_height);
        root.maxHeightProperty().bind(System_Info.center_height);
        */
        createTiles();

        root.addRow(0, card1);
        root.addRow(1, card2, card3);
        root.addRow(2, card4, card5);
        root.addRow(3, card6, card7);
        root.addRow(4, card8);
//        root.addRow(2, Submit);
//        root.setHgap(25);
//        root.setVgap(10);
        GridPane.setConstraints(card1, 0, 0, 2, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(card8, 0, 4, 2, 1, HPos.CENTER, VPos.BASELINE);
//        root.getRowConstraints().get(0).maxHeightProperty().get();
//        root.setBackground(Background.fill(Color.GREEN));
//        root.setPrefSize(System_Info.dimension.getWidth(), MaxHeight);
//        root.setMaxSize(System_Info.dimension.getWidth(), MaxHeight);

        System_Info.speed_timer();
        return root;
    }


    //----------------------------------
    private static void createTiles() throws JsonException, URISyntaxException, IOException {
        double SIZE = 6;

        card1 = new Card();
        var t1_lbl = new Label("Speed");
        var batt_icon = new FontIcon(MaterialDesignB.BATTERY_HIGH);
        batt_icon.getStyleClass().add("batt_icon");
        batt_icon.setRotate(90);
        batt_icon.iconCodeProperty().bind(new ObjectBinding<Ikon>() {
            {
                bind(System_Info.battery_level);
            }

            @Override
            protected Ikon computeValue() {
                var code = MaterialDesignB.BATTERY_HIGH;
                if (System_Info.battery_level.get() < 33) {
                    if (System_Info.battery_plugged.get()) {
                        code = MaterialDesignB.BATTERY_CHARGING_LOW;
                    } else {
                        code = MaterialDesignB.BATTERY_LOW;
                    }
                    batt_icon.setIconColor(Color.RED);
                } else if (System_Info.battery_level.get() < 66) {
                    if (System_Info.battery_plugged.get()) {
                        code = MaterialDesignB.BATTERY_CHARGING_MEDIUM;
                    } else {
                        code = MaterialDesignB.BATTERY_MEDIUM;
                    }
                    batt_icon.setIconColor(Color.ORANGE);
                } else {
                    if (System_Info.battery_plugged.get()) {
                        code = MaterialDesignB.BATTERY_CHARGING_HIGH;
                    } else {
                        code = MaterialDesignB.BATTERY_HIGH;
                    }
                    batt_icon.setIconColor(Color.LAWNGREEN);
                }

                return code;
            }
        });
        var t1_header = new Pane(t1_lbl, batt_icon) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                batt_icon.setLayoutX(getWidth() - batt_icon.getIconSize());
                batt_icon.setLayoutY(15);
                t1_lbl.setLayoutX(0);
            }
        };
        card1.setHeader(t1_header);
//        var t2 = BBCodeParser.createLayout("Speed[center][heading=3]0.0[/heading][/center]");
        var t1 = new Label();
        t1.textProperty().bind(System_Info.speed);
        t1.getStyleClass().add("t1");
//        t2.getStyleClass().add(Styles.LARGE);
        var t1_box = new HBox(t1);
        t1_box.setAlignment(Pos.CENTER);
        card1.setBody(t1_box);
        var txt1 = new Label("Avg: ");
        var avg = new Label();
        avg.getStyleClass().add("t3");
        avg.textProperty().bind(System_Info.speed_average);
        var legend1 = new Label("km/h");
        var t1_footer = new Pane(txt1, avg, legend1) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                legend1.setLayoutX(getWidth() - legend1.getWidth());
                txt1.setLayoutX(0);
                avg.setLayoutX(txt1.getWidth() + 10);
                avg.setLayoutY(txt1.getLayoutY() - avg.getHeight() / 3);
            }
        };
        card1.setFooter(t1_footer);
        card1.prefWidthProperty().bind(System_Info.display_width);
        card1.prefHeightProperty().bind(System_Info.center_height.divide(SIZE / 2)); // 1/3


        card2 = new Card();
//        tile2.setBorder(new Border(new BorderStroke(Color.RED,
//                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        card2.setHeader(new Label("Altitude"));
//        var t2 = BBCodeParser.createLayout("Speed[center][heading=3]0.0[/heading][/center]");
        var t2 = new Label();
        t2.textProperty().bind(System_Info.alt);
        t2.getStyleClass().add("t2");
        t2.getStyleClass().add(Styles.TEXT_BOLD);
        var t2_box = new HBox(t2);
        t2_box.setAlignment(Pos.CENTER);
        card2.setBody(t2_box);
        var t2_footer = new HBox(new Label("m"));
        t2_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card2.setFooter(t2_footer);
        card2.prefWidthProperty().bind(System_Info.display_width_half);
        card2.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card3 = new Card();
        double CIRCLE_SCALE = 0.75;           // Smanjujem Pane tj. circle
        Text title = new Text("Wind");
        Text wind_spd = new Text();
        wind_spd.textProperty().bind(Forecast_current.wind_speed_text);
        Text heading_dir = new Text();
        heading_dir.textProperty().bind(System_Info.heading_dir_short);
        Text wind_dir = new Text();
        wind_dir.textProperty().bind(Forecast_current.wind_dir_short);
        Text wind_dir_deg = new Text();
        wind_dir_deg.textProperty().bind(Forecast_current.wind_direction_text);
        Text temp = new Text();
        temp.textProperty().bind(Forecast_current.temp_text);
        Circle circle = new Circle();
        wind_arc = draw_arc();
        var t3 = new Pane(title, wind_dir, wind_dir_deg, temp, wind_spd, heading_dir, circle, wind_arc) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                title.setX(0);
                title.setY(10);
                double txt_width = title.getLayoutBounds().getWidth();
                wind_dir.setX(txt_width / 2 - wind_dir.getLayoutBounds().getWidth() / 2);
                wind_dir.setY(title.getFont().getSize() + 10 + 10);                         // 10px od title
                wind_dir_deg.setX(txt_width / 2 - wind_dir_deg.getLayoutBounds().getWidth() / 2);
                wind_dir_deg.setY(wind_dir.getY() + wind_dir.getFont().getSize() + 2);      // 2px od wind_dir
                txt_width = temp.getLayoutBounds().getWidth();
                temp.setX(getWidth() - txt_width);
                temp.setY(10);
                txt_width = heading_dir.getLayoutBounds().getWidth();
                heading_dir.setX(getWidth() / 2 - txt_width / 2);
                heading_dir.setY(3);
                txt_width = wind_spd.getLayoutBounds().getWidth();
                wind_spd.setX(getWidth() - txt_width);
                wind_spd.setY(getHeight());
                circle.setRadius((getHeight() / 2) * CIRCLE_SCALE);
                circle.setCenterX(getWidth() / 2);
                circle.setCenterY(getHeight() / 2);
                wind_arc.setCenterX(getWidth() / 2);
                wind_arc.setCenterY(getHeight() / 2 - circle.getRadius() + wind_arc.getRadiusY());
                rotate.setPivotX(getWidth() / 2); // tacka rotacije po x
                rotate.setPivotY(getHeight() / 2); // tacka rotacije po y
//                rotate.setAngle(270);
//                circle.setTranslateX(getWidth()/2 - circle.getRadius()/2);
//                circle.setTranslateY(getHeight()/2 - circle.getRadius()/2);
            }
        };
//        circle.radiusProperty().bind(t3.heightProperty().divide(2));
//        circle.centerXProperty().bind(t3.widthProperty().divide(2));
//        circle.centerXProperty().bind(t3.heightProperty().divide(2));
//        circle.translateXProperty().bind(t3.layoutXProperty());
//        circle.translateYProperty().bind(t3.layoutYProperty());
        if (theme.isDarkMode()) {
            circle.setStroke(Color.WHITE);
        } else {
            circle.setStroke(Color.BLACK);
        }
        circle.setFill(new ImagePattern(bike_img));
        rotate.angleProperty().bind(wind_rotation.subtract(System_Info.compass));
//        circle.setRotate(-90);
        card3.setBody(t3);
        card3.prefWidthProperty().bind(System_Info.display_width_half);
        card3.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card4 = new Card();
        card4.setHeader(new Label("Slope"));
        var t4 = new Label();
        t4.textProperty().bind(System_Info.slope);
        t4.getStyleClass().add("t2");
        t4.getStyleClass().add(Styles.TEXT_BOLD);
        var t4_box = new HBox(t4);
        t4_box.setAlignment(Pos.CENTER);
        card4.setBody(t4_box);
        var t4_footer = new HBox(new Label("%"));
        t4_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card4.setFooter(t4_footer);
        card4.prefWidthProperty().bind(System_Info.display_width_half);
        card4.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card5 = new Card();
        card5.setHeader(new Label("Distance"));
//        var t2 = BBCodeParser.createLayout("Speed[center][heading=3]0.0[/heading][/center]");
        var t5 = new Label();
        t5.textProperty().bind(total_distance);
        t5.getStyleClass().add("t2");
        t5.getStyleClass().add(Styles.TEXT_BOLD);
        var t5_box = new HBox(t5);
        t5_box.setAlignment(Pos.CENTER);
        card5.setBody(t5_box);
        var t5_footer = new HBox(new Label("km"));
        t5_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card5.setFooter(t5_footer);
        card5.prefWidthProperty().bind(System_Info.display_width_half);
        card5.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card6 = new Card();
        card6.setHeader(new Label("Alt Gain"));
        var t6 = new Label();
        t6.textProperty().bind(System_Info.alt_gain_up);
        t6.getStyleClass().add("t2");
        t6.getStyleClass().add(Styles.TEXT_BOLD);
        var t6_box = new HBox(t6);
        t6_box.setAlignment(Pos.CENTER);
        card6.setBody(t6_box);
        var t6_footer = new HBox(new Label("m"));
        t6_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card6.setFooter(t6_footer);
        card6.prefWidthProperty().bind(System_Info.display_width_half);
        card6.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card7 = new Card();
//        var t7 = BBCodeParser.createLayout("Power[center][heading=3]0.0[/heading][/center]");
        card7.setHeader(new Label("Power"));
        var t7 = new Label();
        t7.textProperty().bind(System_Info.power);
        t7.getStyleClass().add("t2");
        t7.getStyleClass().add(Styles.TEXT_BOLD);
        var t7_box = new HBox(t7);
        t7_box.setAlignment(Pos.CENTER);
        card7.setBody(t7_box);
        var legend = new Label("Watts");
        var avg_lbl2 = new Label();
        avg_lbl2.textProperty().bind(System_Info.power_avg);
        avg_lbl2.getStyleClass().add("t4");
        var avg_lbl = new Label();
        avg_lbl.textProperty().bind(new StringBinding() {
            {
                bind(System_Info.power_avg);
            }

            @Override
            protected String computeValue() { // computeValue() se poziva svaki put kada se bind promenljiva promeni
                if (System_Info.power_avg_up.get()) {
                    avg_lbl.setStyle("-fx-text-fill: green;");
                    return "+Avg: ";
                } else {
                    avg_lbl.setStyle("-fx-text-fill: red;");
                    return "-Avg: ";
                }
            }
        });
        var t7_footer = new Pane(avg_lbl, avg_lbl2, legend) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                legend.setLayoutX(getWidth() - legend.getWidth());
                avg_lbl.setLayoutX(0);
                avg_lbl2.setLayoutX(avg_lbl.getWidth() + 5);
                avg_lbl2.setLayoutY(avg_lbl.getLayoutY() - avg_lbl2.getHeight() / 4);
            }
        };
        card7.setFooter(t7_footer);
        card7.prefWidthProperty().bind(System_Info.display_width_half);
        card7.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card8 = new Card();
        var gps_icon = new FontIcon(Material2AL.GPS_OFF);
        System_Info.gps_exist.addListener((observable, oldValue, newValue) -> {
            // Only if completed
            if (newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        gps_icon.setIconCode(System_Info.gps_exist.get() ? Material2AL.GPS_FIXED : Material2AL.GPS_NOT_FIXED);
//                        System_Info.gps_exist.set(false);
                    }
                });
            }
        });
        var lbl = new Label("GPS Data", gps_icon);
        card8.setHeader(lbl);
        var tp_content = new VBox(City_country, suburb, road_house_number);
        tp_content.setAlignment(Pos.CENTER);
        card8.setBody(tp_content);
        card8.prefWidthProperty().bind(System_Info.display_width);
        card8.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        LOGGER.debug("Net available: " + System_Info.net_exist.get());
        City_country.textProperty().bind(System_Info.city_country_text);
        suburb.textProperty().bind(System_Info.suburb_text);
        road_house_number.textProperty().bind(System_Info.road_house_number_text);
//        altitude.textProperty().bind(System_Info.alt);
    }


    //---------------------------------------------------
    private static Arc draw_arc() {
        Arc arc = new Arc();
        arc.setCenterX(0);
        arc.setCenterY(0);
        arc.setRadiusX(16);
        arc.setRadiusY(16);
        arc.setStartAngle(65);    // pocetni ugao desno
        arc.setLength(50);        // od pocetnog ugla
        arc.setType(ArcType.ROUND);
        arc.setFill(Color.RED);

        rotate = new Rotate();

        //setting properties for the rotate object.
        rotate.setAngle(0);
        rotate.setPivotX(0);     // tacka rotacije po x
        rotate.setPivotY(0);     // tacka rotacije po y
        arc.getTransforms().add(rotate);

        return arc;
    }
}
