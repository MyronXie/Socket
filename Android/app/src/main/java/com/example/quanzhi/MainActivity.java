package com.example.quanzhi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import android.os.Handler;
import android.widget.ToggleButton;


public class MainActivity extends Activity implements OnClickListener {

    public ToggleButton btnWiFiPower;
    public Button btnHelp;
    public Button btnStatus;
    public Button btnConnect;
    public Button btnDisconnect;
    public Button btnSend;
    public Button btnGetData;
    public Button btnClear;
    public EditText tvMsg;
    public EditText tvCommand;

	public WifiAdmin mWifiAdmin;

    public StringBuffer stringBuffer;
    List<ScanResult> list;
    ScanResult mScanResult;

    String buffer = "";
    String command;

    String LOGTAG = "cubie2";

    String posiBuffer[] = new String[]{"客厅","厨房","主卧","次卧","阳台","卫生间"};

    Socket socket = null;
    OutputStream ou = null;
    BufferedReader bff = null;

    public int wifiStatus = 0;
    public int socketStatus = 0;

    EditText tvData[] = new EditText[6];

    int disData[] = new int[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mWifiAdmin = new WifiAdmin(MainActivity.this);

        //控件ID配置
        btnWiFiPower=(ToggleButton)findViewById(R.id.btnWiFiPower);
        btnStatus=(Button)findViewById(R.id.WiFistatus);
        btnHelp=(Button)findViewById(R.id.h_help);
        btnConnect=(Button)findViewById(R.id.connect);
        btnDisconnect=(Button)findViewById(R.id.disconnect);
        btnSend=(Button)findViewById(R.id.sendcommand);
        btnGetData=(Button)findViewById(R.id.btnGetData);
        btnClear=(Button)findViewById(R.id.btnClear);
        tvMsg=(EditText)findViewById(R.id.main_msg);
        tvCommand=(EditText)findViewById(R.id.main_command);
        tvData[0]=(EditText)findViewById(R.id.temp_value);
        tvData[1]=(EditText)findViewById(R.id.humi_value);
        tvData[2]=(EditText)findViewById(R.id.x_value);
        tvData[3]=(EditText)findViewById(R.id.y_value);
        tvData[4]=(EditText)findViewById(R.id.z_value);
        tvData[5]=(EditText)findViewById(R.id.pos_value);

        //设置OnClickListener
        btnWiFiPower.setOnClickListener(this);
        btnHelp.setOnClickListener(this);
        btnStatus.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnGetData.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        //设置WiFi按钮的OnCheckedChangeListener
        btnWiFiPower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!btnWiFiPower.isChecked()){
                    mWifiAdmin.closeWifi();
                    wifiStatus=0;
                    btnWiFiPower.setChecked(false);
                    Toast.makeText(MainActivity.this, "Close wifi", Toast.LENGTH_SHORT).show();
                } else{
                    mWifiAdmin.openWifi();
                    wifiStatus=1;
                    getAllNetWorkList();
                    btnWiFiPower.setChecked(true);
                    Toast.makeText(MainActivity.this, "Open wifi", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{

		    case R.id.h_help:
			    Intent i=new Intent(MainActivity.this,HelpActivity.class);
			    startActivity(i);
			    break;



            case R.id.WiFistatus:
                if(stringBuffer==null) {
                    stringBuffer = new StringBuffer();
                } else if(stringBuffer.length()!=0){
                    stringBuffer.delete(0,stringBuffer.length()-1);
                }
                stringBuffer.append("MAC:" + mWifiAdmin.getMacAddress() + "\n")
                        .append("IP addr:" + intToIp(mWifiAdmin.getIpAddress()) + "\n")
                        .append("BSSID:" + mWifiAdmin.getBSSID() + "\n")
                        .append("SSID:" + mWifiAdmin.getSSID() + "\n");
                tvMsg.setText(stringBuffer);
                break;

            case R.id.connect:
                new MyConnectThread("Connect").start();
                break;

            case R.id.btnGetData:
                new MyThread("GetAll").start();
                break;

            case R.id.sendcommand:
                command = tvCommand.getText().toString();
                new MyThread(command).start();
                break;

            case R.id.disconnect:
                new MyThread("Down").start();
//              new MyDisconnectThread("").start();
                break;

            case R.id.btnClear:
                tvMsg.setText("");
                break;
		}
	}

    public String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    public void getAllNetWorkList(){
        if(stringBuffer==null) {
            stringBuffer = new StringBuffer();
        } else if(stringBuffer.length()!=0){
            stringBuffer.delete(0,stringBuffer.length()-1);
        }
        //开始扫描网络
        mWifiAdmin.startScan();
        list=mWifiAdmin.getWifiList();
        if(list!=null){
            for(int i=0;i<list.size();i++){
                //得到扫描结果
                mScanResult=list.get(i);
                stringBuffer=stringBuffer.append(mScanResult.BSSID + " \t")
                        .append(mScanResult.SSID + " \t")
                        .append(mScanResult.frequency + " \t")
                        .append(mScanResult.level+"\n");
            }

            tvMsg.setText("WiFi Network List: (BSSID/SSID/Freq/Level) \n"+stringBuffer.toString());
        }
    }

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            //获取bundle信息，得到msg.what
            Bundle bundle = msg.getData();
            String bundlemessage=bundle.getString("msg");
            String bundletemp = bundlemessage;


            switch (msg.what)
            {
                //0x01:建立连接成功
                case 0x01:
                    socketStatus = 1;
                    tvCommand.setEnabled(true);
                    btnSend.setEnabled(true);
                    btnGetData.setEnabled(true);
                    btnDisconnect.setEnabled(true);
                    btnConnect.setEnabled(false);
                    tvMsg.setText("Server:Connected!\n");
                    Log.i(LOGTAG, "Success to connect!");
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                    break;

                //0x02:建立连接失败
                case 0x02:
                    socketStatus = 0;
                    Toast.makeText(getApplicationContext(), "Fail to connect! Please try again!", Toast.LENGTH_SHORT).show();
                    Log.i(LOGTAG, "Fail to connect!");
                    break;

                //0x03:断开连接成功
                case 0x03:
                    socketStatus = 0;
                    btnConnect.setEnabled(true);
                    btnSend.setEnabled(false);
                    tvCommand.setEnabled(false);
                    btnGetData.setEnabled(false);
                    btnConnect.setEnabled(false);
                    tvMsg.setText("Server:" + bundlemessage + "\n");
                    Log.i(LOGTAG, "Success to disconnect!");
                    Toast.makeText(getApplicationContext(), "Disconnected!", Toast.LENGTH_SHORT).show();
                    break;

                //0x04:断开连接失败
                case 0x04:
                    socketStatus = 1;
                    Toast.makeText(getApplicationContext(), "Fail to disconnect! Please try again!", Toast.LENGTH_SHORT).show();
                    Log.i(LOGTAG, "Fail to disconnect!");
                    break;

                //0x05:连接丢失
                case 0x05:
                    socketStatus = 1;
                    Toast.makeText(getApplicationContext(), "Connection lost!", Toast.LENGTH_SHORT).show();
                    Log.i(LOGTAG, "Connection lost!");
                    break;

                //0x11:接收普通信息
                case 0x11:
                    if(!bundlemessage.equalsIgnoreCase("")){
                        tvMsg.append("Server:" + bundlemessage + "\n");
                    }
                    Log.i(LOGTAG, "msg:"+bundlemessage);
                    break;

                //0x12:接收数据信息
                case 0x12:
                    String strData[]=bundletemp.split(",");
                    tvMsg.append("Server:" + bundlemessage + "\n");
                    for(int i=0;i<6;i++){
                        disData[i]=Integer.parseInt(strData[i+1]);
                        tvData[i].setText(strData[i+1]);
                    }
                    tvData[5].setText(posiBuffer[disData[5]]);
                    break;

                //0x13:接收断开连接信息
                case 0x13:
                    //new MyDisconnectThread("").start();
                    socketStatus = 0;
                    btnConnect.setEnabled(true);
                    btnSend.setEnabled(false);
                    tvCommand.setEnabled(false);
                    btnGetData.setEnabled(false);
                    btnDisconnect.setEnabled(false);
                    tvMsg.setText("Server:" + bundlemessage + "\n");
                    Toast.makeText(getApplicationContext(), "Disconnected!", Toast.LENGTH_SHORT).show();
                    Log.i(LOGTAG, "Success to disconnect!");
                    break;

                default:

                    break;

            }

            super.handleMessage(msg);
        }
    };

    class MyThread extends Thread {

        public String strCommand;

        public MyThread(String str) {
            strCommand = str;
        }

        @Override
        public void run() {
            //定义消息
            Message msg = new Message();
            msg.what = 0x11;
            Bundle bundle = new Bundle();
            bundle.clear();
            try {

                ou = socket.getOutputStream();
                bff = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //向服务器发送信息
                ou.write(strCommand.getBytes());
                ou.flush();

                //读取发来服务器信息
                String line = null;
                buffer="";
                line=bff.readLine();
                buffer=line;

                if(buffer.contains("data")) msg.what=0x12;
                else if(buffer.contains("Disconnect")) msg.what=0x13;

                if(buffer!=null) Log.i(LOGTAG, buffer.toString());

                bundle.putString("msg", buffer.toString());
                msg.setData(bundle);
                myHandler.sendMessage(msg);

            } catch (SocketTimeoutException aa) {
                msg.what = 0x05;
                msg.setData(bundle);
                myHandler.sendMessage(msg);
            } catch (IOException e) {
                msg.what = 0x05;
                msg.setData(bundle);
                myHandler.sendMessage(msg);
                e.printStackTrace();
            }
        }
    }

    class MyConnectThread extends Thread {

        public String txt1;

        public MyConnectThread(String str) {
            txt1 = str;
        }

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 0x01;
            Bundle bundle = new Bundle();
            bundle.clear();
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress("222.204.248.136", 14000), 14000);

                msg.setData(bundle);//msg.what==0x01;
                myHandler.sendMessage(msg);

            } catch (SocketTimeoutException aa) {
                msg.what = 0x02;
                msg.setData(bundle);
                myHandler.sendMessage(msg);
            } catch (IOException e) {
                msg.what = 0x02;
                msg.setData(bundle);
                myHandler.sendMessage(msg);
                e.printStackTrace();
            }
        }
    }

    class MyDisconnectThread extends Thread {

        public String txt1;

        public MyDisconnectThread(String str) {
            txt1 = str;
        }

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 0x03;
            Bundle bundle = new Bundle();
            bundle.clear();
            try
            {
                socket.close();
                if(bff!=null) bff.close();
                if(ou!=null) ou.close();

                msg.setData(bundle);//msg.what==0x03;
                myHandler.sendMessage(msg);

            } catch (SocketTimeoutException aa) {
                msg.what = 0x04;
                msg.setData(bundle);
                myHandler.sendMessage(msg);

            } catch (IOException e) {
                msg.what = 0x04;
                msg.setData(bundle);
                myHandler.sendMessage(msg);
                e.printStackTrace();
            }

            }
    }

}
