
package com.example.anprapi;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import android.Manifest;


public class MainActivity extends AppCompatActivity {

    private Button gallery;
    private ImageView imageView;
    private TextView plateTxt;
    private ProgressBar progressBar;
    private static final int PICK_IMAGE = 100;
    private final int CAMERA_REQUEST_CODE = 1000;
    private final String token = "c05a8ba736ddcc92440ec3c30c384a5552950297";
    private String plateValue;
    private Button camera;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        gallery = findViewById(R.id.gallery_button);
        plateTxt = findViewById(R.id.car_plate);
        progressBar = findViewById(R.id.homeprogress);
        camera = findViewById(R.id.capture_button);
        imageView = findViewById(R.id.imageView);

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permissions already granted
        }

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = decodeBitmap(imageUri);

                    // Rotate the image to landscape
                    bitmap = rotateToLandscape(bitmap);

                    imageView.setImageBitmap(bitmap);

                    saveImageToCache(bitmap, "selectedImage.jpg");

                    // Compress before sending to recognizePlate
                    Bitmap compressedBitmap = compressBitmap(bitmap, 3 * 1024 * 1024); // 3MB limit
                    saveImageToCache(compressedBitmap, "selectedImageCompressed.jpg");

                    recognizePlate(new File(getCacheDir(), "selectedImageCompressed.jpg").getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing the image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                try {
                    Bitmap bitmap = decodeBitmap(imageUri);

                    // Rotate the image to landscape
                    bitmap = rotateToLandscape(bitmap);

                    imageView.setImageBitmap(bitmap);

                    saveImageToGallery(bitmap);

                    // Compress before sending to recognizePlate
                    Bitmap compressedBitmap = compressBitmap(bitmap, 3 * 1024 * 1024); // 3MB limit
                    saveImageToCache(compressedBitmap, "capturedImageCompressed.jpg");

                    recognizePlate(new File(getCacheDir(), "capturedImageCompressed.jpg").getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing the image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Bitmap rotateToLandscape(Bitmap bitmap) {
        if (bitmap.getWidth() < bitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90); // Rotate 90 degrees to convert portrait to landscape
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap; // Already in landscape
    }

    private Bitmap compressBitmap(Bitmap bitmap, int maxSize) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        while (out.size() > maxSize && quality > 0) {
            out.reset();
            quality -= 5;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }
        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
    }

    private void saveImageToCache(Bitmap bitmap, String fileName) throws IOException {
        File file = new File(getCacheDir(), fileName);
        try (FileOutputStream fOut = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
        }
    }


    private Bitmap decodeBitmap(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




    private void saveImageToGallery(Bitmap bitmap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String coordinatesText = String.format("Location:\nLat: %.4f, Long: %.4f", latitude, longitude);

                    Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    Canvas canvas = new Canvas(mutableBitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#00FF7F"));
                    paint.setTextSize(100);
                    paint.setAntiAlias(true);
                    paint.setShadowLayer(5.0f, 10.0f, 10.0f, Color.BLACK);

                    int padding = 20;
                    canvas.drawText(coordinatesText, padding, mutableBitmap.getHeight() - padding, paint);

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, "CapturedImage_" + System.currentTimeMillis() + ".jpg");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera");

                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);  // High quality (no compression)
                        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Location permission is required to add coordinates to the image", Toast.LENGTH_SHORT).show();
        }
    }



    private void recognizePlate(String filePath) {
        progressBar.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
        File file = new File(filePath);

        // Compression before sending to the API
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        File compressedFile = new File(getCacheDir(), "compressedImage.jpg");
        try (FileOutputStream fOut = new FileOutputStream(compressedFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);  // Compress to reduce the file size to under 3MB
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload", compressedFile.getName(),
                        RequestBody.create(MEDIA_TYPE_JPEG, compressedFile))
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

                if (result.get("plate") instanceof String) {
                    plateValue = result.getString("plate");
                } else {
                    JSONObject plateObject = result.getJSONObject("plate");
                    JSONArray plateProps = plateObject.getJSONObject("props").getJSONArray("plate");
                    plateValue = plateProps.getJSONObject(0).getString("value").toUpperCase();
                }

                plateTxt.setText("Recognized Plate: " + plateValue);

                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                intent.putExtra("vehicleNumber", plateValue);
                startActivity(intent);

            } else {
                plateTxt.setText("No plate found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}