package com.vinaysshenoy.routz;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by vinaysshenoy on 26/12/15.
 */
public class Utils {

    private static final String TAG = "Utils";

    private Utils() {

    }

    public static void printBundle(@Nullable Bundle bundle) {

        if (bundle == null) {
            Log.d(TAG, "Bundle is null");
        } else {
            Log.d(TAG, "Bundle Size: " + bundle.size());
            Object object;
            for (String key : bundle.keySet()) {
                object = bundle.get(key);
                Log.d(TAG, key + " => " + (object == null ? "null" : object.toString()));
            }

        }
    }
}
