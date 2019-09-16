package com.example.edisonoffice.server_with_all_wifi;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import static android.content.ContentValues.TAG;
import static com.example.edisonoffice.server_with_all_wifi.TCP_client.mRun;
import static com.example.edisonoffice.server_with_all_wifi.TCP_client.statuss;


public class Configuration_class extends AppCompatActivity implements OnMessageReceived{
    TextView tv,ipaddr1,portno,messageTv,textStatus;
    Button start_server;
    public static final int SERVER_PORT = 3000;
    public static ArrayList zigbee_ids_list;
    ArrayList<Socket> tempclientarray;
    ArrayList<Socket> tempclientarray_clint;
    ArrayList<Socket> tempclientarray_swb;
    Thread serverThread = null;
    Thread Client_sending_data = null;
    private ServerSocket serverSocket;
    int portno1;
    String message = "";
    String messagetoswb;
    static Vector<ClientHandler> ar = new Vector<>();
    private String inp;
    String data;
    String data_to_snd;


    private TextView display;
    private TextView format,baudrate,linkkey,securityenable,leavepan,established,Transmiterpowerlevel,sinkmode,identity,waitresp;
    private EditText editText;
   // private MyHandler mHandler;
    String a,b,s1,s11,s2,s22,s3,s33,s4,s44,str,str11,ucast_string;
    public static String ucast_zigbee;
    private Button retry;
    String button_data_p_or_T;
    private String currentcommand="format";
    private String coordinatorjpan="JPAN:19,7DBFF284ABA1BBF4";
    private String coordinatorID="000D6F000D9EE7A4";
    private String confdeviceID="0";
    int i=0;

  //  DataBaseHandler db = null;
//-----tcp
    TCP_client mTcpClient;
    public static  String SERVER_IP_TCP_ = ""; //server IP address
    public static  int SERVER_PORT_TCP = 0;
    public static String CMD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout relativeLayout=(RelativeLayout) findViewById(R.id.rel);
        relativeLayout.requestFocus();




        //mHandler = new MyHandler(this);

        String ipaddr = getIpAddress();
        tv = (TextView) findViewById(R.id.tv);
        ipaddr1 = (TextView) findViewById(R.id.ipaddr);
        portno = (TextView) findViewById(R.id.portno);

        ipaddr1.setText(ipaddr);
        portno.setText("PORT : " + SERVER_PORT);
        portno1= 3000;

        messageTv = (TextView) findViewById(R.id.messageTv);

        textStatus = (TextView) findViewById(R.id.status);
        start_server = (Button) findViewById(R.id.start_server);
        tempclientarray = new ArrayList<Socket>();
        tempclientarray_clint = new ArrayList<Socket>();
        tempclientarray_swb = new ArrayList<Socket>();

        start_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Starting Server...");
                messageTv.setText("");
                updateMessage("Starting Server...");
                serverThread = new Thread(new ServerThread());
                serverThread.start();


