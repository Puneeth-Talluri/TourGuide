package com.puneeth.wallkingtours;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.SphericalUtil;
import com.puneeth.wallkingtours.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static HashMap<String, FenceData> fenceMap=new HashMap<>();
    private ArrayList<FenceData> allFences=new ArrayList<>();
    private static final float zoomLevel = 15f;

    private boolean hasAllPerms = false;

    private final int notificationPermission = 123;
    private final int locationPermission = 456;
    private final int backgroundLocationPermission = 789;

    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();

    private final ArrayList<LatLng> tourPath=new ArrayList<>();

    private Polyline llHistoryPolyline;
    private Polyline pathPolyline;
    private Marker carMarker;

    private final List<PatternItem> pattern = Collections.singletonList(new Dot());

    private Location currentLoc;

    private LatLng pLatLng;

    private Boolean geoFenceFlag=true;
    private Boolean addressFlag=true;

    private Boolean tourPathFlag=true;

    private Boolean travelPathFlag=true;

    private int permissionsCount=0;

    private static HashMap<String,Circle> circleHash = new HashMap<>();

    private long startTime;
    private static final long minSplashTime = 2000;

    private boolean keepon=true;
//    Checkbox addCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startTime = System.currentTimeMillis();
        int col= Color.parseColor("#FF024715");
        binding.getRoot().setBackgroundColor(col);
        FenceVolley.downloadFences(this);
        doNotificationPermissions();

        SplashScreen.installSplashScreen(this)
                .setKeepOnScreenCondition(
                        new SplashScreen.KeepOnScreenCondition() {
                            @Override
                            public boolean shouldKeepOnScreen() {
                                Log.d(TAG, "shouldKeepOnScreen: " + (System.currentTimeMillis() - startTime));
                                if(System.currentTimeMillis() - startTime >= minSplashTime)
                                {
                                    if(hasAllPerms==true)
                                    {
                                        keepon=false;
                                    }
                                }
                                return keepon || (System.currentTimeMillis() - startTime <= minSplashTime);
                            }
                        }
                );
        setContentView(binding.getRoot());



        pLatLng = new LatLng(0.0,0.0);

        binding.tourCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tourPathFlag = true;
                    createTourPathOnMap();
                    // perform action when checkbox is checked
                } else {
                    tourPathFlag = false;
                    removeTourPath();

                    // perform action when checkbox is unchecked
                }
            }
        });


        binding.travelCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    travelPathFlag = true;
                    // perform action when checkbox is checked
                } else {
                    travelPathFlag = false;

                    // perform action when checkbox is unchecked
                }
            }
        });

        binding.geoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    geoFenceFlag = true;

                    addFence();

                    // perform action when checkbox is checked
                } else {

                    geoFenceFlag = false;
                    removeAllFences();
                    // perform action when checkbox is unchecked
                }
            }
        });
        checkPermissionsAndInitialize();
    }


    private void checkPermissionsAndInitialize() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
            // At least one permission is not granted
            hasAllPerms = false;
            doNotificationPermissions();
        } else {
            // All permissions are granted
            hasAllPerms = true;
            initializeAfterPermissionGranted();
        }
    }

    private void initializeAfterPermissionGranted() {
        // Initialization code that should run only after all permissions are granted
        // For example, setting up map or starting geofence service

        if (!hasAllPerms) {
            showAlert();
        }
    }

    public void removeTourPath(){
        pathPolyline.remove();
    }

    public void showAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Permissions Denied");
        alertDialogBuilder.setMessage(
                "This application needs all three permissions in order for it to work.It will not function properly without it");
        alertDialogBuilder.setPositiveButton("ok", (arg0, arg1) ->
                finish());

            alertDialogBuilder.setIcon(R.drawable.logo2);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();
    }

    public void initMap() {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            Log.d(TAG, "initMap: Map is not empty");
            mapFragment.getMapAsync(this);
        }

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


    public void acceptFenceData(ArrayList<FenceData> fenceData, ArrayList<LatLng> path_list){
        allFences.addAll(fenceData);
        Log.d(TAG, "acceptFenceData: added "+allFences.size()+" Fences");
        tourPath.addAll(path_list);
        Log.d(TAG, "acceptPathData: added "+tourPath.size()+" Paths");
        initMap();
    }

