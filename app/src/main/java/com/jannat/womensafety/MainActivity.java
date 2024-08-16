package com.jannat.womensafety;

import android.Manifest;
import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpOnClickListeners();
    }

    private void setUpOnClickListeners() {

        binding.SetEmergencyContact.setOnClickListener(v -> setEmergencyContact());

        binding.ShareLocation.setOnClickListener(v -> SetUpShareLocation());

        binding.SOS.setOnClickListener(v -> setUpSOS());
    }

    private void setEmergencyContact() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);

        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                R.layout.bottom_sheet_contact, null);

        EditText editTextPhone = bottomSheetView.findViewById(R.id.editTextPhone);
        MaterialCardView buttonSaveContact = bottomSheetView.findViewById(R.id.btnSaveContact);

        SharedPreferences sharedPreferences = getSharedPreferences("WomenSafetyApp", MODE_PRIVATE);
        String savedContact = sharedPreferences.getString("emergency_contact", "");
        editTextPhone.setText(savedContact);

        buttonSaveContact.setOnClickListener(v -> {
            String contact = editTextPhone.getText().toString().trim();

            if (!contact.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("emergency_contact", contact);
                editor.apply();
                bottomSheetDialog.dismiss();
            } else {
                editTextPhone.setError("Please enter a valid phone number");
            }

            Toast.makeText(MainActivity.this, "Contact saved successfully", Toast.LENGTH_SHORT).show();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();


    }

    private void SetUpShareLocation() {

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

    private void setUpSOS() {

        // For Android 13 and above, notify the user that they might need to confirm the call manually

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 102);
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:999")); // Replace "999" with the emergency number

        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(MainActivity.this, "Permission denied to make a call", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpSOS();
            } else {
                Toast.makeText(this, "Permission denied. Cannot make SOS call.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}