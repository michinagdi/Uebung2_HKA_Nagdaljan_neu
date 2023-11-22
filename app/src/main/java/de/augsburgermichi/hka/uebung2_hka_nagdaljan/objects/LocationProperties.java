package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objects;

import com.google.gson.annotations.SerializedName;

public class LocationProperties {

    @SerializedName("distance")
    public double distance;

    public double getDistance() {
        return distance;
    }
}
