package de.augsburgermichi.hka.uebung2_hka_nagdaljan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import de.augsburgermichi.hka.uebung2_hka_nagdaljan.network.EfaAPIClient;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.network.NextbikeAPIClient;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsEfa.EfaCoordResponse;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike.City;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike.Country;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike.NextbikeResponse;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsNextbike.Place;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private MapView mapView;
    private Marker startMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        XYTileSource mapServer = new XYTileSource("MapName",
                8,
                20,
                256,
                ".png",
                new String[]{"https://tileserver.svprod01.app/styles/default/"}
        );

        String authorizationString = this.getMapServerAuthorizationString("ws2223@hka", "LeevwBfDi#2027");
        Configuration.getInstance().getAdditionalHttpRequestProperties().put("Authorization", authorizationString);

        mapView = this.findViewById(R.id.mapView);
        mapView.setTileSource(mapServer);

        GeoPoint startPoint = new GeoPoint(49.0069, 8.4037);
        IMapController mapController = mapView.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(startPoint);

        startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        //startMarker.setTextIcon("Du bist hier!");
        startMarker.setTitle("Du bist hier!");
        startMarker.setIcon(getResources().getDrawable(R.mipmap.gps_punkt, getTheme()));
        mapView.getOverlays().add(startMarker);

        this.mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION};

        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                initLocationListener();

                Log.d("MapActivity", "onGranted");
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);

                Log.d("MapActivity", "onDenied");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void initLocationListener()
    {

        LocationListener locationListener = location -> {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            GeoPoint startPoint = new GeoPoint(latitude, longitude);

            IMapController mapController = mapView.getController();
            mapController.setCenter(startPoint);
            startMarker.setPosition(startPoint);

        };

        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

    }

    private String getMapServerAuthorizationString(String username, String password)
    {
        String authorizationString = String.format("%s:%s", username, password);
        return "Basic " + Base64.encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    private void loadClosestStops(double latitude, double longitude) {
        Call<EfaCoordResponse> efaCall = EfaAPIClient
                .getInstance()
                .getClient()
                .loadStopsWithinRadius(
                        EfaAPIClient
                                .getInstance()
                                .createCoordinateString(
                                        latitude,
                                        longitude
                                ),
                        1000
                );

        efaCall.enqueue(new Callback<EfaCoordResponse>() {
            @Override
            public void onResponse(Call<EfaCoordResponse> call, Response<EfaCoordResponse> response) {
                Log.d("MapActivity", String.format("Response %d Locations", response.body().getLocations().size()));
                Log.d("MapActivity", String.valueOf(response.raw()));
            }

            @Override
            public void onFailure(Call<EfaCoordResponse> call, Throwable t) {
                Log.d("MapActivity", "Failure");
            }
        });
    }

    public void loadNextbikes(double latitude, double longitude) {

        Call<NextbikeResponse> nextbikeCall = NextbikeAPIClient
                .getInstance()
                .getClient()
                .loadNextbikesWithinRadius(Double.toString(latitude), Double.toString(longitude), Integer.toString(1000));



        nextbikeCall.enqueue(new Callback<NextbikeResponse>() {
            @Override
            public void onResponse(Call<NextbikeResponse> call, Response<NextbikeResponse> response) {

                int bikeAmount = 0;

                for (Country country : response.body().getCountries()) {
                    for (City city : country.getCities()) {
                        for (Place place : city.getPlaces()) {
                            bikeAmount = bikeAmount + place.getBikesAmount();
                            Marker bikeMarker = new Marker(mapView);
                            bikeMarker.setPosition(new GeoPoint(place.getLat(), place.getLng()));
                            bikeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                            bikeMarker.setTitle("Nextbike " + place.getName());
                            bikeMarker.setIcon(getResources().getDrawable(R.mipmap.bike, getTheme()));
                            mapView.getOverlays().add(bikeMarker);
                        }
                    }
                }

                Log.d("MapActivity", "Anzahl Nextbikes im Radius: " + bikeAmount);
                Log.d("MapActivity", String.valueOf(response.raw()));
            }

            @Override
            public void onFailure(Call<NextbikeResponse> call, Throwable t) {

            }
        });
    }



}