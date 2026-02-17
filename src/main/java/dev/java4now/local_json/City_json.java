package dev.java4now.local_json;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

//@JsonIgnoreProperties(value = {"name"})
public class City_json {

//    private String country_code;
    private String name;
    private Map<String, String> address;
    private Map<String, String> other_names;
    private String display_name;
    private String osm_type;
    private Long osm_id;
    private String type;
    private List<Double> bbox;
    private Double[] location;
    private Long population;
//    private String state;
//    private String village;
//    private String hamlet;

// IMPORTANT - kada konvertujemo json to object , object package ( putanju ) treba dodati u modules-info - exports dev.java4now.entity;
// IMPORTANT - treba obavezno postaviti getters and setters za postavlanje variable koja mora da ima json key word naziv ( name , country_code etc )


    public Map<String, String> getAddress() {
        return address;
    }

    // public void setAddress(Map<String, String> address) {
    //   this.address = address;
//  }
    public Double[] getLocation() {
        return location;
    }

    public String getCountry_code() {
        return address.get("country_code");
    }

    public String getCity() {
        String str = address.get("city");
        return str == null ? this.name : str;
    }

    public String getCountry() {
        return address.get("country");
    }

    public Double getLatitude() {
        return location[1];
    }

    public Double getLongitude() {
        return location[0];
    }

    public Long getPopulation() {
        return population;
    }

    public String getState() {
        return address.get("state");
    }

    public String getVillage() {
        String str = address.get("village");
        return str == null ? this.name : str;
    }

    public String getHamlet() {
        return address.get("hamlet");
    }

    @Override
    public String toString() {
        return "City [country_code=" + getCountry_code() + ", city=" + getCity() + ", country=" + getCountry() + ", latitude=" + getLatitude() + ", longitude=" + getLongitude() +
                ", population=" + population + ", village: " + getVillage() + ", hamlet: " + getHamlet() + "]";
    }


    public Map<String, String> getOther_names() {
        return other_names;
    }

    public void setOther_names(Map<String, String> str) {
        this.other_names = str;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getOsm_type() {
        return osm_type;
    }

    public void setOsm_type(String osm_type) {
        this.osm_type = osm_type;
    }

    public Long getOsm_id() {
        return osm_id;
    }

    public void setOsm_id(Long osm_id) {
        this.osm_id = osm_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getBbox() {
        return bbox;
    }

    public void setBbox(List<Double> bbox) {
        this.bbox = bbox;
    }

    public void setAddress(Map<String, String> address) {
        this.address = address;
    }

    public void setLocation(Double[] location) {
        this.location = location;
    }

    public void setPopulation(Long population) {
        this.population = population;            // IMPORTANT - Kada je direktno obj. ( ne array or map ) onda iz json-a po imenu
    }

    public void setName(String name) {
        this.name = name;
    }
}
