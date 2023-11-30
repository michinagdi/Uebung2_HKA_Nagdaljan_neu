package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NextbikeResponse {

    @SerializedName("countries")
    public List<Country> countries;

    @SerializedName("name")
    public String name;

    public List<Country> getCountries() {
        return countries;

    }

    public String getName() {
        return name;
    }




}
