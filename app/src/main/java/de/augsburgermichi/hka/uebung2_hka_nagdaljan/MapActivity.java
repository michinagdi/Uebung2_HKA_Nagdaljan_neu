package de.augsburgermichi.hka.uebung2_hka_nagdaljan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.OptionalInt;

import de.augsburgermichi.hka.uebung2_hka_nagdaljan.network.EfaAPIClient;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.network.NextbikeAPIClient;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsEfa.EfaCoordResponse;
import de.augsburgermichi.hka.uebung2_hka_nagdaljan.objectsEfa.Location;
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
    private ArrayList<GeoPoint> bikesOnMap = new ArrayList<>();
    private ArrayList<GeoPoint> oepnvOnMap = new ArrayList<>();
    private HashMap<GeoPoint, Integer> stationsHashMap = new HashMap<>();
    IMapController mapController;
    private Button button_refresh;
    private Button button_mobScore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        button_refresh = this.findViewById(R.id.btn_refresh);
        button_mobScore = this.findViewById(R.id.btn_mobscore);



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
        mapController = mapView.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(startPoint);

        startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        startMarker.setTitle("Du bist hier!");
        startMarker.setIcon(getResources().getDrawable(R.mipmap.gps_punkt, getTheme()));
        mapView.getOverlays().add(startMarker);

        mapView.setMinZoomLevel(15.0);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        this.mapView.addMapListener(new MapListener() {

            int antiLagProtection = 0;
            @Override
            public boolean onScroll(ScrollEvent event) {

                if (antiLagProtection >= 40) {
                    loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                    loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                    antiLagProtection = 0;
                    return false;
                }
                antiLagProtection++;
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {

                if (antiLagProtection >= 40) {
                    loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                    loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                    antiLagProtection = 0;
                    return false;
                }
                antiLagProtection++;
                return false;
            }
        });



        button_refresh.setOnClickListener(view -> {
            loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
            loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
        });

        button_mobScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

                //Set title for AlertDialog
                builder.setTitle("DuckMove App");

                //Set body message of Dialog
                builder.setMessage("Dein Mobilitätsscore: " + calcMobilityScore(bikesOnMap, oepnvOnMap, stationsHashMap));

                // Is dismiss when touching outside?
                builder.setCancelable(false);

                //Positive Button and it onClicked event listener
                builder.setPositiveButton("Fertig", (dialogInterface, i) -> {

                });

                AlertDialog dialog = builder.create();
                dialog.show();
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

        loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
        loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
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

                for (Location location : response.body().getLocations()) {
                    double[] doubleCoord = location.getCoordinates();
                    GeoPoint oepnvPosition = new GeoPoint(doubleCoord[0], doubleCoord[1]);

                    if (!oepnvOnMap.contains(oepnvPosition)) {
                        oepnvOnMap.add(oepnvPosition);

                        OptionalInt highestProductTypeOI = Arrays.stream(location.getProductClasses()).min();
                        int highestProductType = highestProductTypeOI.getAsInt();
                        stationsHashMap.put(oepnvPosition, highestProductType);

                        Marker oepnvMarker = new Marker(mapView);
                        oepnvMarker.setPosition(oepnvPosition);
                        oepnvMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        oepnvMarker.setTitle("Haltestelle: " + location.getName() + "\n" + "Typ: " + highestProductType);

                        switch (highestProductType) {
                            case 0:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.db_logo, getTheme())); break;
                            case 1:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.sbahn_logo, getTheme())); break;
                            case 2:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.ubahn_logo, getTheme())); break;
                            case 3:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.stadtbahn_logo, getTheme())); break;
                            case 4:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.tram_logo, getTheme())); break;
                            case 5:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.bus_logo, getTheme())); break;
                            default:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.station, getTheme())); break;
                        }

                        mapView.getOverlays().add(oepnvMarker);

                        oepnvMarker.setOnMarkerClickListener((marker, mapView) -> {
                            marker.showInfoWindow();
                            mapController.zoomTo(17.0);
                            mapController.animateTo(marker.getPosition());
                            return false;
                        });
                    }
                }
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
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onResponse(Call<NextbikeResponse> call, Response<NextbikeResponse> response) {

                int bikeAmount = 0;

                for (Country country : response.body().getCountries()) {
                    for (City city : country.getCities()) {
                        for (Place place : city.getPlaces()) {
                            GeoPoint bikePostition = new GeoPoint(place.getLat(), place.getLng());
                            GeoPoint gpsPosition = new GeoPoint(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());

                            if (!bikesOnMap.contains(bikePostition)) {
                                bikesOnMap.add(bikePostition);
                                bikeAmount += place.getBikesAmount();

                                Marker bikeMarker = new Marker(mapView);
                                bikeMarker.setPosition(bikePostition);
                                bikeMarker.setTitle("Nextbike " + place.getName());
                                bikeMarker.setIcon(getResources().getDrawable(R.mipmap.bike_logo, getTheme()));

                                mapView.getOverlays().add(bikeMarker);
                            }
                        }
                    }
                }

                Log.d("MapActivity", "Anzahl Nextbikes im Radius: " + bikeAmount);
                Log.d("MapActivity", String.valueOf(response.raw()));
            }


            @Override
            public void onFailure(Call<NextbikeResponse> call, Throwable t) {
                Log.d("MapActivity", "Failure");
            }
        });

    }

    private double calculateDistance(GeoPoint point1, GeoPoint point2) {
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());

        // Haversine-Formel
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Erdradius in Metern (kann je nach Anwendung angepasst werden)
        double radius = 6371000;

        // Berechne die Entfernung
        return radius * c;
    }

    public int calcMobilityScore(ArrayList<GeoPoint> bikeLocations, ArrayList<GeoPoint> efaLocations, HashMap<GeoPoint, Integer> efaHashMap)  {

        double mobilityScore = 0d;
        double bikeScore = 0d;
        double efaScore = 0d;

        ArrayList<GeoPoint> bikeinRadius = new ArrayList<>();
        ArrayList<GeoPoint> efainRadius = new ArrayList<>();

        //min. 50m - max. 550m
        double maxLaufenDistance = 50.0d * (((double) MainActivity.getLaufenValue() / 10.0d) + 1.0d);

        for (GeoPoint bikeLocation : bikeLocations) {
            if (calculateDistance(bikeLocation, startMarker.getPosition()) < maxLaufenDistance) {
                bikeinRadius.add(bikeLocation);
            }
        }
        for (GeoPoint efaLocation : efaLocations) {
            if (calculateDistance(efaLocation, startMarker.getPosition()) < maxLaufenDistance) {
                efainRadius.add(efaLocation);
            }
        }

        if (MainActivity.isNextbikeBoolean()) {
            bikeScore = (bikeinRadius.size() * 10d) * 1.5d;
        } else {
            bikeScore = (bikeinRadius.size() * 10d);
        }

        for (GeoPoint station : efainRadius) {
            switch (efaHashMap.get(station)) {
                case 0: efaScore += 60d; break;
                case 1: efaScore += 30d; break;
                case 2:
                case 3:
                    efaScore += 25d; break;
                case 4: efaScore += 15d; break;
                case 5: efaScore += 10d; break;
            }
        }

        if (MainActivity.isOpnvBoolean()) {
            efaScore = efaScore * 1.5d;
        }

        mobilityScore = efaScore + bikeScore;

        return (int) mobilityScore;
    }



}