
package com.hx.campus.utils.common;


import android.content.Context;
import android.os.Parcelable;

import com.tencent.mmkv.MMKV;

import java.util.Set;


public final class MMKVUtils {

    private static MMKV sMMKV;

    private MMKVUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    
    public static void init(Context context) {
        MMKV.initialize(context.getApplicationContext());
        sMMKV = MMKV.defaultMMKV();
    }

    public static MMKV getsMMKV() {
        if (sMMKV == null) {
            sMMKV = MMKV.defaultMMKV();
        }
        return sMMKV;
    }



    
    public static boolean put(String key, Object value) {
        if (value instanceof Integer) {
            return getsMMKV().encode(key, (Integer) value);
        } else if (value instanceof Float) {
            return getsMMKV().encode(key, (Float) value);
        } else if (value instanceof String) {
            return getsMMKV().encode(key, (String) value);
        } else if (value instanceof Boolean) {
            return getsMMKV().encode(key, (Boolean) value);
        } else if (value instanceof Long) {
            return getsMMKV().encode(key, (Long) value);
        } else if (value instanceof Double) {
            return getsMMKV().encode(key, (Double) value);
        } else if (value instanceof Parcelable) {
            return getsMMKV().encode(key, (Parcelable) value);
        } else if (value instanceof byte[]) {
            return getsMMKV().encode(key, (byte[]) value);
        } else if (value instanceof Set) {
            return getsMMKV().encode(key, (Set<String>) value);
        }
        return false;
    }




    
    public static Object get(String key, Object defaultValue) {
        if (defaultValue instanceof Integer) {
            return getsMMKV().decodeInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Float) {
            return getsMMKV().decodeFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof String) {
            return getsMMKV().decodeString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return getsMMKV().decodeBool(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Long) {
            return getsMMKV().decodeLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Double) {
            return getsMMKV().decodeDouble(key, (Double) defaultValue);
        } else if (defaultValue instanceof byte[]) {
            return getsMMKV().decodeBytes(key);
        } else if (defaultValue instanceof Set) {
            return getsMMKV().decodeStringSet(key, (Set<String>) defaultValue);
        }
        return null;
    }


    
    public static boolean getBoolean(String key, boolean defValue) {
        try {
            return getsMMKV().getBoolean(key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    
    public static long getLong(String key, long defValue) {
        try {
            return getsMMKV().getLong(key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    
    public static float getFloat(String key, float defValue) {
        try {
            return getsMMKV().getFloat(key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    
    public static String getString(String key, String defValue) {
        try {
            return getsMMKV().getString(key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    
    public static int getInt(String key, int defValue) {
        try {
            return getsMMKV().getInt(key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }


    
    public static double getDouble(String key, double defValue) {
        try {
            return getsMMKV().decodeDouble(key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }


    
    public static <T extends Parcelable> T getObject(String key, Class<T> tClass) {
        return getsMMKV().decodeParcelable(key, tClass);
    }

    
    public static <T extends Parcelable> T getObject(String key, Class<T> tClass, T defValue) {
        try {
            return getsMMKV().decodeParcelable(key, tClass, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }


    
    public static boolean containsKey(String key) {
        return getsMMKV().containsKey(key);
    }

    
    public static void remove(String key) {
        getsMMKV().remove(key).apply();
    }

}
