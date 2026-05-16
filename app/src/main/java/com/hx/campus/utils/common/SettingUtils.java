package com.hx.campus.utils.common;



public final class SettingUtils {

    private static final String IS_FIRST_OPEN_KEY = "is_first_open_key";
    private static final String IS_AGREE_PRIVACY_KEY = "is_agree_privacy_key";

    private SettingUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    
    public static boolean isFirstOpen() {
        return MMKVUtils.getBoolean(IS_FIRST_OPEN_KEY, true);
    }

    
    public static void setIsFirstOpen(boolean isFirstOpen) {
        MMKVUtils.put(IS_FIRST_OPEN_KEY, isFirstOpen);
    }

    
    public static boolean isAgreePrivacy() {
        return MMKVUtils.getBoolean(IS_AGREE_PRIVACY_KEY, false);
    }

    
    public static void setIsAgreePrivacy(boolean isAgreePrivacy) {
        MMKVUtils.put(IS_AGREE_PRIVACY_KEY, isAgreePrivacy);
    }


}
