package dev.java4now;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Tile;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Theme;
import atlantafx.base.controls.ToggleSwitch;
import com.github.cliftonlabs.json_simple.JsonException;
import dev.java4now.AudioUtils.Sound;
import dev.java4now.View.*;
import dev.java4now.local_json.CityService_json;
import dev.java4now.util.CadenceMeterReader;
import dev.java4now.util.ImageUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.java4now.AudioUtils.Sound.*;
import static dev.java4now.System_Info.*;
import static dev.java4now.http.PingService.startPing;

/*
This application uses the Gluon Maps library, which is licensed under the GNU General Public License v3 (GPLv3).
The source code for this application is available at [URL to your source code repository].
 */

// mvn clean -Pandroid gluonfx:build gluonfx:package gluonfx:install gluonfx:nativerun

public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static Theme theme;
    public static Stage stage_copy;
    public static boolean KEEP_SCREEN = false;
    public static boolean IS_SCREEN_ON = false;
    StackPane stackPane;
    public static ModalPane modalPane;
    TextField search_txt;
    Board board;
    public static HBox app_top_bar;
    public static Image bike_img, bike_img_light, bike_img_dark, middle_finger;
    public static Image bike_icon, bike_icon_light, bike_icon_dark;
    public static Button camera_btn;
    public static ToggleSwitch toggle_screen;
    public static Sound sound_sonar;
    public static Sound sound_click;
    public static Sound sound_finish;
    static ArrayList<InputStream> streams;

    static {
        InputStream city_stream = Objects.requireNonNull(App.class.getResourceAsStream("data_json/place-city.ndjson"));
        InputStream town_stream = Objects.requireNonNull(App.class.getResourceAsStream("data_json/place-town.ndjson"));
        InputStream village_stream = Objects.requireNonNull(App.class.getResourceAsStream("data_json/place-village.ndjson"));
        InputStream hamlet_stream = Objects.requireNonNull(App.class.getResourceAsStream("data_json/place-hamlet.ndjson"));
        streams = new ArrayList<>();

        bike_img_light = new Image(Objects.requireNonNull(App.class.getResourceAsStream("bike_above.png")));
        bike_img_dark = new Image(Objects.requireNonNull(App.class.getResourceAsStream("bike_above_black.png")));
        middle_finger = new Image(Objects.requireNonNull(App.class.getResourceAsStream("Gemini_Generated.png")));

        bike_icon_light = new Image(Objects.requireNonNull(App.class.getResourceAsStream("bicycle_icon.png")));
        bike_icon_dark = new Image(Objects.requireNonNull(App.class.getResourceAsStream("bicycle_icon_black.png")));

        streams.add(city_stream);
        streams.add(town_stream);
        streams.add(village_stream);
        streams.add(hamlet_stream);
    }

    public void start(Stage stage) throws JsonException, URISyntaxException, IOException {

//        System_Info.save_user_name("test05");     // IMPORTANT - save user name to settings file - DEVELOPER MODE - instalacija na uredjaj
//        user_name.set("test05");                  // IMPORTANT - save user name to settings file - DEVELOPER MODE - samo za emulator - probati na mobilni

        try {
            System_Info.cityService = new CityService_json(streams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System_Info.retrieve_all_settings();
        if (Boolean.parseBoolean(System_Info.retrieve_theme_settings())) {
            theme = new CupertinoLight();
        } else {
            theme = new CupertinoDark();
        }
        KEEP_SCREEN = Boolean.parseBoolean(System_Info.retrieve_screen_settings());
        int platform_result = System_Info.get_platform();
        System_Info.get_display();
        System_Info.get_orientation();
        System_Info.net_available();
        System_Info.get_compass();
        System_Info.get_battery();
        if (KEEP_SCREEN) {
            IS_SCREEN_ON = System_Info.displayService.keepScreenOn(); // IMPORTANT - Custom display attach service - keep screen on/off implementacija za android
        }
        LOGGER.debug("JavaFX: {}\nrunning on Java: {}\nPlatform: {}\nIS_SCREEN_ON: {}", System_Info.javafxVersion(), System_Info.javaVersion(), System_Info.platform, IS_SCREEN_ON);

        if (theme.isDarkMode()) {
            bike_img = bike_img_light;
            bike_icon = bike_icon_light;
        } else {
            bike_img = bike_img_dark;
            bike_icon = bike_icon_dark;
        }
        stage_copy = stage;
        modalPane = new ModalPane();
        modalPane.displayProperty().addListener((obs, old, val) -> {
            if (!val) {
                modalPane.setAlignment(Pos.CENTER);
                modalPane.usePredefinedTransitionFactories(null);
            }
        });
        modalPane.setId("modalPane");

        System_Info.progress_d = progress_dialog();

        var start_btn = new Button("Start");
        start_btn.getStyleClass().addAll(/*Styles.BUTTON_OUTLINED,*/ Styles.ELEVATED_4, Styles.TEXT_CAPTION, Styles.MEDIUM, Styles.SUCCESS);
        start_btn.setOnAction(e -> {
//            startPing();  // TODO
            sound_sonar.play();
            if (user_name.get() == null) {
                CustomDialog dialog = new CustomDialog("Register", input -> {   // execute when ok button click
                    LOGGER.debug("Dialog result: " + input.toString());
                    user_name.set((String) input.getSource());
                    save_user_name(user_name.get());
                });
                dialog.show(stackPane);
            }
            if (start_btn.getText().equals("Finnish")) {
                var alert = confirmation_dialog();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get().getButtonData() == ButtonBar.ButtonData.YES) {
                    sound_finish.play();
                    start_btn.setText("Start");
                    System_Info.recorder.stopRecording();
                    String style_danger = Styles.DANGER;
                    start_btn.getStyleClass().removeAll(style_danger);  // uklanjamo sve danger style ako ga ima
                    start_btn.getStyleClass().add(Styles.SUCCESS);
                } else {
                    e.consume();
                }
            } else {
                start_btn.setText("Finnish");
                images_view.forEach(ImageUtils::deleteFile);
                images_view.clear();
                LOGGER.debug("All images cleared.");
                System_Info.recorder.startRecording();
                String style_success = Styles.SUCCESS;
                start_btn.getStyleClass().removeAll(style_success); // uklanjamo sve sucess style ako ga ima
                start_btn.getStyleClass().add(Styles.DANGER);
                if (user_name.get() != null) {
                    System_Info.show_notification("Alpha version - Data may not be persistent", Styles.DANGER);  // notifikacija na main screen
                }
            }
        });

        var camera_font = new FontIcon(Material2AL.CAMERA_ALT);
        camera_font.getStyleClass().addAll("camera-button");
        camera_btn = new Button(null, camera_font);
        camera_btn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.LARGE, Styles.ACCENT);

        stackPane = new StackPane(modalPane, createWelcomePane(), start_btn, System_Info.notification(), camera_btn) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                start_btn.setLayoutX(getWidth() / 2 - start_btn.getWidth() / 2);
                start_btn.setLayoutY(0);
                camera_btn.setLayoutX(0);
                camera_btn.setLayoutY(100);  // gore -  getHeight() - camera_btn.getHeight() - 0  // dole levo
            }
        }; // notification create and add to root stack pane

        Scene scene = new Scene(stackPane, System_Info.dimension.getWidth(), System_Info.dimension.getHeight());
        scene.getStylesheets().add(theme.getUserAgentStylesheet());
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
        // Make sure the application quits completely on close
        stage.setOnCloseRequest(t -> {
            cleanExit();
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                System_Info.cityService = new CityService(stream);   // .txt
                try {
                    getGPSNames();
                } catch (IOException | JsonException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                dev.java4now.System_Info.capture_image();
//                camera_btn.setOnAction(e -> {
//                    CadenceMeterReader.icon_code.set(50); // custom debug mode for test icon code purposes
//                });
                sound_sonar = new Sound(sonar);
                sound_click = new Sound(click);
                sound_finish = new Sound(finish);

//                System.out.println("Find city: " + System_Info.cityService.findByLatLong(44.87834717084236, 20.665928677876934)); // important debug
            }
        });
    }


    public static void cleanExit() {
        Platform.exit();
        System.exit(0);
    }

    //---------------------------------------------------
    private BorderPane createWelcomePane() throws JsonException, URISyntaxException, IOException {

        var leftDialog = make_left_dialog();
//        var settingsDialog = make_settings_dialog();
        var searchDialog = make_search_dialog();

//-------------------------------- MENU BAR -------------------------------------
        var menuBtn = new Button(null, new FontIcon(Feather.MENU));
        menuBtn.getStyleClass().addAll(
                Styles.BUTTON_CIRCLE, Styles.FLAT
        );
        menuBtn.setOnAction(evt -> {
            modalPane.setAlignment(Pos.TOP_LEFT);
            modalPane.usePredefinedTransitionFactories(Side.LEFT);
            modalPane.show(leftDialog);
        });

        var searchBtn = new Button(null, new FontIcon(Feather.SEARCH));
        searchBtn.getStyleClass().addAll(
                Styles.BUTTON_CIRCLE, Styles.FLAT
        );
        searchBtn.setOnAction(evt -> {
//            searchBtn.requestFocus();
            modalPane.setAlignment(Pos.TOP_CENTER);
            modalPane.usePredefinedTransitionFactories(Side.TOP);
            modalPane.show(searchDialog);
//            searchBtn.requestFocus();
        });

        var settingsBtn = new Button(null, new FontIcon(Feather.SETTINGS));
        settingsBtn.getStyleClass().addAll(
                Styles.BUTTON_CIRCLE, Styles.FLAT
        );
        settingsBtn.setOnAction(evt -> {
            modalPane.setAlignment(Pos.TOP_RIGHT);
            modalPane.usePredefinedTransitionFactories(Side.RIGHT);
            modalPane.show(make_settings_dialog()); // IMPORTANT - uvek kreirati novi dialog da bi bile ucitane promene tj. nove vrednosti
        });

        var left_app_top_bar = new HBox(menuBtn);
        left_app_top_bar.setAlignment(Pos.TOP_LEFT);

        Label lbl = new Label("CyclePower");
        var center_app_top_bar = new HBox(/*lbl*/);
        center_app_top_bar.setAlignment(Pos.CENTER);

        var right_app_top_bar = new HBox(searchBtn, settingsBtn);
        right_app_top_bar.setAlignment(Pos.TOP_RIGHT);

        app_top_bar = new HBox(left_app_top_bar, center_app_top_bar, right_app_top_bar);
//        app_top_bar.setStyle("-fx-padding: 10px;-fx-background-color:rgba(255, 0, 0, 0.2);");           // izbaciti padding za borderpane
        if (theme.isDarkMode()) {
            app_top_bar.getStyleClass().add("menu_bar_dark");
        }else{
            app_top_bar.getStyleClass().add("menu_bar_light");
        }

//        var smallSep = new javafx.scene.control.Separator(Orientation.HORIZONTAL);
//        smallSep.getStyleClass().add(Styles.SMALL);
//        var app_top_bar_v = new  VBox(app_top_bar, smallSep);

        var border_pane = new BorderPane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();   // IMPORTANT - Mora pre postavljanja i dodeljivanja vrednosti
                left_app_top_bar.setPrefWidth(getWidth() / 3);
                center_app_top_bar.setPrefWidth(getWidth() / 3);
                right_app_top_bar.setPrefWidth(getWidth() / 3);
                app_top_bar.setPrefWidth(getWidth());
                searchDialog.setMaxHeight(app_top_bar.getHeight() + 2); // important bice visina modal pane-a koji sadrzi search dialog
                leftDialog.setMaxWidth(getWidth() / 2);
//                LOGGER.debug(app_top_bar.getHeight());
                System_Info.display_width.set(getWidth());
                System_Info.display_width_half.set(getWidth() / 2);
                System_Info.center_height.set(getHeight() - app_top_bar.getHeight());
                System_Info.display_height.set(getHeight());
//                LOGGER.debug(System_Info.display_height.get());
            }
        };
        border_pane.setTop(app_top_bar);
        BorderPane.setAlignment(app_top_bar, javafx.geometry.Pos.TOP_CENTER);
        board = new Board();
        border_pane.setCenter(board);
        BorderPane.setAlignment(board, Pos.CENTER);
