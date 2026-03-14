package dev.java4now.View;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import com.github.cliftonlabs.json_simple.JsonException;
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
    static Text north;
    static Rotate north_rotate;
    static Text south;
    static Rotate south_rotate;
    static Text east;
    static Rotate east_rotate;
    static Text west;
    static Rotate west_rotate;

    public static final DoubleProperty wind_rotation = new SimpleDoubleProperty(0.0);
    public static final DoubleProperty north_angle = new SimpleDoubleProperty(0.0);
    public static final DoubleProperty south_angle = new SimpleDoubleProperty(180.0);
    public static final DoubleProperty east_angle = new SimpleDoubleProperty(90.0);
    public static final DoubleProperty west_angle = new SimpleDoubleProperty(270.0);
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
        root.addRow(2, card4, card5); // bez card 5 ako zelim da card 3 zauzima 2 reda
        root.addRow(3, card6, card7);
        root.addRow(4, card8);
//        root.addRow(2, Submit);
//        root.setHgap(25);
//        root.setVgap(10);
        GridPane.setConstraints(card1, 0, 0, 2, 1, HPos.CENTER, VPos.BASELINE);
//         GridPane.setConstraints(card3, 1, 1, 1, 2, HPos.CENTER, VPos.BASELINE); // ako zelimo da card3 bude u redu 1 a da zauzima 2. kolumnu i 2 reda
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
        Text speed = new Text("speed");
        speed.getStyleClass().add("t5");
        Text wind_spd = new Text();
        wind_spd.getStyleClass().add("t3");
        wind_spd.textProperty().bind(new StringBinding() {
            {
                bind(Forecast_current.wind_speed_text);
            }
            @Override
            protected String computeValue() { // computeValue() se poziva svaki put kada se bind promenljiva promeni
                if (Forecast_current.wind_speed.doubleValue() < 10.0) {
                    wind_spd.setFill(Color.LIMEGREEN);
                    return Forecast_current.wind_speed_text.get();
                } else if ( Forecast_current.wind_speed.doubleValue() < 20.0){
                    wind_spd.setFill(Color.web("#00BCD4"));
                    return Forecast_current.wind_speed_text.get();
                } else if ( Forecast_current.wind_speed.doubleValue() < 30.0){
                    wind_spd.setFill(Color.ORANGE);
                    return Forecast_current.wind_speed_text.get();
                } else if ( Forecast_current.wind_speed.doubleValue() < 45.0){
                    wind_spd.setFill(Color.RED);
                    return Forecast_current.wind_speed_text.get();
                }else {
                    wind_spd.setFill(Color.web("#9e28a3"));
                    return Forecast_current.wind_speed_text.get();
                }
            }
        });
        Text km_h = new Text("km/h");
        km_h.getStyleClass().add("t5");
        Text direction = new Text("direction");
        direction.getStyleClass().add("t5");
        Text wind_dir = new Text();
        wind_dir.getStyleClass().addAll("t4");
        wind_dir.textProperty().bind(Forecast_current.wind_dir_short);
        wind_dir.fillProperty().bind(wind_spd.fillProperty());
        Text wind_dir_deg = new Text();
        wind_dir_deg.getStyleClass().add("t_normal");
        wind_dir_deg.textProperty().bind(Forecast_current.wind_direction_text);
        Text temp_txt = new Text("temp");
        temp_txt.getStyleClass().add("t5");
        Text temp = new Text();
        temp.textProperty().bind(Forecast_current.temp_text);
        Circle circle_out = new Circle();
        Circle circle_inner = new Circle();
        wind_arc = draw_arc();

        setup_compass_direction_name();

        var t3 = new Pane(title, speed,wind_dir, direction,wind_dir_deg, temp_txt, temp, wind_spd,km_h, circle_out, circle_inner,north,east,south,west, wind_arc) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                title.setX(0);
                title.setY(10);
                speed.setX(0);
                speed.setY(title.getY() + title.getFont().getSize() + 10);
                wind_spd.setX(0);
                wind_spd.setY(speed.getY() + speed.getFont().getSize()+15);
                double txt_width = wind_spd.getLayoutBounds().getWidth();
                km_h.setX(txt_width + 10);
                km_h.setY(wind_spd.getY());

                wind_dir.setX(0);
                wind_dir.setY( getHeight() );
                txt_width = wind_dir.getLayoutBounds().getWidth();
                wind_dir_deg.setX(txt_width + 10);
                wind_dir_deg.setY(wind_dir.getY());
                direction.setX(0);
                direction.setY(wind_dir.getY() - wind_dir.getFont().getSize() - 2);

                temp_txt.setX(getWidth() - temp_txt.getLayoutBounds().getWidth() );
                temp_txt.setY(0);
                txt_width = temp.getLayoutBounds().getWidth();
                temp.setX(getWidth() - txt_width);
                temp.setY(temp_txt.getY() + temp_txt.getFont().getSize() + 5);
                circle_out.setRadius((getHeight() / 2) * CIRCLE_SCALE);
                circle_out.setCenterX(getWidth() - circle_out.getRadius());
                circle_out.setCenterY(getHeight() - circle_out.getRadius() );
                circle_inner.setRadius(circle_out.getRadius() - wind_arc.getRadiusY());
                circle_inner.setCenterX(getWidth() - circle_out.getRadius());
                circle_inner.setCenterY(getHeight() - circle_out.getRadius() );

                wind_arc.setCenterX(getWidth() - circle_out.getRadius() );
                wind_arc.setCenterY(getHeight() - circle_out.getRadius()  - circle_inner.getRadius() + wind_arc.getRadiusY());
                rotate.setPivotX(getWidth() - circle_out.getRadius() ); // tacka rotacije po x
                rotate.setPivotY(getHeight() - circle_out.getRadius() ); // tacka rotacije po y

                north.setX(getWidth() - circle_out.getRadius() - north.getLayoutBounds().getWidth() / 2);
                north.setY(getHeight() - circle_out.getRadius()  - circle_inner.getRadius()-2 );
                north_rotate.setPivotX(getWidth() - circle_out.getRadius() );
                north_rotate.setPivotY(getHeight() - circle_out.getRadius() );
                south.setX(getWidth() - circle_out.getRadius() - south.getLayoutBounds().getWidth() / 2);
                south.setY(getHeight() - circle_out.getRadius()  - circle_inner.getRadius()-2 );
                south_rotate.setPivotX(getWidth() - circle_out.getRadius() );
                south_rotate.setPivotY(getHeight() - circle_out.getRadius() );
                east.setX(getWidth() - circle_out.getRadius() - east.getLayoutBounds().getWidth() / 2 );
                east.setY(getHeight() - circle_out.getRadius()  - circle_inner.getRadius()-2);
                east_rotate.setPivotX(getWidth() - circle_out.getRadius() );
                east_rotate.setPivotY(getHeight() - circle_out.getRadius() );
                west.setX(getWidth() - circle_out.getRadius() - west.getLayoutBounds().getWidth() / 2);
                west.setY(getHeight() - circle_out.getRadius()  - circle_inner.getRadius()-2 );
                west_rotate.setPivotX(getWidth() - circle_out.getRadius() );
                west_rotate.setPivotY(getHeight() - circle_out.getRadius() );
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
            circle_out.setStroke(Color.WHITE);
            circle_inner.setStroke( Color.WHITE.darker());
        } else {
            circle_out.setStroke(Color.BLACK);
            circle_inner.setStroke( Color.BLACK.darker());
        }
        circle_inner.setFill(new ImagePattern(bike_img));
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
    private static void setup_compass_direction_name() {
        north = new Text("N");
//        north.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        north.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 9pt; -fx-font-weight: bold;");
        north_rotate = new Rotate();
        //setting properties for the rotate object.
        north_rotate.setPivotX(0);     // tacka rotacije po x
        north_rotate.setPivotY(0);     // tacka rotacije po y
        north.getTransforms().add(north_rotate);

        south = new Text("S");
//        south.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        south.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 9pt; -fx-font-weight: bold;");
        south_rotate = new Rotate();
//        south_rotate.setAngle(0);
        south_rotate.setPivotX(0);     // tacka rotacije po x
        south_rotate.setPivotY(0);     // tacka rotacije po y
        south.getTransforms().add(south_rotate);

        east = new Text("E");
//        east.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        east.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 9pt; -fx-font-weight: bold;");
        east_rotate = new Rotate();
//        east_rotate.setAngle(0);
        east_rotate.setPivotX(0);     // tacka rotacije po x
        east_rotate.setPivotY(0);     // tacka rotacije po y
        east.getTransforms().add(east_rotate);

        west = new Text("W");
//        west.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        west.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 9pt; -fx-font-weight: bold;");
        west_rotate = new Rotate();
//        west_rotate.setAngle(0);
        west_rotate.setPivotX(0);     // tacka rotacije po x
        west_rotate.setPivotY(0);     // tacka rotacije po y
        west.getTransforms().add(west_rotate);

        north_rotate.angleProperty().bind(north_angle.subtract(System_Info.compass));
        south_rotate.angleProperty().bind(south_angle.subtract(System_Info.compass));
        east_rotate.angleProperty().bind(east_angle.subtract(System_Info.compass));
        west_rotate.angleProperty().bind(west_angle.subtract(System_Info.compass));
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
