package dev.java4now.View;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.BBCodeParser;
import dev.java4now.http.SaveUser;
import dev.java4now.model.User;
//import io.github.wycst.wast.json.JSON;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.bytedance.BytedanceIconsBoldMZ;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import static dev.java4now.App.middle_finger;
import static dev.java4now.App.theme;
import static dev.java4now.System_Info.SERVER_URL;
import static dev.java4now.http.SendToServer.httpClient;


public class CoffeeDialog {
    private final String message;
    private final EventHandler<ActionEvent> onResult;
    private BorderPane dialogBox;
    private Pane overlay;
    Label messageLabel;


    public CoffeeDialog(String message, EventHandler<ActionEvent> onResult) {
        this.message = message;
        this.onResult = onResult;
    }

    public void show(Pane parent) {
        // Overlay (semi-transparent background)
        overlay = new Pane();
//        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Gray overlay
        overlay.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100, 0.5), null, null)));
        overlay.setPrefSize(parent.getWidth(), parent.getHeight()); // zacrnjen screen

        // Dialog content
        messageLabel = new Label(message);
        messageLabel.setAlignment(Pos.CENTER);
//        Text txt = new Text("Buy me coffee if you like this application");
//        TextFlow text_flow = BBCodeParser.createFormattedText(txt.getText());
//        text_flow.setTextAlignment(TextAlignment.CENTER);
        var centerBox = new VBox(create_image_text_flow()/*text_flow*/);
        centerBox.setAlignment(Pos.CENTER);
//        text_flow.setMaxWidth(330); // text se prelama auto na ovu sirinu sa -fx-wrap-text: true; u css-u ( za TextFlow )
        Button okButton = new Button("Ok", new FontIcon(FontAwesomeSolid.THUMBS_UP));
        okButton.getStyleClass().addAll(Styles.SUCCESS);
        okButton.setFont(Font.font(okButton.getFont().getSize() + 2));
//        System.out.println("CoffeeDialog: " + okButton.getFont().getSize());
        okButton.setOnAction(e -> {
            onResult.handle(new ActionEvent("true", null));
            close(parent);
        });
//        okButton.getStyleClass().add("button");
        javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView(middle_finger);
//        icon.setFitHeight(okButton.getFont().getSize());
        Button cancelButton = new Button("No", new FontIcon(FontAwesomeSolid.HAND_MIDDLE_FINGER/*icon*/));
//        cancelButton.setStyle("-fx-background-color: #ff0000;");
        cancelButton.getStyleClass().addAll(Styles.DANGER);
        cancelButton.setFont(Font.font(cancelButton.getFont().getSize() + 2));
        cancelButton.setOnAction(e -> {
            onResult.handle(new ActionEvent("false", null));
            close(parent);
        });
        var hbox = new HBox(40, okButton,cancelButton);
        hbox.setAlignment(Pos.CENTER);

        dialogBox = new BorderPane();
        dialogBox.setTop(messageLabel);
        dialogBox.setCenter(centerBox);
        dialogBox.setBottom(hbox);
        BorderPane.setAlignment(messageLabel,  Pos.CENTER);
//        BorderPane.setAlignment(text_flow,  Pos.BOTTOM_CENTER); // radi za label ali ne za textflow
        dialogBox.setPadding(new Insets(20));
//        dialogBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");
//        dialogBox.getStylesheets().add(getClass().getResource("root.css").toExternalForm());
        if (theme.isDarkMode()) {
            dialogBox.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1), null, null)));
        } else {
            dialogBox.setBackground(new Background(new BackgroundFill(Color.rgb(235, 235, 235, 1), null, null)));
        }
        dialogBox.setBorder(new Border(new BorderStroke(Color.DARKGREEN,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        dialogBox.setMaxWidth(Math.max(300, parent.getWidth() / 6));
        dialogBox.setMaxHeight(parent.getHeight() / 4);
//        dialogBox.setMaxHeight(Region.USE_COMPUTED_SIZE);

        // Center the dialog
        StackPane dialogPane = new StackPane(overlay, dialogBox);
        dialogPane.setAlignment(Pos.CENTER);

        // Add to parent and block interaction
        parent.getChildren().add(dialogPane);
        overlay.setOnMouseClicked(e -> e.consume()); // Prevent clicks on overlay
    }

    private void close(Pane parent) {
        parent.getChildren().remove(overlay.getParent());
    }


    private static FlowPane create_image_text_flow(){
 //       String unicodeText = createUnicodeText(orig_string);
        List<Node> nodes = new java.util.ArrayList<>(List.of());// = TextUtils.convertToTextAndImageNodes(unicodeText);

        TextFlow text_flow_1 = new TextFlow(new Text("Buy me  "));
        var coffee_font = new FontIcon(Feather.COFFEE);
        coffee_font.getStyleClass().addAll(Styles.DANGER);   // za non atlantafx colors mora 1. remove styles and add again or setIconColor(Color.RED);
        TextFlow text_flow_2 = new TextFlow(new Text("  if you like this application"));
//        nodes.addAll(List.of(text_flow_1,coffee_font,text_flow_2));
        nodes.add(text_flow_1);
        nodes.add(coffee_font);
        nodes.add(text_flow_2);

        FlowPane flowPane = new FlowPane();
        flowPane.setAlignment(Pos.CENTER);
//        flowPane.setPrefWrapLength(330);
        flowPane.getChildren().setAll(nodes);

        return flowPane;
    }
}
