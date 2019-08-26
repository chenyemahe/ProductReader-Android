package com.acme.productreader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.HapticFeedbackConstants;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * This activity handles scanning of echecks.
 *
 */
public class ScanActivity extends AppCompatActivity implements Camera.PreviewCallback {

    // Debugging
    private final String TAG = "acme_test";

    // Camera
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler mAutoFocusHandler;
    private boolean mPreviewing = false;
    private boolean scanning = false;
    private ImageView leftReticle;
    private ImageView rightReticle;
    private BarcodeDetector detector;

    private String LOG_TAG = "acme_test";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Setup the window
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.echeck_layout_capture);

        mAutoFocusHandler = new Handler();

        Button exit = (Button) findViewById(R.id.exit_button);
        exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                closeScanActivity();
            }
        });

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.CODE_128 | Barcode.UPC_A | Barcode.EAN_13)
                .build();
        if (!detector.isOperational()) {
            Log.d("acme_test","Could not set up the detector!");
            return;
        }
    }

    public void onResume() {
        super.onResume();
        Log.i(TAG, "--- ON RESUME ---");

        try {
            mCamera = Camera.open();
            setCameraDisplayOrientation(this,0, mCamera);

            mPreview = new CameraPreview(this, mCamera, this, autoFocusCB);
//        setContentView(mPreview);

            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
            frameLayout.addView(mPreview);

            leftReticle = (ImageView) findViewById(R.id.left_reticle);
            leftReticle.bringToFront();

            rightReticle = (ImageView) findViewById(R.id.right_reticle);
            rightReticle.bringToFront();

            TextView helpText = (TextView) findViewById(R.id.help_text);
            helpText.bringToFront();

            RelativeLayout exitLayoutControls = (RelativeLayout) findViewById(R.id.controls_exit_layout);
            exitLayoutControls.bringToFront();

            mPreviewing = true;

        } catch (Exception e) {
            Log.e(TAG,"open camera failed",e);
        }

    }

    public void onPause() {
        super.onPause();
        Log.i(TAG, "--- ON PAUSE ---");
        mPreviewing = false;
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
        }
    }

    protected void onDestroy() {
        Log.i(TAG, "--- ON DESTROY ---");
        super.onDestroy();
    }



    private final Runnable doAutoFocus = new Runnable() {
        public void run() {
            if(mCamera != null && mPreviewing) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    // Mimic continuous auto-focusing
    private final Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(!scanning) {
            Log.d(TAG, "[onPreviewFrame]");
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            Bitmap bitmap = yuv2Bitmap(data, size.width, size.height);
            /*int beginningX = 0;
            int beginningY = 0;
            assert bitmap != null;
            int x = bitmap.getHeight();
            int y = bitmap.getWidth();
            if(leftReticle != null) {
                int[] locations = new int[2];
                leftReticle.getLocationOnScreen(locations);
                beginningX = locations[0];
                beginningY = locations[1];
            }
            if(rightReticle!= null) {
                int[] locations = new int[2];
                rightReticle.getLocationOnScreen(locations);
                x = x - (beginningX * 2); // Remove area outside reticles.
                y = 300;//yHeight - (beginningY * 2);
            }*/
            //bitmap = Bitmap.createBitmap(bitmap, beginningX, beginningY/4, y, x); // Bottom half of bitmap to increase ocr speed.
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, 1920, 1080);
            if (bitmap != null) {
                scanning = true;
                new ocrTask().execute(bitmap);
            }
        }
    }

    private class ocrTask extends AsyncTask<Object, Integer, Bundle> {

        protected Bundle doInBackground(Object... data) {
            Bundle bundle = new Bundle();
            if (data != null) {
                Bitmap bitmap = (Bitmap) data[0];
                if (bitmap != null) {
                    if (detector.isOperational() && bitmap != null) {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<Barcode> barcodes = detector.detect(frame);
                        for (int index = 0; index < barcodes.size(); index++) {
                            Barcode code = barcodes.valueAt(index);
                            Log.d(LOG_TAG, code.displayValue + "\n");

                            //Required only if you need to extract the type of barcode
                            int type = barcodes.valueAt(index).valueFormat;
                            Log.d("acme_test", "type = " + String.valueOf(type));
                            switch (type) {
                                case Barcode.CONTACT_INFO:
                                    Log.i(LOG_TAG, code.contactInfo.title);
                                    bundle.putString("barcode", code.contactInfo.title);
                                    break;
                                case Barcode.EMAIL:
                                    Log.i(LOG_TAG, code.email.address);
                                    bundle.putString("barcode", code.email.address);
                                    break;
                                case Barcode.ISBN:
                                    Log.i(LOG_TAG, code.rawValue);
                                    bundle.putString("barcode", code.rawValue);
                                    break;
                                case Barcode.PHONE:
                                    Log.i(LOG_TAG, code.phone.number);
                                    bundle.putString("barcode", code.phone.number);
                                    break;
                                case Barcode.PRODUCT:
                                    Log.i(LOG_TAG, code.rawValue);
                                    bundle.putString("barcode", code.rawValue);
                                    bundle.putString("type", PrConstant.type_upc);
                                    break;
                                case Barcode.SMS:
                                    Log.i(LOG_TAG, code.sms.message);
                                    bundle.putString("barcode", code.sms.message);
                                    break;
                                case Barcode.TEXT:
                                    Log.i(LOG_TAG, code.rawValue);
                                    bundle.putString("barcode", code.rawValue);
                                    bundle.putString("type", PrConstant.type_fsku);
                                    break;
                                case Barcode.URL:
                                    Log.i(LOG_TAG, "url: " + code.url.url);
                                    bundle.putString("barcode", code.url.url);
                                    break;
                                case Barcode.WIFI:
                                    Log.i(LOG_TAG, code.wifi.ssid);
                                    bundle.putString("barcode", code.wifi.ssid);
                                    break;
                                case Barcode.GEO:
                                    Log.i(LOG_TAG, code.geoPoint.lat + ":" + code.geoPoint.lng);
                                    bundle.putString("barcode", code.geoPoint.lat + ":" + code.geoPoint.lng);
                                    break;
                                case Barcode.CALENDAR_EVENT:
                                    Log.i(LOG_TAG, code.calendarEvent.description);
                                    bundle.putString("barcode", code.calendarEvent.description);
                                    break;
                                case Barcode.DRIVER_LICENSE:
                                    Log.i(LOG_TAG, code.driverLicense.licenseNumber);
                                    bundle.putString("barcode", code.driverLicense.licenseNumber);
                                    break;
                                default:
                                    Log.i(LOG_TAG, code.rawValue);
                                    bundle.putString("barcode", code.rawValue);
                                    break;
                            }
                        }
                        if (barcodes.size() == 0) {
                            Log.d(LOG_TAG, "Scan Failed: Found nothing to scan");
                        }
                    } else {
                        Log.d(LOG_TAG, "Could not set up the detector!");
                    }

                }
            }
            return bundle;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Bundle bundle) {
            if ( bundle.containsKey("barcode")) {
                Log.d(TAG, "Scan pass!");
                Intent returnIntent = new Intent();
                returnIntent.putExtras(bundle);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                Log.e(TAG, "Barcode not found. Try again.");
            }
            scanning = false;
        }
    }

    /**
     * YUV to bitmap
     * @param data yuv data
     * @param width
     * @param height
     * @return RGB565 format bitmap
     */
    private static Bitmap yuv2Bitmap(byte[] data, int width, int height) {
        final YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        if(!image.compressToJpeg(new Rect(0, 0, width, height), 100, os)){
            return null;
        }
        byte[] tmp = os.toByteArray();
        return BitmapFactory.decodeByteArray(tmp, 0,tmp.length);
    }

    private void closeScanActivity(){
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public static void setCameraDisplayOrientation (Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo (cameraId , info);
        int rotation = activity.getWindowManager ().getDefaultDisplay ().getRotation ();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = ( info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation (result);
    }
}