//        border_pane.setPadding(new Insets(10, 0, 0, 0)); // Ako primenimo padding sa setstyle onda ovo ukloniti

        return border_pane;
    }


    //------------------------------------------------------------------
    public void changeTheme() {
        if (theme.isDarkMode()) {
            theme = new CupertinoLight();
        } else {
            theme = new CupertinoDark();
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System_Info.save_theme_settings(String.valueOf(!theme.isDarkMode()));
                    stage_copy.close();
                    System.gc();
                    start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    //------------------------------------------------------------------
    private BorderPane make_left_dialog() {
        var root = new BorderPane();
        FontIcon[] theme_icon_btn = {new FontIcon(Material2OutlinedMZ.WB_SUNNY), new FontIcon(Feather.SUNSET)};
        var theme_change_btn = new Button(null, theme.isDarkMode() ? theme_icon_btn[0] : theme_icon_btn[1]);
        theme_change_btn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, "theme_change_btn");
        var tile = new Tile();
//        tile.getStyleClass().addAll(Styles.BG_SUCCESS_MUTED);
        if (theme.isDarkMode()) {
            tile.setStyle("-fx-background-color:#424F3D;");
        } else {
            tile.setStyle("-fx-background-color:#37BC04;");
        }
        var url = "https://github.com/CommonGrounds";
        tile.setDescription("\n\n\n\n\n[hr/][url=" + url + "]Github Source[/url]");
        tile.setAction(theme_change_btn);
        tile.setGraphic(/*root_icon*/new ImageView(bike_icon));
//        tile.getGraphic().setScaleX(2.0);
//        tile.getGraphic().setScaleY(2.0);
        // IMPORTANT kada ima vise akcija za isti tile mora event filter a ne buton.onAction()
        tile.addEventFilter(ActionEvent.ACTION, e -> {
            if (e.getTarget() instanceof Hyperlink link) {
                LOGGER.debug(link.getUserData().toString());
                if (System_Info.net_exist.get()) {
                    Browser.launch(link.getUserData().toString());
                } else {
                    if (System_Info.platform.equals("Desktop")) {      // Zato sto na destop-u attach ConnectivityService uvek daje false
                        Browser.launch(link.getUserData().toString());
                    } else {
                        modalPane.hide(true);  // 1. mora modal hide
                        System_Info.show_notification("Net is not available", Styles.DANGER); // pa onda notifikacija na main screen
                        LOGGER.warn("Net is not available");
                    }
                }
            }
            if (e.getTarget() instanceof Button btn) {
//                btn.getStyleClass().add(Styles.SUCCESS);
                changeTheme();
//                theme_change_btn.setGraphic( theme_change_btn.graphicProperty().isEqualTo( theme_icon_btn[1]).get() ? theme_icon_btn[0]:theme_icon_btn[1]);
//                LOGGER.debug(e.getEventType().getName());
            }
            e.consume();
        });
        root.setTop(tile);

        var beer = new Button("Cofee");
        var beer_icon = new FontIcon(Feather.COFFEE);
        beer_icon.getStyleClass().addAll(Styles.SUCCESS);
        var beer_box = new HBox(beer_icon, beer);
        HBox.setHgrow(beer, Priority.ALWAYS);
        beer.getStyleClass().addAll(Styles.FLAT);

        ActionEvent beer_action = new ActionEvent(){
            @Override
            public Object getSource() {
                modalPane.hide(true);
                CoffeeDialog dialog = new CoffeeDialog("I need Coffee now!", input -> {   // execute when ok button click
                    LOGGER.debug("CoffeeDialog result: " + input.getSource());
                    if (input.getSource().equals("true")) {
//                    System_Info.show_notification("Coffee is delicious", Styles.SUCCESS);
                        if (System_Info.net_exist.get()) {
                            Browser.launch(coffee_url);
                        } else {
                            if (System_Info.platform.equals("Desktop")) {      // Zato sto na destop-u attach ConnectivityService uvek daje false
                                Browser.launch(coffee_url);
                            } else {
                                System_Info.show_notification("Net is not available", Styles.DANGER); // pa onda notifikacija na main screen
                                LOGGER.warn("Net is not available");
                            }
                        }
                    } else {
                        System_Info.show_notification("No coffee , make better application", Styles.DANGER);
                    }
                });
                dialog.show(stackPane);
                return "true";
            }
        };
        beer.setOnAction(evt -> {
           Object obj = beer_action.getSource();
//         CoffeeDialog dialog = (CoffeeDialog) obj; // ako vratim dialog iz ActionEvent umesto String ( true )
           if(obj.toString().equals("true")){
               LOGGER.debug("ActionEvent success");
           }
            evt.consume();
        });
        beer_icon.setOnTouchReleased( evt -> {
            beer_action.getSource();
            evt.consume();
        });
        beer_icon.setOnMouseClicked( evt -> {
            beer_action.getSource();
            evt.consume();
        });


        var bike = new Button("Bike Settings");
        bike.setOnAction(evt -> {
            modalPane.hide(true);
            modalPane.setAlignment(Pos.TOP_RIGHT);
            modalPane.usePredefinedTransitionFactories(Side.RIGHT);
            modalPane.show(make_settings_dialog()); // IMPORTANT - uvek kreirati novi dialog da bi bile ucitane promene tj. nove vrednosti
        });
        var bike_icon = new FontIcon(Material2OutlinedAL.DIRECTIONS_BIKE);
        bike_icon.setOnTouchReleased(evt -> {
            modalPane.hide(true);
            modalPane.setAlignment(Pos.TOP_RIGHT);
            modalPane.usePredefinedTransitionFactories(Side.RIGHT);
            modalPane.show(make_settings_dialog()); // IMPORTANT - uvek kreirati novi dialog da bi bile ucitane promene tj. nove vrednosti
        });
        bike_icon.getStyleClass().addAll(Styles.SUCCESS);
        var bike_box = new HBox(bike_icon, bike);
        HBox.setHgrow(bike, Priority.ALWAYS);
        bike.getStyleClass().addAll(Styles.FLAT);

        toggle_screen = new ToggleSwitch(IS_SCREEN_ON ? "Screen On" : "Screen Off");
        toggle_screen.setSelected(IS_SCREEN_ON);
        toggle_screen.setLabelPosition(HorizontalDirection.RIGHT);  // po defaultu je labela levo
        toggle_screen.pseudoClassStateChanged(Styles.STATE_SUCCESS, IS_SCREEN_ON);
        toggle_screen.setAlignment(Pos.TOP_CENTER);
        /*
        var screen_icon = new FontIcon(Material2MZ.WB_SUNNY);
        screen_icon.getStyleClass().addAll(Styles.SUCCESS);
        screen_icon.setOnTouchReleased(evt -> {
            if (IS_SCREEN_ON) {
                IS_SCREEN_ON = !System_Info.displayService.keepScreenOff();
                toggle_screen.setText(IS_SCREEN_ON ? "Screen Off" : "Screen On");
            } else {
                IS_SCREEN_ON = System_Info.displayService.keepScreenOn();
                toggle_screen.setText(IS_SCREEN_ON ? "Screen Off" : "Screen On");
            }
            LOGGER.debug("Screen Always On: " + IS_SCREEN_ON);
            System_Info.save_screen_settings(String.valueOf(IS_SCREEN_ON));
            modalPane.hide(true);
        });
         */
        var screen_box = new HBox(/*screen_icon,*/ toggle_screen){
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                toggle_screen.setPrefWidth(getWidth()/2/* - screen_icon.getIconSize()*/);
//                toggle_screen.setLayoutX(getWidth() / 2 - toggle_screen.getWidth() / 2);
            }
        };
        screen_box.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(toggle_screen, Priority.ALWAYS);
        toggle_screen.getStyleClass().addAll(Styles.FLAT);
        toggle_screen.selectedProperty().addListener((obs, old, val) -> {
                    IS_SCREEN_ON = val;
                    System_Info.save_screen_settings(String.valueOf(IS_SCREEN_ON));
                    if (IS_SCREEN_ON) {
//                        IS_SCREEN_ON = !System_Info.displayService.keepScreenOff();
                        toggle_screen.setText(IS_SCREEN_ON ? "Screen On" : "Screen Off");
                    } else {
//                        IS_SCREEN_ON = System_Info.displayService.keepScreenOn();
                        toggle_screen.setText(IS_SCREEN_ON ? "Screen On" : "Screen Off");
                    }
                    LOGGER.debug("Screen Always On: " + IS_SCREEN_ON);
                    toggle_screen.pseudoClassStateChanged(Styles.STATE_SUCCESS, IS_SCREEN_ON);
//            modalPane.hide(true);
                }
        );

        var btn_help = new Button("Help");
        btn_help.setAlignment(Pos.TOP_CENTER);
        var help_icon = new FontIcon(Material2AL.HELP);
        help_icon.getStyleClass().addAll(Styles.SUCCESS);
        help_icon.setOnTouchReleased(evt -> {
            modalPane.hide(true);
            modalPane.setAlignment(Pos.TOP_CENTER);
            modalPane.usePredefinedTransitionFactories(Side.TOP);
            modalPane.show(make_help_dialog());
        });
        var help_box = new HBox(help_icon, btn_help);
        HBox.setHgrow(btn_help, Priority.ALWAYS);
        btn_help.getStyleClass().addAll(Styles.FLAT);
        btn_help.setOnAction(evt -> {
            modalPane.hide(true);
            modalPane.setAlignment(Pos.TOP_CENTER);
            modalPane.usePredefinedTransitionFactories(Side.TOP);
            modalPane.show(make_help_dialog());
        });

        var btn_about = new Button("About");
        btn_about.setAlignment(Pos.TOP_CENTER);
        var about_icon = new FontIcon(Material2AL.ANDROID);
        about_icon.getStyleClass().addAll(Styles.SUCCESS);
        about_icon.setOnTouchReleased(evt -> {
            modalPane.hide(true);
            Board.showAbout();
        });
        var about_box = new HBox(about_icon, btn_about);
        HBox.setHgrow(btn_about, Priority.ALWAYS);
        btn_about.getStyleClass().addAll(Styles.FLAT);
        btn_about.setOnAction(evt -> {
            modalPane.hide(true);
            Board.showAbout();
        });

        var btn = new Button("Back");
        btn.setAlignment(Pos.TOP_CENTER);
        var close_icon = new FontIcon(Feather.SKIP_BACK);
        close_icon.getStyleClass().addAll(Styles.SUCCESS);
        close_icon.setOnTouchReleased(evt -> {
            modalPane.hide(true);
        });
        var close_box = new HBox(close_icon, btn);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.getStyleClass().addAll(Styles.FLAT);
        btn.setOnAction(evt -> {
            modalPane.hide(true);
        });

        var smallSep = new Separator(Orientation.HORIZONTAL);
        smallSep.getStyleClass().add(Styles.SMALL);

        var center = new VBox( 20,screen_box, smallSep, beer_box, bike_box, help_box, about_box, close_box) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();

                beer_box.setPrefWidth(getWidth());
                beer_icon.setIconSize((int) beer_box.getHeight());
                beer.setPrefWidth(beer_box.getWidth() - beer_icon.getIconSize());

                bike_box.setPrefWidth(getWidth());
                bike_icon.setIconSize((int) bike_box.getHeight());
                bike.setPrefWidth(bike_box.getWidth() - bike_icon.getIconSize());

                screen_box.setPrefWidth(getWidth());
//                screen_icon.setIconSize((int) screen_box.getHeight());
//                LOGGER.info("toggle_screen width: " + toggle_screen.getWidth() + ", screen_box.getWidth(): " + screen_box.getWidth());

                help_box.setPrefWidth(getWidth());
                help_icon.setIconSize((int) help_box.getHeight());
                btn_help.setPrefWidth(help_box.getWidth() - help_icon.getIconSize());

                about_box.setPrefWidth(getWidth());
                about_icon.setIconSize((int) about_box.getHeight());
                btn_about.setPrefWidth(about_box.getWidth() - about_icon.getIconSize());

                close_box.setPrefWidth(getWidth());
                close_icon.setIconSize((int) close_box.getHeight());
                btn.setPrefWidth(close_box.getWidth() - close_icon.getIconSize());
            }
        };
        center.setPadding(new Insets(20, 0, 0, 0));
        center.setAlignment(Pos.TOP_CENTER);
