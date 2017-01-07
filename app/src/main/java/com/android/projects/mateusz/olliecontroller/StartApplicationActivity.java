package com.android.projects.mateusz.olliecontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.projects.mateusz.olliecontroller.common.Constant;
import com.android.projects.mateusz.olliecontroller.common.ServerRequest;
import com.android.projects.mateusz.olliecontroller.model.ClientModel;

import java.util.ArrayList;
import java.util.List;

public class StartApplicationActivity extends AppCompatActivity {

    private ClientModel clientModel;
    private SharedPreferences preferences;
    private TCPClient tcpClient;
    private BluetoothAdapter bluetoothAdapter;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(Constant.UI_OPTIONS);
        setContentView(R.layout.activity_start_application);

        preferences = this.getSharedPreferences(Constant.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        clientModel = ClientModel.getInstance();
        clientModel.setHostAddress(preferences.getString(Constant.HOST_ADDRESS_KEY, "0.0.0.0"));

        requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1 );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED){
                Log.e("Ollie", "Location permission has not already been granted");
                List<String> permission = new ArrayList<>();
                permission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permission.toArray(new String[permission.size()]), Constant.REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                Log.d("Ollie", "Location permission already granted");
            }
        }

        checkNfcAndBluetoothEnabled();
    }

    private boolean checkNfcAndBluetoothEnabled(){
        if (!isBluetoothEnabled() && !isNfcEnabled()){
            alert(Constant.BLUETOOTH_NFC_TITLE, Constant.BLUETOOTH_NFC_MESSAGE);
            return false;
        } else if(!isNfcEnabled()){
            alert(Constant.NFC_TITLE, Constant.NFC_MESSAGE);
            return false;
        } else if (!isBluetoothEnabled() ) {
            alert(Constant.BLUETOOTH_TITLE, Constant.BLUETOOTH_MESSAGE);
            return false;
        }
        return true;
    }

    private void alert(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private boolean isBluetoothEnabled(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNfcEnabled(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(Constant.UI_OPTIONS);
        }
    }

    public void goToGameInfoActivity(View view){
        if (checkNfcAndBluetoothEnabled()) {
            Intent intent = new Intent(this, GameInfoActivity.class);
            startActivity(intent);
        }
    }

    public void goToSettingsActivity(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    public void exitApplication(View view){
        OllieController.getInstance().disconnect();
        tcpClient = TCPClient.getInstance();
        if (tcpClient != null ) {
            tcpClient.sendMessageToServer(ServerRequest.DISCONNECT);
            tcpClient.closeSocket();
            TCPClient.setInstance(null);
        }
        this.finishAffinity();
    }
}
