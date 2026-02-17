package dev.java4now.View;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.IntegerStringConverter;
import dev.java4now.App;
import dev.java4now.System_Info;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.coreui.CoreUiBrands;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.Icon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;

import javafx.css.StyleableIntegerProperty;

import static dev.java4now.App.*;


public class SettingsPage {


    static Spinner<Integer> sp0;
    static Spinner<Integer> sp1;
    static CheckBox screen_chk;
//    static MyFontIcon icon;

    public static Pane top_screen(){

        var back_btn = new Button("Back");
        back_btn.getStyleClass().addAll(Styles.ROUNDED);
        back_btn.setOnAction(evt -> {
            modalPane.hide(true);
            System.gc();
        });

        var right_btn = new Button("Save");
        right_btn.getStyleClass().addAll(Styles.ROUNDED);
        right_btn.setOnAction(evt -> {
            if(!screen_chk.isSelected()){
                IS_SCREEN_ON = !System_Info.displayService.keepScreenOff(); // IMPORTANT - Custom display attach service - keep screen on/off implementacija za android
                screen_chk.setSelected(IS_SCREEN_ON);
                App.toggle_screen.setSelected(IS_SCREEN_ON);
            }else{
                IS_SCREEN_ON = System_Info.displayService.keepScreenOn();
                screen_chk.setSelected(IS_SCREEN_ON);
                App.toggle_screen.setSelected(IS_SCREEN_ON);
            }
            System_Info.save_all_settings(String.valueOf(!theme.isDarkMode()),String.valueOf(IS_SCREEN_ON),
                    sp1.getValue(),sp0.getValue());
            modalPane.hide(true);
        });

        var line = new Line();
        var t_header =new Pane(back_btn,right_btn,line){
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                back_btn.setLayoutX(10);
                back_btn.setLayoutY(10);
                right_btn.setLayoutX(getWidth() - right_btn.getWidth() - 10);
                right_btn.setLayoutY(10);
                line.setStartX(0);line.setEndX(getWidth());
                line.setStartY(getHeight());line.setEndY(getHeight());
            }
        };
        t_header.prefWidthProperty().bind(System_Info.display_width);
        t_header.prefHeightProperty().bind(System_Info.display_height.divide(10));
        if (theme.isDarkMode()) {
            t_header.getStyleClass().addAll("menu_bar_dark");
        }else{
            t_header.getStyleClass().addAll("menu_bar_light");
        }

        return t_header;
    }



