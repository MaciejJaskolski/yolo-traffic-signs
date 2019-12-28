package com.example.yolov2_traffic_signs;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.yolov2_traffic_signs.models.TrafficSigns;
import com.example.yolov2_traffic_signs.services.GetTrafficSigns;
import com.example.yolov2_traffic_signs.utils.SignIcon;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    private Button switchToCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        switchToCamera = findViewById(R.id.btn_switch_view_to_camera);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        listenToButton();
    }

    private void listenToButton() {
        switchToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(cameraIntent);
            }
        });
    }

    private void placeMarkersOnMap(List<TrafficSigns> trafficSigns) {
        for(TrafficSigns sign : trafficSigns) {
            LatLng pos = new LatLng(sign.latitude, sign.longitude);
            mMap.addMarker(new MarkerOptions().position(pos).icon(new SignIcon().getIcon(this, sign.id)));
        }
    }

    private List<TrafficSigns> signs;

    public Task<List<TrafficSigns>> synchronizeData() {
        return Tasks.<Void>forResult(null).continueWithTask(new GetTrafficSigns()).addOnSuccessListener(new OnSuccessListener<List<TrafficSigns>>() {
            @Override
            public void onSuccess(List<TrafficSigns> trafficSigns) {

            }
        })
        .addOnCompleteListener(new OnCompleteListener<List<TrafficSigns>>() {
            @Override
            public void onComplete(@NonNull Task<List<TrafficSigns>> task) {
                if (task.isComplete() && task.isSuccessful()) {
                    signs = task.getResult();
                    placeMarkersOnMap(signs);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        Intent intent = getIntent();
        double[] location = intent.getDoubleArrayExtra("currentLocation");
        assert location != null;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location[0], location[1]), 19f ));

        synchronizeData();
        if (signs != null) {
            placeMarkersOnMap(signs);
        }
    }
}
