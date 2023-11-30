package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Place {

    @SerializedName("bike_list")
    public List<BikeList> bikes;

    @SerializedName("lat")
    public double lat;

    public String getName() {
        return name;
    }

    @SerializedName("name")
    public String name;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @SerializedName("lng")
    public double lng;

    public int getBikesAmount() {
        return bikes.size();
    }

}
