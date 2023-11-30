package de.augsburgermichi.hka.uebung2_hka_nagdaljan.network;

import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike.NextbikeResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NextbikeAPI {

    @GET("maps/nextbike-live.json")
    Call<NextbikeResponse> loadNextbikesWithinRadius(@Query("lat") String latitude, @Query("lng") String longitude, @Query("distance") String radius);
}
