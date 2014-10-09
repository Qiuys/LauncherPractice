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
	private TextView dTime;//时钟区域
	private boolean usbIsOn=false;//USB打开、关闭状态的标记
	private boolean webIsOn=false;//WEB打开、关闭状态的标记
	private boolean wifiIsOn=false;//WIFI打开、关闭状态的标记
	private static final int msgKey1 = 1;//更新时钟的message
	private static final int msgKey2 = 2;//更新USB的message
	private static final int msgKey3 = 3;//更新WEB的message
	private static final int msgKey4 = 4;//更新WIFI的message
	ToolsReceiver toolsRec;//USB、WEB、WIFI的广播接收器
	TimeThread tThread;//更新时钟的线程
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**备份参考代码 隐藏标题 设置全屏
		 * requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
		 *getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		 *WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
		 *1.需在setContentView之前调用
		 *2.通过java代码隐藏标题栏  进入Activity标题栏会短暂出现，所以选择在XML中隐藏*/
		setContentView(R.layout.layout_welcome);
		
		/**要先setContentView后才添加Listener*/
		setWelListener();//添加 监听点击事件及响应
		setTime();//添加时钟
		setToolsReceiver();//注册广播Receiver 处理USB/WEB/WIFI图标
		initTools();//初始化USB/WEB/WIFI图标
	}
	
	private void initTools(){//初始化USB/WEB/WIFI图标
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

	//检测USB状态 [可能的耗时操作]
	private void ifUsbOn(){
		File usbPath_rockchipFile = new File("/mnt/usb_storage/USB_DISK0");
		File usbPath_amlogicFile = new File("/storage/external_storage/sda1");
		usbIsOn=(getUSB(usbPath_rockchipFile)||getUSB(usbPath_amlogicFile));
	}

	//检测USB状态辅助函数
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

	//检测Web Internet连接状态
	/*private void ifWebOn(){
    	ConnectivityManager connectMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isAvailable()){
        	webIsOn=true;//已联网
        }else{
        	webIsOn=false;//未联网
        }
	}*/
	
	//检测有线网络状态
	private void ifWebOn(){
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo web = manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		if(web.isConnected()){
			webIsOn=true;
		}else{
			webIsOn=false;
		}
	}

	//检测WIFI状态
	private void ifWifiOn(){
		/**检测wifi是否打开，不一定联网
		WifiManager wifiMgr=(WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(wifiMgr.isWifiEnabled())
			wifiIsOn=true;
		else
			wifiIsOn=false;*/
		
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(wifi.isConnected())//检测WIFI已联网
			wifiIsOn=true;
		else
			wifiIsOn=false;		
	}

	//设置时钟
	private void setTime(){
		dTime=(TextView)findViewById(R.id.welButtomClock);
		tThread=new TimeThread();
		tThread.start();
	}

	//定时更新时钟数值的线程
	public class TimeThread extends Thread {
		private boolean  running  = true;//结束线程的标记	
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
            } while(running);//true时死循环
        }
		
	    public void stopTime() {
	    	running=false;
	    }
	}

	//UI线程Handler 获取USB/WEB/WIFI的状态标记、并更新图标
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

  //添加 监听点击事件及响应 [多个Listener待优化为1个]
	private void setWelListener(){
		ImageButton allApps=(ImageButton)findViewById(R.id.welGrid_allApps);
		allApps.setOnClickListener(new ImageButton.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i=new Intent(WelcomeActivity.this, AllAppsActivity2.class);
				startActivity(i);//打开AllApps
			}
		});

		ImageButton setting=(ImageButton)findViewById(R.id.welList_setting);
		setting.setOnClickListener(new ImageButton.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();  
				ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings");  
				intent.setComponent(comp);
				startActivity(intent);//打开Setting
			}
		});

		ImageButton game=(ImageButton)findViewById(R.id.welGrid_game);
		game.setOnClickListener(new ImageButton.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i=new Intent(WelcomeActivity.this, GameActivity.class);
				startActivity(i);//打开Game
			}
		});
	}

	//注册广播Receiver 处理USB/WEB/WIFI图标
	private void setToolsReceiver(){
		toolsRec= new ToolsReceiver();
		IntentFilter filter = new IntentFilter();//WIFI和网络的Filter
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//wifi是否打开
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//wifi网络是否能联网
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//监听网络状态

		IntentFilter filter2 = new IntentFilter();//USB的Filter
		filter2.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
		filter2.addAction(Intent.ACTION_MEDIA_EJECT);
		filter2.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter2.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter2.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter2.addDataScheme("file");//USB必须添加该Scheme

		registerReceiver(toolsRec, filter);//注册Receiver
		registerReceiver(toolsRec, filter2);
	}

	//接受广播的自定义BroadcastReceiver
	private class ToolsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
	        //USB事件
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
	        	}.start();//判断USB状态 [可能的耗时操作]启动线程处理，并通知Handler重绘界面
	        }
	        //网络事件
	        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
	        	ifWebOn();
				Message msg = new Message();
	            msg.what = msgKey3;
	            mHandler.sendMessage(msg);
	        }	        
			//WIFI状态变化事件
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
	protected void onDestroy() {//onDestroy中进行回收资源操作
		unregisterReceiver(toolsRec);//退出必须注销Receiver
		tThread.stopTime();//标记更新时钟的线程停止
		tThread.interrupt();//停止线程
		//System.gc();
		super.onDestroy();
	}
}