package cn.giec.launcherpractice;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity {
	private TextView dTime;//ʱ������
	private boolean usbIsOn=false;//USB�򿪡��ر�״̬�ı��
	private boolean webIsOn=false;//WEB�򿪡��ر�״̬�ı��
	private boolean wifiIsOn=false;//WIFI�򿪡��ر�״̬�ı��
	private static final int msgKey1 = 1;//����ʱ�ӵ�message
	private static final int msgKey2 = 2;//����USB��message
	private static final int msgKey3 = 3;//����WEB��message
	private static final int msgKey4 = 4;//����WIFI��message
	ToolsReceiver toolsRec;//USB��WEB��WIFI�Ĺ㲥������
	TimeThread tThread;//����ʱ�ӵ��߳�
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**���ݲο����� ���ر��� ����ȫ��
		 * requestWindowFeature(Window.FEATURE_NO_TITLE);//���ر���
		 *getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		 *WindowManager.LayoutParams.FLAG_FULLSCREEN);//����ȫ��
		 *1.����setContentView֮ǰ����
		 *2.ͨ��java�������ر�����  ����Activity����������ݳ��֣�����ѡ����XML������*/
		setContentView(R.layout.layout_welcome);
		
		/**Ҫ��setContentView������Listener*/
		setWelListener();//��� ��������¼�����Ӧ
		setTime();//���ʱ��
		setToolsReceiver();//ע��㲥Receiver ����USB/WEB/WIFIͼ��
		initTools();//��ʼ��USB/WEB/WIFIͼ��
	}
	
	private void initTools(){//��ʼ��USB/WEB/WIFIͼ��
		ifUsbOn();
		ifWebOn();
		ifWifiOn();
		Message msg2 = new Message();
        msg2.what = msgKey2;
        mHandler.sendMessage(msg2);
		Message msg3 = new Message();
        msg3.what = msgKey3;
        mHandler.sendMessage(msg3);
		Message msg4 = new Message();
        msg4.what = msgKey4;
        mHandler.sendMessage(msg4);
	}

	//���USB״̬ [���ܵĺ�ʱ����]
	private void ifUsbOn(){
		File usbPath_rockchipFile = new File("/mnt/usb_storage/USB_DISK0");
		File usbPath_amlogicFile = new File("/storage/external_storage/sda1");
		usbIsOn=(getUSB(usbPath_rockchipFile)||getUSB(usbPath_amlogicFile));
	}

	//���USB״̬��������
	private boolean getUSB(File path) {
		boolean isUsb = false;
		if(path.isDirectory())
		{
			try{
				File[] files = path.listFiles();
				if(files.length > 0)
					isUsb = true;
				else {
					isUsb = false;
				}
			}catch(Exception e)
			{
				return false;
			}
		}
		return isUsb;
	}

	//���Web Internet����״̬
	/*private void ifWebOn(){
    	ConnectivityManager connectMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isAvailable()){
        	webIsOn=true;//������
        }else{
        	webIsOn=false;//δ����
        }
	}*/
	
	//�����������״̬
	private void ifWebOn(){
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo web = manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		if(web.isConnected()){
			webIsOn=true;
		}else{
			webIsOn=false;
		}
	}

	//���WIFI״̬
	private void ifWifiOn(){
		/**���wifi�Ƿ�򿪣���һ������
		WifiManager wifiMgr=(WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(wifiMgr.isWifiEnabled())
			wifiIsOn=true;
		else
			wifiIsOn=false;*/
		
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(wifi.isConnected())//���WIFI������
			wifiIsOn=true;
		else
			wifiIsOn=false;		
	}

	//����ʱ��
	private void setTime(){
		dTime=(TextView)findViewById(R.id.welButtomClock);
		tThread=new TimeThread();
		tThread.start();
	}

	//��ʱ����ʱ����ֵ���߳�
	public class TimeThread extends Thread {
		private boolean  running  = true;//�����̵߳ı��	
		@Override
        public void run () {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(running);//trueʱ��ѭ��
        }
		
	    public void stopTime() {
	    	running=false;
	    }
	}

	//UI�߳�Handler ��ȡUSB/WEB/WIFI��״̬��ǡ�������ͼ��
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("yyyy/MM/dd HH:mm:ss", sysTime);
                    dTime.setText(sysTimeStr);
                    break;
                case msgKey2:
                	ImageView usbImage=(ImageView)findViewById(R.id.welButtomTools_usb);
                	if(usbIsOn)
                		usbImage.setImageResource(R.drawable.low_usb_r);
                	else
                		usbImage.setImageResource(R.drawable.low_usb_w);
                	break;
                case msgKey3:
                	ImageView webImage=(ImageView)findViewById(R.id.welButtomTools_web);
                	if(webIsOn)
                		webImage.setImageResource(R.drawable.low_web_r);
                	else
                		webImage.setImageResource(R.drawable.low_web_w);    
                	break;
                case msgKey4:
                	ImageView wifiImage=(ImageView)findViewById(R.id.welButtomTools_wifi);
                	if(wifiIsOn)
                		wifiImage.setImageResource(R.drawable.low_wifi_r);
                	else
                		wifiImage.setImageResource(R.drawable.low_wifi_w);      
                	break;
                default:
                    break;
            }
        }
    };

  //��� ��������¼�����Ӧ [���Listener���Ż�Ϊ1��]
	private void setWelListener(){
		ImageButton allApps=(ImageButton)findViewById(R.id.welGrid_allApps);
		allApps.setOnClickListener(new ImageButton.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i=new Intent(WelcomeActivity.this, AllAppsActivity2.class);
				startActivity(i);//��AllApps
			}
		});

		ImageButton setting=(ImageButton)findViewById(R.id.welList_setting);
		setting.setOnClickListener(new ImageButton.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();  
				ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings");  
				intent.setComponent(comp);
				startActivity(intent);//��Setting
			}
		});

		ImageButton game=(ImageButton)findViewById(R.id.welGrid_game);
		game.setOnClickListener(new ImageButton.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i=new Intent(WelcomeActivity.this, GameActivity.class);
				startActivity(i);//��Game
			}
		});
	}

	//ע��㲥Receiver ����USB/WEB/WIFIͼ��
	private void setToolsReceiver(){
		toolsRec= new ToolsReceiver();
		IntentFilter filter = new IntentFilter();//WIFI�������Filter
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//wifi�Ƿ��
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//wifi�����Ƿ�������
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//��������״̬

		IntentFilter filter2 = new IntentFilter();//USB��Filter
		filter2.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
		filter2.addAction(Intent.ACTION_MEDIA_EJECT);
		filter2.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter2.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter2.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter2.addDataScheme("file");//USB������Ӹ�Scheme

		registerReceiver(toolsRec, filter);//ע��Receiver
		registerReceiver(toolsRec, filter2);
	}

	//���ܹ㲥���Զ���BroadcastReceiver
	private class ToolsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
	        //USB�¼�
	        if(Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())
	        		||Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())
	        		||Intent.ACTION_MEDIA_REMOVED.equals(intent.getAction())
	        		||Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())){
	        	new Thread(){
	        		public void run(){
	        			try {
	        				ifUsbOn();
	        			} catch (Exception e) {
	        				e.printStackTrace();
	        			}
	        			Message msg = new Message();
	        			msg.what = msgKey2;
	        			mHandler.sendMessage(msg);
	        		}
	        	}.start();//�ж�USB״̬ [���ܵĺ�ʱ����]�����̴߳�����֪ͨHandler�ػ����
	        }
	        //�����¼�
	        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
	        	ifWebOn();
				Message msg = new Message();
	            msg.what = msgKey3;
	            mHandler.sendMessage(msg);
	        }	        
			//WIFI״̬�仯�¼�
	        if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
	        		||WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
	        	ifWifiOn();
				Message msg = new Message();
	            msg.what = msgKey4;
	            mHandler.sendMessage(msg);
	        }
		}
	}

	@Override
	protected void onDestroy() {//onDestroy�н��л�����Դ����
		unregisterReceiver(toolsRec);//�˳�����ע��Receiver
		tThread.stopTime();//��Ǹ���ʱ�ӵ��߳�ֹͣ
		tThread.interrupt();//ֹͣ�߳�
		//System.gc();
		super.onDestroy();
	}
}