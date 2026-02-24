package dev.java4now.util;

/* SPDX-License-Identifier: MIT */

import atlantafx.base.util.BBCodeHandler;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomBBCodeHandler<T extends Pane> extends BBCodeHandler.Default<T> {

    // Automatski podržava SVE Feather ikone – nikad više ručno dodavanje!
    private static final Set<String> SUPPORTED_ICONS = Arrays.stream(Feather.values())
            .map(feather -> feather.name().toLowerCase().replace("_", "-"))  // FTH_EYE_OFF → eye-off
            .collect(Collectors.toSet());

    private static final Set<String> SUPPORTED_ICONS_2 = Arrays.stream(CarbonIcons.values())
            .map(carbonIcons -> carbonIcons.name().toLowerCase().replace("_", "-"))  // FTH_EYE_OFF → eye-off
            .collect(Collectors.toSet());

    private static final Set<String> SUPPORTED_ICONS_3 = Arrays.stream(FontAwesome.values())
            .map(fontAwesome -> fontAwesome.name().toLowerCase().replace("_", "-"))  // FTH_EYE_OFF → eye-off
            .collect(Collectors.toSet());

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

            String normalized = iconName.toLowerCase().replace("-", "_");
            if (!SUPPORTED_ICONS.contains(normalized.replace("_", "-"))
                    && !SUPPORTED_ICONS_2.contains(normalized.replace("_", "-"))
                    && !SUPPORTED_ICONS_3.contains(normalized.replace("_", "-"))) {
                return null; // nepoznata ikona
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
//            System.out.println("icon: " + normalized);

            FontIcon icon;
            try{
                icon = new FontIcon(Feather.valueOf(normalized));
            }catch ( IllegalArgumentException e){
                try {
                    icon = new FontIcon(FontAwesome.valueOf(normalized));
                } catch (IllegalArgumentException ex) {
                    icon = new FontIcon(CarbonIcons.valueOf(normalized));
                }
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