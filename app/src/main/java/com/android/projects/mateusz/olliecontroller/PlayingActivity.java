package com.android.projects.mateusz.olliecontroller;
//
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.android.projects.mateusz.olliecontroller.model.ClientModel;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Ollie;
import com.orbotix.async.AsyncMessage;
import com.orbotix.async.CollisionDetectedAsyncMessage;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.common.DiscoveryAgent;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.response.DeviceResponse;
import com.orbotix.subsystem.SensorControl;

import java.util.List;


public class PlayingActivity extends AppCompatActivity {

    public RelativeLayout layoutWithJoyStick;
    public JoyStick joyStick;
    public CheckBox chbCalibrate;
    public TextView labelConnecting;
    public ProgressBar progressBarConnecting;
    public Button btnMoveBack;

    private DiscoveryAgent discoveryAgent;
    private ConvenienceRobot convenienceRobot;
    private boolean calibration = false;

    private TCPClient tcpClient;
    private ClientModel clientModel;

    private boolean collisionDetected = false;
    private int toCollisionCountDetectReset = 0;
    private boolean finishRide = false;
    private boolean startRide = false;

    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private String rideTime = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(Constant.UI_OPTIONS);
        setContentView(R.layout.activity_playing);

        tcpClient = TCPClient.getInstance();
        clientModel = ClientModel.getInstance();
        startRide = false;

        initialElements();
        initialJoyStick();
        showConnectingElements();
        startDiscovery();
        connectToOllie();

        chbCalibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calibration = isChecked;
                convenienceRobot.calibrating(calibration);
                if ( !isChecked ) joyStick.startPosition();
            }
        });



        btnMoveBack = (Button) findViewById(R.id.btnMoveBack);
        btnMoveBack.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    convenienceRobot.setRawMotors(2, 80, 2, 80);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    convenienceRobot.stop();
                }
                return false;
            }
        });


