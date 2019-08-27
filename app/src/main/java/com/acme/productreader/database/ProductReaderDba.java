
package com.acme.productreader.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.acme.productreader.PrUtils;
import com.acme.productreader.ProductProfile;

import java.util.ArrayList;
import java.util.List;

public class ProductReaderDba {

    private static final String TAG = "ProductReaderDba";

    private static ProductReaderDba mDba;

    // Query string constants to work with database.
    private static String PROFILE_SELECTION_BY_DATE = PrProvider.ProfileColumns.ORDER_DATE + " LIKE ? ";

    public static String PROFILE_SELECTION_BY_CASHBACK_STATE = PrProvider.ProfileColumns.ORDER_PRICE + " LIKE ? ";

    private static String PROFILE_SELECTION_BY_ID = PrProvider.ProfileColumns._ID + " LIKE ? ";

    private static String PROFILE_SELECTION_BY_SKU = PrProvider.ProfileColumns.ORDER_SKU + " LIKE ? ";
    private static String PROFILE_SELECTION_BY_FSKU = PrProvider.ProfileColumns.ORDER_FNSKU + " LIKE ? ";
    private static String PROFILE_SELECTION_BY_UPC = PrProvider.ProfileColumns.ORDER_UPC + " LIKE ? ";
    private static String PROFILE_SELECTION_BY_NAME = PrProvider.ProfileColumns.ORDER_ID + " LIKE ? ";
    private static String PROFILE_SELECTION_BY_ASIN = PrProvider.ProfileColumns.ORDER_ASIN + " LIKE ? ";


    public static String ID_SELECTION = BaseColumns._ID + "=?";

    public static ProductReaderDba getDB() {
        if (mDba == null)
            mDba = new ProductReaderDba();
        return mDba;
    }

    /**
     * Save order
     *
     * @param cr
     * @param profile
     * @return
     */
    public Uri saveCbProfile(ContentResolver cr, ProductProfile profile) {
        if (profile == null) {
            return null;
        }

        ContentValues values = new ContentValues();
        PrUtils.toContentValues(profile, values);

        Log.d(TAG, "insert order " + profile.getDate());
        return cr.insert(PrProvider.ProfileColumns.CONTENT_URI, values);
    }

