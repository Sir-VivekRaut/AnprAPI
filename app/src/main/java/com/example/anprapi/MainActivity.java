package com.example.anprapi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView plateTxt;
    private ProgressBar progressBar;
    private static final int PICK_IMAGE = 100;
    private final String token = "c05a8ba736ddcc92440ec3c30c384a5552950297";
    private String plateValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        plateTxt = findViewById(R.id.car_plate);
        progressBar = findViewById(R.id.homeprogress);
        Button nextImage = findViewById(R.id.next_image);

        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            try {
                Uri imageUri = data.getData();
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                imageView.setImageBitmap(bitmap);

                File file = new File(getCacheDir(), "selectedImage.jpg");
                FileOutputStream fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                fOut.flush();
                fOut.close();

                recognizePlate(file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error processing the image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void recognizePlate(String filePath) {
        progressBar.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
        File file = new File(filePath);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload", file.getName(),
                        RequestBody.create(MEDIA_TYPE_JPEG, file))
                .build();

        Request request = new Request.Builder()
                .url("https://api.platerecognizer.com/v1/plate-reader/")
                .header("Authorization", "Token " + token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to recognize the plate", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String result = response.body().string();
                    runOnUiThread(() -> {
                        // Update UI
                        parseJsonResponse(result);
                        progressBar.setVisibility(View.GONE);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Failed to recognize the plate", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
            }
        });
    }

    private void parseJsonResponse(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray resultsArray = jsonObject.getJSONArray("results");

            if (resultsArray.length() > 0) {
                JSONObject result = resultsArray.getJSONObject(0);

                // Check if 'plate' is a string
                if (result.get("plate") instanceof String) {
                    plateValue = result.getString("plate");
                } else {
                    // If 'plate' is an object, handle it as before
                    JSONObject plateObject = result.getJSONObject("plate");
                    JSONArray plateProps = plateObject.getJSONObject("props").getJSONArray("plate");
                    plateValue = plateProps.getJSONObject(0).getString("value");
                }

                // Display the results
                plateTxt.setText("Recognized Plate: " + plateValue);

                // Start DisplayActivity and pass the plate value
                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                intent.putExtra("vehicleNumber", plateValue); // Pass the plate value
                startActivity(intent);

            } else {
                plateTxt.setText("No plate found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
        }
    }


}
