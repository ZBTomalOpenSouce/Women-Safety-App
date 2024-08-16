package com.jannat.womensafety;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.jannat.womensafety.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    //set view binding
    public ActivityMainBinding binding;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpOnClickListeners();
    }

    private void setUpOnClickListeners() {

        binding.SetEmergencyContact.setOnClickListener(v -> {
            setEmergencyContact();
        });

        binding.ShareLocation.setOnClickListener(v -> {
            SetUpShareLocation();
        });

        binding.SOS.setOnClickListener(v -> {
            setUpSOS();
        });
    }


    private void setUpSOS() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 102);
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:999"));

        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(MainActivity.this, "Permission denied to make a call", Toast.LENGTH_SHORT).show();
        }

    }

    private void SetUpShareLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS}, 101);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        String locationMessage = "I need help. My location is: http://maps.google.com/maps?q=" + latitude + "," + longitude;

                        SharedPreferences sharedPreferences = getSharedPreferences("WomenSafetyApp", MODE_PRIVATE);
                        String emergencyContact = sharedPreferences.getString("emergency_contact", "");

                        if (!emergencyContact.isEmpty()) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(emergencyContact, null, locationMessage, null, null);
                            Toast.makeText(MainActivity.this, "Location sent to " + emergencyContact, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Emergency contact not set", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void setEmergencyContact() {

        // Create a BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);

        // Inflate a new view instance
        View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                R.layout.bottom_sheet_contact, null);

        EditText editTextPhone = bottomSheetView.findViewById(R.id.editTextPhone);
        MaterialCardView buttonSaveContact = bottomSheetView.findViewById(R.id.btnSaveContact);

        // Load existing contact if available
        SharedPreferences sharedPreferences = getSharedPreferences("WomenSafetyApp", MODE_PRIVATE);
        String savedContact = sharedPreferences.getString("emergency_contact", "");
        editTextPhone.setText(savedContact);

        // Set button click listener
        buttonSaveContact.setOnClickListener(v -> {
            String contact = editTextPhone.getText().toString().trim();

            if (!contact.isEmpty()) {
                // Save the contact
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("emergency_contact", contact);
                editor.apply();
                bottomSheetDialog.dismiss();
            } else {
                editTextPhone.setError("Please enter a valid phone number");
            }

            Toast.makeText(MainActivity.this, "Contact saved successfully", Toast.LENGTH_SHORT).show();
        });

        // Set the content view of the BottomSheetDialog
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();


    }
}