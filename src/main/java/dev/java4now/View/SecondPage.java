package dev.java4now.View;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import atlantafx.base.util.BBCodeParser;
import com.gluonhq.impl.maps.BaseMap;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import dev.java4now.App;
//import dev.java4now.Maps.CustomTileLayer;
import dev.java4now.Maps.PoiLayer;
import dev.java4now.System_Info;
import dev.java4now.util.AQI_Index;
import dev.java4now.util.CadenceMeterReader;
import dev.java4now.util.Forecast_current;
import dev.java4now.util.SpeedCalculator;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;

import static dev.java4now.App.theme;

public class SecondPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecondPage.class);

    static GridPane root;
    static Card card1;
    static Card card2;
    static Card card3;
    static Card card4;
    static Card card5;
    static Card card6;

    static FontIcon ble_batt_icon;

    public static MapPoint[] mapPoint;
    static MapView view;
    static PoiLayer answer;
    private static final int DEFAULT_ZOOM = 1;
    private static final int MAX_ZOOM = 19;
    private static final double DEFAULT_LAT = 44.80510316891895;
    private static final double DEFAULT_LONG = 20.470690246079375;

    static public final IntegerProperty actual_zoom = new SimpleIntegerProperty(1);
    public static final StringProperty total_time = new SimpleStringProperty("00:00:00");
    public static final StringProperty style_text = new SimpleStringProperty("---");  // InitialValue mora biti zamenjena da bi se aktivirao styleProperty() change
    public static final StringProperty style_text_left = new SimpleStringProperty("---"); // ~

    public static ImageView weather_icon;

    static {
        weather_icon = new ImageView(new Image(
                Objects.requireNonNull(App.class.getResource("mm_api_symbols/wsymbol_0999_unknown.png")).toExternalForm()));
    }

    public static GridPane set() {

        mapPoint = new MapPoint[1];
        view = new MapView();
        // ------- custom Tile_provider ---------
//        CustomTileLayer customLayer = new CustomTileLayer(view);
//        view.addLayer(customLayer);
        //--------------------------------------
        answer = new PoiLayer();

        System_Info.location_flag.addListener((obs, ov, nv) -> {
            if (nv) {
                //  in javaFX only the FX thread can modify the UI elements
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        mapPoint[0] = new MapPoint(System_Info.lat, System_Info.lon);
                        answer.addPoint(mapPoint[0], new Circle(3.5, Color.RED));
//                        answer.resizeRelocate(0,0,System_Info.display_width.get(),System_Info.center_height.divide(2).get());
//                        answer.localToScene(card1.getLayoutBounds());
                        view.setCenter(mapPoint[0]);
                        System_Info.location_flag.set(false);
                    }
                });
            }
        });


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
        root.addRow(3, card6);
