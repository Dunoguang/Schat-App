package com.example.rootwebviewdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.webkit.JavascriptInterface;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Base64;

public class JsBridge {

    @JavascriptInterface
    public String root_cmd(String mycmd) {
        RootShellExecutor.CommandResult result = RootShellExecutor.execute(mycmd);
        JSONArray data = new JSONArray();
        data.put(result.stdout);
        data.put(result.stderr);
        data.put(result.statusCode);
        return data.toString();
    }

    @JavascriptInterface
    public String saveImage(String base64Data, boolean isFrontCamera) {
        JSONObject response = new JSONObject();
        try {
            // Decode base64 to bitmap
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap == null) {
                response.put("success", false);
                response.put("error", "Failed to decode image");
                return response.toString();
            }

            // If front camera and we haven't already flipped in JS, flip here
            // Actually, the flip is already done in JavaScript for display,
            // but we apply it again here to ensure the saved image is also flipped
            if (isFrontCamera) {
                bitmap = flipBitmapHorizontally(bitmap);
            }

            // Save to Pictures directory
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!picturesDir.exists()) {
                picturesDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + ".png";
            File imageFile = new File(picturesDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }

            response.put("success", true);
            response.put("path", imageFile.getAbsolutePath());
            return response.toString();

        } catch (Exception e) {
            try {
                response.put("success", false);
                response.put("error", e.getMessage());
            } catch (Exception ignored) {
            }
            return response.toString();
        }
    }

    private Bitmap flipBitmapHorizontally(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
