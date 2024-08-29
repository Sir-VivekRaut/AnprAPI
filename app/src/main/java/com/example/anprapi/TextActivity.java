package com.example.anprapi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TextActivity extends AppCompatActivity {

    private EditText vehicleNumEditText;
    private Button fetchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        vehicleNumEditText = findViewById(R.id.vehicle_num);
        fetchButton = findViewById(R.id.text);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vehicleNumber = vehicleNumEditText.getText().toString().trim();

                if (!vehicleNumber.isEmpty()) {
                    Intent intent = new Intent(TextActivity.this, DisplayActivity.class);
                    intent.putExtra("vehicleNumber", vehicleNumber);
                    startActivity(intent);
                }else{
                    Toast.makeText(TextActivity.this, "Enter valid number!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
