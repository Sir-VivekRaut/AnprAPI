package com.example.anprapi;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.*;

import java.util.Locale;

public class DisplayActivity extends AppCompatActivity {

    private TextView regNo, owner, chassis, engine, vehicleClass, regAuthority,
            vehicleManufacturerName, manufactureDate, model, vehicleColour,
            fuelType, normsType, regDate, fitUpto, puccUpto,
            vehicleInsuranceCompanyName, vehicleInsuranceUpto;

    String vehicleNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);


        regNo = findViewById(R.id.regNo);
        owner = findViewById(R.id.owner);
        chassis = findViewById(R.id.chassis);
        engine = findViewById(R.id.engine);
        vehicleClass = findViewById(R.id.vehicleClass);
        regAuthority = findViewById(R.id.regAuthority);
        vehicleManufacturerName = findViewById(R.id.vehicleManufacturerName);
        manufactureDate = findViewById(R.id.manufactureDate);
        model = findViewById(R.id.model);
        vehicleColour = findViewById(R.id.vehicleColour);
        fuelType = findViewById(R.id.fuelType);
        normsType = findViewById(R.id.normsType);
        regDate = findViewById(R.id.regDate);
        fitUpto = findViewById(R.id.fitUpto);
        puccUpto = findViewById(R.id.puccUpto);
        vehicleInsuranceCompanyName = findViewById(R.id.vehicleInsuranceCompanyName);
        vehicleInsuranceUpto = findViewById(R.id.vehicleInsuranceUpto);

        // Get the vehicle number (plateValue) passed from MainActivity
        Intent intent = getIntent();
        vehicleNumber = intent.getStringExtra("vehicleNumber");
        fetchVehicleDetails(vehicleNumber);

    }


    private void fetchVehicleDetails(String vehicleNumber) {

        String secretKey = "bIZZsS4mcT10EWRmnR8nu0ymjaSduqnkGKs1TB1ZqS6SzP2nIgYSRCrPAA4FPFYwA";
        String clientId = "67bb8033cb15130769f1db21fc220eac:f6689e1a0a820d48c2196931c602689f";


        AndroidNetworking.post("https://api.invincibleocean.com/invincible/vehicleRcV7")
                .addHeaders("clientId", clientId)
                .addHeaders("secretKey", secretKey)
                .addHeaders("Content-Type", "application/json")
                .addBodyParameter("vehicleNumber", vehicleNumber)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());

                        try {
                            // Check if 'result' key is present in the JSON response
                            if (response.has("result")) {
                                // Extract the 'result' object from the response
                                JSONObject objResult = response.getJSONObject("result");

                                // Check if 'data' key is present in the 'result' object
                                if (objResult.has("data")) {
                                    // Extract the 'data' object from 'result'
                                    JSONObject objData = objResult.getJSONObject("data");

                                    // Get the 'Car' object safely
                                    JSONObject objCar = objData.optJSONObject("Car");

                                    // Extract data from the JSON response
                                    String regNoText = objData.optString("Vehicle_Num", "N/A");
                                    String ownerText = objData.optString("Owner_Name", "N/A");
                                    String chassisText = objData.optString("Chasis_No", "N/A");
                                    String engineText = objData.optString("Engine_No", "N/A");
                                    String vehicleClassText = objData.optString("Vehicle_Class", "N/A");
                                    String regAuthorityText = objData.optString("Rto", "N/A");
                                    String vehicleManufacturerNameText = objData.optString("Make", "N/A");
                                    String manufactureDateText = objData.optString("Manu_Date", "N/A");
                                    String modelText = (objCar != null) ? objCar.optString("ModelName", "N/A") : "N/A";
                                    String vehicleColourText = objData.optString("Color", "N/A");
                                    String fuelTypeText = objData.optString("Fuel_Type", "N/A");
                                    String normsTypeText = objData.optString("Norms_Desc", "N/A");
                                    String regDateText = objData.optString("Regist_Date", "N/A");
                                    String fitUptoText = objData.optString("Fit_Upto", "N/A");
                                    String puccUptoText = objData.optString("Puc_Expiry", "N/A");
                                    String vehicleInsuranceCompanyNameText = objData.optString("Previous_Insurer", "N/A");
                                    String vehicleInsuranceUptoText = objData.optString("Insurance_Upto", "N/A");

                                    updateUI(regNoText, ownerText, chassisText, engineText, vehicleClassText, regAuthorityText, vehicleManufacturerNameText, manufactureDateText, modelText, vehicleColourText, fuelTypeText, normsTypeText, regDateText, fitUptoText, puccUptoText, vehicleInsuranceCompanyNameText, vehicleInsuranceUptoText);
                                } else {
                                    Log.e("JSON Error", "'data' key not found in 'result'");
                                }
                            } else {
                                Log.e("JSON Error", "'result' key not found in response");
                            }
                        } catch (JSONException e) {
                            Log.e("JSON Error", "Error parsing JSON", e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("APIError", "Error fetching details", anError);
                    }
                });
    }

    private void updateUI(String regNo, String owner, String chassis, String engine, String vehicleClass,
                          String regAuthority, String vehicleManufacturerName, String manufactureDate,
                          String model, String vehicleColour, String fuelType, String normsType,
                          String regDate, String fitUpto, String puccUpto,
                          String vehicleInsuranceCompanyName, String vehicleInsuranceUpto) {

        // Update TextViews with the data
        this.regNo.setText(regNo);
        this.owner.setText(owner);
        this.chassis.setText(chassis);
        this.engine.setText(engine);
        this.vehicleClass.setText(vehicleClass);
        this.regAuthority.setText(regAuthority);
        this.vehicleManufacturerName.setText(vehicleManufacturerName);
        this.manufactureDate.setText(manufactureDate);
        this.model.setText(model);
        this.vehicleColour.setText(vehicleColour);
        this.fuelType.setText(fuelType);
        this.normsType.setText(normsType);
        this.regDate.setText(regDate);
        this.fitUpto.setText(fitUpto);
        this.puccUpto.setText(puccUpto);
        this.vehicleInsuranceCompanyName.setText(vehicleInsuranceCompanyName);
        this.vehicleInsuranceUpto.setText(vehicleInsuranceUpto);

        // Get the current date
        java.util.Date currentDate = new java.util.Date();

        // Helper method to check and set color
        checkAndSetColor(this.fitUpto, fitUpto, currentDate);
        checkAndSetColor(this.puccUpto, puccUpto, currentDate);
        checkAndSetColor(this.vehicleInsuranceUpto, vehicleInsuranceUpto, currentDate);
    }

    private void checkAndSetColor(TextView textView, String dateString, java.util.Date currentDate) {
        try {
            // Define the date format used in your JSON
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            // Parse the date string into a Date object
            java.util.Date date = dateFormat.parse(dateString);

            // Compare with the current date
            if (date != null && date.before(currentDate)) {
                textView.setTextColor(Color.RED);
            } else {
                textView.setTextColor(Color.BLACK);
            }
        } catch (Exception e) {
            // If parsing fails, log the error and set default color
            Log.e("DateError", "Error parsing date", e);
            textView.setTextColor(Color.BLACK);
        }
    }

}