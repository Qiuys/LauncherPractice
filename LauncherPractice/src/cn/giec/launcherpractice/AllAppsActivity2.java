package cn.giec.launcherpractice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

public class AllAppsActivity2 extends Activity {
	private ArrayList<List<AppItem> > appListSet;//每一屏的appList的二维数组
	private ArrayList<ArrayList<HashMap<String,Object>>> toDrawListSet;//每一屏toDrawList的二维数组
	private int NUMBER_PER_SCREEN=24;//一个屏幕显示的APP数量
	private int ScreenCount=1;//GridView的屏数
	private int ScreenNo=0;//当前屏编号
	private ViewFlipper viewFlipper=null;//切换GridView方法二  ViewFlipper
	private float startX=0;//接触屏幕事件发生时X坐标
	AppReceiver appRec;//响应APP状态变化广播的BroadcastReceiver，用于即时重绘GridView
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_allapps2);
		try{
			findViewById(R.id.allapps2).setBackgroundResource(R.drawable.app);
		}catch (OutOfMemoryError e) {
			System.out.println("内存不足！！");
			System.gc();
			findViewById(R.id.allapps2).setBackgroundResource(R.drawable.app);
		}

		findAllApps();//获取所有已添加快捷方式APP
		setFlipper();//设置ViewFlipper
		setDot();//设置小圆点
		setAppReceiver();//设置appRec
	}

	//获取所有已添加快捷方式APP，并将每一屏的APP数据提前准备好
	private void findAllApps(){
		List<AppItem> appList=null;//保存APP pkg name 和 activity name 用于创建Intent
		ArrayList<HashMap<String,Object>> toDrawList=null;//保存APP Icon 和 label 用于创建图标
		AppList al=new AppList(this);
		al.queryAllApps();
		appList=al.getAppList();
		toDrawList=al.getToDrawList();
		
		ScreenNo=0;
		//计算屏数
		if(appList.size()%NUMBER_PER_SCREEN == 0){
			ScreenCount=appList.size()/NUMBER_PER_SCREEN;
		}else{
			ScreenCount=appList.size()/NUMBER_PER_SCREEN+1;
		}
		appListSet=new ArrayList<List<AppItem> >();
		toDrawListSet=new  ArrayList<ArrayList<HashMap<String,Object>>>();

		int i=0;
		for(; i<ScreenCount-1;i++){//独立设置每一屏GridView的数据
			List<AppItem> templ=new ArrayList<AppItem>();
			ArrayList<HashMap<String,Object>> tempto=new ArrayList<HashMap<String,Object>>();
			for(int j=0;j<NUMBER_PER_SCREEN;j++){
				templ.add(appList.get(i*NUMBER_PER_SCREEN+j));
				tempto.add(toDrawList.get(i*NUMBER_PER_SCREEN+j));
			}

			appListSet.add(templ);
			toDrawListSet.add(tempto);
		}
		//最后一屏可能不满NUMBER_PER_SCREEN个，不放在for循环中设置
		List<AppItem> templ=new ArrayList<AppItem>();
		ArrayList<HashMap<String,Object>> tempto=new ArrayList<HashMap<String,Object>>();		
		for(int j=i*NUMBER_PER_SCREEN;j<appList.size();j++){
			templ.add(appList.get(j));
			tempto.add(toDrawList.get(j));
		}
		appListSet.add(templ);
		toDrawListSet.add(tempto);
	}

	//设置ViewFlipper
	private void setFlipper(){
		viewFlipper=(ViewFlipper)findViewById(R.id.allApp2Flipper);
		if(viewFlipper!=null){//清空原来的屏幕数据   防止更新APP事件响应中 重绘屏幕发生错误
			viewFlipper.removeAllViews();
		}
		for( int i=0;i<ScreenCount;i++){//逐屏构造GridView并添加到ViewFlipper中
			View view=LayoutInflater.from(this).inflate(R.layout.layout_game_gridview, null);
			GridView gv=(GridView)view.findViewById(R.id.gameGrid);
			SimpleAdapter appGridSimpleAdapter=new SimpleAdapter(this,
					toDrawListSet.get(i),
					R.layout.layout_allapps_item,
					new String[] {"itemIcon","itemName"},
					new int[]{R.id.appitemicon,R.id.appitemname});
			gv.setAdapter(appGridSimpleAdapter);
			/**解决SimpleAdapter不接收Drawable而发生的错误*/
			appGridSimpleAdapter.setViewBinder(new ViewBinder(){
				public boolean setViewValue(View view,Object data,String textRepresentation){
					if(view instanceof ImageView && data instanceof Drawable){
						ImageView iv=(ImageView)view;
						iv.setImageDrawable((Drawable)data);
						return true;
						}
					else return false;
					}
				});
			gv.setOnTouchListener(new MyFlipperListener());
			gv.setOnItemClickListener(new MyGVClickListener(i));
			viewFlipper.addView(view);//构造完毕 加入ViewFlipper
		}
	}

	//GridView的点击Listener 作用是打开APP
	class MyGVClickListener implements OnItemClickListener{
		private int loc;
		MyGVClickListener(int i){
			loc=i;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			Intent intent=new Intent();
			intent.setComponent(new ComponentName(appListSet.get(loc).get(position).getPkgName(),  
					appListSet.get(loc).get(position).getActivityName()));
			startActivity(intent);
		}
	};

	//设置小圆点
	private void setDot(){
		LinearLayout dotline=(LinearLayout)findViewById(R.id.allApp2Dot);
		dotline.removeAllViews();
		for(int i=0 ; i<ScreenCount; i++){
			if(ScreenNo==i){
				ImageView iv=new ImageView(this);
				iv.setImageResource(R.drawable.page_focus_bg);
				dotline.addView(iv);
			}else{
				ImageView iv=new ImageView(this);
				iv.setImageResource(R.drawable.page_bg);
				dotline.addView(iv);		
			}
		}
	}
	
	//更新小圆点
	private void changeDot(int a,int b){//a为退出屏编号 b为进入屏编号
		LinearLayout dotline=(LinearLayout)findViewById(R.id.allApp2Dot);
		ImageView i=(ImageView)dotline.getChildAt(a);
		i.setImageResource(R.drawable.page_bg);
		i=(ImageView)dotline.getChildAt(b);
		i.setImageResource(R.drawable.page_focus_bg);
	}

	//切换到上一屏
	private void prev(){
		if(ScreenNo>0){
			changeDot(ScreenNo,ScreenNo-1);
			ScreenNo--;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_left));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_right));						
			viewFlipper.showPrevious();
		}else if(ScreenCount!=1 && ScreenNo==0){//循环滑动
			changeDot(ScreenNo,ScreenCount-1);
			ScreenNo=ScreenCount-1;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_left));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_right));						
			viewFlipper.showPrevious();					
		}
	}

	//切换到下一屏
	private void next(){
		if(ScreenNo < ScreenCount-1){
			changeDot(ScreenNo,ScreenNo+1);
			ScreenNo++;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_right));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_left));
			viewFlipper.showNext();
		}else if(ScreenCount!=1 && ScreenNo==ScreenCount-1){//循环滑动
			changeDot(ScreenNo,0);
			ScreenNo=0;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_right));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_left));
			viewFlipper.showNext();					
		}
	}

	//注册在GridView上的OnTouchListener
	class MyFlipperListener implements OnTouchListener{
		/**利用ViewFlipper实现GridView切换，如果GridView没满，OnTouchListener能获取空白区域的触屏事件*/
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				startX=event.getX();
				break;
			case MotionEvent.ACTION_UP:
				if(event.getX()-startX>50){//滑动50后退
					prev();
				}
				else if(event.getX()-startX<-50){//滑动50前进
					next();
				}
				break;
			default:
				break;
			}
		return false;
		}
	};

	//监听按键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i=new Intent(AllAppsActivity2.this, WelcomeActivity.class);
			startActivity(i);
			AllAppsActivity2.this.finish();
        	return true;
         }else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
        	 next();
         }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
        	 prev();
         }
         return super.onKeyDown(keyCode, event);
     }

	//注册处理APP更新的BroadcastReceiver 参考自AppsManager
	private void setAppReceiver(){
		appRec= new AppReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(appRec, filter);
	}

	//有APP安装、卸载和更新等事件发生时，接受广播并更新GridView内容
	private class AppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				final String packageName = intent.getData()
						.getSchemeSpecificPart();
				final boolean replacing = intent.getBooleanExtra(
						Intent.EXTRA_REPLACING, false);
				if (packageName == null || packageName.length() == 0) {
					//无效Intent
					return;
				}

				if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
					Log.i("AppReceiver", "ACTION_PACKAGE_CHANGED");
					//更新View
					findAllApps();
					setFlipper();
					setDot();
				} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
					//更新View
					findAllApps();
					setFlipper();
					setDot();
				} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					//更新View
					findAllApps();
					setFlipper();
					setDot();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(appRec);//注销appRec
		//退出时通知虚拟机销毁以下资源
		/*for(ArrayList<HashMap<String,Object>> i:toDrawListSet){
			for(HashMap<String,Object> j:i){
				Drawable a=(Drawable)(j.get("itemIcon"));
				a.setCallback(null);
			}
		}
		Drawable bg=this.getResources().getDrawable(R.drawable.app);
		bg.setCallback(null);*/
		
		super.onDestroy();
	}
}