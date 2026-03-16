package dev.java4now.util;

/* SPDX-License-Identifier: MIT */

import atlantafx.base.util.BBCodeHandler;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.boxicons.BoxiconsSolid;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CustomBBCodeHandler<T extends Pane> extends BBCodeHandler.Default<T> {

    private static final Map<String, Supplier<FontIcon>> ICON_SUPPLIERS = new HashMap<>();

    static {
        // Feather
        for (Feather f : Feather.values()) {
            String key = f.name();//.toLowerCase().replace("_", "-");
            ICON_SUPPLIERS.put(key, () -> new FontIcon(f));
        }
        // Carbon
        for (CarbonIcons c : CarbonIcons.values()) {
            String key = c.name();//.toLowerCase().replace("_", "-");
            ICON_SUPPLIERS.put(key, () -> new FontIcon(c));
        }
        // FontAwesome
        for (FontAwesome fa : FontAwesome.values()) {
            String key = fa.name();//.toLowerCase().replace("_", "-");
            ICON_SUPPLIERS.put(key, () -> new FontIcon(fa));
        }
        // BoxiconsSolid
        for (BoxiconsSolid b : BoxiconsSolid.values()) {
            String key = b.name();//.toLowerCase().replace("_", "-");
            ICON_SUPPLIERS.put(key, () -> new FontIcon(b));
        }
        // Ako dodaš još jednu biblioteku → samo dodaj novi for petlju
    }

    public CustomBBCodeHandler(T root) {
        super(root);
    }

    @Override
    protected @Nullable Tag createTag(String name, @Nullable Map<String, String> params) {
        if ("icon".equals(name)) {
            String iconName = params != null ? params.getOrDefault("icon", "") : "";
            if (iconName.isBlank()) {
                return null; // ignorisi [icon] bez parametra
            }

            // Koristimo običan Tag, ali sa posebnim imenom i parametrima
            // Možemo dodati i "size" parametar kasnije ako hoćemo
            return new Tag("icon", Tag.Type.SELF_CLOSE, params, Set.of("icon"), Set.of());
        }

        // Ostali tagovi – default ponašanje
        return super.createTag(name, params);
    }



    @Override
    protected void appendSelfCloseTag(Tag tag) {
        if ("icon".equals(tag.name())) {
            String iconName = tag.getParam("icon");
            if (iconName == null || iconName.isBlank()) {
                return;
            }

            String normalized = iconName.toUpperCase().replace("-", "_");
//            String normalized = iconName.toLowerCase().replace("-", "_");
//            System.out.println("icon: " + normalized);

            Supplier<FontIcon> supplier = ICON_SUPPLIERS.get(normalized);

            FontIcon icon = (supplier != null) ? supplier.get() : null; // ili default ikona
    //        System.out.println("icon: " + icon);

            if (icon == null) {
                // fallback, log, ili default ikona npr. question-mark
                icon = new FontIcon(Feather.HELP_CIRCLE); // ili šta god
            }

            icon.setOnMouseClicked(e -> System.out.println("clicked: " + iconName ));
//            System.out.println(icon.getIconCode());

            int size = parseIntOrDefault(tag.getParam("size"), 18);
//            System.out.println("size: " + size);
//            icon.setIconSize(size);
//            icon.setStyle("-fx-icon-size: " + size + "px;");
            icon.getStyleClass().addAll("bb-icon"/*, "icon-" + iconName*/);

            // Nasleđivanje bold, italic, underline, font-size...
            icon.getStyleClass().addAll(String.valueOf(getStyleClass()));

            // KLJUČNO: Izvuci samo -fx-fill iz roditeljskog stila
            String parentStyle = getStyle();
            String fillStyle = null;
            if (parentStyle != null && parentStyle.contains("-fx-fill:")) {
                // Izvuci deo posle -fx-fill:
                int start = parentStyle.indexOf("-fx-fill:") + 9;
                int end = parentStyle.indexOf(";", start);
                if (end == -1) end = parentStyle.length();
                fillStyle = "-fx-icon-color: " + parentStyle.substring(start, end).trim();
            }

            // Postavi samo -fx-fill (ili fallback na crnu/belu)
//            System.out.println("1: " + icon.getStyle());
            String all_styles = icon.getStyle();
            icon.setStyle(all_styles + (fillStyle != null ? fillStyle + ";": "-fx-icon-color: -fx-fill;") + " -fx-icon-size: " + size + "px;");
//            System.out.println(fillStyle);
//            System.out.println("2: " + icon.getStyle());

            // VAŽNO: Dodaj ikonu u odgovarajući branch umesto direktno u root
            if (!openBlocks.isEmpty()) {
                // Ako smo unutar bloka (npr. [center]), dodaj u taj block
                openBlocks.getFirst().addText(icon);
            } else {
                // Ako nismo unutar bloka, dodaj u root preko TextFlow-a
                TextFlow textFlow = new TextFlow(icon);
                textFlow.getStyleClass().addAll(getStyleClass());
                textFlow.setStyle(getStyle());
                appendTextToRoot(textFlow);
            }
            return;
        }

        super.appendSelfCloseTag(tag);
    }
}