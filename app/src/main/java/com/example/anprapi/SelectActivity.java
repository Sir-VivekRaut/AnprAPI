package com.example.anprapi;


import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectActivity extends AppCompatActivity {

    private Button imageButton, textButton;
    private ImageView sir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        imageButton = findViewById(R.id.image);
        textButton = findViewById(R.id.text);
        sir = findViewById(R.id.sir);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, ImageActivity.class);
                startActivity(intent);
            }
        });

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, TextActivity.class);
                startActivity(intent);
            }
        });

        sir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Intent to open the camera
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(cameraIntent);
                }
            }
        });
    }
}
