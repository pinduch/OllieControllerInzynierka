package com.android.projects.mateusz.olliecontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.projects.mateusz.olliecontroller.common.Constant;
import com.android.projects.mateusz.olliecontroller.model.ClientModel;

public class StartApplicationActivity extends AppCompatActivity {

    private ClientModel clientModel;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(Constant.UI_OPTIONS);
        setContentView(R.layout.activity_start_application);

        preferences = this.getSharedPreferences(Constant.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        clientModel = ClientModel.getInstance();
        clientModel.setHostAddress(preferences.getString(Constant.HOST_ADDRESS_KEY, "0.0.0.0"));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(Constant.UI_OPTIONS);
        }
    }

    public void goToGameInfoActivity(View view){
        Intent intent = new Intent(this, GameInfoActivity.class);
        startActivity(intent);
    }

    public void goToSettingsActivity(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    public void exitApplication(View view){
        this.finishAffinity();
    }
}
