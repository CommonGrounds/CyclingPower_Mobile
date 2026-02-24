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
                """;
        var article = """
                [hr/]
                
                [heading=3][color=skyblue][icon=wind-gusts size=24/][/color] Wind Card[/heading]
                
                The red arrow shows wind direction relative to your bike (Top = Front).
                [ul]
                [li]Orientation is based on magnetic north.[/li]
                [li]Keep your device away from magnetic mounts for accuracy.[/li]
                [/ul]
                
                [heading=3][color=orange][icon=photo size=24/][/color] Display Settings[/heading]
                [ul]
                [li][b]Wake Lock:[/b] Keeps the screen on during your ride.[/li]
                [li][b]Dark Mode:[/b] Recommended to preserve battery life.[/li]
                [/ul]
                
                [heading=3][color=yellow][icon=balance-scale size=24/][/color] Bike Physics[/heading]
                
                Accuracy of power measurement depends on precise input of both [b]bike and rider weight[/b] in the settings menu.
                
                [center][icon=github/] [url="https://github.com/CommonGrounds"]Source Code[/url][/center]
                """;

        var image = getImage();
// Dodajemo stil za moderniji izgled
        image.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

// Kreiramo poseban VBox za sliku sa marginama
        VBox imageContainer = new VBox(image);
        imageContainer.setPadding(new Insets(10, 0, 20, 0));
        imageContainer.setAlignment(Pos.CENTER);

        VBox container = new VBox(10);
        CustomBBCodeHandler<VBox> handler = new CustomBBCodeHandler<>(container);
        BBCodeParser parser = new BBCodeParser(article, handler);
        parser.parse();

        root.setPadding(new Insets(0, padding, 0, padding)); // IMPORTANT - stavljamo padding pre dodavanja child vbox-a da scroll ne bi setao horizontalno
        root.getChildren().addAll(BBCodeParser.createLayout(header),imageContainer, container );

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
            e.consume();
        });
    }
}
