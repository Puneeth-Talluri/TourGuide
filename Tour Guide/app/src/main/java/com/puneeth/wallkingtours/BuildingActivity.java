package com.puneeth.wallkingtours;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import com.puneeth.wallkingtours.databinding.ActivityBuildingBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class BuildingActivity extends AppCompatActivity {

    private static final String TAG = "BuildingActivity";
    private Intent intent;
    ActivityBuildingBinding binding;
    FenceData fence;

    private String buildingName;
    private String buildingAddress;
    private String buildingDes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this activity
        binding = ActivityBuildingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the content view to the root of the binding

        // Now you can safely access the toolbar through the binding object
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle("Hello");

        intent = getIntent();
        if (intent != null) {
            String id = intent.getStringExtra("FENCE");
            fence = MapsActivity.getFenceData(id);
            buildingName=fence.getId();
            buildingAddress=fence.getAddress();
            buildingDes=fence.getDescription();
            Log.d(TAG, "onCreate: " + fence.address);
            binding.buildingName.setText(buildingName);
            Log.d(TAG, "onCreate: ");
            binding.buildingAddress.setText(buildingAddress);
            binding.buildingDes.setText(buildingDes);
            binding.buildingDes.setMovementMethod(new ScrollingMovementMethod());

            long start = System.currentTimeMillis();
            Picasso.get().load(fence.image)
                    .into(binding.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            long time = System.currentTimeMillis() - start;
                            Log.d(TAG, "onSuccess: " + time);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d(TAG, "onError: " + e);
                        }
                    });

        }
    }
}