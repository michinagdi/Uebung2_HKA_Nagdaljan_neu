package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Place {

    @SerializedName("bike_list")
    public List<BikeList> bikes;

    public int getBikesAmount() {
        return bikes.size();
    }

}
