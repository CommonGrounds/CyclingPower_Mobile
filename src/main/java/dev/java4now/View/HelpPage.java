package dev.java4now.View;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.BBCodeParser;
import dev.java4now.App;
import dev.java4now.Browser;
import dev.java4now.System_Info;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.Objects;


public class HelpPage {

    public static ImageView wind_image;
    private static final int padding = 10;

    static {
        wind_image = new ImageView(new Image(
                Objects.requireNonNull(App.class.getResource("Help_files/wind.png")).toExternalForm()));
    }



    //----------------------------------
    public static ImageView getImage(){
        wind_image.setFitWidth(System_Info.display_width.get() - padding*2);wind_image.setPreserveRatio(true);

        return wind_image;
    }



    //----------------------------------
    public static VBox show_Help(){

        var root = new VBox();
        root.setPadding(new Insets(0, padding, 0, padding)); // IMPORTANT - stavljamo padding pre dodavanja child vbox-a da scroll ne bi setao horizontalno

        var article = """
        [left][heading=1]CyclePower[/heading][/left]
        [ul]
        [li]This is Cycling computer application developed with open source [url="https://gluonhq.com/"]gluon software[/url] and it is under GPL V3 Licence.
        Source code of is on this [url="https://github.com/CommonGrounds"]Github link[/url].[/li]\
        [left][heading=3]Wind card[/heading][/left]\
        [li]Is showing wind direction ( red arrow ) in relation on heading direction - bike front is Up.
        N is north , W is west, E is east and S is south ( NW is north-west ) orientation is in relation to magnetic north,
        so keep your phone away of magnetic objects.[/li]\
        [left][heading=3]Keep Screen Always On[/heading][/left]\
        [li]make phone stays with screen on while measuring ride activity
        better to choose dark theme because light theme drain battery faster.[/li]\
        [left][heading=3]Bike settings[/heading][/left]\
        [li]In Bike settings screen - is important to enter exact bike and rider weight so the power measurement will be correct[/li]\
        [/ul]
       """;

        root.getChildren().add(BBCodeParser.createLayout(article));

        root.addEventFilter(ActionEvent.ACTION, e -> {
            if (e.getTarget() instanceof Hyperlink link) {
//                BrowseEvent.fire((String) link.getUserData());
                System.out.println(link.getUserData().toString());
                if(System_Info.net_exist.get()){
                    Browser.launch(link.getUserData().toString());
                }else{
                    if(System_Info.platform.equals("Desktop")){      // Zato sto na destop-u attach ConnectivityService uvek daje false
                        Browser.launch(link.getUserData().toString());
                    }else{
                        App.modalPane.hide(true);  // 1. mora modal hide
                        System_Info.show_notification("Net is not available",Styles.DANGER); // pa onda notifikacija na main screen
                        System.out.println("Net is not available");
                    }
                }
            }
            e.consume();
        });

        return root;
    }
}
