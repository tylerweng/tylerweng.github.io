package areality.game;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyPlacesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String LANDMARK_ID = "com.example.areality.MESSAGE";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private JSONObject[] landmarks;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_places);
        ButterKnife.bind(this);

        Log.d("MyPlacesActivity", "here in onCreate");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

    }

    @OnClick(R.id.profileButton) void switchToProfile() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        retrieveLandmarks();
        setMarkers();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    private void retrieveLandmarks() {
        Log.d("MyPlacesActivity", "here in retrieveLandmarks");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        int landmarkCount = pref.getInt("landmarks_size", 0);
        landmarks = new JSONObject[landmarkCount];

        for (int i = 0; i < landmarkCount; i++) {
            try {
                landmarks[i] = new JSONObject();
                landmarks[i].put("id", pref.getString("landmark_" + i + "_id", null));
                landmarks[i].put("name", pref.getString("landmark_" + i + "_name", null));
                landmarks[i].put("lat", pref.getString("landmark_" + i + "_lat", null));
                landmarks[i].put("lon", pref.getString("landmark_" + i + "_lon", null));
            } catch (JSONException e) {
                Log.e("ProfileActivity", "JSON error: ", e);
            }
        }

        // ["landmarks_size", "landmark_0_id", "landmark_0_lat", "landmark_0_lon", "landmark_0_name"]
    }

    private void setMarkers() {

        for (int i = 0; i < landmarks.length; i++) {
            String id = "";
            String name = "";
            LatLng latLng = new LatLng(0, 0);

            try {
                id = landmarks[i].getString("id");
                name = landmarks[i].getString("name");
                Double lat = landmarks[i].getDouble("lat");
                Double lon = landmarks[i].getDouble("lon");
                latLng = new LatLng(lat, lon);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("MyPlacesActivity", "latLng object: " + latLng);

            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(name)
                    .snippet(id));

            if (i == landmarks.length - 1) mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getApplicationContext(), LandmarkPage.class);
                String landmarkId = marker.getSnippet();
                intent.putExtra(LANDMARK_ID, landmarkId);
                startActivity(intent);
            }
        });

    }

    private LatLng getLatLng(JSONObject jsonObject) throws JSONException {
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
        Double lat = location.getDouble("lat");
        Double lng = location.getDouble("lng");
        return new LatLng(lat, lng);
    }

    private String getDetailUrl(String placeId) {

        StringBuilder googlePlacesDetailUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googlePlacesDetailUrl.append("placeid=" + placeId);
        googlePlacesDetailUrl.append("&key=" + "AIzaSyB3IPyQhFjPS0kysn8Fh9xxhZ2SN12ek1Y");
        return (googlePlacesDetailUrl.toString());
    }

    protected String makeHTTPRequest(String urlString) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpURLConnection connection;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String content = "", line;
            while ((line = rd.readLine()) != null) {
                content += line + "\n";
            }
            return content;
        } catch (IOException e) {
            Log.d("error", e.toString());
            return e.toString();
        }
    }

}
