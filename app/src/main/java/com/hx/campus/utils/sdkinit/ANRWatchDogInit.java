
package com.hx.campus.utils.sdkinit;

import com.github.anrwatchdog.ANRWatchDog;
import com.xuexiang.xutil.common.logger.Logger;


public final class ANRWatchDogInit {

    private static final String TAG = "ANRWatchDog";
    
    private static final int ANR_DURATION = 4000;
    
    private final static ANRWatchDog.ANRListener SILENT_LISTENER = error -> Logger.eTag(TAG, error);
    
    private final static ANRWatchDog.ANRListener CUSTOM_LISTENER = error -> {
        Logger.eTag(TAG, "Detected Application Not Responding!", error);


        throw error;
    };
    
    private static ANRWatchDog sANRWatchDog;

    private ANRWatchDogInit() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void init() {

        sANRWatchDog = new ANRWatchDog(2000);
        sANRWatchDog.setANRInterceptor(duration -> {
            long ret = ANR_DURATION - duration;
            if (ret > 0) {
                Logger.wTag(TAG, "Intercepted ANR that is too short (" + duration + " ms), postponing for " + ret + " ms.");
            }

            return ret;
        }).setANRListener(SILENT_LISTENER).start();
    }

    public static ANRWatchDog getANRWatchDog() {
        return sANRWatchDog;
    }
}