//        VBox.setMargin(screen_box, new Insets(0, 0, 20, 0));
        root.setCenter(center);

        if (theme.isDarkMode()) {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1), null, null)));
        } else {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(235, 235, 235, 1), null, null)));
        }

        return root;
    }


    //------------------------------------------------------------------
    private BorderPane make_settings_dialog() {
        var root = new BorderPane();
        root.setPrefWidth(System_Info.dimension.getWidth());

        var app_top_bar = new HBox(SettingsPage.top_screen());
//        app_top_bar.setPrefWidth(System_Info.dimension.getWidth());
//        app_top_bar.setAlignment(Pos.CENTER_LEFT);

        var vBox = new VBox(SettingsPage.Bike_and_screen());
        vBox.setPadding(new Insets(20, 0, 0, 0));
        vBox.setAlignment(Pos.TOP_CENTER);

        root.setTop(app_top_bar);
        BorderPane.setAlignment(app_top_bar, javafx.geometry.Pos.TOP_CENTER);
        root.setCenter(vBox);
        BorderPane.setAlignment(vBox, Pos.CENTER);
        root.setPadding(new Insets(10, 0, 0, 0));

        if (theme.isDarkMode()) {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1), null, null)));
        } else {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(235, 235, 235, 1), null, null)));
        }
        return root;
    }


    //------------------------------------------------------------------
    private HBox make_search_dialog() {

        var root = new HBox();

        search_txt = new TextField();
        search_txt.setOnAction(evt -> {    // enter
            LOGGER.debug("Search");
            modalPane.hide(true);
            System_Info.show_notification("Nothing to search", Styles.WARNING);  // notifikacija na main screen
        });
        search_txt.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        search_txt.requestFocus();
                    }
                });
            }
        });

        var leftBtn = new Button("", new FontIcon(Feather.SEARCH));
        leftBtn.setCursor(Cursor.HAND);
        leftBtn.getStyleClass().addAll(Styles.BUTTON_ICON/*,Styles.SMALL*/);
        leftBtn.setOnAction(e -> {
            LOGGER.debug("Search");
            modalPane.hide(true);
            System_Info.show_notification("Nothing to search", Styles.WARNING);
        });
        var group = new InputGroup(leftBtn, search_txt);
        HBox.setHgrow(search_txt, Priority.ALWAYS);

        root.setMaxWidth(System_Info.dimension.getWidth());
