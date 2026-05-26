package dev.java4now.local;

import dev.java4now.System_Info;
import dev.java4now.util.HaversineUtils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class CityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityService.class);
    long start,end;

    private Map<String, List<City>> cities = new HashMap<>();
    private volatile boolean isLoading = false;
    private volatile boolean isLoaded = false;
    static public final DoubleProperty data_progress = new SimpleDoubleProperty(0.0);
    public static final BooleanProperty loading_done = new SimpleBooleanProperty(false);

    public CityService() {
        // Prazan konstruktor
    }

    // Asinhrono učitavanje sa callback-om
    public void loadCitiesAsync(InputStream stream, Consumer<Boolean> onComplete, Consumer<Exception> onError) {
        if (isLoading) {
            LOGGER.warn("Učitavanje je već u toku!");
            return;
        }

        Task<Boolean> loadTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return loadCitiesSync(stream);
            }
        };

        loadTask.setOnSucceeded(event -> {
            isLoaded = true;
            loading_done.set(true);
            if (onComplete != null) {
                onComplete.accept(loadTask.getValue());
            }
        });

        loadTask.setOnFailed(event -> {
            if (onError != null) {
                onError.accept((Exception) loadTask.getException());
            }
        });

        new Thread(loadTask).start();
    }

    // Sinhrona metoda za učitavanje (koristi se interno)
    private boolean loadCitiesSync(InputStream stream) {
        /*
        List<String> lines = new BufferedReader(new InputStreamReader(stream))
                .lines()
                .toList();

        long total = lines.size(); // sada znam total ali onda radimo for ( String line : lines )
         */
        long total = 144836;       // total ako znam unapred

        isLoading = true;
        cities.clear();
        LOGGER.info("Load Time: Starting to read the text file for cities.");

        long start = System.currentTimeMillis();
        long lastUpdate = 0;
        int counter = 0;

        try (Scanner sc = new Scanner(stream)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.trim().isEmpty()) continue;

                City city = new City();
                String[] str = line.split("\t");

                if (str.length < 9) continue; // Provera da li ima dovoljno elemenata

                city.setCity(str[1]);
                city.setAsciiname(str[2]);
                city.setLatitude(Double.valueOf(str[4]));
                city.setLongitude(Double.valueOf(str[5]));
                city.setCountrycode(str[8]);
                addtoMap(city);

                counter++;
                LOGGER.debug("City read from the file " + city.toString());
                // Ažuriraj progress na svakih ~1% (1448 redova)
                if (counter - lastUpdate >= 1448) {
                    lastUpdate = counter;
                    final double progress = (double) counter / total;
                    // Ovo se izvršava na JavaFX thread-u, ali samo ~100 puta umesto 144.836
                    javafx.application.Platform.runLater(() -> {
                        data_progress.set(progress);
                    });
                }
//                if(counter % 10 == 0 ) LOGGER.info("out"); // usporenje radi debug-a
            }
        } catch (Exception e) {
            LOGGER.error("Greška pri učitavanju fajla", e);
            isLoading = false;
            return false;
        }

        long end = System.currentTimeMillis();
        LOGGER.info("Load Time: {} ms", (end - start));
        isLoading = false;
        return true;
    }

    public City findByLatLong(Double latitude, Double longitude) {
        return getFromMap(latitude, longitude);
    }

    /* Adds the given city to the hashmap with location based index*/
    private void addtoMap(City city) {

        String index = HaversineUtils.LOCINDEX(city.getLatitude(), city.getLongitude());
        LOGGER.debug("Index for (" + city.getLatitude() + ", " + city.getLongitude() + ") is " + index);

        List<City> sameIndexCities = (List<City>) cities.get(index);

        if (sameIndexCities == null)
            sameIndexCities = new ArrayList<City>();

        //Add the new city into the list of cities sharing the same index.
        sameIndexCities.add(city);
        //Add the list into the hashmap
        cities.put(index, sameIndexCities);
    }


    /* Gets a city from the hashmap for given latitude and longitude */
    private City getFromMap(Double latitude, Double longitude) {
//    LOGGER.info("getFromMap");

        City result = null;
        start = System.nanoTime();

        String[] indexes = HaversineUtils.INDEXESAROUND(latitude, longitude);

        for (String index : indexes) {

            List<City> citiesForIndex = (List<City>) cities.get(index);
//      System.out.println("indexes: " + citiesForIndex);

            if (citiesForIndex != null) {
                for (City city : citiesForIndex) {
//          System.out.println("city: " + city.toString());

                    if (result == null)
                        result = city;
                    else {
                        float resultDistance = HaversineUtils.getDistance(latitude, longitude, result.getLatitude(), result.getLongitude());
                        LOGGER.debug("Distance for " + result.getCity() + " is " + resultDistance + "m");
                        float cityDistance = HaversineUtils.getDistance(latitude, longitude, city.getLatitude(), city.getLongitude());
                        LOGGER.debug("Distance for " + city.getCity() + " is " + cityDistance + "m");

                        if (cityDistance < resultDistance) //if this city is closer than earlier result
                            result = city;
                    }
                }
            }
        }

        LOGGER.debug("result: " + result);
        System_Info.city_country_text.set("City: ( " + result.getCity() + " ) State: ( " + result.getCountrycode() + " )" );
        System_Info.suburb_text.set("Suburb: " + result.getAsciiname());
        System_Info.road_house_number_text.set("Distance: %.1f m".formatted(HaversineUtils.getDistance(latitude, longitude, result.getLatitude(), result.getLongitude())));
        end = System.nanoTime();
        LOGGER.info("Search Time: %.3f ms".formatted((double)(end - start)/1000000));

        return result;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}