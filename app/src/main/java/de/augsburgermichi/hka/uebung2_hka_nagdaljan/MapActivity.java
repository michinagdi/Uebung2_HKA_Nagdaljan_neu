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


    /**
     * Diese Methode wird aufgerufen, wenn die MapActivity erstellt wird.
     * Hier werden Layout-Ressourcen initialisiert, Views referenziert
     * und die Kartenansicht konfiguriert. Es werden auch Tiles von einem
     * Tile-Server geladen und ein Startmarker auf der Karte platziert.
     * Ein MapListener wird hinzugefügt, um Aktionen bei Scrollen und Zoomen
     * auf der Karte auszulösen, wodurch die Funktionen 'loadClosestStops' und 'loadNextbikes' aufgerufen werden.
     * Zwei Buttons ({@link MapActivity#button_refresh} und {@link MapActivity#button_mobScore}) werden konfiguriert, um Aktionen auszulösen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialisiere Views
        button_refresh = this.findViewById(R.id.btn_refresh);
        button_mobScore = this.findViewById(R.id.btn_mobscore);


        // Konfiguriere den Kartenansicht
        XYTileSource mapServer = new XYTileSource("MapName",
                8,
                20,
                256,
                ".png",
                new String[]{"https://tileserver.svprod01.app/styles/default/"}
        );

        // Setze Autorisierungs-Header für den Tile-Server
        String authorizationString = this.getMapServerAuthorizationString("ws2223@hka", "LeevwBfDi#2027");
        Configuration.getInstance().getAdditionalHttpRequestProperties().put("Authorization", authorizationString);

        // Initialisiere die MapView und setze den TileSource
        mapView = this.findViewById(R.id.mapView);
        mapView.setTileSource(mapServer);

        // Setze die Startposition und Zoomstufe der Karte
        GeoPoint startPoint = new GeoPoint(49.0069, 8.4037);
        mapController = mapView.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(startPoint);

        // Füge einen Startmarker zur Karte hinzu
        startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        startMarker.setTitle("Du bist hier!");
        startMarker.setIcon(getResources().getDrawable(R.mipmap.gps_punkt, getTheme()));
        mapView.getOverlays().add(startMarker);

        // Konfiguriere weitere Einstellungen für die MapView
        mapView.setMinZoomLevel(15.0);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        // Füge einen MapListener hinzu, um Aktionen bei Scrollen und Zoomen auszulösen
        this.mapView.addMapListener(new MapListener() {

            int antiLagProtection = 0;

            // Führe Aktionen bei Scrollen aus, z.B., lade nahegelegene Haltestellen und Nextbikes
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

            // Führe Aktionen bei Zoomen aus, z.B., lade nahegelegene Haltestellen und Nextbikes
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

        // Setze Click-Listener für den "btn_refresh" Button
        button_refresh.setOnClickListener(view -> {
            loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
            loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
        });

        // Setze Click-Listener für den "btn_mobscore" Button
        button_mobScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Erzeuge einen AlertDialog mit Mobilitätsscore und Fertig-Button
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

                builder.setTitle("DuckMove App");
                builder.setMessage("Dein Mobilitätsscore: " + calcMobilityScore(bikesOnMap, oepnvOnMap, stationsHashMap));
                builder.setCancelable(false);
                builder.setPositiveButton("Fertig", (dialogInterface, i) -> {

                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }


    /**
     * Diese Methode wird aufgerufen, wenn die MapActivity wieder in den Vordergrund kommt.
     * Hier werden Berechtigungen für den Zugriff auf den genauen Standort überprüft.
     * Wenn die Berechtigungen erteilt wurden, wird der Standort-Listener initialisiert.
     * Andernfalls wird auf die Verweigerung reagiert und eine Log-Nachricht ausgegeben.
     * Zusätzlich werden nahegelegene Haltestellen und Nextbikes geladen.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Definiere die benötigten Berechtigungen
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        // Definiere die benötigten Berechtigungen
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {

                // Initialisiere den Standort-Listener, wenn Berechtigungen gewährt wurden
                initLocationListener();

                // Behandle die Verweigerung von Berechtigungen und protokolliere dies
                Log.d("MapActivity", "onGranted");
            }

            // Behandle die Verweigerung von Berechtigungen und protokolliere dies
            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);

                Log.d("MapActivity", "onDenied");
            }
        });

        // Lade nahegelegene Haltestellen und Nextbikes basierend auf der aktuellen Kartenansicht
        loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
        loadNextbikes(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
    }


    /**
     * Initialisiert einen Standort-Listener für die MapActivity.
     * Der Listener wird aufgerufen, wenn sich der Standort ändert.
     * Die Methode verwendet GPS-Provider, um Standortaktualisierungen zu erhalten.
     * Die Aktualisierungsrate beträgt 2000 Millisekunden (2 Sekunden) und der Mindestabstand
     * für Aktualisierungen beträgt 10 Meter.
     *
     * @SuppressLint("MissingPermission") wird verwendet, um statische Code-Analyse-Warnungen zu unterdrücken,
     * da die Methode als "MissingPermission" markiert ist.
     */
    @SuppressLint("MissingPermission")
    private void initLocationListener() {

        // Definiere einen Standort-Listener
        LocationListener locationListener = location -> {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Definiere einen Standort-Listener
            GeoPoint startPoint = new GeoPoint(latitude, longitude);

            // Erhalte die MapController-Instanz und setze den neuen Standort als Zentrum
            IMapController mapController = mapView.getController();
            mapController.setCenter(startPoint);

            // Aktualisiere die Position des Startmarkers auf der Karte
            startMarker.setPosition(startPoint);

        };

        // Erhalte die LocationManager-Instanz
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Fordere Standortaktualisierungen vom GPS-Provider an
        // mit einer Aktualisierungsrate von 2000 Millisekunden und einem Mindestabstand von 10 Metern
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

    }

    /**
     * Generiert eine Autorisierungszeichenfolge für den Zugriff auf den Karten-Server.
     * Die Methode erstellt einen Benutzername-Passwort-String im Format "username:password"
     * und codiert ihn mit Base64, um die Autorisierungszeichenfolge im Basic-Authentifizierungsformat zu erhalten.
     *
     * @param username Der Benutzername für die Authentifizierung.
     * @param password Das Passwort für die Authentifizierung.
     * @return Die generierte Autorisierungszeichenfolge im Basic-Authentifizierungsformat.
     */
    private String getMapServerAuthorizationString(String username, String password) {
        String authorizationString = String.format("%s:%s", username, password);
        return "Basic " + Base64.encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }


    /**
     * Lädt nahegelegene Haltestellen basierend auf den gegebenen Koordinaten (Breitengrad, Längengrad).
     * Die Methode verwendet die EfaAPI, um Haltestellen innerhalb eines Radius von 1000 Metern abzurufen.
     * Bei erfolgreicher Antwort werden die Haltestellen auf der Karte platziert, wobei bereits vorhandene Haltestellen
     * nicht erneut hinzugefügt werden. Jede Haltestelle wird mit einem Marker dargestellt, der den Haltestellentyp anzeigt.
     * Marker-Icons werden entsprechend des höchsten Produkttyps der Haltestelle ausgewählt.
     * Ein Klick auf einen Marker zeigt Informationen zur Haltestelle an und zentriert die Karte auf die Haltestelle.
     *
     * @param latitude  Der Breitengrad der aktuellen Position.
     * @param longitude Der Längengrad der aktuellen Position.
     */
    private void loadClosestStops(double latitude, double longitude) {
        // Erzeuge einen API-Aufruf, um Haltestellen im gegebenen Radius zu laden
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

        // Führe den API-Aufruf aus
        efaCall.enqueue(new Callback<EfaCoordResponse>() {

            // Bei erfolgreicher Antwort
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onResponse(Call<EfaCoordResponse> call, Response<EfaCoordResponse> response) {
                Log.d("MapActivity", String.format("Response %d Locations", response.body().getLocations().size()));
                Log.d("MapActivity", String.valueOf(response.raw()));

                for (Location location : response.body().getLocations()) {
                    // Iteriere über die geladenen Haltestellen
                    double[] doubleCoord = location.getCoordinates();
                    GeoPoint oepnvPosition = new GeoPoint(doubleCoord[0], doubleCoord[1]);

                    // Überprüfe, ob die Haltestelle bereits auf der Karte vorhanden ist
                    if (!oepnvOnMap.contains(oepnvPosition)) {
                        // Falls nicht, füge die Haltestelle hinzu
                        oepnvOnMap.add(oepnvPosition);

                        // Ermittle den höchsten Produkttyp der Haltestelle
                        OptionalInt highestProductTypeOI = Arrays.stream(location.getProductClasses()).min();
                        int highestProductType = highestProductTypeOI.getAsInt();
                        stationsHashMap.put(oepnvPosition, highestProductType);

                        // Erzeuge einen Marker für die Haltestelle
                        Marker oepnvMarker = new Marker(mapView);
                        oepnvMarker.setPosition(oepnvPosition);
                        oepnvMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        oepnvMarker.setTitle("Haltestelle: " + location.getName() + "\n" + "Typ: " + highestProductType);

                        // Wähle das Marker-Icon basierend auf dem höchsten Produkttyp aus
                        switch (highestProductType) {
                            case 0:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.db_logo, getTheme()));
                                break;
                            case 1:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.sbahn_logo, getTheme()));
                                break;
                            case 2:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.ubahn_logo, getTheme()));
                                break;
                            case 3:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.stadtbahn_logo, getTheme()));
                                break;
                            case 4:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.tram_logo, getTheme()));
                                break;
                            case 5:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.bus_logo, getTheme()));
                                break;
                            default:
                                oepnvMarker.setIcon(getResources().getDrawable(R.mipmap.station, getTheme()));
                                break;
                        }

                        // Füge den Marker zur MapView hinzu
                        mapView.getOverlays().add(oepnvMarker);

                        // Setze einen Klick-Listener für den Marker
                        oepnvMarker.setOnMarkerClickListener((marker, mapView) -> {
                            marker.showInfoWindow();
                            mapController.zoomTo(17.0);
                            mapController.animateTo(marker.getPosition());
                            return false;
                        });
                    }
                }
            }

            // Bei Fehlschlag des API-Aufrufs
            @Override
            public void onFailure(Call<EfaCoordResponse> call, Throwable t) {
                Log.d("MapActivity", "Failure");
            }
        });
    }

    /**
     * Lädt Nextbikes basierend auf den gegebenen Koordinaten (Breitengrad, Längengrad).
     * Die Methode verwendet den NextbikeAPI-Aufruf, um Nextbikes innerhalb eines Radius von 1000 Metern abzurufen.
     * Bei erfolgreicher Antwort werden die Nextbikes auf der Karte platziert, wobei bereits vorhandene Nextbikes
     * nicht erneut hinzugefügt werden. Jedes Nextbike wird mit einem Marker dargestellt, der den Standort anzeigt.
     * Ein Klick auf einen Marker zeigt den Standortnamen an.
     *
     * @param latitude  Der Breitengrad der aktuellen Position.
     * @param longitude Der Längengrad der aktuellen Position.
     */
    public void loadNextbikes(double latitude, double longitude) {
        // Erzeuge einen API-Aufruf, um Nextbikes im gegebenen Radius zu laden
        Call<NextbikeResponse> nextbikeCall = NextbikeAPIClient
                .getInstance()
                .getClient()
                .loadNextbikesWithinRadius(Double.toString(latitude), Double.toString(longitude), Integer.toString(1000));


        // Führe den API-Aufruf aus
        nextbikeCall.enqueue(new Callback<NextbikeResponse>() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onResponse(Call<NextbikeResponse> call, Response<NextbikeResponse> response) {
                // Bei erfolgreicher Antwort
                int bikeAmount = 0;

                for (Country country : response.body().getCountries()) {
                    // Iteriere über die geladenen Nextbikes
                    for (City city : country.getCities()) {
                        for (Place place : city.getPlaces()) {
                            GeoPoint bikePostition = new GeoPoint(place.getLat(), place.getLng());
                            GeoPoint gpsPosition = new GeoPoint(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());

                            // Überprüfe, ob das Nextbike bereits auf der Karte vorhanden ist
                            if (!bikesOnMap.contains(bikePostition)) {
                                // Falls nicht, füge das Nextbike hinzu
                                bikesOnMap.add(bikePostition);
                                bikeAmount += place.getBikesAmount();

                                // Erzeuge einen Marker für das Nextbike
                                Marker bikeMarker = new Marker(mapView);
                                bikeMarker.setPosition(bikePostition);
                                bikeMarker.setTitle("Nextbike " + place.getName());
                                bikeMarker.setIcon(getResources().getDrawable(R.mipmap.bike_logo, getTheme()));

                                // Füge den Marker zur MapView hinzu
                                mapView.getOverlays().add(bikeMarker);
                            }
                        }
                    }
                }
            }


            // Bei Fehlschlag des API-Aufrufs
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


    /**
     * Berechnet den Mobilitätsscore basierend auf den gegebenen Positionen von Nextbikes, ÖPNV-Haltestellen
     * und einem Startpunkt. Der Score wird unter Berücksichtigung der Laufdistanz zum Startpunkt, der Anzahl
     * der Nextbikes und der ÖPNV-Haltestellen sowie ihrer Typen berechnet. Der Mobilitätsscore wird auf der
     * Grundlage von konfigurierbaren persönlichen Faktoren gewichtet und summiert.
     *
     * @param bikeLocations Die Positionen der Nextbikes.
     * @param efaLocations  Die Positionen der ÖPNV-Haltestellen.
     * @param efaHashMap    Eine Zuordnung der ÖPNV-Haltestellen zu ihren Typen.
     * @return Der berechnete Mobilitätsscore als Ganzzahl.
     */
    public int calcMobilityScore(ArrayList<GeoPoint> bikeLocations, ArrayList<GeoPoint> efaLocations, HashMap<GeoPoint, Integer> efaHashMap) {

        double mobilityScore;
        double bikeScore;
        double efaScore = 0d;

        ArrayList<GeoPoint> bikeinRadius = new ArrayList<>();
        ArrayList<GeoPoint> efainRadius = new ArrayList<>();

        // Definiere die Laufdistanz basierend auf der Nutzereinstellung
        //min. 50m - max. 550m
        double maxLaufenDistance = 50.0d * (((double) MainActivity.getLaufenValue() / 10.0d) + 1.0d);

        // Filtere Nextbikes und ÖPNV-Haltestellen innerhalb der Laufdistanz
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

        // Berechne den Bike-Score basierend auf der Anzahl der Nextbikes im Radius
        if (MainActivity.isNextbikeBoolean()) {
            bikeScore = (bikeinRadius.size() * 10d) * 1.5d;
        } else {
            bikeScore = (bikeinRadius.size() * 10d);
        }

        // Berechne den ÖPNV-Score basierend auf der Anzahl und dem Typ der ÖPNV-Haltestellen im Radius
        for (GeoPoint station : efainRadius) {
            switch (efaHashMap.get(station)) {
                case 0:
                    efaScore += 60d;
                    break;
                case 1:
                    efaScore += 30d;
                    break;
                case 2:
                case 3:
                    efaScore += 25d;
                    break;
                case 4:
                    efaScore += 15d;
                    break;
                case 5:
                    efaScore += 10d;
                    break;
            }
        }

        // Gewichte den ÖPNV-Score bei Bedarf aufgrund der Nutzereinstellung
        if (MainActivity.isOpnvBoolean()) {
            efaScore = efaScore * 1.5d;
        }

        // Summiere die berechneten Scores zum Gesamtmobilitätsscore
        mobilityScore = efaScore + bikeScore;

        // Konvertiere den Mobilitätsscore in eine Ganzzahl und gib ihn zurück
        return (int) mobilityScore;
    }


}