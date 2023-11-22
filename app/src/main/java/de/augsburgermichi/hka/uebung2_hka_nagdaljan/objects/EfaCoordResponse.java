package de.augsburgermichi.hka.uebung2_hka_nagdaljan.objects;

import android.location.Location;

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
        return locations;
    }
}
