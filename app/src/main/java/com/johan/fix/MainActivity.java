package com.johan.fix;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.johan.ndk.Test;
import com.johan.tryfix.TryFix;
import com.johan.tryfix.helper.FixFileHelper;
import com.johan.tryfix.loader.FixSOLoader;
import com.johan.tryfix.service.FixReceiver;

/**
 * Created by johan on 2019/3/14.
 */

public class MainActivity extends AppCompatActivity implements PermissionHelper.OnPermissionCallback {

    private TextView testView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testView = (TextView) findViewById(R.id.test_view);
        testView.setText("=============>" + Test.getInfo() + "\n" + new Error().error());
        PermissionHelper.requestPermission(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, this);
        FixReceiver.register(this, fixReceiver);
    }

    @Override
    protected void onDestroy() {
        FixReceiver.unregister(this, fixReceiver);
        super.onDestroy();
    }

    private FixReceiver fixReceiver = new FixReceiver() {
        @Override
        protected void onReceiveResult(int version, int code, String message) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionHelper.handlePermissionResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionAccept(int requestCode, String... permissions) {
        TryFix.fix(this, 1, "http://99.56888.net/wlapp/download/test/patch.apk");
    }

    @Override
    public void onPermissionRefuse(int requestCode, String... permission) {

    }

}