                return;
            }
        });



    display = (TextView) findViewById(R.id.textView1);
      editText = (EditText) findViewById(R.id.ettext);


        Button test = (Button) findViewById(R.id.buttonsenddss);



        Button sendButton = (Button) findViewById(R.id.buttonSend);
        retry = (Button) findViewById(R.id.retry);

      // Toast.makeText(getBaseContext(),zigbee_ids_list.toString(),Toast.LENGTH_SHORT).show();

    }


    @Override
    public void messageReceived(String message) {
        sendMessageToEveryone(message);
    }


    class ServerThread implements Runnable {
        // Vector to store active clients

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();


                     //   Log.d("TAG", "New client request received :" + socket);
                        // obtain input and output streams
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        Log.d("TAG", "Creating a new handler for this client...");

                        updateMessage("client conneted..." + socket);
                        tempclientarray.add(socket);
                        //start-----------if all are android ph both switch and client then use this
                        /*if(tempclientarray_swb.size()==0){
                            tempclientarray_swb.add(tempclientarray.get(0));
                        }
                        for(int i=1;i<tempclientarray.size();i++) {
                            if (tempclientarray.size() > 1) {
                                tempclientarray_clint.add(tempclientarray.get(i));
                            }
                        } ---------------------------------end    */
                        // Create a new handler object for handling this request.
                        ClientHandler mtch = new ClientHandler(socket, "client " + i, dis, dos);

                        // Create a new Thread with this object.
                        Thread t = new Thread(mtch);

                        Log.d("TAG", "Adding this client to active client list");

                        // add this client to active clients list
                        ar.add(mtch);

                        // start the thread.
                        t.start();

                        // increment i for new client.
                        // i is used for naming only, and can be replaced
                        // by any naming scheme
                        i++;
                       /* CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class ClientHandler implements Runnable {
        // Scanner scn = new Scanner(System.in);
        private String name;
        final DataInputStream dis;
        DataOutputStream dos;
        Socket s;
        boolean isloggedin;
        private BufferedReader input;

        // constructor
        public ClientHandler(Socket s, String name,
                             DataInputStream dis, DataOutputStream dos) {
            this.dis = dis;
            this.dos = dos;
            this.name = name;
            this.s = s;
            this.isloggedin = true;

            try {
                this.input = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            String received;
            while (true) {
                int charsRead = 0;
                char[] buffer = new char[1024];
                try {

                    while ((charsRead = input.read(buffer)) != -1) {
                        message += new String(buffer).substring(0, charsRead);
                        //  tempBuf2 = new byte[Integer.parseInt(message)];
                        //   Log.d("TAG", "message" + message);
                        //   final String readMessage = new String(readBuf, 0,readBuf.length);

                        messagetoswb = message;
                        if (message.equals("logout\n")) {
                           /* this.isloggedin = false;
                            this.s.close();
                            tempclientarray.remove(s);*/

                            mTcpClient.stopClient();
                            break;
                        }
                     //   updateMessage(getTime() + " | Client : " + message);
                        //  Log.d("TAG", message);
                        messagetoswb = message;
                        if(messagetoswb.equals("a\n")){
                            SERVER_IP_TCP_="192.168.1.30";
                            SERVER_PORT_TCP=500;
                            CMD="a";

                            if (mRun==true) {
                                mTcpClient.sendMessage("a");
                            }else if(statuss ==true ||mRun==false){
                                new ConnectTask().execute(CMD);
                            }

                        }
                        if(messagetoswb.equals("b\n")){
                            SERVER_IP_TCP_="192.168.1.31";
                            SERVER_PORT_TCP=1000;
                            CMD="b";
                            if (mRun==true) {
                                mTcpClient.sendMessage("b");
                            }else if(statuss ==true ||mRun==false){
                                new ConnectTask().execute(CMD);
                            }

                          //  new ConnectTask().execute(CMD);
                           /* if (mTcpClient != null) {
                                mTcpClient.sendMessage("b");
                            }*/

                        }

                        //start-----------if all are android ph both switch and client then use this
                       /* if (message.endsWith("*\n")) {// send to swb
                              for (Socket s : tempclientarray_swb) {
                                  try {
                                    s.getOutputStream().write(message.getBytes());
                                    s.getOutputStream().flush();
                                    //   Log.d("TAG", "send to client looop........" + msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                        if (message.endsWith("#\n")) {//send to clint
                            sendMessageToEveryone(message);
                        }*/
//      //-----------------------------------------------------end
                        messagetoswb = "";
                        message = "";

                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }


                try {
                    // closing resources
                    this.dis.close();
                    this.dos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    //sending mess to all clients
    public void sendMessageToEveryone(String msg) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        for (Socket s : tempclientarray) {
            //     Log.d("TAG", "send to client looop........tempclientarray...." + tempclientarray);
            //     Log.d("TAG", "send to client looop........1");
            try {
                s.getOutputStream().write(msg.getBytes());

                s.getOutputStream().flush();
             //   Log.d("TAG", "send to client looop........" + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //update message
    public void updateMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message != null) {
                    messageTv.append(message + "\n");
                }

            }
        });
    }

    //getting ip of server
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "LocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }


    /*Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {

            for (Socket s : tempclientarray) {
                try {
                    s.getOutputStream().write(data_to_snd.getBytes());
                    s.getOutputStream().flush();
                    Log.d("TAG", "send to client looop........" + data_to_snd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    });*/



    public class ConnectTask extends AsyncTask<String, String, TCP_client> {

        @Override
        protected TCP_client doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TCP_client(new TCP_client.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    sendMessageToEveryone(message);
                    //this method calls the onProgressUpdate
                  //  publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server

            Log.d("TAG", "response " + values[0]);
            //process server response here....

        }

    }
}