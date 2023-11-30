package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsEfa;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class Location
{
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("coord")
    public double[] coordinates;

    @SerializedName("productClasses")
    public int[] productClasses;

    @SerializedName("properties")
    public LocationProperties properties;



    public String getId() {
        Log.d("MapActivity", id);
        return id;
    }

    public String getName() {
        Log.d("MapActivity", name);
        return name;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public int[] getProductClasses() {
        return productClasses;
    }

    public LocationProperties getProperties() {
        return properties;
    }
}