//        root.addRow(2, Submit);
//        root.setHgap(25);
//        root.setVgap(10);
        GridPane.setConstraints(card1, 0, 0, 2, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(card6, 0, 3, 2, 1, HPos.CENTER, VPos.BASELINE);
//        root.getRowConstraints().get(0).maxHeightProperty().get();
//        root.setBackground(Background.fill(Color.GREEN));
//        root.setPrefSize(System_Info.dimension.getWidth(), MaxHeight);
//        root.setMaxSize(System_Info.dimension.getWidth(), MaxHeight);
        view.setZoom(DEFAULT_ZOOM);
        return root;
    }


    //----------------------------------
    private static void createTiles() {
        double SIZE = 6;

        card1 = new Card();
//        view.setMinSize(System_Info.display_width.get(),System_Info.center_height.divide(SIZE / 3).get());
//        view.setMaxSize(System_Info.display_width.get(),System_Info.center_height.divide(SIZE / 3).get());
        view.maxHeightProperty().bind(card1.maxHeightProperty());
        mapPoint[0] = new MapPoint(DEFAULT_LAT, DEFAULT_LONG);
        answer.addPoint(mapPoint[0], new Circle(1, Color.TRANSPARENT));
        view.addLayer(answer);
//        view.setZoom(DEFAULT_ZOOM);
        final Group copyright = createCopyright();
        var plus = new Button(null, new FontIcon(Material2OutlinedMZ.PLUS));
        plus.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.DANGER, "zoom");
        plus.setOnAction(evt -> {
//            System.out.println(actual_zoom);
            if (actual_zoom.get() >= MAX_ZOOM) {
                return;
            }
            actual_zoom.set(actual_zoom.get() + 1);
//            zoom(actual_zoom.get());           // Reflection (Less Recommended)
            view.setZoom(actual_zoom.get());
            answer.addPoint(mapPoint[0], new Circle(1, Color.TRANSPARENT));
            view.setCenter(mapPoint[0]);
        });
        var minus = new Button(null, new FontIcon(Material2OutlinedMZ.MINUS));
        minus.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.DANGER, "zoom");
        minus.setOnAction(evt -> {
            if (actual_zoom.get() <= 1) {
                return;
            }
            actual_zoom.set(actual_zoom.get() - 1);
//            zoom(actual_zoom.get());        // Reflection (Less Recommended)
            view.setZoom(actual_zoom.get());
            answer.addPoint(mapPoint[0], new Circle(1, Color.TRANSPARENT));
            view.setCenter(mapPoint[0]);
        });
        var zoom_lbl = new Label();
        if (theme.isDarkMode()) {
            zoom_lbl.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65) !important;");
        } else {
            zoom_lbl.setStyle("-fx-background-color: rgba(255, 255, 255, 0.65) !important;");
        }
        zoom_lbl.textProperty().bind(new StringBinding() {
            {
                bind(actual_zoom);
            }

            @Override
            protected String computeValue() { // computeValue() se poziva svaki put kada se bind promenljiva promeni
                return "Zoom: " + actual_zoom.get();
            }
        });
        Region blocker = new Region();  // blocker za event-e - Region ( preko MapView ) je po defaultu ne interaktivan ( PickOnBounds(false) )
        blocker.setStyle("-fx-background-color: transparent;"); // Make it transparent
        var t1 = new StackPane(view, blocker, copyright, plus, minus, zoom_lbl) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                copyright.setLayoutX(getWidth() - copyright.prefWidth(-1));
                copyright.setLayoutY(getHeight() - copyright.prefHeight(-1));
                minus.setLayoutX(getWidth() - minus.prefWidth(-1) - 5);
                minus.setLayoutY(getHeight() - copyright.getLayoutBounds().getHeight() - minus.prefHeight(-1) - 5);
                plus.setLayoutX(getWidth() - plus.prefWidth(-1) - 5);
                plus.setLayoutY(getHeight() - copyright.getLayoutBounds().getHeight() - minus.prefHeight(-1) - plus.prefHeight(-1) - 15);
                zoom_lbl.setLayoutX(0);
                zoom_lbl.setLayoutY(0);
            }
        };
        card1.setBody(t1);
        // Make the blocker fill the StackPane:
        StackPane.layoutInArea(blocker, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE,
                0, null, true, true, HPos.CENTER, VPos.CENTER, true);
        card1.prefWidthProperty().bind(System_Info.display_width);
        card1.maxHeightProperty().bind(System_Info.center_height.divide(SIZE / 3)); // 1/2
        card1.minHeightProperty().bind(System_Info.center_height.divide(SIZE / 3)); // 1/2
//        view.clipProperty().bind(card1.clipProperty());
//        view.setOnMouseMoved(evt -> {
//            System.out.println(view.getLayoutBounds());
//        });
        view.flyTo(1., mapPoint[0], 2.);


        card2 = new Card();
        card2.setHeader(new Label("Pace"));
//        var t2 = BBCodeParser.createLayout("Speed[center][heading=3]0.0[/heading][/center]");
        var t2 = new Label();
        t2.textProperty().bind(SpeedCalculator.pace);  // TODO
        t2.getStyleClass().add("t3");
        t2.getStyleClass().add(Styles.TEXT_BOLD);
        var t2_box = new HBox(t2);
        t2_box.setAlignment(Pos.CENTER);
        card2.setBody(t2_box);
        var t2_footer = new HBox(new Label("per km"));
        t2_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card2.setFooter(t2_footer);
        card2.prefWidthProperty().bind(System_Info.display_width_half);
        card2.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card3 = new Card();
