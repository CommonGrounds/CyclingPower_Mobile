package dev.java4now.View;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.BBCodeParser;
import dev.java4now.App;
import dev.java4now.Browser;
import dev.java4now.System_Info;
import dev.java4now.util.CustomBBCodeHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Objects;


public class HelpPage {

    public static ImageView wind_image;
    private static final int padding = 20;

    static {
        wind_image = new ImageView(new Image(
                Objects.requireNonNull(App.class.getResource("Help_files/wind.png")).toExternalForm()));
    }


    //----------------------------------
    public static ImageView getImage() {
        wind_image.setFitWidth(System_Info.display_width.get() - padding * 2);
        wind_image.setPreserveRatio(true);

        return wind_image;
    }


    //----------------------------------
    public static VBox show_Help() {

        var root = new VBox();


        var header = """
                [center][heading=1]CyclePower[/heading][/center]
                [i]Open-source cycling computer powered by [url="https://gluonhq.com/"]Gluon[/url]. Licensed under GPL V3.[/i]
                [hr/]
                [heading=3][color=skyblue][icon=wind-gusts size=24/][/color] Wind Card[/heading]
                """;

        var article = """
                The [b]color arrow[/b] shows wind direction relative to your bike 
                ([b]Top = Front / Headwind[/b], Bottom = Tailwind, Sides = Crosswind).
                
                [heading=3][color=skyblue][icon=compass size=24/][/color] Wind Card Colors[/heading]
                
                The sector color changes based on average wind speed (km/h):
                [ul spacing=8]
                [li][color=limegreen][icon=pie-chart-alt size=22/][/color] [b]Green[/b] (< 10 km/h) – Light breeze, almost no effect[/li]
                [li][color=#00BCD4][icon=pie-chart-alt size=22/][/color] [b]Cyan[/b] (10–19 km/h) – Noticeable, watch crosswinds[/li]
                [li][color=orange][icon=pie-chart-alt size=22/][/color] [b]Orange[/b] (20–29 km/h) – Strong, headwind slows you, crosswind unstable[/li]
                [li][color=red][icon=pie-chart-alt size=22/][/color] [b]Red[/b] (30–44 km/h) – Very hard, exhausting headwind, risky crosswind[/li]
                [li][color=#9e28a3][icon=pie-chart-alt size=22/][/color] [b]Purple[/b] (≥ 45 km/h) – Extreme, dangerous – avoid riding if possible[/li]
                [/ul]
                [color=gray][i]The same pie-chart icon represents the wind sector in your compass – only the color changes with speed.[/i][/color]
                
                [heading=3][color=orange][icon=settings size=24/][/color] Display Settings[/heading]
                [ul]
                [li][b]Wake Lock:[/b] Keeps the screen on during your ride.[/li]
                [li][b]Dark Mode:[/b] Recommended for better visibility and battery saving.[/li]
                [/ul]
                
                [heading=3][color=yellow][icon=balance-scale size=24/][/color] Bike Physics[/heading]
                
                Power measurement accuracy depends on correctly entering your [b]bike + rider weight[/b] in the settings.
                
                [center][icon=github size=%f/] [url="https://github.com/CommonGrounds"]Source Code on GitHub[/url][/center]
                """.formatted(Font.getDefault().getSize() - 3);


        var image = getImage();
// Dodajemo stil za moderniji izgled
        image.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

// Kreiramo poseban VBox za sliku sa marginama
        VBox imageContainer = new VBox(image);
        imageContainer.setPadding(new Insets(10, 0, 20, 0));
        imageContainer.setAlignment(Pos.CENTER);

        VBox container_header = new VBox(10);
        CustomBBCodeHandler<VBox> handler_0 = new CustomBBCodeHandler<>(container_header);
        BBCodeParser parser_0 = new BBCodeParser(header, handler_0);
        parser_0.parse();

        VBox container = new VBox(10);
        CustomBBCodeHandler<VBox> handler = new CustomBBCodeHandler<>(container);
        BBCodeParser parser = new BBCodeParser(article, handler);
        parser.parse();

        root.setPadding(new Insets(0, padding, 0, padding)); // IMPORTANT - stavljamo padding pre dodavanja child vbox-a da scroll ne bi setao horizontalno
        root.getChildren().addAll(container_header/*BBCodeParser.createLayout(header)*/, imageContainer, container);

        root.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: " + (Font.getDefault().getSize() - 4) + "pt;"); // important - da bi imali sve bold, italic etc opcije

        addHandlers(root);

        return root;
    }


    //---------------------------------------------------------------
    private static void addHandlers(VBox root) {

        root.addEventFilter(ActionEvent.ACTION, e -> {
            if (e.getTarget() instanceof Hyperlink link) {
//                BrowseEvent.fire((String) link.getUserData());
                System.out.println(link.getUserData().toString());
                if (System_Info.net_exist.get()) {
                    Browser.launch(link.getUserData().toString());
                } else {
                    if (System_Info.platform.equals("Desktop")) {      // Zato sto na destop-u attach ConnectivityService uvek daje false
                        Browser.launch(link.getUserData().toString());
                    } else {
                        App.modalPane.hide(true);  // 1. mora modal hide
                        System_Info.show_notification("Net is not available", Styles.DANGER); // pa onda notifikacija na main screen
                        System.out.println("Net is not available");
                    }
                }
            }
//            System.out.println("Action event: " + e.getTarget());
            e.consume();
        });
    }
}
