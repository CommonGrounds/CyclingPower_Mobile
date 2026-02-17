package dev.java4now.local_json;

//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONObject;
import io.github.wycst.wast.json.JSON;
//import com.google.gson.Gson;
//import com.fasterxml.jackson.databind.ObjectMapper;
import dev.java4now.System_Info;
import dev.java4now.util.HaversineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CityService_json {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityService_json.class);

    Map<String, List<City_json>> cities = new ConcurrentHashMap<>();
    long start,end;

    public CityService_json(ArrayList<InputStream> streams) throws IOException {

        cities.clear();
        LOGGER.info("Starting to read the JSON file for cities.");
 /*
//---------- BLOCKING NACIN --------------------
        Scanner sc = null;
        start = System.nanoTime();
        for(InputStream stream : streams){
            sc = new Scanner(stream);

            while (sc.hasNextLine()) {

                // FASTJSON                                                          // 520 ms
//                JSONObject obj = JSON.parseObject(sc.nextLine());
//                City_json city = obj.toJavaObject(City_json.class);

                // GSON - ?                                                          // 877 ms
                // IMPORTANT - mora  - opens dev.java4now.entity to com.google.gson;    u modules-info ako ga imamo inace ne mora
//                Gson gson = new Gson();
//                City_json city = gson.fromJson(sc.nextLine(), City_json.class);

                //JAKSON                                                             // 2000 ms
//                ObjectMapper MAPPER = new ObjectMapper();
//                City_json city = MAPPER.readValue(sc.nextLine(), City_json.class);

                // WAST JSON                                                         // 400 ms
                City_json city = JSON.parseObject(sc.nextLine(), City_json.class);

                // Moshi - ?                                                         // 987 ms
                // IMPORTANT - mora  - opens dev.java4now.entity to com.squareup.moshi;   u modules-info ako ga imamo inace ne mora
//                Moshi moshi = new Moshi.Builder().build();
//                JsonAdapter<City_json> jsonAdapter = moshi.adapter(City_json.class);
//                City_json city = jsonAdapter.fromJson(sc.nextLine());

                addtoMap(city);
                LOGGER.debug("City read from the file " + city.toString());
                //        System.out.println(sc.nextLine());
            }
        }
        end = System.nanoTime();
        LOGGER.info("JSON Loading Time: %.3f ms".formatted((double)(end - start)/1000000));
 */

        start = System.nanoTime();
//########################## Async future non-blocking ##########################
        CompletableFuture.supplyAsync(() -> processStreams(streams), Executors.newSingleThreadExecutor())
                .thenAccept(result -> {
                    Platform.runLater(() -> {
                        end = System.nanoTime();
                        LOGGER.info("JSON Loading Time: %.3f ms".formatted((double)(end - start)/1000000));
                        // Update UI with the processed cities map if needed
                        LOGGER.info("Processing completed. Total cities: " + result.size());
                        // e.g., update a ListView or Label
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        LOGGER.debug("Processing failed: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    //------------------------------------------------------------------------
    public Map<String, List<City_json>> processStreams(List<InputStream> streams) {
        // Create a thread pool for parallel processing (e.g., one thread per stream)
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(streams.size(), 4)); // Limit to 4 threads to avoid overloading mobile device

        // List to hold all CompletableFutures
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Process each stream asynchronously
        for (InputStream stream : streams) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                processStream(stream);
            }, executor).exceptionally(throwable -> {
                LOGGER.debug("Error processing stream: " + throwable.getMessage());
                LOGGER.error("Failed to process stream", throwable);
                return null; // Return null to allow continuation
            });
            futures.add(future);
        }

        // Wait for all futures to complete and shut down the executor
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        return cities;
    }

    //------------------------------------------------------------------------
    private void processStream(InputStream stream) {
        Scanner sc = null;
//        Gson gson = new Gson();
//        ObjectMapper MAPPER = new ObjectMapper(); // jakson
        try {
            sc = new Scanner(stream);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                LOGGER.debug("Read line: " + line);
                try {
                    City_json city = JSON.parseObject(line, City_json.class);    // wast
//                JSONObject obj = JSON.parseObject(sc.nextLine());                               // fastjson 2
//                City_json city = obj.toJavaObject(City_json.class);                             // fastjson 2
//                City_json city = gson.fromJson(sc.nextLine(), City_json.class);                 // gson
//                    City_json city = MAPPER.readValue(sc.nextLine(), City_json.class);          // jackson
                    LOGGER.debug("Parsed city: " + city.toString());
                    addtoMap(city);
                    LOGGER.debug("City added to map: " + city.getCity());
                } catch (Exception e) {
                    LOGGER.error("Failed to parse JSON line: " + line, e);
                    throw e; // Rethrow to ensure it’s caught by CompletableFuture
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in stream processing", e);
            throw new RuntimeException("Stream processing failed", e);
        } finally {
            if (sc != null) sc.close();
        }
    }
    //####################### End of future non-blocking ########################


    public City_json findByLatLong(Double latitude, Double longitude) {
        return getFromMap(latitude, longitude);
    }

    /* Adds the given city to the hashmap with location based index*/
    private void addtoMap(City_json city) {

        String index = HaversineUtils.LOCINDEX(city.getLatitude(), city.getLongitude());
        LOGGER.debug("Index for (" + city.getLatitude() + ", " + city.getLongitude() + ") is " + index);

        List<City_json> sameIndexCities = (List<City_json>) cities.get(index);

        if (sameIndexCities == null)
            sameIndexCities = new ArrayList<City_json>();

        //Add the new city into the list of cities sharing the same index.
        sameIndexCities.add(city);
        //Add the list into the hashmap
        cities.put(index, sameIndexCities);
        LOGGER.debug("Added city to map: " + city.getCity());
    }


    /* Gets a city from the hashmap for given latitude and longitude */
    private City_json getFromMap(Double latitude, Double longitude) {
//    LOGGER.info("getFromMap");

        City_json result = null;
        start = System.nanoTime();

        String[] indexes = HaversineUtils.INDEXESAROUND(latitude, longitude);

        for (String index : indexes) {

            List<City_json> citiesForIndex = (List<City_json>) cities.get(index);
//      System.out.println("indexes: " + citiesForIndex);

            if (citiesForIndex != null) {
                for (City_json city : citiesForIndex) {
//          System.out.println("city: " + city.toString());

                    if (result == null)
                        result = city;
                    else {
                        float resultDistance = HaversineUtils.getDistance(latitude, longitude, result.getLatitude(), result.getLongitude());
                        LOGGER.debug("Distance for " + result.getCity() + " is " + resultDistance + "m");
                        float cityDistance = HaversineUtils.getDistance(latitude, longitude, city.getLatitude(), city.getLongitude());
                        LOGGER.debug("Distance for " + city.getCity() + " is " + cityDistance + "m");

                        if (cityDistance < resultDistance) // if this city is closer than earlier result
                            result = city;
                    }
                }
            }
        }

        LOGGER.debug("result: " + result);
        System_Info.city_country_text.set("City: ( " + result.getCity() + " ) State: ( " + result.getCountry() + " )" );
        String suburb = result.getVillage() == null ? " --- " : result.getVillage();
        String hamlet = result.getHamlet() == null ? "---" : result.getHamlet();
        System_Info.suburb_text.set("Suburb: " + suburb + "( " + hamlet + " )");
        System_Info.road_house_number_text.set("Distance: %.1f m".formatted(HaversineUtils.getDistance(latitude, longitude, result.getLatitude(), result.getLongitude())));
        end = System.nanoTime();
        LOGGER.info("Search Time: %.3f ms".formatted((double)(end - start)/1000000));
        return result;
    }
}