//        var t3 = BBCodeParser.createLayout("Time[center][heading=3]0.0[/heading][/center]");
        card3.setHeader(new Label("Time"));
        var t3 = new Label();
        t3.textProperty().bind(total_time);
        t3.getStyleClass().add("t3");
        t3.getStyleClass().add(Styles.TEXT_BOLD);
        var t3_box = new HBox(t3);
        t3_box.setAlignment(Pos.CENTER);
        card3.setBody(t3_box);
        var t3_footer = new HBox(new Label("h:m:s"));
        t3_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card3.setFooter(t3_footer);
        card3.prefWidthProperty().bind(System_Info.display_width_half);
        card3.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card4 = new Card();
        card4.setHeader(new Label("Calories"));
        var t4 = new Label();
        t4.textProperty().bind(System_Info.calories); // TODO
        t4.getStyleClass().add("t2");
        t4.getStyleClass().add(Styles.TEXT_BOLD);
        var t4_box = new HBox(t4);
        t4_box.setAlignment(Pos.CENTER);
        card4.setBody(t4_box);
        var t4_footer = new HBox(new Label("kcal"));
        t4_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card4.setFooter(t4_footer);
        card4.prefWidthProperty().bind(System_Info.display_width_half);
        card4.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card5 = new Card();
        var t5_lbl = new Label("Cadence");
        ble_batt_icon = new FontIcon(MaterialDesignB.BATTERY_BLUETOOTH);
        ble_batt_icon.getStyleClass().add("ble_icon");
        ble_batt_icon.iconCodeProperty().bind(new ObjectBinding<Ikon>() {
            {
                bind(CadenceMeterReader.icon_code);
            }

            @Override
            protected Ikon computeValue() {
                Ikon iconCode;
                String styleClass;

                if (CadenceMeterReader.icon_code.get() <= 10) {
                    iconCode = MaterialDesignB.BATTERY_ALERT_BLUETOOTH;
                    styleClass = "battery-critical";
                } else if (CadenceMeterReader.icon_code.get() < 15) {
                    iconCode = MaterialDesignB.BATTERY_10_BLUETOOTH;
                    styleClass = "battery-low";
                } else if (CadenceMeterReader.icon_code.get() < 25) {
                    iconCode = MaterialDesignB.BATTERY_20_BLUETOOTH;
                    styleClass = "battery-low";
                } else if (CadenceMeterReader.icon_code.get() < 35) {
                    iconCode = MaterialDesignB.BATTERY_30_BLUETOOTH;
                    styleClass = "battery-medium";
                } else if (CadenceMeterReader.icon_code.get() < 45) {
                    iconCode = MaterialDesignB.BATTERY_40_BLUETOOTH;
                    styleClass = "battery-medium";
                } else if (CadenceMeterReader.icon_code.get() < 55) {
                    iconCode = MaterialDesignB.BATTERY_50_BLUETOOTH;
                    styleClass = "battery-medium";
                } else if (CadenceMeterReader.icon_code.get() < 65) {
                    iconCode = MaterialDesignB.BATTERY_60_BLUETOOTH;
                    styleClass = "battery-good";
                } else if (CadenceMeterReader.icon_code.get() < 75) {
                    iconCode = MaterialDesignB.BATTERY_70_BLUETOOTH;
                    styleClass = "battery-good";
                } else if (CadenceMeterReader.icon_code.get() < 85) {
                    iconCode = MaterialDesignB.BATTERY_80_BLUETOOTH;
                    styleClass = "battery-good";
                } else if (CadenceMeterReader.icon_code.get() < 95) {
                    iconCode = MaterialDesignB.BATTERY_90_BLUETOOTH;
                    styleClass = "battery-good";
                } else if (CadenceMeterReader.icon_code.get() <= 100) {
                    iconCode = MaterialDesignB.BATTERY_BLUETOOTH;
                    styleClass = "battery-full";
                } else {
                    iconCode = MaterialDesignB.BATTERY_BLUETOOTH;
                    styleClass = theme.isDarkMode() ? "battery-white" : "battery-black";   // 100% and unknown
                }

                // Remove existing battery style classes
                ble_batt_icon.getStyleClass().removeIf(style ->
                        style.startsWith("battery-"));

                // Set icon and style
                ble_batt_icon.getStyleClass().add(styleClass);
                return iconCode;
            }
        });
        var t5_header = new Pane(t5_lbl, ble_batt_icon) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                ble_batt_icon.setLayoutX(getWidth() - ble_batt_icon.getIconSize());
                ble_batt_icon.setLayoutY(10);
                t5_lbl.setLayoutX(0);
            }
        };
        card5.setHeader(t5_header);
        var t5 = new Label();
        t5.textProperty().bind(CadenceMeterReader.cadence_data);
        t5.getStyleClass().add("t2");
        t5.getStyleClass().add(Styles.TEXT_BOLD);
        var t5_box = new HBox(t5);
        t5_box.setAlignment(Pos.CENTER);
        card5.setBody(t5_box);
        var txt1 = new Label("Avg: ");
        var avg = new Label();
        avg.getStyleClass().add("t4");
        avg.textProperty().bind(CadenceMeterReader.cadence_data_avg);
        var legend1 = new Label("rpm");
        var t5_footer = new Pane(txt1, avg, legend1) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                legend1.setLayoutX(getWidth() - legend1.getWidth());
                txt1.setLayoutX(0);
                avg.setLayoutX(txt1.getWidth() + 5);
                avg.setLayoutY(txt1.getLayoutY() - avg.getHeight() / 4);
            }
        };
        card5.setFooter(t5_footer);
        card5.prefWidthProperty().bind(System_Info.display_width_half);
        card5.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));

        card6 = new Card();
        var t6 = new Label("Air quality");
        weather_icon.setFitHeight(48);
        weather_icon.setPreserveRatio(true);
        var t6_header = new Pane(t6, weather_icon) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                weather_icon.setX(getWidth() - weather_icon.getFitWidth() - 48);
                weather_icon.setY(0);
                t6.setLayoutX(0);
                t6.setLayoutY(10);
            }
        };
        card6.setHeader(t6_header);
        var eaqi = new Label();
        eaqi.textProperty().bind(AQI_Index.european_aqi_text);
        eaqi.styleProperty().bind(new StringBinding() {  // Moram da iniciram posle ucitavanja svakog AQI_Index-a u Board klasi
            {
                bind(style_text);
            }

            @Override
            protected String computeValue() { // computeValue() se poziva svaki put kada se bind promenljiva ( style_text ) promeni
                return "-fx-text-fill:" + AQI_Index.color_definition[Board.aqi_index.color_index_aqi] + ";";
            }
        });
        eaqi.getStyleClass().add("t3");
        eaqi.getStyleClass().add(Styles.TEXT_BOLD);
        var t6_box_left = new VBox(eaqi);
        t6_box_left.setAlignment(Pos.CENTER);
        card6.setBody(t6_box_left);
        var t6_footer_right_lbl = new Label();
        t6_footer_right_lbl.textProperty().bind(Forecast_current.weather_code_text);
        var t6_footer_left_lbl = new Label();
        t6_footer_left_lbl.textProperty().bind(AQI_Index.uv_index_text);
        t6_footer_left_lbl.styleProperty().bind(new StringBinding() {
            {
                bind(style_text_left);
            }

            @Override
            protected String computeValue() { // computeValue() se poziva svaki put kada se bind promenljiva ( style_text_left ) promeni
//                System.out.println("style_text_left: " + AQI_Index.uv_color_definition[AQI_Index.get_uv_color(AQI_Index.uv_index.doubleValue())]);
                return "-fx-text-fill:" + AQI_Index.uv_color_definition[AQI_Index.get_uv_color(AQI_Index.uv_index.doubleValue())] + ";";
            }
        });
        var t6_footer = new HBox(t6_footer_left_lbl, t6_footer_right_lbl) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                t6_footer_left_lbl.setLayoutX(getWidth() / 2 - t6_footer_left_lbl.getWidth() / 2);
                if(t6_footer_right_lbl.getText().length() < 8 ){
                    t6_footer_right_lbl.setLayoutX(getWidth() - 24 - t6_footer_right_lbl.getWidth()/2); // 48 / 2
                }else{
                    t6_footer_right_lbl.setLayoutX(getWidth() - t6_footer_right_lbl.getWidth());
                }
            }
        };
