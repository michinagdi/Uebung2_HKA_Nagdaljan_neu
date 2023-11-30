package de.augsburgermichi.hka.uebung2_hka_nagdaljan.network;

import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsEfa.EfaCoordResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EfaAPI
{
    @GET("bwegt-efa/XML_COORD_REQUEST?commonMacro=coord&outputFormat=rapidJSON&coordOutputFormat=WGS84[dd.ddddd]&type_1=STOP&inclFilter=1")
    Call<EfaCoordResponse> loadStopsWithinRadius(@Query("coord") String coordinateString, @Query("radius_1") int radius);
}
