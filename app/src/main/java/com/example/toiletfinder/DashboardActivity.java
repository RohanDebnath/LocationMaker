package com.example.toiletfinder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap googleMap;
    private DatabaseReference databaseReference;
    private HashMap<String, Marker> markerHashMap = new HashMap<>();
    private HashMap<String, MarkerData> markerDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("markers");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable the display of the user's location on the map
            enableMyLocation();
            moveCameraToCurrentLocation();
        } else {
            // Request the location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showMarkerInputDialog(latLng);
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String markerId = (String) marker.getTag();
                if (markerId != null) {
                    showMarkerOptionsDialog(markerId);
                }
            }
        });

        // Load existing markers from Firebase database
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                MarkerData markerData = dataSnapshot.getValue(MarkerData.class);
                if (markerData != null) {
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(markerData.getLatitude(), markerData.getLongitude()))
                            .title(markerData.getTitle())
                            .snippet(markerData.getDetails()));
                    marker.setTag(markerData.getMarkerId());

                    // Store the MarkerData object in the HashMap
                    markerHashMap.put(markerData.getMarkerId(), marker);
                    markerDataMap.put(markerData.getMarkerId(), markerData);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                MarkerData markerData = dataSnapshot.getValue(MarkerData.class);
                if (markerData != null) {
                    String markerId = dataSnapshot.getKey();
                    Marker marker = markerHashMap.get(markerId);
                    if (marker != null) {
                        marker.setTitle(markerData.getTitle());
                        marker.setSnippet(markerData.getDetails());
                        marker.showInfoWindow();

                        // Update the MarkerData object in the HashMap
                        markerDataMap.put(markerId, markerData);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String markerId = dataSnapshot.getKey();
                Marker marker = markerHashMap.get(markerId);
                if (marker != null) {
                    marker.remove();
                    markerHashMap.remove(markerId);

                    // Remove the MarkerData object from the HashMap
                    markerDataMap.remove(markerId);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle the case when a child marker is moved
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the case when the database operation is cancelled
            }
        });
    }

    private void moveCameraToCurrentLocation() {
        // Get the user's current location and move the camera to that location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        }
                    }
                });
    }

    private void requestLocationPermission() {
        // Check if the device is running Android 6.0 (Marshmallow) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request the location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the location permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable the display of the user's location on the map
                enableMyLocation();
                moveCameraToCurrentLocation();
            } else {
                // Permission denied, show a toast message or handle the scenario accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showMarkerInputDialog(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Marker");
        View view = getLayoutInflater().inflate(R.layout.dialog_marker_input, null);
        builder.setView(view);

        final EditText titleEditText = view.findViewById(R.id.titleEditText);
        final EditText detailsEditText = view.findViewById(R.id.detailsEditText);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = titleEditText.getText().toString().trim();
                String details = detailsEditText.getText().toString().trim();

                // Generate a unique marker ID
                String markerId = databaseReference.push().getKey();
                if (markerId != null) {
                    MarkerData markerData = new MarkerData(markerId, latLng.latitude, latLng.longitude, title, details);
                    databaseReference.child(markerId).setValue(markerData);

                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(title)
                            .snippet(details));
                    marker.setTag(markerId);

                    // Store the MarkerData object in the HashMap
                    markerHashMap.put(markerId, marker);
                    markerDataMap.put(markerId, markerData);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showMarkerOptionsDialog(final String markerId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setTitle("Marker Options");
        builder.setItems(new CharSequence[]{"Edit Details", "Delete Marker"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Edit Details
                        showEditMarkerDialog(markerId);
                        break;
                    case 1: // Delete Marker
                        deleteMarker(markerId);
                        break;
                }
            }
        });
        builder.show();
    }

    private void showEditMarkerDialog(final String markerId) {
        // Retrieve the MarkerData object from the HashMap using the markerId
        final MarkerData markerData = markerDataMap.get(markerId);
        if (markerData == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setTitle("Edit Marker Details");
        View view = getLayoutInflater().inflate(R.layout.dialog_marker_input, null);
        builder.setView(view);

        final EditText titleEditText = view.findViewById(R.id.titleEditText);
        final EditText detailsEditText = view.findViewById(R.id.detailsEditText);

        // Set the current marker details in the EditText fields
        titleEditText.setText(markerData.getTitle());
        detailsEditText.setText(markerData.getDetails());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String title = titleEditText.getText().toString().trim();
                String details = detailsEditText.getText().toString().trim();

                // Update the marker details in the Firebase database
                markerData.setTitle(title);
                markerData.setDetails(details);
                databaseReference.child(markerId).setValue(markerData);

                // Update the marker details on the map
                Marker marker = markerHashMap.get(markerId);
                if (marker != null) {
                    marker.setTitle(title);
                    marker.setSnippet(details);
                    marker.showInfoWindow();

                    // Update the MarkerData object in the HashMap
                    markerDataMap.put(markerId, markerData);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void deleteMarker(String markerId) {
        // Remove the marker from the Firebase database
        databaseReference.child(markerId).removeValue();

        // Remove the marker from the map
        Marker marker = markerHashMap.get(markerId);
        if (marker != null) {
            marker.remove();
            markerHashMap.remove(markerId);

            // Remove the MarkerData object from the HashMap
            markerDataMap.remove(markerId);
        }
    }
    private void enableMyLocation() {
        // Check if the GoogleMap object is available
        if (googleMap != null) {
            try {
                // Enable the display of the user's location on the map
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}

