package com.android.projects.mateusz.olliecontroller;
//
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.projects.mateusz.olliecontroller.common.Constant;
import com.android.projects.mateusz.olliecontroller.common.ServerRequest;


public class PlayingActivity extends AppCompatActivity {

    public RelativeLayout layoutWithJoyStick;
    public JoyStick joyStick;
    public CheckBox chbCalibrate;
    public TextView labelConnecting;
    public ProgressBar progressBarConnecting;
    public Button btnMoveBack;
    public Button btnEndRace;
    public Button btnExitToMainMenu;

    private TCPClient tcpClient;
    private OllieController ollie;

    private boolean startRide = false;
    private boolean freeRide;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(Constant.UI_OPTIONS);
        setContentView(R.layout.activity_playing);

        freeRide = getIntent().getBooleanExtra(Constant.FREE_RIDE, false);

        ollie = OllieController.getInstance();
        tcpClient = TCPClient.getInstance();
        startRide = false;

        initialElements();
        initialJoyStick();
        showConnectingElements();
        if (!ollie.isConnected()) {
            ollie.startDiscovery(this);
        }
        connectToOllie();

        chbCalibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ollie.setCalibration(isChecked);
                if ( !isChecked ) joyStick.startPosition();
            }
        });

        btnMoveBack = (Button) findViewById(R.id.btnMoveBack);
        btnMoveBack.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    ollie.getConvenienceRobot().setRawMotors(2, 80, 2, 80);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ollie.stopDrive();
                }
                return false;
            }
        });


        layoutWithJoyStick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(!ollie.isCalibration()){
                    joyStick.drawStick(arg1);
                    if (arg1.getAction() == MotionEvent.ACTION_UP) {
                        ollie.stopDrive();
                    } else {
                        ollie.drive(joyStick.getStickAngle(), joyStick.getJoyStickPower());
                        if (!startRide && tcpClient != null){
                            startRide = true;
                            tcpClient.sendMessageToServer(ServerRequest.START_RACE);
                            btnExitToMainMenu.setEnabled(false);
                        }
                    }
                } else {
                    joyStick.drawCalibrationStick(arg1);
                    ollie.rotate(joyStick.getStickAngle());
                    System.out.println(joyStick.getStickAngle());
                }
                return true;
            }
        });
    }

    public void endGame(View view){
        if (startRide && tcpClient != null){
            tcpClient.sendMessageToServer(ServerRequest.END_RACE);
            btnExitToMainMenu.setEnabled(true);
            finish();
        }
    }


    private void connectToOllie(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                while ( true ) {
                    if ( ollie.getConvenienceRobot() != null ){
                        if ( ollie.getConvenienceRobot().isConnected() ) break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                showPlayingElements();
            }
        }.execute();
    }


    private void initialElements(){
        layoutWithJoyStick = (RelativeLayout) findViewById(R.id.layout_joystick);
        chbCalibrate = (CheckBox) findViewById(R.id.chbCalibrate);
        progressBarConnecting = (ProgressBar) findViewById(R.id.progressBarConnecting);
        labelConnecting = (TextView) findViewById(R.id.labelConnecting);
        btnEndRace = (Button) findViewById(R.id.btnEndRace);
        btnExitToMainMenu = (Button) findViewById(R.id.btnExitToMainMenu);
    }

    private void initialJoyStick(){
        Point screenSizePx = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSizePx);
        int joystickLayoutSize = (int) ( screenSizePx.x * Constant.JOYSTICK_SIZE_BY_SCREEN_PERCENT );

        joyStick = new JoyStick(getApplicationContext(), layoutWithJoyStick, R.drawable.image_button2);
        joyStick.setStickSize(400, 400);
        joyStick.setLayoutSize(joystickLayoutSize, joystickLayoutSize);
        joyStick.setLayoutAlpha(150);
        joyStick.setStickAlpha(255);
        joyStick.setDistanceFromEdge(200);
        joyStick.setMinDistFromCenterJoyStick(50);
        joyStick.startPosition();

        layoutWithJoyStick.setGravity(Gravity.CENTER);
    }

    private void showPlayingElements(){
        chbCalibrate.setVisibility(View.VISIBLE);
        layoutWithJoyStick.setVisibility(View.VISIBLE);
        progressBarConnecting.setVisibility(View.GONE);
        labelConnecting.setVisibility(View.GONE);
        if (!freeRide) {
            btnEndRace.setVisibility(View.VISIBLE);
        }
    }

    private void showConnectingElements(){
        chbCalibrate.setVisibility(View.GONE);
        layoutWithJoyStick.setVisibility(View.GONE);
        progressBarConnecting.setVisibility(View.VISIBLE);
        labelConnecting.setVisibility(View.VISIBLE);
        btnEndRace.setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(Constant.UI_OPTIONS);
        }
    }

    public void backToMainMenu(View view){
        finish();
    }


    @Override
    public void onDestroy(){
//        if (tcpClient != null)
//            tcpClient.sendMessageToServer(ServerRequest.DISCONNECT);
        super.onDestroy();
    }

}