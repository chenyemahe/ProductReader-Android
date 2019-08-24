
package com.acme.productreader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;


import com.acme.productreader.database.PrProvider;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
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

            ArrayList<ProductProfile> list = sortListMap.get(indexYear).get(indexMonth-1);
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
            if(TextUtils.isEmpty(keySort)) {
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

            ArrayList<ProductProfile> list = sortListMap.get(indexStoreList).get(indexMonth-1);
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
            if(!newList.contains(keywords)) {
                newList = newList + "," + keywords;
                Log.d(context.getClass().toString(), "keyword update save checked keywords: " + keywords);
            }
        }
        return prefs.edit().putString(type_key, newList).commit();
    }

    public static String getCustomKeywordList(Context context, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        return prefs.getString(type_key, "");
    }

    public static boolean removeCustomKeyword(Context context,String s, String type_key) {
        SharedPreferences prefs = context.getSharedPreferences(CASHBACK_PREFS, 0);
        String oldList = getCustomKeywordList(context, type_key);
        String newList = "";
        if (TextUtils.isEmpty(oldList)) {
            return true;
        } else if (TextUtils.equals(oldList, s)) {
            newList = "";
        } else {
            String[] temp = oldList.split(",");
            for(int i = 0; i < temp.length; i++ ) {
                if (!TextUtils.equals(temp[i], s)) {
                    if(TextUtils.isEmpty(newList)) {
                        newList = temp[i];
                    } else {
                        newList += "," + temp[i];
                    }
                }
            }
        }
        Log.d(context.getClass().toString(), "keyword remove save checked keywords: " +  s);
        return prefs.edit().putString(type_key, newList).commit();
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
        if(list != null) {
            for(ProductProfile p : list) {
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
        for(ProductProfile p : fullList) {
            if(isOver30Days(p.getDate()) && TextUtils.equals(p.getPrice(), context.getResources().getStringArray(R.array.list_of_status)[1])) {
                list.add(p);
            }
        }
        return list;
    }

    public static boolean isValidDateFormate(String date, Context context) {
        String[] dates = date.split("/");
        if(dates.length != 3) {
            return false;
        }
        if(dates[0].length() != 2 && dates[1].length() != 2 && dates[2].length() != 4) {
            return false;
        }
        String[] mm = context.getResources().getStringArray(R.array.mm);
        if (!isContent(mm,dates[0])) {
            return false;
        }
        String[] day = context.getResources().getStringArray(R.array.dd);
        if (!isContent(day,dates[1])) {
            return false;
        }
        String[] year = context.getResources().getStringArray(R.array.year);
        if (!isContent(year,dates[2])) {
            return false;
        }
        return true;
    }

    public static boolean isContent(String[] array, String s) {
        boolean temp = false;
        for(int i = 0; i < array.length; i++) {
            if(TextUtils.equals(array[i],s)){
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

    public static void readExcelFileFromAssets(Activity activity, String filePath, int type) {

        Log.d("ye chen", "readExcelFileFromAssets");
        InputStream stream = activity.getResources().openRawResource(R.raw.home);
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int r = 0; r<rowsCount; r++) {
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                String[] value = new String[6];
                for (int c = 0; c<6; c++) {
                    value[c] = getCellAsString(row, c, formulaEvaluator);
                }
                if(type == MenuPage.PICKFILE_RESULT_CODE_1) {
                    ProductProfile profile = PrManager.getManager().getDB().getAAProfileBySKU(activity.getContentResolver(),value[0]);
                    if (profile == null) {
                        profile = setProfile(value, "home");
                        PrManager.getManager().getDB().saveCbProfile(activity.getContentResolver(), profile);
                    } else {
                        PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), profile);
                    }
                }
            }
            /*
            // Create a POI File System object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            // We now need something to iterate through the cells.
            Iterator<Row> rowIter = mySheet.rowIterator();
            int rowno = 0;
            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                if (rowno != 0) {
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int totalRom = myRow.getPhysicalNumberOfCells();
                    int colno = 0;
                    String[] value = new String[6];
                    Log.d("ye chen", value.toString());
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        value[colno] = myCell.toString();
                        colno++;
                    }
                    if(type == MenuPage.PICKFILE_RESULT_CODE_1) {
                        ProductProfile profile = PrManager.getManager().getDB().getAAProfileBySKU(activity.getContentResolver(),value[0]);
                        if (profile == null) {
                            profile = setProfile(value);
                            PrManager.getManager().getDB().saveCbProfile(activity.getContentResolver(), profile);
                        } else {
                            PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), profile);
                        }
                    }
                }
                rowno++;
            }*/
        } catch (Exception e) {
            Log.e("ye chen", "error " + e.toString());
        }

        stream = activity.getResources().openRawResource(R.raw.acme);
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int r = 0; r<rowsCount; r++) {
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                String[] value = new String[6];
                for (int c = 0; c<6; c++) {
                    value[c] = getCellAsString(row, c, formulaEvaluator);
                }
                if(type == MenuPage.PICKFILE_RESULT_CODE_1) {
                    ProductProfile profile = PrManager.getManager().getDB().getAAProfileBySKU(activity.getContentResolver(),value[0]);
                    if (profile == null) {
                        profile = setProfile(value, "acme");
                        PrManager.getManager().getDB().saveCbProfile(activity.getContentResolver(), profile);
                    } else {
                        PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), profile);
                    }
                }
            }
            /*
            // Create a POI File System object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            // We now need something to iterate through the cells.
            Iterator<Row> rowIter = mySheet.rowIterator();
            int rowno = 0;
            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                if (rowno != 0) {
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int totalRom = myRow.getPhysicalNumberOfCells();
                    int colno = 0;
                    String[] value = new String[6];
                    Log.d("ye chen", value.toString());
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        value[colno] = myCell.toString();
                        colno++;
                    }
                    if(type == MenuPage.PICKFILE_RESULT_CODE_1) {
                        ProductProfile profile = PrManager.getManager().getDB().getAAProfileBySKU(activity.getContentResolver(),value[0]);
                        if (profile == null) {
                            profile = setProfile(value);
                            PrManager.getManager().getDB().saveCbProfile(activity.getContentResolver(), profile);
                        } else {
                            PrManager.getManager().getDB().updateAAProfile(activity.getContentResolver(), profile);
                        }
                    }
                }
                rowno++;
            }*/
        } catch (Exception e) {
            Log.e("ye chen", "error " + e.toString());
        }
    }

    public static ProductProfile setProfile(String[] value, String filename) {
        ProductProfile p = new ProductProfile();
        p.setSKU(value[0]);
        p.setProductName(value[1]);
        p.setASIN(value[2]);
        p.setFNSKU(value[3]);
        p.setPrice(value[4]);
        p.setAmazonFee(value[5]);
        if(TextUtils.equals(filename,"acme"))
            p.setTotalAdd("acme");
        else if(TextUtils.equals(filename, "home"))
            p.setTotalAdd("home");
        Log.d("ye chen", p.toString());
        return p;
    }

    public static String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = ""+cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        value = ""+numericValue;
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = ""+cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
            Log.e("ye chen", "error " + e.toString());
        }
        return value;
    }
}