    public List<ProductProfile> getAllProfile(ContentResolver cr) {
        List<ProductProfile> profileList = null;
        ProductProfile profile = null;
        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                profileList = new ArrayList<ProductProfile>();
                do {
                    profile = new ProductProfile();
                    PrUtils.fromCursor(cursor, profile);
                    profileList.add(profile);
                } while (cursor.moveToNext());

            }
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profileList;
    }

    public ProductProfile getAAProfileBySKU(ContentResolver cr, String sku) {
        ProductProfile profile = null;
        Log.d(TAG, "{getAAProfile} the SKU is : " + sku);
        if (sku == null)
            return null;

        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, PROFILE_SELECTION_BY_SKU,
                    new String[]{
                            sku
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profile = new ProductProfile();
                PrUtils.fromCursor(cursor, profile);

            }
        } catch (SQLException e) {
            Log.e(TAG, "Error in retrieve Date: " + sku + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profile;
    }

    public ProductProfile getAAProfileByAsin(ContentResolver cr, String asin) {
        ProductProfile profile = null;
        Log.d(TAG, "{getAAProfile} the SKU is : " + asin);
        if (asin == null)
            return null;

        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, PROFILE_SELECTION_BY_ASIN,
                    new String[]{
                            asin
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profile = new ProductProfile();
                PrUtils.fromCursor(cursor, profile);

            }
        } catch (SQLException e) {
            Log.e(TAG, "Error in retrieve Date: " + asin + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profile;
    }

    public ProductProfile getAAProfileByFSKU(ContentResolver cr, String fsku) {
        ProductProfile profile = null;
        Log.d(TAG, "{getAAProfile} the FSKU is : " + fsku);
        if (fsku == null)
            return null;

        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, PROFILE_SELECTION_BY_FSKU,
                    new String[]{
                            fsku
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profile = new ProductProfile();
                PrUtils.fromCursor(cursor, profile);

            }
        } catch (SQLException e) {
            Log.e(TAG, "Error in retrieve Date: " + fsku + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profile;
    }

    public ProductProfile getAAProfileByUpc(ContentResolver cr, String upc) {
        ProductProfile profile = null;
        Log.d(TAG, "{getAAProfile} the FSKU is : " + upc);
        if (upc == null)
            return null;

        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, PROFILE_SELECTION_BY_UPC,
                    new String[]{
                            upc
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profile = new ProductProfile();
                PrUtils.fromCursor(cursor, profile);

            }
        } catch (SQLException e) {
            Log.e(TAG, "Error in retrieve Date: " + upc + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profile;
    }

    public List<ProductProfile> getAAProfileListByName(ContentResolver cr, String name) {
        List<ProductProfile> profileList = null;
        ProductProfile profile = null;
        Log.d(TAG, "{getAAProfile} the Name is : " + name);
        if (name == null)
            return null;

        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, PROFILE_SELECTION_BY_NAME,
                    new String[]{
                            name
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profileList = new ArrayList<ProductProfile>();
                do {
                    profile = new ProductProfile();
                    PrUtils.fromCursor(cursor, profile);
                    profileList.add(profile);
                } while (cursor.moveToNext());

            }
        } catch (SQLException e) {
            Log.e(TAG, "Error in retrieve Date: " + name + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profileList;
    }

    public List<ProductProfile> getAAProfileListByAsin(ContentResolver cr, String asin) {
        List<ProductProfile> profileList = null;
        ProductProfile profile = null;
        Log.d(TAG, "{getAAProfile} the asin is : " + asin);
        if (asin == null)
            return null;

        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, PROFILE_SELECTION_BY_ASIN,
                    new String[]{
                            asin
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profileList = new ArrayList<ProductProfile>();
                do {
                    profile = new ProductProfile();
                    PrUtils.fromCursor(cursor, profile);
                    profileList.add(profile);
                } while (cursor.moveToNext());

            }
        } catch (SQLException e) {
            Log.e(TAG, "Error in retrieve Date: " + asin + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profileList;
    }

    public ProductProfile getAAProfileById(ContentResolver cr, String id) {
        ProductProfile profile = null;
        Log.d(TAG, "{getAAProfile} the ID is : " + id);
        if (id == null)
            return null;

        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, PROFILE_SELECTION_BY_ID,
                    new String[]{
                            id
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profile = new ProductProfile();
                PrUtils.fromCursor(cursor, profile);

            }
        } catch (SQLException e) {
            Log.e(TAG, "Error in retrieve Date: " + id + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profile;
    }

    public List<ProductProfile> getCbProfileBySelection(ContentResolver cr, String selection, String key) {
        List<ProductProfile> profileList = null;
        Log.d(TAG, "{getAAProfile} the KEY is : " + key);
        if (key == null)
            return null;

        ProductProfile profile = null;
        Cursor cursor = null;

        try {
            cursor = cr.query(PrProvider.ProfileColumns.CONTENT_URI, null, selection,
                    new String[]{
                            key
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                profileList = new ArrayList<ProductProfile>();
                do {
                    profile = new ProductProfile();
                    PrUtils.fromCursor(cursor, profile);
                    profileList.add(profile);
                } while (cursor.moveToNext());

            }
        } catch (SQLException e) {
            Log.d(TAG, "Error in retrieve Key: " + key, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return profileList;
    }

    public int deleteAAProfile(ContentResolver cr, ProductProfile profile) {
        int count = 0;
        if (profile != null) {
            count = cr.delete(PrProvider.ProfileColumns.CONTENT_URI, ID_SELECTION, new String[]{
                    profile.getId()
            });
        }
        return count;
    }

    public int deleteAAProfile(ContentResolver cr, String id) {
        int count = 0;
        ProductProfile profile = getAAProfileById(cr, id);
        if (profile != null) {
            count = cr.delete(PrProvider.ProfileColumns.CONTENT_URI, ID_SELECTION, new String[]{
                    profile.getId()
            });
        }
        return count;
    }

    public int updateAAProfile(ContentResolver cr, ProductProfile profile) {
        if (profile == null) {
            return 0;
        }
        ContentValues contentValues = new ContentValues();
        PrUtils.toContentValues(profile, contentValues);
        return cr.update(PrProvider.ProfileColumns.CONTENT_URI, contentValues, ID_SELECTION, new String[] {
                profile.getId()
        });
    }
}
