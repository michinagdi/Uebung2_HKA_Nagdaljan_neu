package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class City {

    @SerializedName("places")
    public List<Place> places;

    public List<Place> getPlaces() {
        return places;
    }

    public int getPlaceAmount() {
        return places.size();
    }
}
