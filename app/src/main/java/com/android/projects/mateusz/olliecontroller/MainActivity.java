package com.android.projects.mateusz.olliecontroller;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.projects.mateusz.olliecontroller.model.ClientModel;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Ollie;
import com.orbotix.async.AsyncMessage;
import com.orbotix.async.CollisionDetectedAsyncMessage;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.command.ConfigureLocatorCommand;
import com.orbotix.common.DiscoveryAgent;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.sensor.AccelerometerData;
import com.orbotix.common.sensor.AttitudeSensor;
import com.orbotix.common.sensor.BackEMFSensor;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.GyroData;
import com.orbotix.common.sensor.QuaternionSensor;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.response.DeviceResponse;
import com.orbotix.response.GetOdometerResponse;
import com.orbotix.subsystem.SensorControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final float ROBOT_VELOCITY = 0.2f;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

    private TCPClient tcpClient;
    private ClientModel clientModel;

    private DiscoveryAgent discoveryAgent;
    private ConvenienceRobot convenienceRobot;

    private Button mBtn0;
    private Button mBtn90;
    private Button mBtn180;
    private Button mBtn270;
    private Button mBtnStop;
    private Button mBtnConfiguration;
    private EditText mEditTextNewX;
    private EditText mEditTextNewY;
    private EditText mEditTextNewYaw;
    private CheckBox mCheckboxFlag;

    private TextView mTextViewLocatorX;
    private TextView mTextViewLocatorY;
    private TextView mTextViewLocatorVX;
    private TextView mTextViewLocatorVY;


    private TextView mAccelX;
    private TextView mAccelY;
    private TextView mAccelZ;
    private TextView mYawValue;
    private TextView mRollValue;
    private TextView mPitchValue;
    private TextView mQ0Value;
    private TextView mQ1Value;
    private TextView mQ2Value;
    private TextView mQ3Value;
    private TextView mGyroX;
    private TextView mGyroY;
    private TextView mGyroZ;
    private TextView mLeftMotor;
    private TextView mRightMotor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();


//        ApplicationController.setClientState(TCPStates.CONNECT);
//        textView.setText(clientModel.getServerResponse());

