package com.example.geofence;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener {

    private GoogleMap mMap;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private DatabaseReference myLocationReference;
    private GeoFire geoFire;
    private List<LatLng> dangerousArea;

    double latitude = -5.137718, longitude = 119.446707;

    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sdf = new SimpleDateFormat("HHmm", Locale.getDefault());

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this);

                        initArea();
                        settingGeoFire();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    }

    private void initArea() {
        dangerousArea = new ArrayList<>();
        dangerousArea.add(new LatLng(latitude, longitude));
    }

    private void settingGeoFire() {
        myLocationReference = FirebaseDatabase.getInstance().getReference("nyLocation");
        geoFire = new GeoFire(myLocationReference);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null) {

                    geoFire.setLocation("You", new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (currentUser != null) currentUser.remove();
                            currentUser = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()))
                                    .title("You"));

                            mMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(currentUser.getPosition(), 17.0f));

                        }
                    });


//                    //new in outside
//                    GeoFire geoTracking = new GeoFire(FirebaseDatabase.getInstance().getReference("nyLocation"));
//                    GeoQuery geoQuery = geoTracking.queryAtLocation(new GeoLocation(latitude, longitude), 0.05f);
//                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                        @Override
//                        public void onKeyEntered(String key, GeoLocation location) {
//                            Toast.makeText(MapsActivity.this, "Suksessss", Toast.LENGTH_SHORT).show();
//                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                                @Override
//                                public void onInfoWindowClick(Marker marker) {
//                                    Toast.makeText(MapsActivity.this, "Siapp Laksanakan", Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onKeyExited(String key) {
//
//                        }
//
//                        @Override
//                        public void onKeyMoved(String key, GeoLocation location) {
//
//                        }
//
//                        @Override
//                        public void onGeoQueryReady() {
//
//                        }
//
//                        @Override
//                        public void onGeoQueryError(DatabaseError error) {
//
//                        }
//                    });
                    //new in outside


                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && )
            for (LatLng latLng : dangerousArea) {
                mMap.addCircle(new CircleOptions().center(latLng)
                        .radius(20).strokeColor(Color.BLUE).fillColor(Color.BLACK).strokeWidth(5.0f));

                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.02f);
                geoQuery.addGeoQueryEventListener(MapsActivity.this);

            }
        }

    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    private void sendNotification(String title, String format) {
//        Toast.makeText(this, "OKee broo", Toast.LENGTH_LONG).show();

        String notif_channel = "irwan_ardyansah";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(notif_channel, "Okee", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notif_channel);
        builder.setContentTitle(title)
                .setContentText(format)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("Wilayah Absensi", String.format("Anda Telah Memasuki Wilayah Absensi", key));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String currentTime = sdf.format(new Date());
                sendInformation(Integer.parseInt(currentTime));
            }
        });
    }

    private void sendInformation(int currentTime){
        if (currentTime > 700 && currentTime<800){
            Toast.makeText(MapsActivity.this, "Absensi Pagi Selesai" +currentTime, Toast.LENGTH_LONG).show();
        }else if (currentTime > 1300 && currentTime<1400){
            Toast.makeText(MapsActivity.this, "Absensi Siang Selesai" +currentTime, Toast.LENGTH_LONG).show();
        }else if (currentTime > 1500 && currentTime<1600){
            Toast.makeText(MapsActivity.this, "Absensi Sore Selesai" +currentTime, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Waktu Absensi Masih Belum Tersedia", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onKeyExited(String key) {

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {

    }
}