//        root.setMaxHeight(search_txt.getHeight() + 10); // podeseno u app_bar

        root.getChildren().add(group);
        root.setAlignment(Pos.CENTER);

        if (theme.isDarkMode()) {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1), null, null)));
        } else {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(235, 235, 235, 1), null, null)));
        }

        return root;
    }


    //---------------------------------------
    private BorderPane make_help_dialog() {

        var root = new BorderPane();
        root.setPrefWidth(System_Info.dimension.getWidth());

        var back_btn = new Button(null, new FontIcon(Feather.ARROW_RIGHT));
        back_btn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.BUTTON_CIRCLE, Styles.LARGE);
        back_btn.setOnAction(evt -> {
            modalPane.hide(true);
            System.gc();
        });
        var app_top_bar = new HBox(back_btn);
        if (theme.isDarkMode()) {
            app_top_bar.getStyleClass().add("menu_bar_dark");
        }else{
            app_top_bar.getStyleClass().add("menu_bar_light");
        }
        app_top_bar.setPrefWidth(System_Info.display_width.get());
        app_top_bar.setAlignment(Pos.CENTER);
        app_top_bar.setPadding(new Insets(0, 0, 10, 0));   // 10 px prazan prostor ispod back_btn dugmeta

        var box = new VBox(HelpPage.getImage(), HelpPage.show_Help());
        var gridScroll = new ScrollPane(box);                               // IMPORTANT - Use ScrollPane za nov unutrasnji VBox ispod button
        gridScroll.setFitToWidth(true);

        root.setTop(app_top_bar);
        BorderPane.setAlignment(app_top_bar, Pos.TOP_CENTER);
        root.setCenter(gridScroll);
        BorderPane.setAlignment(gridScroll, Pos.CENTER);
        root.setPadding(new Insets(10, 0, 0, 0));           // odaljavanje dugmeta tj. celog BorderPane-a od vrha

        if (theme.isDarkMode()) {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1), null, null)));
        } else {
            root.setBackground(new Background(new BackgroundFill(Color.rgb(235, 235, 235, 1), null, null)));
        }

        return root;
    }

    //---------------------------------------
    public static void getGPSNames() throws JsonException, URISyntaxException, IOException {
        System_Info.get_gps();
    }


    //---------------------------------------
    private Alert confirmation_dialog() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.getDialogPane().getStyleClass().add(Styles.BG_DANGER_EMPHASIS);
        alert.setTitle("Confirm");
        alert.setHeaderText(null/*"Header"*/);    // null ako ne zelimo da ima header
        alert.setContentText("Stop Activity");
//------------ Za promenu size dialog-a mora ovako ----------------
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(stage_copy.getWidth() * 0.8); // setMinHeight(Region.USE_PREF_SIZE);
        Platform.runLater(() -> alert.setResizable(false));
//------------------------------------------------------------------
        ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(yesBtn, noBtn);
        alert.initOwner(stage_copy);

        return alert;
    }


    //-------------------------------------------------
    public static void main(String[] args) {
//        System.setProperty("com.sun.javafx.isEmbedded", "true");
//        System.setProperty("com.sun.javafx.virtualKeyboard", "javafx");
//        System.setProperty("com.sun.javafx.touch", "true");
//        System.setProperty("android:keepScreenOn","true");
        launch(args);
    }

}