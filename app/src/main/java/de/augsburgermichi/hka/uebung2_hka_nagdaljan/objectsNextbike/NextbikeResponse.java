package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NextbikeResponse {

    @SerializedName("countries")
    public List<Country> countries;

    public List<Country> getCountries() {
        return countries;

    }

}