//        com.erz.joysticklibrary.JoyStick joyStickTmp;
//        joyStickTmp = (com.erz.joysticklibrary.JoyStick) findViewById(R.id.joy1);
//        joyStickTmp.setButtonDrawable(R.drawable.image_button2);
//        joyStickTmp.setAlpha(0.8f);
//        com.erz.joysticklibrary.JoyStick.JoyStickListener joyStickListener = new com.erz.joysticklibrary.JoyStick.JoyStickListener() {
//            @Override
//            public void onMove(com.erz.joysticklibrary.JoyStick joyStick, double angle, double power) {
//                System.out.println("Angle: " + joyStickTmp.getAngleDegrees() + "          Power: " + joyStickTmp.getPower());
//            }
//        };
//        joyStickTmp.setListener(joyStickListener);


        layoutWithJoyStick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (!calibration) {
                    joyStick.drawStick(arg1);
                    if (arg1.getAction() == MotionEvent.ACTION_UP) {
                        convenienceRobot.stop();
                    } else {
                        convenienceRobot.drive(joyStick.getStickAngle(), joyStick.getJoyStickPower());
                        if (!startRide){
                            startRide = true;
                            startTime = SystemClock.uptimeMillis();
                            customHandler.postDelayed(updateTimerThread, 0);
                        }
                    }
                } else {
                    joyStick.drawCalibrationStick(arg1);
                    convenienceRobot.rotate(joyStick.getStickAngle());
                    System.out.println(joyStick.getStickAngle());
                }
                return true;
            }
        });
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            rideTime = "" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds);
            customHandler.postDelayed(this, 0);
        }
    };


    private void connectToOllie(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                while ( true ) {
                    if ( convenienceRobot != null ) {
                        if ( convenienceRobot.isConnected() ) break;
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

    private void startDiscovery(){
        discoveryAgent = new DiscoveryAgentLE();
        discoveryAgent.addDiscoveryListener(discoveryAgentEventListener);
        discoveryAgent.addRobotStateListener(robotChangedStateListener);
        try {
            discoveryAgent.startDiscovery(this);
        } catch (DiscoveryException e){
            Log.e("Connecting", "Failed to start discovery because " + e);
            e.printStackTrace();
        }
    }

    private void stopDiscovery(){
        discoveryAgent.stopDiscovery();
        discoveryAgent.removeDiscoveryListener(discoveryAgentEventListener);
        discoveryAgent.removeRobotStateListener(robotChangedStateListener);
        discoveryAgent = null;
    }


    private DiscoveryAgentEventListener discoveryAgentEventListener = new DiscoveryAgentEventListener() {
        @Override
        public void handleRobotsAvailable(List<Robot> robots) {
            Log.i("Connecting", "Found" + robots);
        }
    };

    private RobotChangedStateListener robotChangedStateListener = new RobotChangedStateListener() {
        @Override
        public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    SensorFlag sensorFlag = new SensorFlag(
                            SensorFlag.SENSOR_FLAG_VELOCITY,
                            SensorFlag.SENSOR_FLAG_LOCATOR,
                            SensorFlag.SENSOR_FLAG_QUATERNION,
                            SensorFlag.SENSOR_FLAG_ACCELEROMETER_NORMALIZED,
                            SensorFlag.SENSOR_FLAG_GYRO_NORMALIZED,
                            SensorFlag.SENSOR_FLAG_MOTOR_BACKEMF_NORMALIZED,
                            SensorFlag.SENSOR_FLAG_ATTITUDE);

                    Log.i("Connecting", robot + " Online !");
                    convenienceRobot = new Ollie(robot);
                    stopDiscovery();
                    convenienceRobot.setLed(0.f, 1.f, 0.f);


//                    convenienceRobot.enableStabilization(false);
                    convenienceRobot.enableCollisions(true);
                    convenienceRobot.enableSensors(sensorFlag, SensorControl.StreamingRate.STREAMING_RATE20);
                    convenienceRobot.addResponseListener(responseListener);
                    break;
            }
        }
    };

    private ResponseListener responseListener = new ResponseListener() {
        @Override
        public void handleResponse(DeviceResponse deviceResponse, Robot robot) {

        }

        @Override
        public void handleStringResponse(String s, Robot robot) {

        }



        @Override
        public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {
            if (asyncMessage instanceof DeviceSensorAsyncMessage) {
                DeviceSensorAsyncMessage message = (DeviceSensorAsyncMessage) asyncMessage;

                if (message.getSensorDataFrames() == null
                        || message.getSensorDataFrames().isEmpty()
                        || message.getSensorDataFrames().get(0) == null) return;

                DeviceSensorsData data = message.getSensorDataFrames().get(0);

                double x = data.getAccelerometerData().getFilteredAcceleration().x;
                double y = data.getAccelerometerData().getFilteredAcceleration().y;
                double z = data.getAccelerometerData().getFilteredAcceleration().z;

                double tmp = z * 10;
                if ( tmp > -0.15 && tmp < 0.15 && collisionDetected && toCollisionCountDetectReset > 0) {
//                    tcpClient.sendMessageToServer("FINISH     " + countCollisionOnMeta);
                    finishRide = true;
                    toCollisionCountDetectReset = 0;

                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);

                    tcpClient.sendMessageToServer(clientModel.getActualUserName() + " - result: " + rideTime);
                    System.out.println(clientModel.getActualUserName() + " - result: " + rideTime);
                } else {
                    if ( toCollisionCountDetectReset <= 0 )
                        collisionDetected = false;
                    else toCollisionCountDetectReset--;
                }

            }

            if( asyncMessage instanceof CollisionDetectedAsyncMessage) {
                //Collision occurred.
                if(!collisionDetected) {
                    collisionDetected = true;
                    toCollisionCountDetectReset = 100;
                }
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    };



    private void initialElements(){
        layoutWithJoyStick = (RelativeLayout) findViewById(R.id.layout_joystick);
        chbCalibrate = (CheckBox) findViewById(R.id.chbCalibrate);
        progressBarConnecting = (ProgressBar) findViewById(R.id.progressBarConnecting);
        labelConnecting = (TextView) findViewById(R.id.labelConnecting);
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
    }

    private void showConnectingElements(){
        chbCalibrate.setVisibility(View.GONE);
        layoutWithJoyStick.setVisibility(View.GONE);
        progressBarConnecting.setVisibility(View.VISIBLE);
        labelConnecting.setVisibility(View.VISIBLE);
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

        if (convenienceRobot != null) {
            convenienceRobot.disconnect();
            convenienceRobot = null;
        }
        if (tcpClient != null)
            tcpClient.sendMessageToServer(ServerRequest.DISCONNECT);
        super.onDestroy();
    }

}