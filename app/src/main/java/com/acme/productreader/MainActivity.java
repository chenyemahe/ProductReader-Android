package com.acme.productreader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;
    private TextView scanResults;
    private Uri imageUri;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    private TextView upcCount;
    private TextView store;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        scanResults = (TextView) findViewById(R.id.productname);
        upcCount = findViewById(R.id.upccount);
        store = findViewById(R.id.store);
        if (savedInstanceState != null) {
            //imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            //scanResults.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
        });

        Button mMenu = (Button) findViewById(R.id.bt_menu);
        mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MenuPage.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bundle result = data.getExtras();
            String value = result.getString("barcode");
            String type = result.getString("type");
            if (TextUtils.equals(type, PrConstant.type_fsku)) {
                ProductProfile profile = PrManager.getManager().getDB().getAAProfileByFSKU(getContentResolver(),value);
                if(profile != null)
                    scanResults .setText(profile.getProductName());
            }
            if (TextUtils.equals(type, PrConstant.type_upc)) {
                ProductProfile profile = PrManager.getManager().getDB().getAAProfileByUpc(getContentResolver(),value);
                if(profile != null) {
                    scanResults.setText(profile.getProductName());
                    PrUtils.saveUpcCount(this,value,PrConstant.shared_upc_total);
                    int store_a = 0;
                    int store_b = 0;
                    upcCount.setText("Current UPC count : "+ PrUtils.getCustomKeywordList(this,PrConstant.shared_upc_total));
                    ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByAsin(getContentResolver(),PrUtils.removeMark(profile.getASIN()," "));
                    for(ProductProfile p : fullList) {
                        if(TextUtils.equals(PrConstant.store1,p.getTotalAdd())) {
                            String s = p.getRequestNm();
                            if (s != null)
                                store_a += Float.valueOf(s);
                        }
                        if(TextUtils.equals(PrConstant.store2,p.getTotalAdd())) {
                            String s = p.getRequestNm();
                            if (s != null)
                                store_b += Float.valueOf(s);
                        }
                    }
                    store.setText(PrConstant.store1 + " : " + store_a + "     " + PrConstant.store2 + " : " + store_b);
                    //store.setText(PrUtils.chooseStore(profile.getASIN(),profile.getUPC(),this));
                }
                else {
                    Intent intent = new Intent(this,AddUpc.class);
                    intent.putExtra(PrConstant.type_upc,value);
                    startActivity(intent);
                }
            }
        }
    }

    private void takePicture() {
        Intent intent = new Intent(this,ScanActivity.class);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            //outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        upcCount.setText("Current UPC count : "+ PrUtils.getCustomKeywordList(this,PrConstant.shared_upc_total));
    }
}
