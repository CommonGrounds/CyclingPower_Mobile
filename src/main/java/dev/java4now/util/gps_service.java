package dev.java4now.util;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import dev.java4now.System_Info;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static dev.java4now.System_Info.*;

// https://nominatim.org/release-docs/develop/api/Reverse/
// https://nominatim.openstreetmap.org/reverse?format=json&lat=44.91&lon=20.63
public class gps_service {

    /*
    {osm_id=773759195, place_rank=30, licence=Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright,
    boundingbox=[44.9101789, 44.9102249, 20.6310407, 20.6311064],
    address={country=Србија, country_code=rs, road=Јабучки пут, city=Панчево, ISO3166-2-lvl4=RS-VO, municipality=Град Панчево,
    county=Јужнобанатски управни округ, postcode=26000, suburb=МЗ Горњи град, ISO3166-2-lvl6=RS-04, house_number=293, state=Војводина},
    importance=0.00006201037613484424, lon=20.63107354740845, type=yes, display_name=293, Јабучки пут, МЗ Горњи град, Панчево, Град Панчево, Јужнобанатски управни округ,
     Војводина, 26000, Србија, osm_type=way, name=, addresstype=building, class=building, place_id=57971175, lat=44.91020185}
     */
//    static JsonArray address;

    public static void parseData(String codes) throws URISyntaxException, IOException, JsonException {

//      String jsonResponse = IOUtils.toString(new java.net.URI(codes), StandardCharsets.UTF_8); // important direktno kada ne trebaju parametri ( property )

        java.net.URL url = new java.net.URL(codes);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

        // 1. Postavi veoma specifičan User-Agent (izbegavaj opšta imena)
        connection.setRequestProperty("User-Agent", "CyclePower/1.2 (java4now@gmail.com ; https://github.com/CommonGrounds/CyclingPower_Mobile)");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int status = connection.getResponseCode();

        if (status != java.net.HttpURLConnection.HTTP_OK) {
            // 2. Detaljna dijagnostika za 403
            String errorMsg = "";
            try (java.io.InputStream es = connection.getErrorStream()) {
                if (es != null) {
                    errorMsg = org.apache.commons.io.IOUtils.toString(es, java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            System.err.println("HTTP ERROR: " + status);
            System.err.println("SERVER REASON: " + errorMsg);

            cityService.findByLatLong(lat, lon);    // zbog eventualne poruke 403 na nominatim serveru radim lokalni search

            // Ako je 403, baci precizniji info
            if (status == 403) {
                throw new IOException("Nominatim 403 Forbidden: Proveri User-Agent ili IP blokadu. Server kaže: " + errorMsg);
            }
            return;
        }

        String jsonResponse;
        try (java.io.InputStream is = connection.getInputStream()) {
            jsonResponse = org.apache.commons.io.IOUtils.toString(is, java.nio.charset.StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }

        JsonObject json = (JsonObject) Jsoner.deserialize(jsonResponse);
        JsonObject address = (JsonObject) json.get("address");

        if (address != null) {
            System_Info.city_country_text.set("City: ( " + address.get("city") + " ) State: ( " + address.get("country") + " )");
            System_Info.suburb_text.set("Suburb: " + address.get("suburb"));
            System_Info.road_house_number_text.set("Address: " + address.get("road") + " " + address.get("house_number"));
        }
    }
}
