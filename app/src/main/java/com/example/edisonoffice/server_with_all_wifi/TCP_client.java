package com.example.edisonoffice.server_with_all_wifi;

/**
 * Created by edison office on 8/26/2019.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import static com.example.edisonoffice.server_with_all_wifi.Configuration_class.CMD;
import static com.example.edisonoffice.server_with_all_wifi.Configuration_class.SERVER_IP_TCP_;
import static com.example.edisonoffice.server_with_all_wifi.Configuration_class.SERVER_PORT_TCP;

public class TCP_client {

    public static final String TAG = TCP_client.class.getSimpleName();
    public static final String SERVER_IP = ""; //server IP address
    public static final int SERVER_PORT =0;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    public static boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    String s1,s2;
    Socket socket;
    public static boolean statuss;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCP_client(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;
        try {
            socket.close();
           statuss= socket.isClosed();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }
  //  class  Client implements Runnable {
        public void run() {

            mRun = true;

            try {
                //here you must put your computer's IP address.
                //  InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                Log.d("TCP Client", "C: Connecting...");

                //create a socket to make the connection with the server
                socket = new Socket(SERVER_IP_TCP_, SERVER_PORT_TCP);

                try {

                    //sends the message to the server
                    mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    //receives the message which the server sends back
                    mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    if (socket != null) {
                        mBufferOut.write(CMD);
                        mBufferOut.flush();
                    }


                    //in this while the client listens for the messages sent by the server
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    final StringBuffer buffer = new StringBuffer();

                    while (mRun) {
                        char ch = (char) mBufferIn.read();
                        s1 = Character.toString(ch);
                        buffer.append((char) ch);
                        s2=buffer.toString();
                        if(s2.endsWith("#")){
                          mMessageListener.messageReceived(s2);
                          buffer.setLength(0);


                        }
                        else {


                        }




                        /*if(!s1.equals("#")){
                            buffer.append((char) ch);
                            Log.e("TCP", "S: received   buffrt"+ buffer.append((char) ch) );
                            if(s1.equals("#"))
                            s1 = buffer.toString();
                            Log.e("TCP", "S: received..s1"+ s1 );
                            if(buffer.length()==7||buffer.length()==8){
                                mMessageListener.messageReceived(s1);
                            }

                        }*/

                       /* if ((ch < 0) || (ch == '#')) {
                            // continue;
                            break;

                        }*/
                      //  buffer.append((char) ch);
                    }
                    s1 = buffer.toString();
                    mMessageListener.messageReceived(s1);

                    Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + s1 + "'");

                } catch (Exception e) {
                    Log.e("TCP", "S: Error", e);
                } /*finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }*/

            } catch (Exception e) {
                Log.e("TCP", "C: Error", e);
            }

        }
  //  }
    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
