package com.acme.productreader;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ye1.chen on 4/19/16.
 */
public class MenuPage extends Activity implements View.OnClickListener{

    private TextView productList;
    private TextView restockList;
    private TextView uploadPName;
    private TextView clearUpcCount;
    private TextView submit;
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

        clearUpcCount = findViewById(R.id.menu_4);
        clearUpcCount.setOnClickListener(this);

        submit = findViewById(R.id.menu_5);
        submit.setOnClickListener(this);
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
                break;
            case R.id.menu_3:
                new Thread() {
                    public void run(){
                        PrUtils.updateProductName(MenuPage.this, PrConstant.shared_product_name);
                    }
                }.start();
                break;
            case R.id.menu_4:
                PrUtils.clearCustomPrefs(MenuPage.this, PrConstant.shared_upc_total_store1);
                PrUtils.clearCustomPrefs(MenuPage.this, PrConstant.shared_upc_total_store2);
                PrUtils.clearCustomPrefs(MenuPage.this, PrConstant.shared_product_name);
                Toast.makeText(this,"All Product name and upc cleared!", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_5:
                try{
                    PrUtils.generateCsvFullDB(MenuPage.this);
                }catch (Exception e) {
                    Log.e("ye chen" , e.getMessage());
                }
                break;
            default:
                break;
        }
    }
}
