
package com.acme.productreader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.acme.productreader.database.PrProvider;

import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PrUtils {

    public static final String INTENT_PROFILE_ID = "Item_ID";

    public static final String INTENT_PRODUCT_NAME = "product_name";

    public static final String UNSORT = "unsort";

    public static final double RATE_BV = 0.15;

    public static final double RATE_AMAZON_REF_HEALTHY = 0.15;

    public static final String EXPAND_ADAPTER_ORDER = "expand_adapter_order";

    public static final String EXPAND_ADAPTER_FBA = "expand_adapter_fba";

    public static final String INTENT_EXTRA_ITEM_STYLE = "intent_extra_item_style";

    public static final int TIME = 30;

    public static final String CASHBACK_PREFS = "cashback_prefs";
    public static final String CASHBACK_KEYWORDS_LIST_WEBSITE = "cashback_keywords_list_website";
    public static final String CASHBACK_KEYWORDS_LIST_STORE = "cashback_keywords_list_store";
    public static final String CASHBACK_KEYWORDS_LIST_CAT = "cashback_keywords_list_cat";

    public static final String PREFS_SPINNER_VIEW_OPTION = "prefs_spinner_view_option";
    public static final String PREFS_SPINNER_SORT_OPTION = "prefs_spinner_sort_option";

    public static final String MAIN_SPINNER_TYPE_BY_CB_STORE = "main_spinner_type_by_cb_store";
    public static final String MAIN_SPINNER_TYPE_BY_CA = "main_spinner_type_by_ca";
    public static final String MAIN_SPINNER_TYPE_BY_PAYMENT = "main_spinner_type_by_payment";


    public static void toContentValues(ProductProfile profile, ContentValues values) {
        values.put(PrProvider.ProfileColumns.ORDER_ID, profile.getProductName());
        values.put(PrProvider.ProfileColumns.ORDER_DATE, profile.getDate());
        values.put(PrProvider.ProfileColumns.ORDER_SKU, profile.getSKU());
        values.put(PrProvider.ProfileColumns.ORDER_ASIN, profile.getASIN());
        values.put(PrProvider.ProfileColumns.ORDER_FNSKU, profile.getFNSKU());
        values.put(PrProvider.ProfileColumns.ORDER_PRICE, profile.getPrice());
        values.put(PrProvider.ProfileColumns.ORDER_FEE, profile.getFee());
        values.put(PrProvider.ProfileColumns.ORDER_UPC, profile.getUPC());
        values.put(PrProvider.ProfileColumns.ORDER_SUPPLY_DAY, profile.getSupplyDay());
        values.put(PrProvider.ProfileColumns.ORDER_CURRENT_NUMBER, profile.getCurrentNm());
        values.put(PrProvider.ProfileColumns.ORDER_REQUEST_NUMBER, profile.getRequestNm());
        values.put(PrProvider.ProfileColumns.ORDER_TOTAL_ADDED, profile.getTotalAdd());
    }

    public static void fromCursor(Cursor cursor, ProductProfile profile) {
        int idxId = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns._ID);
        int idxOrderId = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_ID);
        int idxDate = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_DATE);
        int idxStore = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_SKU);
        int idxDetail = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_ASIN);
        int idxCashbackCompany = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_FNSKU);
        int idxCashbackState = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_PRICE);
        int idxCashbackPercent = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_FEE);
        int idxCashbackAmount = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_UPC);
        int idxCategory = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_SUPPLY_DAY);
        int idxCost = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_CURRENT_NUMBER);
        int idxPartCost = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_REQUEST_NUMBER);
        int idxPaymentFrom = cursor.getColumnIndexOrThrow(PrProvider.ProfileColumns.ORDER_TOTAL_ADDED);

        profile.setId(cursor.getString(idxId));
        profile.setProductName(cursor.getString(idxOrderId));
        profile.setDate(cursor.getString(idxDate));
        profile.setSKU(cursor.getString(idxStore));
        profile.setASIN(cursor.getString(idxDetail));
        profile.setFNSKU(cursor.getString(idxCashbackCompany));
        profile.setPrice(cursor.getString(idxCashbackState));
        profile.setAmazonFee(cursor.getString(idxCashbackPercent));
        profile.setUPC(cursor.getString(idxCashbackAmount));
        profile.setSupplyDay(cursor.getString(idxCategory));
        profile.setCurrentQN(cursor.getString(idxCost));
        profile.setRequestNumber(cursor.getString(idxPartCost));
        profile.setTotalAdd(cursor.getString(idxPaymentFrom));
    }

    /**
     * Sort order profile list by date
     *
     * @param profileList
     * @return ArrayList by key year and month for sorted list
     */
    public static synchronized ArrayList<ArrayList<ArrayList<ProductProfile>>> sortProfileByDate(
            List<ProductProfile> profileList) {
        if (profileList == null)
            return null;
        ArrayList<ArrayList<ArrayList<ProductProfile>>> sortListMap = new ArrayList<ArrayList<ArrayList<ProductProfile>>>();
        ArrayList<String> yearList = new ArrayList<String>();
        String year = UNSORT;
        String month = UNSORT;
        String day = UNSORT;
        // date structure Month/Date/Year
        for (ProductProfile profile : profileList) {
            String[] mdate = profile.getDate().split("/");
            if (mdate.length == 3) {
                year = mdate[2];
                month = mdate[0];
                day = mdate[1];
            } else {
                return null;
            }
            int indexYear = -1;
            int indexMonth = Integer.parseInt(month);
            if (!yearList.contains(year)) {
                for (int i = 0; i <= yearList.size(); i++) {
                    if (i == yearList.size()) {
                        sortListMap.add(new ArrayList<ArrayList<ProductProfile>>());
                        yearList.add(year);
                        break;
                    }
                    if (Integer.parseInt(yearList.get(i)) < Integer.parseInt(year)) {
                        sortListMap.add(i, new ArrayList<ArrayList<ProductProfile>>());
                        yearList.add(i, year);
                        break;
                    }
                }
                indexYear = findStElemInArray(yearList, year);
                for (int i = 0; i < 12; i++) {
                    sortListMap.get(indexYear).add(new ArrayList<ProductProfile>());
                }
            } else {
                indexYear = findStElemInArray(yearList, year);
            }

            ArrayList<ProductProfile> list = sortListMap.get(indexYear).get(indexMonth - 1);
            for (int i = 0; i <= list.size(); i++) {
                if (i == list.size()) {
                    list.add(profile);
                    break;
                }
                // sort by latest date
                if (Integer.parseInt(list.get(i).getDate().split("/")[1]) < Integer.parseInt(day)) {
                    list.add(i, profile);
                    break;
                }
            }
        }
        return sortListMap;
    }

    /**
     * Sort order profile list by Cashback Store
     *
     * @param profileList
     * @return ArrayList by key Cb Store and month for sorted list
     */
    public static synchronized ArrayList<ArrayList<ArrayList<ProductProfile>>> sortProfileByKeyWords(String key,
                                                                                                     List<ProductProfile> profileList) {
        if (profileList == null)
            return null;
        ArrayList<ArrayList<ArrayList<ProductProfile>>> sortListMap = new ArrayList<ArrayList<ArrayList<ProductProfile>>>();
        ArrayList<String> storeList = new ArrayList<String>();
        String keySort = UNSORT;
        String month = UNSORT;
        String day = UNSORT;
        // date structure Month/Date/Year
        for (ProductProfile profile : profileList) {
            if (TextUtils.equals(key, PrUtils.MAIN_SPINNER_TYPE_BY_CB_STORE)) {
                keySort = profile.getFNSKU();
            } else if (TextUtils.equals(key, PrUtils.MAIN_SPINNER_TYPE_BY_CA)) {
                keySort = profile.getSupplyDay();
            } else if (TextUtils.equals(key, PrUtils.MAIN_SPINNER_TYPE_BY_PAYMENT)) {
                keySort = profile.getTotalAdd();
            } else {
                return null;
            }
            if (TextUtils.isEmpty(keySort)) {
                keySort = "Default";
            }
            String[] mdate = profile.getDate().split("/");
            if (mdate.length == 3) {
                month = mdate[0];
                day = mdate[1];
            } else {
                return null;
            }
            int indexStoreList = -1;
            int indexMonth = Integer.parseInt(month);
            if (!storeList.contains(keySort)) {
                sortListMap.add(new ArrayList<ArrayList<ProductProfile>>());
                storeList.add(keySort);
                indexStoreList = findStElemInArray(storeList, keySort);
                for (int i = 0; i < 12; i++) {
                    sortListMap.get(indexStoreList).add(new ArrayList<ProductProfile>());
                }
            } else {
                indexStoreList = findStElemInArray(storeList, keySort);
            }

            ArrayList<ProductProfile> list = sortListMap.get(indexStoreList).get(indexMonth - 1);
            for (int i = 0; i <= list.size(); i++) {
                if (i == list.size()) {
                    list.add(profile);
                    break;
                }
                // sort by latest date
                if (Integer.parseInt(list.get(i).getDate().split("/")[1]) < Integer.parseInt(day)) {
                    list.add(i, profile);
                    break;
                }
            }
        }
        return sortListMap;
    }

    private static int findStElemInArray(ArrayList<String> list, String item) {
        int i = 0;
        for (String s : list) {
            if (s.equals(item)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Parse Set to ArrayList
     *
     * @param set
     * @return ArrayList
     */
    public static <T> ArrayList<T> setToArrayList(Set<T> set) {
        if (set == null)
            return null;
        ArrayList<T> tArray = new ArrayList<T>();
        Iterator<T> it = set.iterator();
        while (it.hasNext()) {
            T t = it.next();
            tArray.add(t);
        }
        return tArray;
    }

    /**
     * Parse ArrayList to Set
     *
     * @return Set
     */
    public static <T> Set<T> arrayListToSet(ArrayList<T> list) {
        if (list == null)
            return null;
        Set<T> set = new HashSet<T>();
        for (T t : list) {
            set.add(t);
        }
        return set;
    }

    public static String getTotalProCost(ArrayList<ProductProfile> proList) {
        Double cost = 0.0;
        for (ProductProfile profile : proList) {
            cost += Double.parseDouble(profile.getCurrentNm());
        }
        return String.valueOf(String.format("%.02f", cost));
    }

    public static String calCashbackAmount(String totalCost, String rate) {
        if (TextUtils.isEmpty(totalCost) || TextUtils.isEmpty(rate) || TextUtils.equals(".", totalCost) || TextUtils.equals(".", rate)) {
            return "0";
        }
        return String.valueOf(String.format("%.02f", Double.parseDouble(totalCost) * Double.parseDouble(rate) / 100));
    }

    public static boolean saveSpinnerPrefs(Context context, int keywords, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        return prefs.edit().putInt(type_key, keywords).commit();
    }

    public static int getSpinnerPrefs(Context context, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        return prefs.getInt(type_key, 0);
    }

    public static boolean saveCustomKeyword(Context context, String keywords, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        String newList = getCustomKeywordList(context, type_key);
        if (TextUtils.isEmpty(newList)) {
            newList = keywords;
        } else {
            if (!newList.contains(keywords)) {
                newList = newList + "@" + keywords;
                Log.d(context.getClass().toString(), "keyword update save checked keywords: " + keywords);
            }
        }
        return prefs.edit().putString(type_key, newList).commit();
    }

    public static String getCustomKeywordList(Context context, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        return prefs.getString(type_key, "");
    }

    public static boolean saveUpcCount(Context context, String keywords, String type_key, int number) {
        if(number == 0)
            return false;
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        String newList = getCustomKeywordList(context, type_key);
        if (TextUtils.isEmpty(newList)) {
            keywords = keywords + ";" + number;
            newList = keywords;
        } else {
            if (!newList.contains(keywords)) {
                newList = newList + "@" + keywords + ";" + number;
                Log.d(context.getClass().toString(), "keyword update save checked keywords: " + keywords);
            } else {
                String ammend = null;
                String[] list = newList.split("@");
                for (int i = 0; i < list.length; i++) {
                    String[] upc = list[i].split(";");
                    if (upc.length == 2 && TextUtils.equals(upc[0], keywords)) {
                        int count = Integer.valueOf(upc[1]) + number;
                        list[i] = upc[0] + ";" + String.valueOf(count);
                    }
                    if (ammend == null)
                        ammend = list[i];
                    else if (list[i] != null)
                        ammend = ammend + "@" + list[i];
                }
                return prefs.edit().putString(type_key, ammend).commit();
            }
        }
        return prefs.edit().putString(type_key, newList).commit();
    }

    public static boolean removeCustomKeyword(Context context, String s, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        String oldList = getCustomKeywordList(context, type_key);
        String newList = "";
        if (TextUtils.isEmpty(oldList)) {
            return true;
        } else if (TextUtils.equals(oldList, s)) {
            newList = "";
        } else {
            String[] temp = oldList.split("@");
            for (int i = 0; i < temp.length; i++) {
                if (!TextUtils.equals(temp[i], s)) {
                    if (TextUtils.isEmpty(newList)) {
                        newList = temp[i];
                    } else {
                        newList += "@" + temp[i];
                    }
                }
            }
        }
        Log.d(context.getClass().toString(), "keyword remove save checked keywords: " + s);
        return prefs.edit().putString(type_key, newList).commit();
    }

    public static boolean clearCustomPrefs(Context context, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        return prefs.edit().putString(type_key, "").commit();
    }

    public static boolean isLess100(String value) {
        if (TextUtils.isEmpty(value) || TextUtils.equals(".", value)) {
            return false;
        }
        Double intValue = Double.parseDouble(value);
        if (intValue < 0)
            return false;
        if (intValue <= 100) {
            return true;
        }
        return false;
    }

    public static String removeMark(String s, String mark) {
        return s.replace(mark, "");
    }

    public static String totalCashBack(Context context) {
        String total = null;
        Double total_db = 0.0;
        ArrayList<ProductProfile> list = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAllProfile(context.getContentResolver());
        if (list != null) {
            for (ProductProfile p : list) {
                if (TextUtils.equals(p.getPrice(), context.getResources().getStringArray(R.array.list_of_status)[2]) ||
                        TextUtils.equals(p.getPrice(), context.getResources().getStringArray(R.array.list_of_status)[3]))
                    total_db += Double.parseDouble(p.getUPC());
            }
        }
        total = String.valueOf(String.format("%.02f", total_db));
        return total;
    }

    public static boolean isOver30Days(String sDate) {
        String[] mdate = sDate.split("/");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(mdate[2]));
        cal.set(Calendar.MONTH, Integer.parseInt(mdate[0]) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mdate[1]));
        Date date = cal.getTime();
        long millisecond = date.getTime();
        long current = System.currentTimeMillis();
        if (current - millisecond > TIME * 24 * 60 * 60000) {
            return true;
        }
        return false;
    }

    public static ArrayList<ProductProfile> getListOfUnpaidCbProfile(Context context) {
        ArrayList<ProductProfile> list = new ArrayList<>();
        ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAllProfile(context.getContentResolver());
        for (ProductProfile p : fullList) {
            if (isOver30Days(p.getDate()) && TextUtils.equals(p.getPrice(), context.getResources().getStringArray(R.array.list_of_status)[1])) {
                list.add(p);
            }
        }
        return list;
    }

    public static boolean isValidDateFormate(String date, Context context) {
        String[] dates = date.split("/");
        if (dates.length != 3) {
            return false;
        }
        if (dates[0].length() != 2 && dates[1].length() != 2 && dates[2].length() != 4) {
            return false;
        }
        String[] mm = context.getResources().getStringArray(R.array.mm);
        if (!isContent(mm, dates[0])) {
            return false;
        }
        String[] day = context.getResources().getStringArray(R.array.dd);
        if (!isContent(day, dates[1])) {
            return false;
        }
        String[] year = context.getResources().getStringArray(R.array.year);
        if (!isContent(year, dates[2])) {
            return false;
        }
        return true;
    }

    public static boolean isContent(String[] array, String s) {
        boolean temp = false;
        for (int i = 0; i < array.length; i++) {
            if (TextUtils.equals(array[i], s)) {
                temp = true;
                break;
            }
        }
        return temp;
    }

    public static String keyMatchCbStatus(String rawKey, Context context) {
        if (rawKey.contains(context.getResources().getStringArray(R.array.list_of_status)[1])) {
            return context.getResources().getStringArray(R.array.list_of_status)[1];
        } else if (rawKey.contains(context.getResources().getStringArray(R.array.list_of_status)[2])) {
            return context.getResources().getStringArray(R.array.list_of_status)[2];
        } else if (rawKey.contains(context.getResources().getStringArray(R.array.list_of_status)[3])) {
            return context.getResources().getStringArray(R.array.list_of_status)[3];
        }
        return null;
    }

    public static String keyMatchSort(String rawKey, Context context) {
        if (TextUtils.equals(rawKey, context.getResources().getStringArray(R.array.list_of_view_sorter)[1])) {
            return PrUtils.MAIN_SPINNER_TYPE_BY_CB_STORE;
        } else if (TextUtils.equals(rawKey, context.getResources().getStringArray(R.array.list_of_view_sorter)[2])) {
            return PrUtils.MAIN_SPINNER_TYPE_BY_CA;
        } else if (TextUtils.equals(rawKey, context.getResources().getStringArray(R.array.list_of_view_sorter)[3])) {
            return PrUtils.MAIN_SPINNER_TYPE_BY_PAYMENT;
        }
        return null;
    }

    public static void startFilePickForResult(Activity activity, int requestCode) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        activity.startActivityForResult(chooseFile, requestCode);
    }

    public static void readExcelFileFromAssets(final MenuPage activity, String filePath, int type) {

        Log.d("ye chen", "readExcelFileFromAssets");
        int result = 0;
        final Context context = activity.getApplicationContext();
        switch (type) {
            case MenuPage.PICKFILE_RESULT_CODE_1:
                result = readAndUpdate(activity, type, R.raw.acme, 7, PrConstant.store1);
                if (result != -1)
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Product List for " + PrConstant.store1 + " updating done!", Toast.LENGTH_LONG).show();
                        }
                    });

                result = readAndUpdate(activity, type, R.raw.home, 7, PrConstant.store2);
                if (result != -1)
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Product List for " + PrConstant.store2 + " updating done!", Toast.LENGTH_LONG).show();
                        }
                    });
                break;

            case MenuPage.PICKFILE_RESULT_CODE_2:
                result = readAndUpdate(activity, type, R.raw.restock_acme, 5, null);
                if (result != -1)
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Restock List for " + PrConstant.store1 + " updating done!", Toast.LENGTH_LONG).show();
                        }
                    });

                result = readAndUpdate(activity, type, R.raw.restock_home, 5, null);
                if (result != -1)
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Restock List for " + PrConstant.store2 + " updating done!", Toast.LENGTH_LONG).show();
                        }
                    });
                break;
            default:
                break;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.dismissprogressbar();
            }
        });
    }

    /**
     * @param activity   context
     * @param type       result code
     * @param resourceId raw file name
     * @param totalRow   max row items
     * @param storeName  store name
     * @return
     */
    public static int readAndUpdate(final Activity activity, int type, int resourceId, int totalRow, String storeName) {
        int result = 0;
        InputStream stream = activity.getResources().openRawResource(resourceId);
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();int i =0, j=0;
            for (int r = 0; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                //int cellsCount = row.getPhysicalNumberOfCells();
                String[] value = new String[totalRow];
                for (int c = 0; c < totalRow; c++) {
                    value[c] = getCellAsString(row, c, formulaEvaluator);
                }
                try {
                if (type == MenuPage.PICKFILE_RESULT_CODE_1) {
                    String sku = removeMark(value[0], " ");
                    ProductProfile profile = PrManager.getManager().getDB().getAAProfileBySKU(activity.getContentResolver(), sku);
                    if (profile == null) {
                        i++;
                        profile = setProfileForProduct(profile, value, storeName);
                        PrManager.getManager().getDB().saveCbProfile(activity.getContentResolver(), profile);
                        Log.e("ye chen", "not in db ");
                    } else {
                        profile = setProfileForProduct(profile, value, storeName);
                        PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), profile);
                    }
                } else if (type == MenuPage.PICKFILE_RESULT_CODE_2) {
                    String sku = removeMark(value[1], " ");
                    ProductProfile profile = PrManager.getManager().getDB().getAAProfileBySKU(activity.getContentResolver(), sku);
                    if (profile == null) {
                        final String notfoundS = sku;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, "Cannot find " + notfoundS + " in DB!", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("ye chen", "not in db ");
                    } else {
                        profile = setProfileForRestock(profile, value);
                        PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), profile);
                        Log.e("ye chen", profile.getRequestNm());
                    }
                }} catch (Exception e) {
                }
            }
        } catch (Exception e) {
            Log.e("ye chen 3", "error " + e.toString());
            result = -1;
        }
        return result;
    }

    public static ProductProfile setProfileForProduct(ProductProfile p, String[] value, String filename) {
        if (p == null)
            p = new ProductProfile();
        String sku = removeMark(value[0], " ");
        p.setSKU(sku);
        p.setProductName(value[1]);
        p.setASIN(removeMark(value[2], " "));
        p.setFNSKU(removeMark(value[3], " "));
        p.setPrice(value[4]);
        p.setAmazonFee(value[5]);
        if(value[6].contains("upc"))
            value[6] = value[6].substring(3);
        if(value.length >6 && value[6] != null && (value[6].length() == 12 || value[6].length() == 13 || value[6].length() == 11))
            if(value[6].length() == 11)
                p.setUPC("0" + value[6]);
            else
                p.setUPC(value[6]);
        if (TextUtils.equals(filename, PrConstant.store1))
            p.setTotalAdd(PrConstant.store1);
        else if (TextUtils.equals(filename, PrConstant.store2))
            p.setTotalAdd(PrConstant.store2);
        return p;
    }

    public static ProductProfile setProfileForRestock(ProductProfile p, String[] value) {
        if (p == null)
            p = new ProductProfile();
        String sku = removeMark(value[1], " ");
        p.setProductName(value[0]);
        p.setSKU(sku);
        p.setCurrentQN(removeMark(value[3], " "));
        p.setRequestNumber(removeMark(value[4], " "));
        return p;
    }

    public static String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        value = "" + numericValue;
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
            Log.e("ye chen", "error " + e.toString());
        }
        return value;
    }

    public static void updateProductName(Activity activity, String type) {
        final Context context = activity.getApplicationContext();
        ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAllProfile(activity.getContentResolver());
        for (ProductProfile p : fullList) {
            if (p.getProductName() == null)
                continue;
            saveCustomKeyword(activity, p.getProductName(), type);
            Log.e("ye chen", "updateProductName " + p.getProductName());
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, "Restock List for " + PrConstant.store2 + " updating done!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static String chooseStore(String asin, String upc, Activity activity) {
        int store_a = 0;
        int store_b = 0;
        boolean inStoreA = false;
        boolean inStoreB = false;
        ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByAsin(activity.getContentResolver(), removeMark(asin, " "));
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

        if (!inStoreA)
            return PrConstant.store2;
        if (!inStoreB)
            return PrConstant.store1;

        int currentUpc_store1 = getUpcCount(activity, upc, PrConstant.shared_upc_total_store1);
        int currentUpc_store2 = getUpcCount(activity, upc, PrConstant.shared_upc_total_store2);
        int value1 = store_a - currentUpc_store1;
        int value2 = store_b - currentUpc_store2;
        Log.d("ye chen", "Store a count: " + store_a + " Store a UPC count : " + currentUpc_store1 + " Store b count: " + store_b + " Store b UPC count : " + currentUpc_store2);
        if (value1 > value2)
            return PrConstant.store1;
        else
            return PrConstant.store2;
    }

    public static int getUpcCount(Context context, String upc, String type) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        String newList = getCustomKeywordList(context, type);
        if (TextUtils.isEmpty(newList)) {
            return 0;
        } else {
            if (!newList.contains(upc)) {
                return 0;
            } else {
                String[] list = newList.split("@");
                for (int i = 0; i < list.length; i++) {
                    String[] list2 = list[i].split(";");
                    if (list2.length == 2 && TextUtils.equals(list2[0], upc)) {
                        return Integer.valueOf(list2[1]);
                    }
                }
            }
        }
        return 0;
    }

    public static List<String[]> getUpcList(Context context, String type) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        String newList = getCustomKeywordList(context, type);
        ArrayList<String[]> result = new ArrayList<String[]>();
        if (TextUtils.isEmpty(newList)) {
            return result;
        } else {
            String[] list = newList.split("@");
            for (int i = 0; i < list.length; i++) {
                String[] list2 = list[i].split(";");
                if (list2.length == 2) {
                    result.add(list2);
                }
            }
        }
        return result;
    }

    public static void generateCsvFullDB(Activity context) throws IOException {

        File csvFile;
        final String NEW_LINE_SEPARATOR = "\n";

        //CSV file header
        final Object[] FILE_HEADER = {"SKU", "Product Name", "ASIN", "FNSKU", "PRICE", "FEE", "UPC", "Request Quantity", "Store"};
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "report_amazon.csv";
        String filePath = baseDir + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + fileName;
        csvFile = new File(filePath);
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        FileWriter fileWriter = null;

        CSVPrinter csvFilePrinter = null;

        try {

            fileWriter = new FileWriter(filePath);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            BigDecimal netAmount = BigDecimal.ZERO;
            BigDecimal refundAmount = BigDecimal.ZERO;

            //Create CSV file header
            csvFilePrinter.printRecord("Store ACME");
            csvFilePrinter.printRecord(FILE_HEADER);

            ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAllProfile(context.getContentResolver());
            for (ProductProfile p : fullList) {
                if (TextUtils.equals(p.getTotalAdd(), PrConstant.store1)) {
                    ArrayList<String> lineItemDataRecord = new ArrayList<>();
                    lineItemDataRecord.add(p.getSKU());
                    lineItemDataRecord.add(p.getProductName());
                    lineItemDataRecord.add(p.getASIN());
                    lineItemDataRecord.add(p.getFNSKU());
                    lineItemDataRecord.add(p.getPrice());
                    lineItemDataRecord.add(p.getFee());
                    String upc = "upc" + p.getUPC();
                    lineItemDataRecord.add(upc);
                    lineItemDataRecord.add(p.getRequestNm());
                    lineItemDataRecord.add(p.getTotalAdd());
                    csvFilePrinter.printRecord(lineItemDataRecord);
                }
            }
            csvFilePrinter.printRecord("");
            csvFilePrinter.printRecord("");
            csvFilePrinter.printRecord("");

            csvFilePrinter.printRecord("Store HOME");
            for (ProductProfile p : fullList) {
                if (TextUtils.equals(p.getTotalAdd(), PrConstant.store2)) {
                    ArrayList<String> lineItemDataRecord = new ArrayList<>();
                    lineItemDataRecord.add(p.getSKU());
                    lineItemDataRecord.add(p.getProductName());
                    lineItemDataRecord.add(p.getASIN());
                    lineItemDataRecord.add(p.getFNSKU());
                    lineItemDataRecord.add(p.getPrice());
                    lineItemDataRecord.add(p.getFee());
                    String upc = "upc" + p.getUPC();
                    lineItemDataRecord.add(upc);
                    lineItemDataRecord.add(p.getRequestNm());
                    lineItemDataRecord.add(p.getTotalAdd());
                    csvFilePrinter.printRecord(lineItemDataRecord);
                }
            }
        } catch (Exception e) {
            Log.e("ye chen", "Error in CsvFileWriter!!!", e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                Log.e("ye chen", "Error while flushing/closing fileWriter/csvPrinter!!!", e);
            }
        }
        String subject = "总数据";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(csvFile));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivityForResult(Intent.createChooser(intent, "E-mail"), 1);

    }

    public static void generateCsvForReport(Activity context) throws IOException {

        File csvFile;
        final String NEW_LINE_SEPARATOR = "\n";

        //CSV file header
        final Object[] FILE_HEADER = {"SKU", "Product Name", "ASIN", "FNSKU", "UPC", "Request Quantity", "Store", "Total Restock"};
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "report_amazon_restock.csv";
        String filePath = baseDir + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + fileName;
        csvFile = new File(filePath);
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        FileWriter fileWriter = null;

        CSVPrinter csvFilePrinter = null;

        try {

            fileWriter = new FileWriter(filePath);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            BigDecimal netAmount = BigDecimal.ZERO;
            BigDecimal refundAmount = BigDecimal.ZERO;

            //Create CSV file header
            csvFilePrinter.printRecord("Store ACME");
            csvFilePrinter.printRecord(FILE_HEADER);

            List<String[]> mobileOrderList = getUpcList(context, PrConstant.shared_upc_total_store1);
            for (String[] mobileOrder : mobileOrderList) {
                ProductProfile keyProfile = PrManager.getManager().getDB().getAAProfileByUpc(context.getContentResolver(), mobileOrder[0]);
                if(keyProfile == null)
                    continue;;
                ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByAsin(context.getContentResolver(), removeMark(keyProfile.getASIN(), " "));
                for (ProductProfile p : fullList) {
                    if (TextUtils.equals(p.getTotalAdd(), PrConstant.store1)) {
                        ArrayList<String> lineItemDataRecord = new ArrayList<>();
                        lineItemDataRecord.add(p.getSKU());
                        lineItemDataRecord.add(p.getProductName());
                        lineItemDataRecord.add(p.getASIN());
                        lineItemDataRecord.add(p.getFNSKU());
                        lineItemDataRecord.add(p.getUPC());
                        lineItemDataRecord.add(p.getRequestNm());
                        lineItemDataRecord.add(p.getTotalAdd());
                        lineItemDataRecord.add(mobileOrder[1]);
                        csvFilePrinter.printRecord(lineItemDataRecord);
                    }
                }
            }
            csvFilePrinter.printRecord("");
            csvFilePrinter.printRecord("");
            csvFilePrinter.printRecord("");

            List<String[]> mobileOrderList2 = getUpcList(context, PrConstant.shared_upc_total_store2);
            csvFilePrinter.printRecord("Store HOME");
            for (String[] mobileOrder : mobileOrderList2) {
                ProductProfile keyProfile = PrManager.getManager().getDB().getAAProfileByUpc(context.getContentResolver(), mobileOrder[0]);
                if(keyProfile == null)
                    continue;;
                ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByAsin(context.getContentResolver(), removeMark(keyProfile.getASIN(), " "));
                for (ProductProfile p : fullList) {
                    if (TextUtils.equals(p.getTotalAdd(), PrConstant.store2)) {
                        ArrayList<String> lineItemDataRecord = new ArrayList<>();
                        lineItemDataRecord.add(p.getSKU());
                        lineItemDataRecord.add(p.getProductName());
                        lineItemDataRecord.add(p.getASIN());
                        lineItemDataRecord.add(p.getFNSKU());
                        lineItemDataRecord.add(p.getUPC());
                        lineItemDataRecord.add(p.getRequestNm());
                        lineItemDataRecord.add(p.getTotalAdd());
                        lineItemDataRecord.add(mobileOrder[1]);
                        csvFilePrinter.printRecord(lineItemDataRecord);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ye chen", "Error in CsvFileWriter!!!", e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                Log.e("ye chen", "Error while flushing/closing fileWriter/csvPrinter!!!", e);
            }
        }
        String subject = "补货单";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(csvFile));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivityForResult(Intent.createChooser(intent, "E-mail"), 3);
    }

    public static void reduceShippedItem(Activity activity) {
        int store_a = 0;
        int store_b = 0;
        List<String[]> list1 = getUpcList(activity, PrConstant.shared_upc_total_store2);
        List<String[]> list2 = getUpcList(activity, PrConstant.shared_upc_total_store2);
        for (int i = 0; i < list1.size(); i++) {
            for (int j = 0; j < list2.size(); j++) {
                if (TextUtils.equals(list1.get(i)[0], list2.get(j)[0])) {
                    list2.get(j)[0] = "0";
                }
            }
        }
        for (String[] mobileOrder : list1) {
            int currentUpc_store1 = getUpcCount(activity, mobileOrder[0], PrConstant.shared_upc_total_store1);
            int currentUpc_store2 = getUpcCount(activity, mobileOrder[0], PrConstant.shared_upc_total_store2);
            ProductProfile keyProfile = PrManager.getManager().getDB().getAAProfileByUpc(activity.getContentResolver(), mobileOrder[0]);
            if(keyProfile == null)
                continue;;
            ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByAsin(activity.getContentResolver(), keyProfile.getASIN());
            for (ProductProfile p : fullList) {
                try {
                    if (TextUtils.equals(PrConstant.store1, p.getTotalAdd())) {
                        String s = p.getRequestNm();
                        if (s != null) {
                            float temp = Float.valueOf(s);
                            store_a = (int) temp;
                            if (store_a < currentUpc_store1) {
                                store_a = 0;
                                currentUpc_store1 = currentUpc_store1 - store_a;
                            } else
                                store_a = store_a - currentUpc_store1;
                            p.setRequestNumber(String.valueOf(store_a));
                            PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), p);
                        }
                    }
                    if (TextUtils.equals(PrConstant.store2, p.getTotalAdd())) {
                        String s = p.getRequestNm();
                        if (s != null) {
                            float temp = Float.valueOf(s);
                            store_b = (int) temp;
                            if (store_b < currentUpc_store2) {
                                store_b = 0;
                                currentUpc_store2 = currentUpc_store2 - store_b;
                            } else
                                store_b = store_b - currentUpc_store2;
                            p.setRequestNumber(String.valueOf(store_b));
                            PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), p);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ye chen", e.toString());
                }
            }
        }

        for (String[] mobileOrder : list2) {
            int currentUpc_store1 = getUpcCount(activity, mobileOrder[0], PrConstant.shared_upc_total_store1);
            int currentUpc_store2 = getUpcCount(activity, mobileOrder[0], PrConstant.shared_upc_total_store2);
            ProductProfile keyProfile = PrManager.getManager().getDB().getAAProfileByUpc(activity.getContentResolver(), mobileOrder[0]);
            if(keyProfile == null)
                continue;;
            ArrayList<ProductProfile> fullList = (ArrayList<ProductProfile>) PrManager.getManager().getDB().getAAProfileListByAsin(activity.getContentResolver(), keyProfile.getASIN());
            for (ProductProfile p : fullList) {
                if (TextUtils.equals(PrConstant.store1, p.getTotalAdd())) {
                    String s = p.getRequestNm();
                    if (s != null) {
                        float temp = Float.valueOf(s);
                        store_a = (int) temp;
                        if (store_a < currentUpc_store1) {
                            store_a = 0;
                            currentUpc_store1 = currentUpc_store1 - store_a;
                        } else
                            store_a = store_a - currentUpc_store1;
                        p.setRequestNumber(String.valueOf(store_a));
                        PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), p);
                    }
                }
                if (TextUtils.equals(PrConstant.store2, p.getTotalAdd())) {
                    String s = p.getRequestNm();
                    if (s != null) {
                        float temp = Float.valueOf(s);
                        store_b = (int) temp;
                        if (store_b < currentUpc_store2) {
                            store_b = 0;
                            currentUpc_store2 = currentUpc_store2 - store_b;
                        } else
                            store_b = store_b - currentUpc_store2;
                        p.setRequestNumber(String.valueOf(store_b));
                        PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), p);
                    }
                }
            }
        }
    }
}