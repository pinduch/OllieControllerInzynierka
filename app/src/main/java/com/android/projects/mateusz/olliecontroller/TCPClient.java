package com.android.projects.mateusz.olliecontroller;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;

import com.android.projects.mateusz.olliecontroller.common.ServerRequest;
import com.android.projects.mateusz.olliecontroller.model.ClientModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by Mateusz on 19.11.2016.
 *
 * TCP Client class.
 */

public class TCPClient extends Thread implements Serializable {

    public enum TcpConnectionState {CONNECT, DISCONNECT}

    private BufferedReader in;
    private PrintWriter out;
    private String serverResponse;
    private ClientModel clientModel;
    private Socket socket;

    private String response;


    private static TCPClient instance = null;
    private TcpConnectionState tcpConnectionState;

    public static void setInstance(TCPClient tcpClient){
        instance = tcpClient;
    }

    public static TCPClient getInstance(){
        if (instance == null){
            instance = new TCPClient();
        }
        return instance;
    }

//    public static TCPClient getInstance(){
//        if (instance == null){
//            instance = new TCPClient();
//        }
//        return instance;
//    }
//
//    public static TCPClient reset(){
//        instance = new TCPClient();
//        return instance;
//    }


    /**
     * TCPClient class constructor.
     */
    public TCPClient(){
        clientModel = ClientModel.getInstance();
        tcpConnectionState = TcpConnectionState.DISCONNECT;
    }

    public TcpConnectionState getTcpConnectionState() {
        return tcpConnectionState;
    }

    public void setTcpConnectionState(TcpConnectionState tcpConnectionState) {
        this.tcpConnectionState = tcpConnectionState;
    }

    @Override
    public void run(){
        try {
            socket = new Socket();
            InetSocketAddress socketAddress = new InetSocketAddress(clientModel.getHostAddress(), 9090);
            socket.connect(socketAddress, 2000);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            tcpConnectionState = TcpConnectionState.CONNECT;

            getMessageFromServer();
            sendMessageToServer(ServerRequest.CONNECT);
            sendMessageToServer(Build.MODEL);

        } catch (IOException e) {
//            instance = null;
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
                while (tcpConnectionState.equals(TcpConnectionState.CONNECT)) {
                    try {
                        serverResponse = in.readLine();
                        if (serverResponse != null) {
                            clientModel.setServerResponse(serverResponse);

                            if (serverResponse.equals(ServerRequest.SERVER_SHUTDOWN)){
                                closeSocket();
                            }
                        }

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
        if (tcpConnectionState.equals(TcpConnectionState.CONNECT)) {
            out.println(messageToSend);
        }
    }

    /**
     * Method to check if client is connected to server.
     *
     * @return
     */
    public boolean isConnectionWithServer(){
        if ( tcpConnectionState.equals(TcpConnectionState.CONNECT))
            return true;
        else
            return false;
    }

    /**
     * Method to close socket.
     */
    public void closeSocket(){
        try {
            if (socket != null) socket.close();
            tcpConnectionState = TcpConnectionState.DISCONNECT;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
