package com.acme.productreader;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class UpcScannedProductPage extends Activity implements View.OnClickListener{

    private EditText ed_a;
    private EditText ed_b;
    private EditText ed_c;
    private Button bt_a;
    private Button bt_b;
    private Button bt_c;
    private TextView tv_a;
    private TextView tv_b;

    private TextView submit;
    private ArrayList<ProductProfile> fullList;
    private int restock_a;
    private int restock_b;

    private int cached_a;
    private int cached_b;

    private String upc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upcproduct_page);
        ed_a = findViewById(R.id.ed_scanned_number);
        ed_b = findViewById(R.id.ed_product_name);
        ed_c = findViewById(R.id.ed_product_name_b);

        ed_a.setText("1");

        bt_a = findViewById(R.id.bt_submit);
        bt_b = findViewById(R.id.bt_set_2);
        bt_c = findViewById(R.id.bt_set_3);
        bt_a.setOnClickListener(this);
        bt_b.setOnClickListener(this);
        bt_c.setOnClickListener(this);

        tv_a = findViewById(R.id.tv_upc_result_a);
        tv_b = findViewById(R.id.tv_upc_result_b);

        submit = findViewById(R.id.menu_5);
        submit.setOnClickListener(this);

        restock_a = 0;
        restock_b = 0;
        cached_a = 0;
        cached_b = 0;

        upc = "0";
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle result = getIntent().getExtras();
        upc = result.getString("barcode");
        ProductProfile profile = PrManager.getManager().getDB().getAAProfileByUpc(getContentResolver(),upc);
        fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByAsin(getContentResolver(), profile.getASIN());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_submit:
                calculateResult(upc, Integer.valueOf(ed_a.getText().toString()));
                tv_a.setText("A 总补: " + cached_a + " A 分配：" + restock_a);
                tv_b.setText("B 总补: " + cached_b + " B 分配：" + restock_b);
                Log.d("ye chen", " " +restock_a+restock_b);
                break;
            case R.id.bt_set_2:
                cached_a = Integer.valueOf(ed_b.getText().toString());
                updateValue_a();
                calculateResult(upc, Integer.valueOf(ed_a.getText().toString()));
                tv_a.setText("A 总补: " + cached_a + " A 分配：" + restock_a);
                tv_b.setText("B 总补: " + cached_b + " B 分配：" + restock_b);
                break;
            case R.id.bt_set_3:
                cached_b = Integer.valueOf(ed_c.getText().toString());
                updateValue_b();
                calculateResult(upc, Integer.valueOf(ed_a.getText().toString()));
                tv_a.setText("A 总补: " + cached_a + " A 分配：" + restock_a);
                tv_b.setText("B 总补: " + cached_b + " B 分配：" + restock_b);
                break;
            case R.id.menu_5:
                PrUtils.saveUpcCount(this,upc,PrConstant.shared_upc_total_store1,restock_a);
                PrUtils.saveUpcCount(this,upc,PrConstant.shared_upc_total_store2,restock_b);
                this.finish();
            default:
                    break;
        }
    }

    private void calculateResult(String upc, int number) {
        restock_a = 0;
        restock_b = 0;
        int store_a = 0;
        int store_b = 0;
        boolean inStoreA = false;
        boolean inStoreB = false;
        for (ProductProfile p : fullList) {
            if (TextUtils.equals(PrConstant.store1, p.getTotalAdd())) {
                inStoreA = true;
                String s = p.getRequestNm();
                if (s != null)
                    store_a += Float.valueOf(s);
            }
            if (TextUtils.equals(PrConstant.store2, p.getTotalAdd())) {
                inStoreB = true;
                String s = p.getRequestNm();
                if (s != null)
                    store_b += Float.valueOf(p.getRequestNm());
            }
        }

        cached_a = store_a;
        cached_b = store_b;

        if (!inStoreA) {
            restock_a = 0;
            restock_b = number;
            return;
        }
        if (!inStoreB) {
            restock_b = 0;
            restock_a = number;
            return;
        }

        int upc_a = PrUtils.getUpcCount(this, upc, PrConstant.shared_upc_total_store1);
        int upc_b = PrUtils.getUpcCount(this, upc, PrConstant.shared_upc_total_store2);

        if(cached_b == cached_a) {
            restock_b = number / 2;
            restock_a = number - restock_b;
            return;
        }


        for(int i = 1; i <= number; i++) {
            if ((store_a - upc_a - i) >= (store_b - upc_b)) {
                restock_a = i;
            }
        }

        if (restock_a == number) {
            restock_b = 0;
            return;
        }

        for(int i = 1; i <= number; i++) {
            if ((store_b - upc_b - i) >= (store_a - upc_a)) {
                restock_b = i;
            }
        }

        if (restock_b == number) {
            restock_a = 0;
            return;
        }

        if (restock_a > 0) {
            int temp = number - restock_a;
            restock_b = temp/2;
            restock_a += temp - restock_b;
            return;
        }

        if (restock_b > 0) {
            int temp = number - restock_b;
            restock_a = temp/2;
            restock_b += temp - restock_a;
            return;
        }
        Log.d("ye chen", "Store a count: " + store_a + " Store a UPC count : " + restock_a + " Store b count: " + store_b + " Store b UPC count : " + restock_b);
    }

    private void updateValue_a() {
        boolean isFirstinA = true;
        for (ProductProfile p : fullList) {
            if (TextUtils.equals(PrConstant.store1, p.getTotalAdd())) {
                if (isFirstinA) {
                    p.setRequestNumber(String.valueOf(cached_a));
                    PrManager.getManager().getDB().updateAAProfile(getContentResolver(), p);
                    isFirstinA = false;
                } else {
                    p.setRequestNumber(String.valueOf(0));
                    PrManager.getManager().getDB().updateAAProfile(getContentResolver(), p);
                }
            }
        }
    }

    private void updateValue_b() {
        boolean isFirstinB = true;
        for (ProductProfile p : fullList) {
            if (TextUtils.equals(PrConstant.store2, p.getTotalAdd())) {
                if (isFirstinB) {
                    p.setRequestNumber(String.valueOf(cached_b));
                    PrManager.getManager().getDB().updateAAProfile(getContentResolver(), p);
                    isFirstinB = false;
                } else {
                    p.setRequestNumber(String.valueOf(0));
                    PrManager.getManager().getDB().updateAAProfile(getContentResolver(), p);
                }
            }
        }
    }
}
