package com.johan.fix;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.johan.tryfix.TryFix;
import com.johan.tryfix.helper.FixFileHelper;
import com.johan.tryfix.loader.FixSOLoader;

/**
 * Created by johan on 2019/3/15.
 */

public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        TryFix.debug();
        TryFix.patch(base);
    }

}
