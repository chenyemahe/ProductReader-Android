package com.acme.productreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AddUpc extends Activity implements View.OnClickListener{

    private TextView upc;
    private AutoCompleteTextView mProductName;
    private TextView add;
    private String upcnumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_upc);
        upc = findViewById(R.id.tv_upc_n);
        mProductName = findViewById(R.id.ed_product_name);
        add = findViewById(R.id.add);
        add.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        upcnumber = intent.getExtras().getString(PrConstant.type_upc);
        upc.setText(upcnumber);
        ArrayAdapter<String> sAdapter4 = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, getStringArray());
        mProductName.setAdapter(sAdapter4);
        mProductName.setOnClickListener(this);
    }

    private String[] getStringArray() {
        return PrUtils.getCustomKeywordList(this, PrConstant.shared_product_name).split(",");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add :
                ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByName(this.getContentResolver(), mProductName.getText().toString());
                for(ProductProfile p : fullList) {
                    p.setUPC(upcnumber);
                    PrManager.getManager().getDB().updateAAProfile(getContentResolver(), p);
                }
                this.finish();
        }
    }
}