//
    public void createTourPathOnMap() {
        if (mMap == null) {
            Log.d(TAG, "GoogleMap is not initialized.");
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        for(LatLng latLng : tourPath) {
            polylineOptions.add(latLng);
        }

            pathPolyline = mMap.addPolyline(polylineOptions);
            pathPolyline.setEndCap(new RoundCap());
            pathPolyline.setWidth(8);
            pathPolyline.setColor(Color.YELLOW);


    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: invoked");
        mMap = googleMap;


        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);


        if (checkAllPerms()) {
            setupLocationListener();
            makeFences();
            startGeoService();
            createTourPathOnMap();
        }

    }

    private boolean checkAllPerms() {
        if (hasAllPerms) {
            return true;
        } else {
            setupLocationListener();
            return false;
        }
    }

    //permissions--------------------------------------------------------
    public void doNotificationPermissions() { // 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.POST_NOTIFICATIONS}, notificationPermission);
            } else {
                permissionsCount++;
                doLocationPermissions();
            }
        }
    }

    public void doLocationPermissions() { // 2
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, locationPermission);
        } else {
            permissionsCount++;
            doBackgroundLocationPermissions();
        }
    }

    public void doBackgroundLocationPermissions() { // 3
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, backgroundLocationPermission);
        } else {
            permissionsCount++;
            hasAllPerms = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == notificationPermission) && (grantResults.length > 0) &&
                (permissions[0].equals(android.Manifest.permission.POST_NOTIFICATIONS)) &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            doLocationPermissions();
            permissionsCount++;
            return;
        }

        if ((requestCode == locationPermission) && (grantResults.length > 0) &&
                (permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            doBackgroundLocationPermissions();
            permissionsCount++;
            return;
        }

        if ((requestCode == backgroundLocationPermission) && (grantResults.length > 0) &&
                (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            hasAllPerms = true;
            permissionsCount++;
            initMap();

        }
    }

    //location listner----------------------------------
    private void setupLocationListener() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocListener(this);


        if (hasAllPerms && locationManager != null)
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
    }


    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLoc=location;
        if(addressFlag==true){
            isAddressChecked();
        }
        latLonHistory.add(latLng); // Add the LL to our location history

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            if(travelPathFlag==true){
                llHistoryPolyline = mMap.addPolyline(polylineOptions);
                llHistoryPolyline.setEndCap(new RoundCap());
                llHistoryPolyline.setWidth(10);
                llHistoryPolyline.setColor(Color.GREEN);
            }

            float r = getRadius();
            if (r > 0) {
                int i = getMarkerDirection(pLatLng,latLng);
                pLatLng = latLng;
                Bitmap icon;
                if(i==1){
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_up);
                } else if (i==2) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);

                }else if (i==3) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_down);

                } else{
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                }

                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                options.rotation(location.getBearing());
                options.anchor(0.5f,0.5f);

                if (carMarker != null) {
                    carMarker.remove();
                }

                carMarker = mMap.addMarker(options);

            }
        }
        Log.d(TAG, "updateLocation: " + mMap.getCameraPosition().zoom);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        sumIt();
    }

    public int getMarkerDirection(LatLng prevLocation, LatLng newLocation){

        double lonDiff = newLocation.longitude - prevLocation.longitude;
        double latDiff = newLocation.latitude - prevLocation.latitude;
        double angle = Math.atan2(lonDiff,latDiff); angle = Math.toDegrees(angle) + 90;
        System.out.println(angle);

        if(angle>=45 && angle<135){
            return 1;

        } else if (angle>=135 && angle<225) {
            return  2;
        }else if (angle>=225 && angle<315) {
            return  3;
        } else{
            return  4;
        }

    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        return 15f * z - 145f;
    }

    private void sumIt() {
        double sum = 0;
        LatLng last = latLonHistory.get(0);
        for (int i = 1; i < latLonHistory.size(); i++) {
            LatLng current = latLonHistory.get(i);
            sum += SphericalUtil.computeDistanceBetween(current, last);
            last = current;
        }
        Log.d(TAG, "sumIt: " + String.format("%.3f km", sum / 1000.0));

    }

    //make and add fences---------------------------------------------------
    private void makeFences() {
        Log.d(TAG, "makeFences: invoked");
        int i=0;
        for(FenceData fd:allFences)
        {
            FenceData temp=fd;
            Log.d(TAG, "makeFences: "+fd.id);
            fenceMap.put(temp.id, temp);
            LatLng ll = new LatLng(Double.parseDouble(temp.latitude), Double.parseDouble(temp.longitude));
            Log.d(TAG, "makeFences: about to complete");
            if(i==allFences.size()-1){
                Log.d(TAG, "makeFences: "+i+" fences added");
            }
            i++;
            addFence();
        }

    }

    private void addFence() {

        for(FenceData fd:allFences){
            String n=fd.id;
            LatLng m=new LatLng(Double.parseDouble(fd.latitude),Double.parseDouble(fd.longitude));
            double r=Double.parseDouble(fd.radius);
            int c=Color.parseColor(fd.fenceColor);


            int fill = ColorUtils.setAlphaComponent(c, 50);



            if(geoFenceFlag==true){
                CircleOptions circleOptions = new CircleOptions()
                        .center(m)
                        .radius(r)
                        .strokePattern(pattern)
                        .strokeColor(c)
                        .fillColor(fill);
                Circle circle = mMap.addCircle(circleOptions);
                circleHash.put(n, circle);
            }

        }

    }

    private void removeFence() {

        ArrayList<String> requestIdList = new ArrayList<>();


        for(FenceData fenceData: allFences){

            requestIdList.add(fenceData.getId());
            Circle circle = circleHash.remove(fenceData.getId());

            if (circle != null) {
                circle.remove();
            }

        }
      //  geofencingClient.removeGeofences(requestIdList);


//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                        Log.d(TAG, "onSuccess: "+requestId+" removed Successfully");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Failed to remove geofences
//                        Log.d(TAG, "onFailure:  Failed to remove GeoFence");
//                    }
//                });
//
//        for()
//        Circle circle = circleHash.remove(requestId);
//        if (circle != null) {
//            circle.remove();
//        }


    }

    private void removeAllFences() {
        Log.d(TAG, "removeAllFences: triggered");
        for (Circle circle : circleHash.values()) {
            circle.remove(); // Removes the circle from the map
        }
//        circleHash.clear(); // Clears the HashMap, removing all entries
    }

    //-----------geo coder------------------------------------------------------------------------------------
    private String getPlace(Location loc) {
        StringBuilder sb = new StringBuilder();

        if (loc != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addr = addresses.get(0).getAddressLine(0);
                    sb.append(String.format(Locale.getDefault(), "%s%n%nProvider: %s%n%n%.5f, %.5f", addr, loc.getProvider(), loc.getLatitude(), loc.getLongitude()));
                } else {
                    sb.append(getString(R.string.cannot_determine_location));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sb.append(getString(R.string.Location_not_available));
        }

        String temp=sb.toString();
        int index = temp.lastIndexOf("USA");

        // If "USA" is found and it is not at the beginning of the string
        if (index != -1) {
            // Extract the substring from the beginning up to the end of "USA"
            return temp.substring(0, index + "USA".length());
        } else {
            // Return null or an appropriate value if "USA" is not found
            return temp;
        }
    }

    public void isAddressChecked() {
        if (currentLoc != null) {

                String address = getPlace(currentLoc);
                binding.addressText.setText(address);

        }
    }

//---GEO SERVICE-----------------------------------------------------------
    private void startGeoService() {

        //starting service
        Intent intent = new Intent(this, GeoFenceService.class);
        intent.putExtra("FENCES", fenceMap);

        Log.d(TAG, "startService: START");
        ContextCompat.startForegroundService(this, intent);
        Log.d(TAG, "startService: END");
    }

    public static FenceData getFenceData(String fenceId) {
        return fenceMap.get(fenceId);
    }

    @Override
    protected void onResume() {
        super.onResume();



        binding.addCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addressFlag = true;
                    String address = getPlace(currentLoc);
                    binding.addressText.setText(address);
                   // setupLocationListener();
                    // perform action when checkbox is checked
                } else {
                    addressFlag = false;
                    binding.addressText.setText("");

                    // perform action when checkbox is unchecked
                }
            }
        });


    }
}