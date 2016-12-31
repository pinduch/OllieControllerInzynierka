package com.android.projects.mateusz.olliecontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.android.projects.mateusz.olliecontroller.common.Constant;
import com.android.projects.mateusz.olliecontroller.model.ClientModel;

public class SettingsActivity extends AppCompatActivity {

    public EditText[] txtHostAddress;

    private ClientModel clientModel;
    private String hostAddress;
    private View decorView;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_settings);
        decorView.setSystemUiVisibility(6);

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                decorView.setSystemUiVisibility(Constant.UI_OPTIONS);
            }
        });

        clientModel = ClientModel.getInstance();
        txtHostAddress = new EditText[] {
                (EditText) findViewById(R.id.txtHostAddress1),
                (EditText) findViewById(R.id.txtHostAddress2),
                (EditText) findViewById(R.id.txtHostAddress3),
                (EditText) findViewById(R.id.txtHostAddress4)};

        for (int i = 0; i < txtHostAddress.length; i++){
            txtHostAddress[i].setText(clientModel.getHostAddress().getHostAddress().split("\\.")[i]);
        }

        preferences = this.getSharedPreferences(Constant.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);

    }

    public void saveSettings(final View view){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(Constant.SETTINGS_ALERT_TITLE);
        alertDialog.setMessage(Constant.SETTING_ALERT_MESSAGE);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hostAddress = "";
                for (int i = 0; i < txtHostAddress.length; i++) {
                    if ( i != txtHostAddress.length - 1 )
                        hostAddress += txtHostAddress[i].getText().toString() + ".";
                    else
                        hostAddress += txtHostAddress[i].getText().toString();
                }

                clientModel.setHostAddress(hostAddress);
                preferences.edit().putString(Constant.HOST_ADDRESS_KEY, hostAddress).apply();
                finish();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void backToMainMenu(View view){
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(Constant.UI_OPTIONS);
        }
    }

}
