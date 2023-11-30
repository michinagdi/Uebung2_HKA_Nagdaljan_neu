package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Country {

    @SerializedName("cities")
    public List<City> cities;

    @SerializedName("name")
    public String name;

    public List<City> getCities() {
        return cities;
    }

    public String getName() {
        return name;
    }



}