//        t6_footer.setAlignment(Pos.BOTTOM_RIGHT);
        card6.setFooter(t6_footer);
        card6.prefWidthProperty().bind(System_Info.display_width);
        card6.prefHeightProperty().bind(System_Info.center_height.divide(SIZE));
    }


    //---------------------------------------------
    private static Group createCopyright() {
        final Label copyright = new Label(
                "Map data © OpenStreetMap contributors, CC-BY-SA."
        );
        copyright.getStyleClass().add("copyright");
        copyright.setAlignment(Pos.CENTER);
        copyright.setMaxWidth(Double.MAX_VALUE);
        return new Group(copyright);
    }


    //------------------------------------
    public static void preventMapHandlers() {
        view.addEventFilter(SwipeEvent.ANY, event -> {
            // Consume the event to prevent the MapView from handling it
            event.consume();
            LOGGER.debug("prevent: swipe MapView()");

            Parent parent = view.getParent();
            if (parent != null) {
                // Create a copy of the event with the parent as the source and target
                SwipeEvent copiedEvent = event.copyFor(parent, parent);
                // Fire the copied event on the parent
                parent.fireEvent(copiedEvent);
            }
        });

        view.addEventFilter(TouchEvent.ANY, event -> {
            // Consume the event to prevent the MapView from handling it
            event.consume();
            LOGGER.debug("prevent: touch MapView()");

            Parent parent = view.getParent();
            if (parent != null) {
                // Create a copy of the event with the parent as the source and target
                TouchEvent copiedEvent = event.copyFor(parent, parent);
                // Fire the copied event on the parent
                parent.fireEvent(copiedEvent);
            }
        });


        // Ensure the parent handles the swipe event
        Parent parentContainer = view.getParent();
        parentContainer.addEventHandler(SwipeEvent.ANY, event -> {
            if (event.getEventType() == SwipeEvent.SWIPE_LEFT) {
                LOGGER.debug("prevent: Swiped left on parent!");
            } else if (event.getEventType() == SwipeEvent.SWIPE_RIGHT) {
                LOGGER.debug("prevent: Swiped right on parent!");
            }
        });
/*
        view.addEventFilter(SwipeEvent.ANY, event -> {
            // Consume the event to prevent the MapView from handling it
            event.consume();
            LOGGER.debug("prevent: swipe Map()");
        });

        view.addEventFilter(TouchEvent.ANY,event -> {
            // Consume the event to prevent the MapView from handling it
            event.consume();
            LOGGER.debug("prevent: touch Map()");
        });
 */
    }


    //--------------------------------------------
    public static void preventMapHandlers_2() {
        // pickOnBounds = false: Simplest, but completely disables all interaction. Use this if you want the map to be entirely non-interactive. - Najbolji simple nacin
        view.setPickOnBounds(false);
    }


    //--------------------------------------------
    public static void preventMapHandlers_3() {
        // Consuming Events: Good if you want some visual feedback but no actual map changes. More flexible than pickOnBounds. - NE RADI Za MapView
        view.setOnTouchPressed(event -> event.consume());
        view.setOnTouchMoved(event -> event.consume());
    }


    //----------------------------------------------
    /*
    You could use reflection to access the BaseMap field within the MapView.  This is generally discouraged because it relies on internal implementation details
    and might break with future versions of the library.  However, if other options fail, it's a possibility:
     */
    private static void zoom(int zoom_level) {
        try {
            Field baseMapField = MapView.class.getDeclaredField("baseMap"); // "baseMap" is the likely field name, but it might change
            baseMapField.setAccessible(true);
            BaseMap baseMap = (BaseMap) baseMapField.get(view);
            if (baseMap != null) {
                baseMap.setZoom(zoom_level);
                double actualZoom = baseMap.getZoom();
                LOGGER.debug("Actual Zoom: " + actualZoom);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }
}
