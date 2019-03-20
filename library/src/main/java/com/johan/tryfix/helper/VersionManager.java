package com.johan.tryfix.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by johan on 2019/3/15.
 */

public class VersionManager {

    public static int getVersion(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("TryFix", Context.MODE_PRIVATE);
        return preferences.getInt("version", 0);
    }

    public static void setVersion(Context context, int version) {
        SharedPreferences preferences = context.getSharedPreferences("TryFix", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("version", version);
        editor.commit();
    }

}
