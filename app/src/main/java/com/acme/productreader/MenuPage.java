package com.acme.productreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.acme.productreader.R;

/**
 * Created by ye1.chen on 4/19/16.
 */
public class MenuPage extends Activity implements View.OnClickListener{

    private TextView productList;
    private TextView restockList;
    private TextView uploadPName;
    public static final int PICKFILE_RESULT_CODE_1 = 1;
    public static final int PICKFILE_RESULT_CODE_2 = 2;
    private Uri fileUri;
    private String filePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        productList = findViewById(R.id.menu_1);
        productList.setOnClickListener(this);

        restockList = findViewById(R.id.menu_2);
        restockList.setOnClickListener(this);

        uploadPName = findViewById(R.id.menu_3);
        uploadPName.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_1:
                //PrUtils.startFilePickForResult(this, PICKFILE_RESULT_CODE_1);
                new Thread() {
                    public void run(){
                        PrUtils.readExcelFileFromAssets(MenuPage.this, filePath, PICKFILE_RESULT_CODE_1);
                    }
                }.start();
                break;
            case R.id.menu_2:
                new Thread() {
                    public void run(){
                        PrUtils.readExcelFileFromAssets(MenuPage.this, filePath, PICKFILE_RESULT_CODE_2);
                    }
                }.start();

            case R.id.menu_3:
                new Thread() {
                    public void run(){
                        PrUtils.updateProductName(MenuPage.this, PrConstant.shared_product_name);
                    }
                }.start();
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ye chen", String.valueOf(requestCode) + "  " + String.valueOf(resultCode));
        switch (requestCode) {
            case PICKFILE_RESULT_CODE_1:
                if (resultCode == -1) {
                    fileUri = data.getData();
                    filePath = fileUri.getPath();
                    new Thread() {
                        public void run(){
                            PrUtils.readExcelFileFromAssets(MenuPage.this, filePath, PICKFILE_RESULT_CODE_1);
                        }
                    }.start();
                }
                break;
        }
    }
}
