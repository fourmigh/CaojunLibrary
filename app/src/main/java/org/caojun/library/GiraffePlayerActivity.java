package org.caojun.library;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.caojun.giraffeplayer.GiraffePlayer;
import org.caojun.giraffeplayer.PlayerManager;

/**
 * Created by TangChao on 2017/6/15.
 */

public class GiraffePlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GiraffePlayer.Companion.setDebug(true);//show java logs
        GiraffePlayer.Companion.setNativeDebug(false);//not show native logs

        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new GiraffePlayerFragment()).commit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                Toast.makeText(this, "please grant read permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PlayerManager.Companion.getInstance().onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (PlayerManager.Companion.getInstance().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

}
