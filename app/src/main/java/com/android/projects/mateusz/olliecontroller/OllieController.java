package com.android.projects.mateusz.olliecontroller;

import android.content.Context;
import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.Ollie;
import com.orbotix.async.AsyncMessage;
import com.orbotix.async.CollisionDetectedAsyncMessage;
import com.orbotix.common.DiscoveryAgent;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.response.DeviceResponse;
import com.orbotix.subsystem.SensorControl;

import java.util.List;

/**
 * Created by Mateusz on 05.01.2017.
 */

public class OllieController {

    private DiscoveryAgent discoveryAgent;
    private ConvenienceRobot convenienceRobot;
    private boolean calibration = false;
    private static OllieController instance;

    public static OllieController getInstance() {
        if (instance == null ) {
            instance = new OllieController();
        }
        return instance;
    }

    public boolean isConnected(){
        if (convenienceRobot != null){
            if (convenienceRobot.isConnected()){
                return true;
            } else return false;
        } else return false;
    }

    public void disconnect(){
        if (convenienceRobot != null) {
            convenienceRobot.disconnect();
            convenienceRobot = null;
        }
    }

    public boolean isCalibration() {
        return calibration;
    }

    public void setCalibration(boolean calibration) {
        this.calibration = calibration;
        convenienceRobot.calibrating(this.calibration);
    }

    public ConvenienceRobot getConvenienceRobot() {
        return convenienceRobot;
    }

    public void setConvenienceRobot(ConvenienceRobot convenienceRobot) {
        this.convenienceRobot = convenienceRobot;
    }

    public void stopDrive(){
        convenienceRobot.stop();
    }

    public void drive(float angle, float velocity){
        convenienceRobot.drive(angle, velocity);
    }

    public void rotate(float angle){
        convenienceRobot.rotate(angle);
    }

    public void startDiscovery(Context context){
        discoveryAgent = new DiscoveryAgentLE();
        discoveryAgent.addDiscoveryListener(discoveryAgentEventListener);
        discoveryAgent.addRobotStateListener(robotChangedStateListener);
        try {
            discoveryAgent.startDiscovery(context);
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

            if( asyncMessage instanceof CollisionDetectedAsyncMessage) {
                //Collision occurred.
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    };

}
