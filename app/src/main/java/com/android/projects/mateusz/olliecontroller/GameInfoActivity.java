package com.android.projects.mateusz.olliecontroller;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.projects.mateusz.olliecontroller.common.Constant;
import com.android.projects.mateusz.olliecontroller.common.ServerRequest;

public class GameInfoActivity extends AppCompatActivity {

    public enum GameInfoState {NO_CONNECTION, CONNECTED, USERNAME_AVAILABLE, FREE_RIDE}

    private CheckBox chbServerConnection;
    private CheckBox chbUsernameAvailable;
    private CheckBox chbFreeRide;
    private EditText txtUsername;
    private Button btnConnect;
    private Button btnStart;
    private Button btnCheck;
    private ProgressBar progressBarConnectingToServer;
    private ProgressBar progressBarCheckingUsername;
    private View decorView;

    private TCPClient tcpClient;
    private GameInfoState gameInfoState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(Constant.UI_OPTIONS);
        setContentView(R.layout.activity_game_info);
        decorView.setSystemUiVisibility(6);

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                decorView.setSystemUiVisibility(Constant.UI_OPTIONS);
            }
        });

        initElements();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(Constant.UI_OPTIONS);
        }
    }

    public void backToMainMenu(View view){
        finish();
    }

    public void checkUsernameAvailability(View view){
        chbUsernameAvailable.setChecked(!chbUsernameAvailable.isChecked());
        new AsyncTask<Void, Void, Void>() {

            String username;

            @Override
            protected void onPreExecute() {
                btnCheck.setVisibility(View.GONE);
                progressBarCheckingUsername.setVisibility(View.VISIBLE);
                username = txtUsername.getText().toString();
            }

            @Override
            protected Void doInBackground(Void... params) {
                tcpClient.sendMessageToServer("CHECK USERNAME");
                tcpClient.sendMessageToServer(username);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                btnCheck.setVisibility(View.VISIBLE);
                progressBarCheckingUsername.setVisibility(View.GONE);
            }
        }.execute();
    }

    public void connectToServer(View view){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                btnConnect.setVisibility(View.GONE);
                progressBarConnectingToServer.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                tcpClient = TCPClient.getInstance();
                tcpClient.start();
                try {
                    tcpClient.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                btnConnect.setVisibility(View.VISIBLE);
                progressBarConnectingToServer.setVisibility(View.GONE);

                switch (tcpClient.getTcpConnectionState()) {
                    case CONNECT:
                        chbServerConnection.setChecked(true);
                        break;
                    case DISCONNECT:
                        chbServerConnection.setChecked(false);
                        break;
                }
            }

        }.execute();

    }

    private void initElements(){
        // initial elements
        chbUsernameAvailable = (CheckBox) findViewById(R.id.chbUsernameAvailable);
        chbServerConnection = (CheckBox) findViewById(R.id.chbServerConnection);
        chbFreeRide = (CheckBox) findViewById(R.id.chbFreeRide);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        progressBarConnectingToServer = (ProgressBar) findViewById(R.id.progressBarConnectingToServer);
        progressBarCheckingUsername = (ProgressBar) findViewById(R.id.progressBarCheckingUsername);

        // visibility of elements
        btnConnect.setVisibility(View.VISIBLE);
        btnCheck.setVisibility(View.VISIBLE);
        progressBarConnectingToServer.setVisibility(View.GONE);
        progressBarCheckingUsername.setVisibility(View.GONE);

        // enabled elements
        btnStart.setEnabled(false);
        btnCheck.setEnabled(false);

        // set checkbox
        setCheckBox(chbUsernameAvailable, Color.RED, Constant.USERNAME_NOT_AVAILABLE, false);
        setCheckBox(chbServerConnection, Color.RED, Constant.SERVER_NO_CONNECTION, false);

        // listeners
        chbUsernameAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBox(chbUsernameAvailable, Color.GREEN, Constant.USERNAME_AVAILABLE, true);
                    btnCheck.setEnabled(false);
                    btnStart.setEnabled(true);
                } else {
                    setCheckBox(chbUsernameAvailable, Color.RED, Constant.USERNAME_NOT_AVAILABLE, false);
                    btnCheck.setEnabled(true);
                    btnStart.setEnabled(false);
                }
            }
        });

        chbServerConnection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBox(chbServerConnection, Color.GREEN, Constant.SERVER_CONNECTED, true);
                    btnConnect.setEnabled(false);
                    btnCheck.setEnabled(true);
                } else {
                    setCheckBox(chbServerConnection, Color.RED, Constant.SERVER_NO_CONNECTION, false);
                    btnConnect.setEnabled(true);
                    btnCheck.setEnabled(false);
                }
            }
        });

        chbFreeRide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    btnCheck.setEnabled(false);
                    btnConnect.setEnabled(false);
                    btnStart.setEnabled(true);
                } else {
                    btnCheck.setEnabled(chbServerConnection.isChecked() && !chbUsernameAvailable.isChecked());
                    btnConnect.setEnabled(!chbServerConnection.isChecked());
                    btnStart.setEnabled(chbServerConnection.isChecked() && chbUsernameAvailable.isChecked());
                }
            }
        });

        txtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (chbUsernameAvailable.isChecked() && !chbFreeRide.isChecked())
                    chbUsernameAvailable.setChecked(false);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void setCheckBox(CheckBox checkBox, int color, String textValue, boolean checked){
        checkBox.setChecked(checked);
        checkBox.setTextColor(color);
        checkBox.setText(textValue);
    }

    @Override
    public void onStop(){
        super.onStop();
//        tcpClient.sendMessageToServer(ServerRequest.DISCONNECT);
    }

    @Override
    public void onDestroy(){
        if (tcpClient.getTcpConnectionState().equals(TCPClient.TcpConnectionState.CONNECT))
            tcpClient.sendMessageToServer(ServerRequest.DISCONNECT);
        super.onDestroy();
    }

}