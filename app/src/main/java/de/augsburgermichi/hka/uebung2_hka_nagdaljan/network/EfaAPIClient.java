package de.augsburgermichi.hka.uebung2_hka_nagdaljan.network;

import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EfaAPIClient
{
    private static EfaAPIClient instance;

    private Retrofit retrofit;

    public static EfaAPIClient getInstance() {
        if (instance == null) {
            instance = new EfaAPIClient();
        }

        return instance;
    }

    public EfaAPIClient() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://www.bwegt.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public String createCoordinateString(double latitude, double longitude) {
        return String.format(Locale.ENGLISH, "%f:%f:WGS84[dd.ddddd]", longitude, latitude);
    }

    public EfaAPI getClient() {
        return this.retrofit.create(EfaAPI.class);
    }
}
