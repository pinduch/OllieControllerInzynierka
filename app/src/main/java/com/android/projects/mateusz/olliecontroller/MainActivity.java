package com.android.projects.mateusz.olliecontroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.projects.mateusz.olliecontroller.stateEnums.TCPStates;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText txtResponse;

    InetAddress serverAddress;
    Integer i = 0;


    public TCPClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.message);
        txtResponse = (EditText) findViewById(R.id.txtResponse);

        try {
            serverAddress = InetAddress.getByName("172.17.169.0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        ApplicationController.setClientState(TCPStates.CONNECT);
        tcpClient = new TCPClient(serverAddress, this);
        tcpClient.start();

    }


    public void sendMessage(View view){
        i++;
        tcpClient.sendMessageToServer(txtResponse.getText().toString() + " #" + i.toString());
        txtResponse.setText("");
    }


    @Override
    public void onStop(){
        super.onStop();
        tcpClient.sendMessageToServer("Application is closed");
        ApplicationController.setClientState(TCPStates.DISCONNECT);
    }
}
