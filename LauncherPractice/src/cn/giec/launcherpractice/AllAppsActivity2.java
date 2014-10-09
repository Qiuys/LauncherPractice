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
	private ArrayList<List<AppItem> > appListSet;//ÿһ����appList�Ķ�ά����
	private ArrayList<ArrayList<HashMap<String,Object>>> toDrawListSet;//ÿһ��toDrawList�Ķ�ά����
	private int NUMBER_PER_SCREEN=24;//һ����Ļ��ʾ��APP����
	private int ScreenCount=1;//GridView������
	private int ScreenNo=0;//��ǰ�����
	private ViewFlipper viewFlipper=null;//�л�GridView������  ViewFlipper
	private float startX=0;//�Ӵ���Ļ�¼�����ʱX����
	AppReceiver appRec;//��ӦAPP״̬�仯�㲥��BroadcastReceiver�����ڼ�ʱ�ػ�GridView
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_allapps2);
		try{
			findViewById(R.id.allapps2).setBackgroundResource(R.drawable.app);
		}catch (OutOfMemoryError e) {
			System.out.println("�ڴ治�㣡��");
			System.gc();
			findViewById(R.id.allapps2).setBackgroundResource(R.drawable.app);
		}

		findAllApps();//��ȡ��������ӿ�ݷ�ʽAPP
		setFlipper();//����ViewFlipper
		setDot();//����СԲ��
		setAppReceiver();//����appRec
	}

	//��ȡ��������ӿ�ݷ�ʽAPP������ÿһ����APP������ǰ׼����
	private void findAllApps(){
		List<AppItem> appList=null;//����APP pkg name �� activity name ���ڴ���Intent
		ArrayList<HashMap<String,Object>> toDrawList=null;//����APP Icon �� label ���ڴ���ͼ��
		AppList al=new AppList(this);
		al.queryAllApps();
		appList=al.getAppList();
		toDrawList=al.getToDrawList();
		
		ScreenNo=0;
		//��������
		if(appList.size()%NUMBER_PER_SCREEN == 0){
			ScreenCount=appList.size()/NUMBER_PER_SCREEN;
		}else{
			ScreenCount=appList.size()/NUMBER_PER_SCREEN+1;
		}
		appListSet=new ArrayList<List<AppItem> >();
		toDrawListSet=new  ArrayList<ArrayList<HashMap<String,Object>>>();

		int i=0;
		for(; i<ScreenCount-1;i++){//��������ÿһ��GridView������
			List<AppItem> templ=new ArrayList<AppItem>();
			ArrayList<HashMap<String,Object>> tempto=new ArrayList<HashMap<String,Object>>();
			for(int j=0;j<NUMBER_PER_SCREEN;j++){
				templ.add(appList.get(i*NUMBER_PER_SCREEN+j));
				tempto.add(toDrawList.get(i*NUMBER_PER_SCREEN+j));
			}

			appListSet.add(templ);
			toDrawListSet.add(tempto);
		}
		//���һ�����ܲ���NUMBER_PER_SCREEN����������forѭ��������
		List<AppItem> templ=new ArrayList<AppItem>();
		ArrayList<HashMap<String,Object>> tempto=new ArrayList<HashMap<String,Object>>();		
		for(int j=i*NUMBER_PER_SCREEN;j<appList.size();j++){
			templ.add(appList.get(j));
			tempto.add(toDrawList.get(j));
		}
		appListSet.add(templ);
		toDrawListSet.add(tempto);
	}

	//����ViewFlipper
	private void setFlipper(){
		viewFlipper=(ViewFlipper)findViewById(R.id.allApp2Flipper);
		if(viewFlipper!=null){//���ԭ������Ļ����   ��ֹ����APP�¼���Ӧ�� �ػ���Ļ��������
			viewFlipper.removeAllViews();
		}
		for( int i=0;i<ScreenCount;i++){//��������GridView����ӵ�ViewFlipper��
			View view=LayoutInflater.from(this).inflate(R.layout.layout_game_gridview, null);
			GridView gv=(GridView)view.findViewById(R.id.gameGrid);
			SimpleAdapter appGridSimpleAdapter=new SimpleAdapter(this,
					toDrawListSet.get(i),
					R.layout.layout_allapps_item,
					new String[] {"itemIcon","itemName"},
					new int[]{R.id.appitemicon,R.id.appitemname});
			gv.setAdapter(appGridSimpleAdapter);
			/**���SimpleAdapter������Drawable�������Ĵ���*/
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
			viewFlipper.addView(view);//������� ����ViewFlipper
		}
	}

	//GridView�ĵ��Listener �����Ǵ�APP
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

	//����СԲ��
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
	
	//����СԲ��
	private void changeDot(int a,int b){//aΪ�˳������ bΪ���������
		LinearLayout dotline=(LinearLayout)findViewById(R.id.allApp2Dot);
		ImageView i=(ImageView)dotline.getChildAt(a);
		i.setImageResource(R.drawable.page_bg);
		i=(ImageView)dotline.getChildAt(b);
		i.setImageResource(R.drawable.page_focus_bg);
	}

	//�л�����һ��
	private void prev(){
		if(ScreenNo>0){
			changeDot(ScreenNo,ScreenNo-1);
			ScreenNo--;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_left));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_right));						
			viewFlipper.showPrevious();
		}else if(ScreenCount!=1 && ScreenNo==0){//ѭ������
			changeDot(ScreenNo,ScreenCount-1);
			ScreenNo=ScreenCount-1;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_left));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_right));						
			viewFlipper.showPrevious();					
		}
	}

	//�л�����һ��
	private void next(){
		if(ScreenNo < ScreenCount-1){
			changeDot(ScreenNo,ScreenNo+1);
			ScreenNo++;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_right));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_left));
			viewFlipper.showNext();
		}else if(ScreenCount!=1 && ScreenNo==ScreenCount-1){//ѭ������
			changeDot(ScreenNo,0);
			ScreenNo=0;
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_in_right));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(AllAppsActivity2.this, R.anim.slide_out_left));
			viewFlipper.showNext();					
		}
	}

	//ע����GridView�ϵ�OnTouchListener
	class MyFlipperListener implements OnTouchListener{
		/**����ViewFlipperʵ��GridView�л������GridViewû����OnTouchListener�ܻ�ȡ�հ�����Ĵ����¼�*/
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				startX=event.getX();
				break;
			case MotionEvent.ACTION_UP:
				if(event.getX()-startX>50){//����50����
					prev();
				}
				else if(event.getX()-startX<-50){//����50ǰ��
					next();
				}
				break;
			default:
				break;
			}
		return false;
		}
	};

	//��������
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

	//ע�ᴦ��APP���µ�BroadcastReceiver �ο���AppsManager
	private void setAppReceiver(){
		appRec= new AppReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(appRec, filter);
	}

	//��APP��װ��ж�غ͸��µ��¼�����ʱ�����ܹ㲥������GridView����
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
					//��ЧIntent
					return;
				}

				if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
					Log.i("AppReceiver", "ACTION_PACKAGE_CHANGED");
					//����View
					findAllApps();
					setFlipper();
					setDot();
				} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
					//����View
					findAllApps();
					setFlipper();
					setDot();
				} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					//����View
					findAllApps();
					setFlipper();
					setDot();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(appRec);//ע��appRec
		//�˳�ʱ֪ͨ���������������Դ
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