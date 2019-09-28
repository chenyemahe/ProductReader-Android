package com.acme.productreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AddUpc extends Activity implements View.OnClickListener{

    private TextView upc;
    private AutoCompleteTextView mProductName;
    private TextView add;
    private String upcnumber;
    private EditText upc_enter;
    private  TextView proname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_upc);
        upc = findViewById(R.id.tv_upc_n);
        mProductName = findViewById(R.id.ed_product_name);
        add = findViewById(R.id.add);
        add.setOnClickListener(this);
        upc_enter = findViewById(R.id.ed_upc_e);
        proname = findViewById(R.id.tv_prname);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        upcnumber = intent.getExtras().getString(PrConstant.type_upc);
        if(TextUtils.equals(upcnumber, "no_value")) {
            upc.setVisibility(View.GONE);
            mProductName.setVisibility(View.GONE);
            upc_enter.setVisibility(View.VISIBLE);
            proname.setVisibility(View.VISIBLE);
            return;
        }
        upc.setText(upcnumber);
        ArrayAdapter<String> sAdapter4 = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, getStringArray());
        mProductName.setAdapter(sAdapter4);
        mProductName.setOnClickListener(this);
    }

    private String[] getStringArray() {
        return PrUtils.getCustomKeywordList(this, PrConstant.shared_product_name).split("@");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add :
                if(TextUtils.equals(upcnumber, "no_value")) {
                    String s = upc_enter.getText().toString();
                    if (s == null || (s.length() != 12 && s.length() != 13)) {
                        Toast.makeText(this,R.string.upc_error, Toast.LENGTH_LONG).show();
                        break;
                    }
                    ProductProfile profile = PrManager.getManager().getDB().getAAProfileByUpc(getContentResolver(),s);

                    if(profile != null) {
                        proname.setText(profile.getProductName());
                        Intent intent = new Intent(this, UpcScannedProductPage.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("barcode", s);
                        bundle.putString("type", PrConstant.type_fsku);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this,AddUpc.class);
                        intent.putExtra(PrConstant.type_upc,s);
                        startActivity(intent);
                    }
                }

                String pName = mProductName.getText().toString();
                ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByName(this.getContentResolver(), pName);
                if(fullList != null) {
                    for(ProductProfile p : fullList) {
                        if(p == null)
                            continue;
                        p.setUPC(upcnumber);
                        PrManager.getManager().getDB().updateAAProfile(getContentResolver(), p);
                    }
                    this.finish();
                } else {
                    ProductProfile p = PrManager.getManager().getDB().getAAProfileByAsin(this.getContentResolver(), pName);
                    if(p != null){
                        p.setUPC(upcnumber);
                        PrManager.getManager().getDB().updateAAProfile(getContentResolver(), p);
                        this.finish();
                    } else
                        Toast.makeText(this,"Saving fail!", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        upc.setVisibility(View.VISIBLE);
        mProductName.setVisibility(View.VISIBLE);
        upc_enter.setVisibility(View.GONE);
        proname.setVisibility(View.GONE);
    }
}
