package com.acme.productreader;

public class ProductProfile {

    private String mId;
    private String mName;
    private String mDate;
    private String mSKU;
    private String mASIN;
    private String mFNSKU;
    private String mPrice;
    private String mFee;
    private String mUPC;
    private String mSupDay;
    private String mCurrentQN;
    private String mRequestNm;
    private String mTotalAdd;

    public ProductProfile() {
    }

    public void setId(String id) {
        mId = id;
    }

    public void setProductName(String name) {
        mName = name;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setSKU(String sku) {
        mSKU = sku;
    }

    public void setASIN(String asin) {
        mASIN = asin;
    }

    public void setFNSKU(String fnsku) {
        mFNSKU = fnsku;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    public void setAmazonFee(String fee) {
        mFee = fee;
    }

    public void setUPC(String upc) {
        mUPC = upc;
    }

    public void setSupplyDay(String day) {
        mSupDay = day;
    }

    public void setCurrentQN(String number) {
        mCurrentQN = number;
    }

    public void setRequestNumber(String number) {
        mRequestNm = number;
    }

    public void setTotalAdd(String number) {
        mTotalAdd = number;
    }

    public String getId() {
        return mId;
    }

    public String getProductName() {
        return mName;
    }

    public String getDate() {
        return mDate;
    }

    public String getSKU() {
        return mSKU;
    }

    public String getASIN() {
        return mASIN;
    }

    public String getFNSKU() {
        return mFNSKU;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getFee() {
        return mFee;
    }

    public String getUPC() {
        return mUPC;
    }

    public String getSupplyDay() {
        return mSupDay;
    }

    public String getCurrentNm() {
        return mCurrentQN;
    }

    public String getRequestNm() {
        return mRequestNm;
    }

    public String getTotalAdd() {
        return mTotalAdd;
    }
}
