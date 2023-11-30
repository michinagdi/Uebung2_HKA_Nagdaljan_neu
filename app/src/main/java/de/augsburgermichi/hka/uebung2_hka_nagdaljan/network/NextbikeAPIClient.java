package de.augsburgermichi.hka.uebung2_hka_nagdaljan.network;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NextbikeAPIClient
{
    private static NextbikeAPIClient instance;
    private Retrofit retrofit;

    public static NextbikeAPIClient getInstance() {
        if (instance == null) {
            instance = new NextbikeAPIClient();
        }

        return instance;
    }

    public NextbikeAPIClient() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.nextbike.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    public NextbikeAPI getClient() {
        return this.retrofit.create(NextbikeAPI.class);
    }

}
