package com.android.projects.mateusz.olliecontroller;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import com.android.projects.mateusz.olliecontroller.stateEnums.TCPStates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Mateusz on 19.11.2016.
 *
 * TCP Client class.
 */

public class TCPClient extends Thread {

    private BufferedReader in;
    private PrintWriter out;
    private Activity activity;
    private InetAddress serverAddress;
    private TextView textView;
    private String messageFromServer;


    /**
     * TCPClient class constructor.
     *
     * @param serverAddress - server address IP.
     * @param activity - View which start connection to server ( probably it will be delete )
     */
    public TCPClient(InetAddress serverAddress, Activity activity){
        this.activity = activity;
        this.serverAddress = serverAddress;
        this.textView = (TextView) activity.findViewById(R.id.message);
    }

    @Override
    public void run(){
        try {
            Socket socket = new Socket(serverAddress, 9090);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            getMessageFromServer();

            if (ApplicationController.getClientState().equals(TCPStates.DISCONNECT)) {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to start new thread in which is started listener for server response.
     */
    private void getMessageFromServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (ApplicationController.getClientState().equals(TCPStates.CONNECT)) {
                    try {
                        messageFromServer = in.readLine();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(messageFromServer);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Method which allow to send data to server.
     *
     * @param messageToSend - String data to send.
     */
    public void sendMessageToServer(String messageToSend){
        out.println(messageToSend);
    }

}