//------------------------------------------------------
    public static VBox Bike_and_screen(){

        System_Info.retrieve_all_settings();

        var box = new VBox(10);

        var medSep = new javafx.scene.control.Separator(Orientation.HORIZONTAL);
        medSep.getStyleClass().add(Styles.MEDIUM);
        var smallSep = new javafx.scene.control.Separator(Orientation.HORIZONTAL);
        smallSep.getStyleClass().add(Styles.SMALL);

        TextFlow textFlowPane = new TextFlow();
        Text txt_user = new Text("Log Name:  ");
//        txt_user.setFont(new Font(15));
        Text txt_user_name = new Text();
//        txt_user_name.setFont(new Font(15));
//        txt_user_name.setFill(Color.GREEN);
        txt_user_name.getStyleClass().addAll(Styles.TEXT_BOLD,Styles.TEXT_UNDERLINED,Styles.TEXT,Styles.SUCCESS); // Mora ovako ako je atlantaFX
        txt_user_name.textProperty().bind(System_Info.user_name);
        ObservableList<Node> list = textFlowPane.getChildren();
        list.addAll(txt_user,txt_user_name);
        textFlowPane.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(textFlowPane,new Insets(0, 0, 20, 0));

        var lbl_theme = new Label("Theme: " + theme.getName());
        VBox.setMargin(lbl_theme,new Insets(0, 0, 20, 0));

        var lbl0 = new Label("Rider Weight ( Kg )");

        sp0 = new Spinner<Integer>(1, 200, System_Info.r_weight.get());
        IntegerStringConverter.createFor(sp0);
        sp0.getStyleClass().addAll(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        sp0.setPrefWidth(120);
        sp0.setPrefHeight(sp0.getPrefHeight() * 0.8);
        sp0.setEditable(true);
        sp0.getProperties().put("vkType", "numeric"); // ovo samo kada je fx virtual keyboard , ne za android soft keyboard
        VBox.setMargin(sp0,new Insets(0, 0, 20, 0));

        var lbl1 = new Label("Bike Weight ( Kg )");

        sp1 = new Spinner<Integer>(1, 30, System_Info.b_weight.get());
        IntegerStringConverter.createFor(sp1);
        sp1.getStyleClass().addAll(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        sp1.setPrefWidth(120);
        sp1.setPrefHeight(sp1.getPrefHeight() * 0.8);
        sp1.setEditable(true);
        sp1.getProperties().put("vkType", "numeric"); // ovo samo kada je fx virtual keyboard , ne za android soft keyboard

        screen_chk = new CheckBox("Keep Screen On");
        screen_chk.setSelected(IS_SCREEN_ON);
        VBox.setMargin(screen_chk,new Insets(10, 0, 70, 0));

//        icon = new MyFontIcon(MaterialDesignT.TOOLS);
//        icon.iconSizeProperty().bind(System_Info.display_height.divide(5));
//        var icon = new FontIcon(MaterialDesignT.TOOLS);
        var icon = new FontIcon(CoreUiBrands.GNU);
        icon.getStyleClass().add("batt_icon_2");

        box.getChildren().addAll( textFlowPane,lbl_theme , lbl0 , sp0, lbl1 , sp1 , screen_chk , icon);
        box.setAlignment(Pos.TOP_CENTER);

        return box;
    }




//-----------------------------------------------------------------------
    private static class MyFontIcon extends FontIcon implements Icon {

        private static final double EPSILON = 0.000001d;
        protected StyleableIntegerProperty iconSize;

        public MyFontIcon(){
            super();
            this.fontProperty().addListener((v, o, n) -> {
                int size = (int) n.getSize();
                if (size != this.getIconSize()) {
                    this.setIconSize(size);
                }
            });
        }

        public MyFontIcon(Ikon iconCode) {
            this();
            this.setIconCode(iconCode);
        }

        @Override
        public StyleableIntegerProperty iconSizeProperty() {
            if (iconSize == null) {
                iconSize = new StyleableIntegerProperty(8) {

                    @Override
                    public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                        return null;
                    }

                    @Override
                    public Object getBean() {
                        return MyFontIcon.this;
                    }

                    @Override
                    public String getName() {
                        return "iconSize";
                    }

                    @Override
                    public StyleOrigin getStyleOrigin() {
                        return StyleOrigin.USER_AGENT;
                    }

                };
                this.iconSize.addListener((v, o, n) -> {
                    Font font = MyFontIcon.this.getFont();
                    if (Math.abs(font.getSize() - n.doubleValue()) >= EPSILON) {
//                        System.out.println(n.doubleValue());
                        MyFontIcon.this.setFont(Font.font(font.getFamily(), n.doubleValue()));
//                        MyFontIcon.this.setStyle(normalizeStyle(getStyle(), "-fx-font-size", n.intValue() + "px"));
                    }else{
                        MyFontIcon.this.setFont(Font.font(font.getFamily(), 72));
                    }
                });
            }
            return this.iconSize;
        }
    }
}
/*
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.scene.text.Font;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

public class CustomFontIcon extends FontIcon {
    private static final double EPSILON = 1e-4;

    // Define CSS metadata for iconSize (same as original)
    private static final CssMetaData<CustomFontIcon, Number> CUSTOM_ICON_SIZE =
        new CssMetaData<>("-fx-icon-size",
                          StyleConverter.getSizeConverter(),
                          8) { // Default size matches original
        @Override
        public boolean isSettable(CustomFontIcon node) {
            return node.iconSize == null || !node.iconSize.isBound();
        }

        @Override
        public StyleableProperty<Number> getStyleableProperty(CustomFontIcon node) {
            return (StyleableProperty<Number>) node.iconSizeProperty();
        }
    };

    @Override
    public IntegerProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = new StyleableIntegerProperty(8) {
                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return CUSTOM_ICON_SIZE;
                }

                @Override
                public Object getBean() {
                    return CustomFontIcon.this;
                }

                @Override
                public String getName() {
                    return "iconSize";
                }

                @Override
                public StyleOrigin getStyleOrigin() {
                    return StyleOrigin.USER_AGENT;
                }
            };

            // Modified listener
            iconSize.addListener((v, o, n) -> {
                Font font = CustomFontIcon.this.getFont();
                double newSize = n.doubleValue();

                // Custom logic (e.g., scale the icon differently)
                if (Math.abs(font.getSize() - newSize) >= EPSILON) {
                    Font newFont = Font.font(font.getFamily(), newSize * 1.5); // 1.5x scaling
                    CustomFontIcon.this.setFont(newFont);
                    CustomFontIcon.this.setStyle(
                        normalizeStyle(getStyle(), "-fx-font-size", (int) (newSize * 1.5) + "px")
                    );
                }
            });
        }
        return iconSize;
    }

    public CustomFontIcon(Ikon icon) {
        super(icon);
    }
}
 */