//        tcpClient = TCPClient.getInstance();
//        tcpClient.start();

        requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1 );


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED){
                Log.e("Ollie", "Location permission has not already been granted");
                List<String> permission = new ArrayList<>();
                permission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permission.toArray(new String[permission.size()]), REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                Log.d("Ollie", "Location permission already granted");
            }
        }

        startDiscovery();

    }

    public void initViews(){
        mBtn0 = (Button) findViewById(R.id.btn_0);
        mBtn90 = (Button) findViewById(R.id.btn_90);
        mBtn180 = (Button) findViewById(R.id.btn_180);
        mBtn270 = (Button) findViewById(R.id.btn_270);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnConfiguration = (Button) findViewById(R.id.btn_configure);
        mEditTextNewX = (EditText) findViewById(R.id.edit_new_x);
        mEditTextNewY = (EditText) findViewById(R.id.edit_new_y);
        mEditTextNewYaw = (EditText) findViewById(R.id.edit_new_yaw);
        mCheckboxFlag = (CheckBox) findViewById(R.id.checkbox_flag);
        mTextViewLocatorX = (TextView) findViewById(R.id.txt_locator_x);
        mTextViewLocatorY = (TextView) findViewById(R.id.txt_locator_y);
        mTextViewLocatorVX = (TextView) findViewById(R.id.txt_locator_vx);
        mTextViewLocatorVY = (TextView) findViewById(R.id.txt_locator_vy);


        mAccelX = (TextView) findViewById(R.id.mAccelX);
        mAccelY = (TextView) findViewById(R.id.mAccelY);
        mAccelZ = (TextView) findViewById(R.id.mAccelZ);

        mRollValue = (TextView) findViewById(R.id.mRollValue);
        mPitchValue = (TextView) findViewById(R.id.mPitchValue);
        mYawValue = (TextView) findViewById(R.id.mYawValue);

        mQ0Value = (TextView) findViewById(R.id.mQ0Value);
        mQ1Value = (TextView) findViewById(R.id.mQ1Value);
        mQ2Value = (TextView) findViewById(R.id.mQ2Value);
        mQ3Value = (TextView) findViewById(R.id.mQ3Value);

        mGyroX = (TextView) findViewById(R.id.mGyroX);
        mGyroY = (TextView) findViewById(R.id.mGyroY);
        mGyroZ = (TextView) findViewById(R.id.mGyroZ);

        mLeftMotor = (TextView) findViewById(R.id.mLeftMotor);
        mRightMotor = (TextView) findViewById(R.id.mRightMotor);

        mBtn0.setOnClickListener(onClickListener);
        mBtn90.setOnClickListener(onClickListener);
        mBtn180.setOnClickListener(onClickListener);
        mBtn270.setOnClickListener(onClickListener);
        mBtnStop.setOnClickListener(onClickListener);
        mBtnConfiguration.setOnClickListener(onClickListener);

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
            if( deviceResponse instanceof GetOdometerResponse) {
                GetOdometerResponse odometerResponse = (GetOdometerResponse) deviceResponse;
                long lifetimeDistanceRolled = odometerResponse.getDistanceInCentimeters();
                System.out.println("!!!!!!!!!!! + " + lifetimeDistanceRolled + " !!!!!!!!!!!!!!1");
            }
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

                float positionX = message.getSensorDataFrames().get(0).getLocatorData().getPositionX();
                float positionY = message.getSensorDataFrames().get(0).getLocatorData().getPositionY();
                float velocityX = message.getSensorDataFrames().get(0).getLocatorData().getVelocity().x;
                float velocityY = message.getSensorDataFrames().get(0).getLocatorData().getVelocity().y;

                mTextViewLocatorX.setText(positionX + " cm");
                mTextViewLocatorY.setText(positionY + " cm");
                mTextViewLocatorVX.setText(velocityX + " cm/s");
                mTextViewLocatorVY.setText(velocityY + " cm/s");

                //Retrieve DeviceSensorsData from the async message
                DeviceSensorsData data = message.getSensorDataFrames().get(0);

                //Extract the accelerometer data from the sensor data
                displayAccelerometer(data.getAccelerometerData());

                //Extract attitude data (yaw, roll, pitch) from the sensor data
                displayAttitude(data.getAttitudeData());

                //Extract quaternion data from the sensor data
                displayQuaterions(data.getQuaternion());

                //Display back EMF data from left and right motors
                displayBackEMF(data.getBackEMFData().getEMFFiltered());

                //Extract gyroscope data from the sensor data
                displayGyroscope(data.getGyroData());
            }

            if( asyncMessage instanceof CollisionDetectedAsyncMessage ) {
                //Collision occurred.
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    };


    private void displayBackEMF(BackEMFSensor sensor) {
        if (sensor == null) return;
        mLeftMotor.setText(String.valueOf(sensor.leftMotorValue));
        mRightMotor.setText(String.valueOf(sensor.rightMotorValue));
    }

    private void displayGyroscope(GyroData data) {
        mGyroX.setText(String.valueOf(data.getRotationRateFiltered().x));
        mGyroY.setText(String.valueOf(data.getRotationRateFiltered().y));
        mGyroZ.setText(String.valueOf(data.getRotationRateFiltered().z));
    }

    private void displayAccelerometer(AccelerometerData accelerometer) {
        if (accelerometer == null || accelerometer.getFilteredAcceleration() == null) {
            return;
        }
        //Display the readings from the X, Y and Z components of the accelerometer
        mAccelX.setText(String.format("%.2f", accelerometer.getFilteredAcceleration().x * 100));
        mAccelY.setText(String.format("%.2f", accelerometer.getFilteredAcceleration().y * 100));
        mAccelZ.setText(String.format("%.2f", accelerometer.getFilteredAcceleration().z * 100));
    }

    private void displayAttitude(AttitudeSensor attitude) {
        if (attitude == null)
            return;

        //Display the pitch, roll and yaw from the attitude sensor
        mRollValue.setText(String.format("%3d", attitude.roll) + "°");
        mPitchValue.setText(String.format("%3d", attitude.pitch) + "°");
        mYawValue.setText(String.format("%3d", attitude.yaw) + "°");
    }

    private void displayQuaterions(QuaternionSensor quaternion) {
        if (quaternion == null)
            return;

        //Display the four quaterions data
        mQ0Value.setText(String.format("%.5f", quaternion.getQ0()));
        mQ1Value.setText(String.format("%.5f", quaternion.getQ1()));
        mQ2Value.setText(String.format("%.5f", quaternion.getQ2()));
        mQ3Value.setText(String.format("%.5f", quaternion.getQ3()));

    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (convenienceRobot == null){
                return;
            }

            switch (v.getId()){
                case R.id.btn_0:
                    convenienceRobot.drive(0.0f, ROBOT_VELOCITY);
                    break;
                case R.id.btn_90:
                    convenienceRobot.drive(90.0f, ROBOT_VELOCITY);
                    break;
                case R.id.btn_180:
                    convenienceRobot.drive(180.0f, ROBOT_VELOCITY);
                    break;
                case R.id.btn_270:
                    convenienceRobot.drive(270.0f, ROBOT_VELOCITY);
                    break;
                case R.id.btn_stop:
                    convenienceRobot.stop();
                    break;
                case R.id.btn_configure:
                    configureLocator();
                    break;
            }
        }
    };

    private void configureLocator(){
        if (convenienceRobot == null) return;

        int newX = 0;
        int newY = 0;
        int newYaw = 0;

        try {
            newX = Integer.parseInt(mEditTextNewX.getText().toString());
        } catch (NumberFormatException e) {}

        try {
            newY = Integer.parseInt(mEditTextNewY.getText().toString());
        } catch (NumberFormatException e) {}

        try {
            newYaw = Integer.parseInt(mEditTextNewYaw.getText().toString());
        } catch (NumberFormatException e) {}

        int flag = mCheckboxFlag.isChecked() ?
                ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_ON
                : ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_OFF;

        convenienceRobot.sendCommand(new ConfigureLocatorCommand(flag, newX, newY, newYaw));
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



    float r = 0.0f, g = 0.0f, b = 0.0f;
    public void changeColor(View view){
        Random random = new Random();
        r = random.nextFloat();
        g = random.nextFloat();
        b = random.nextFloat();

        convenienceRobot.setLed(r, g, b);
    }

    public void sendMessage(View view){
//        tcpClient.sendMessageToServer(txtResponse.getText().toString());
//        txtResponse.setText("");

//        clientModel.setServerResponse("NEW");
    }


    @Override
    public void onStop(){
        super.onStop();
//        tcpClient.sendMessageToServer(ServerRequest.DISCONNECT);
//        ApplicationController.setClientState(TCPStates.DISCONNECT);

        if (convenienceRobot != null) {
            convenienceRobot.disconnect();
            convenienceRobot = null;
        }
    }

    @Override
    public void onDestroy(){
//        tcpClient.sendMessageToServer(ServerRequest.DISCONNECT);
//        ApplicationController.setClientState(TCPStates.DISCONNECT);

        if (convenienceRobot != null) {
            convenienceRobot.disconnect();
            convenienceRobot = null;
        }
        super.onDestroy();
    }


}
