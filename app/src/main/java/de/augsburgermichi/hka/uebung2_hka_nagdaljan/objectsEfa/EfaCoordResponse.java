package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsEfa;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EfaCoordResponse
{
    @SerializedName("version")
    public String version;

    @SerializedName("locations")
    public List<Location> locations;

    public String getVersion() {
        return version;
    }

    public List<Location> getLocations() {

        Log.d("MapActivity", locations.toString());

        return locations;
    }
}
