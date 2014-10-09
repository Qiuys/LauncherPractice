package cn.giec.launcherpractice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ViewSwitcher.ViewFactory;

public class GameActivity extends Activity {
	private int NUMBER_PER_SCREEN=24;//每屏显示的APP图标数目
	private ArrayList<AppItem> appList=new ArrayList<AppItem>();//快捷方式APP package名 Activity名 数组
	private ArrayList<HashMap<String,Object>> toDrawList=new ArrayList<HashMap<String,Object>>();//快捷方式APP icon图标 label 数组
	private int screenNo=0;//当前屏编号
	private int screenCount;//全部屏总数
	private ViewSwitcher switcher;//切换两个GridView方法一 ViewSwitcher
	private LayoutInflater inflater;//当前屏幕整个layout
	private HashMap<String,String> tempMap=new HashMap<String,String>();//快捷方式APP activity名-package名 键值对
	private float touchDownX , touchUpX;//触摸屏幕事件坐标 离开屏幕事件坐标
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_game);

		try{
			findViewById(R.id.gameLayout).setBackgroundResource(R.drawable.game);
		}catch (OutOfMemoryError e) {//由于背景图过大，经常在这个地方OutOfMemory
			System.out.println("内存不足！！");
			System.gc();//通知虚拟机回收内存，尝试重新载入背景图
			findViewById(R.id.gameLayout).setBackgroundResource(R.drawable.game);
		}

		findAdded();//获取所有已添加快捷方式APP
		setSwitcher();//设置ViewSwitcher
		setDot();//设置小圆点
	}

	//获取所有已添加快捷方式APP
	private void findAdded(){
		//构造并添加第一个图标
		HashMap<String, Object> editIcon = new HashMap<String, Object>();
		Drawable exitImg=getResources().getDrawable(R.drawable.item_img_add);
		editIcon.put("itemIcon",exitImg);
		editIcon.put("itemName","Edit APP list");
		toDrawList.add(editIcon);
		appList.add(new AppItem("",""));//package和activity为空

		inflater=LayoutInflater.from(this);
		//打开SharedPreferences文件"shortcut"，没有该文件则新建，内容为空
		SharedPreferences sp=this.getSharedPreferences ("shortcut",Context.MODE_PRIVATE);
		PackageManager pm=getPackageManager();//PackageManager用于查询app相关操作
		tempMap=(HashMap<String,String>)sp.getAll();//"shortcut"配置文件中读取所有键值对到tempMap中
		
/*** 以下 获取所有已添加快捷方式APP方法二********************************************************************/
		List<AppItem> altemp=null;//系统中所有APP pkg name 和 activity name
		ArrayList<HashMap<String,Object>> tdtemp=null;//相对应的APP Icon 和 label
		AppList al=new AppList(this);
		al.queryAllApps();
		altemp=al.getAppList();
		tdtemp=al.getToDrawList();
		boolean mark[] = new boolean[altemp.size()];
		for(int i=0;i<altemp.size();i++){
			mark[i]=false;//标记哪些是添加了快捷方式的APP
		}
		Iterator iter =tempMap.entrySet().iterator();//tempMap的迭代器 用于遍历
		while (iter.hasNext()) {//遍历tempMap，从altemp中查找对应APP，并防止将无效APP加入GridView
			HashMap.Entry entry = (HashMap.Entry) iter.next();//获取HashMap每一个键值对对象entry
			for(int i=0;i<altemp.size();i++){
				if(altemp.get(i).getPkgName().equals(entry.getValue())//包名和activity名都要相同才表示找到
						&&altemp.get(i).getActivityName().equals(entry.getKey())){
					mark[i]=true;
					break;
				}
			}
		}
		for(int i=0;i<altemp.size();i++){//以原来的顺序将有标记的APP信息加入两个list
			if(mark[i]){
				appList.add(altemp.get(i));
				toDrawList.add(tdtemp.get(i));
			}
		}
		screenCount=appList.size()%NUMBER_PER_SCREEN==0? appList.size()/NUMBER_PER_SCREEN : appList.size()/NUMBER_PER_SCREEN+1;
	}

/*** 以上 获取所有已添加快捷方式APP方法二********************************************************************/

/*** 以下 获取所有已添加快捷方式APP方法一，存在有的APP名称与其他界面不一致的情况*******************************/		
		//以下/* */注释处为获取添加快捷方式的APP、判断APP是否有效、排序的过程
		//    /** */注释处为笔记
		/*Iterator iter =tempMap.entrySet().iterator();//tempMap的迭代器 用于遍历
		while (iter.hasNext()) {//遍历tempMap所有值，防止将无效APP加入GridView
			HashMap.Entry entry = (HashMap.Entry) iter.next();//获取HashMap每一个键值对对象entry
			AppItem ai=new AppItem((String)entry.getValue(),(String)entry.getKey());//key为activity名 value为包名
			ActivityInfo info;
			try {
				ComponentName comp= new ComponentName(ai.getPkgName(),ai.getActivityName());
				info=pm.getActivityInfo(comp, 0);//PackageManager查询指定app信息
				Drawable icon = info.loadIcon(pm);//icon图标
				String name =info.applicationInfo.loadLabel(pm).toString();*/	//app名称					
				/**info.loadLabel(pm)时，要用toString()，不能用(String)否则获取不到某些app的label时会产生错误
				 *android.text.SpannedString cannot be cast to java.lang.String*/

				/*HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("itemIcon",icon);
				map.put("itemName",name);
				appList.add(ai);
				toDrawList.add(map);
				
			} catch (NameNotFoundException e) {
				//这里不将无效APP显示出来。但没有更新SharedPreferences文件
				//在进入AddShortcutActivity时会删除无效APP
				//e.printStackTrace();
				System.out.println("获取APP失败-下次进入AddShortcut时更新数据"+ai.getPkgName());
			}*/
				
				/**方法二：
				 * ComponentName comp = new ComponentName(ai.getPkgName(),ai.getActivityName());  
				 * Intent appInt =new Intent();
				 * appInt.setComponent(comp);//?要不要 
				 * ResolveInfo reInfo=null;
				 * reInfo=pm.resolveActivity(appInt, 0);
				 * if(reInfo==null){
				 * 		continue;
				 * }
				 * Drawable icon = reInfo.loadIcon(pm);
				 * String name =(String)reInfo.activityInfo.applicationInfo.loadLabel(pm);*/
			
				/**方法二注：指定APP，利用ResolveInfo reInfo获取应用名称，以下几个方法中
				 * reInfo.loadLabel(pm)
				 * reInfo.activityInfo.loadLabel(pm)
				 * reInfo.activityInfo.name
				 * reInfo.activityInfo.applicationInfo.name
				 * reInfo.activityInfo.applicationInfo.loadLabel(pm)
				 *  只有reInfo.activityInfo.applicationInfo.loadLabel(pm)才能在任何情况都正确获取*/
		/*}
		sort();//排序
		screenCount=appList.size()%NUMBER_PER_SCREEN==0? appList.size()/NUMBER_PER_SCREEN : appList.size()/NUMBER_PER_SCREEN+1;		
	}*/

	/*private void sort(){
		if(toDrawList.size()>1){
			for(int i=0;i<toDrawList.size();i++){
				toDrawList.get(i).put("num", i);
			}
			//String的compareTo并不是按照拼音排序，这里按照拼音首字母排序
			Collections.sort(toDrawList.subList(1, toDrawList.size()-1),new MyDisplayNameComparator());
			ArrayList<AppItem> tempAppList=new ArrayList<AppItem>();
			for(int i=0;i<toDrawList.size();i++){//对应排序appList
				tempAppList.add(appList.get((Integer) toDrawList.get(i).get("num")));
			}
			appList=tempAppList;
			tempAppList=null;
		}
	}*/
/*** 以上 获取所有已添加快捷方式APP方法一，存在有的APP名称与其他界面不一致的情况*******************************/

	//设置ViewSwitcher
	private void setSwitcher(){
		switcher=(ViewSwitcher)findViewById(R.id.gameViewSwitcher);
		switcher.setFactory(new ViewFactory(){////ViewSwitcher的关键代码 setFactory
			@Override
			public View makeView() {//动态构造一屏GridView，返回该GridView
				View gv=inflater.inflate(R.layout.layout_game_gridview, null);
				GridView gg=(GridView)gv.findViewById(R.id.gameGrid);
				//GridView区域注册触屏监听、区域外无法获取到触屏事件 除非根据事件分发机制重写相关方法
				gg.setOnTouchListener(new GridTouchListener());
				gg.setOnItemClickListener(new OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if((screenNo*NUMBER_PER_SCREEN+position)!=0){//根据screenNo将GridView点击位置映射到appList中实际位置
							Intent intent=new Intent();
							intent.setComponent(new ComponentName(appList.get(screenNo*NUMBER_PER_SCREEN+position).getPkgName(),  
									appList.get(screenNo*NUMBER_PER_SCREEN+position).getActivityName()));
							startActivity(intent);
							
						}else if((screenNo*NUMBER_PER_SCREEN+position)==0){//第一个图标为打开AddShortcutActivity
							Intent i=new Intent(GameActivity.this, AddShortcutActivity.class);
							startActivity(i);
							GameActivity.this.finish();
						}
					}
				});
				return gv;
				}
		});
		switcher.setOnTouchListener(new STouchListener());//GridView空白处也进行滑动屏幕的监听
		//显示第一屏
		switcher.setInAnimation(this,R.anim.slide_in_right);//switcher进入动画
		switcher.setOutAnimation(this,R.anim.slide_out_left);//switcher退出动画
		((GridView)switcher.getNextView()).setAdapter(adapter);//获得进入的一屏，数据由adapter提供
		switcher.showNext();//对其进行显示
	}

	//设置小圆点
	private void setDot(){
		LinearLayout dotline=(LinearLayout)findViewById(R.id.gameDot);
		for(int i=0 ; i<screenCount; i++){
			if(screenNo==i){
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
	private void changeDot(int a,int b){//a为退出屏的编号 b为进入屏的编号 
		LinearLayout dotline=(LinearLayout)findViewById(R.id.gameDot);
		ImageView i=(ImageView)dotline.getChildAt(a);
		i.setImageResource(R.drawable.page_bg);
		i=(ImageView)dotline.getChildAt(b);
		i.setImageResource(R.drawable.page_focus_bg);
	}

	//GridView触屏响应 切换界面
	private class GridTouchListener implements OnTouchListener {
		/**GridView覆盖的区域switcher获取不到onTouch事件*/
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				touchDownX=event.getX();
				/**这里返回true则GridView每次都是监听到每一行第一个*/
			}else if(event.getAction()== MotionEvent.ACTION_UP){
				touchUpX=event.getX();
				//判断滑动距离
				if(touchDownX-touchUpX >50){//前进滑动50
					next();
				}else if(touchUpX-touchDownX>50){//后退滑动50
					prev();
				}
			}
			return false;
		}
	};

	//Switcher触屏响应 切换界面
	private class STouchListener implements OnTouchListener {
	/**GridView Item不够时，最后一行以下的空白区域无法被GridView监听到 需在switcher也注册listener*/
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				touchDownX=event.getX();
				/**这里返回true，否则无法监听到ACTION_UP*/
				return true;
			}
			if(event.getAction()== MotionEvent.ACTION_UP){
				touchUpX=event.getX();
				//判断滑动距离
				if(touchDownX-touchUpX >50){//前进滑动50
					next();
				}else if(touchUpX-touchDownX>50){//后退滑动50
					prev();
				}
			}
			return false;
		}
	}

	//切换到下一屏
	public void next(){
		if(screenNo < screenCount-1){
			changeDot(screenNo,screenNo+1);
			screenNo++;
			switcher.setInAnimation(this,R.anim.slide_in_right);
			switcher.setOutAnimation(this,R.anim.slide_out_left);
			((GridView)switcher.getNextView()).setAdapter(adapter);
			switcher.showNext();
			return ;
		}
		if(screenCount!=1 && screenNo==screenCount-1){//如果为最后一屏，循环滑动
			changeDot(screenNo,0);
			screenNo=0;
			switcher.setInAnimation(this,R.anim.slide_in_right);
			switcher.setOutAnimation(this,R.anim.slide_out_left);
			((GridView)switcher.getNextView()).setAdapter(adapter);
			switcher.showNext();
		}
	}

	//切换到上一屏
	public void prev(){
		if(screenNo>0){
			changeDot(screenNo,screenNo-1);
			screenNo--;
			switcher.setInAnimation(this,R.anim.slide_in_left);
			switcher.setOutAnimation(this,R.anim.slide_out_right);	
			((GridView)switcher.getNextView()).setAdapter(adapter);
			switcher.showPrevious();
			return ;
		}
		if(screenCount!=1 && screenNo==0){//如果为第一屏，循环滑动
			changeDot(screenNo,screenCount-1);
			screenNo=screenCount-1;
			switcher.setInAnimation(this,R.anim.slide_in_left);
			switcher.setOutAnimation(this,R.anim.slide_out_right);
			((GridView)switcher.getNextView()).setAdapter(adapter);
			switcher.showNext();
		}
	}

	//构造自定义Adapter为GridView提供数据
	private BaseAdapter adapter = new BaseAdapter(){

		@Override
		public int getCount() {//根据screenNo返回该GridView的item数量
			if(screenNo == screenCount-1 && appList.size()%NUMBER_PER_SCREEN !=0){
				return toDrawList.size()%NUMBER_PER_SCREEN;
			}
			return NUMBER_PER_SCREEN;
		}

		@Override
		public HashMap<String, Object> getItem(int position) {
			//根据screenNo返回该位置item在toDrawList的实际位置
			return toDrawList.get(screenNo*NUMBER_PER_SCREEN+position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view=convertView;
			if(convertView == null){
				view=inflater.inflate(R.layout.layout_allapps_item, null);
			}
			ImageView icon=(ImageView)view.findViewById(R.id.appitemicon);
			icon.setImageDrawable((Drawable) getItem(position).get("itemIcon"));
			TextView name=(TextView)view.findViewById(R.id.appitemname);
			name.setText(getItem(position).get("itemName").toString());
			return view;
		}
	};

	//监听按键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i=new Intent(GameActivity.this, WelcomeActivity.class);
			startActivity(i);
			GameActivity.this.finish();
       	return true;
         }else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
        	 next();
         }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
        	 prev();
         }
        /**获取当前屏被点击的item的编号
         * GridView ggg=(GridView)switcher.getChildAt(switcher.getDisplayedChild());
         *被点击的Item位置为   +ggg.getSelectedItemPosition()*/
         return super.onKeyDown(keyCode, event);
     }
	@Override
	protected void onDestroy() {
		//退出时通知虚拟机销毁以下资源
		/*for(HashMap<String,Object>  i:toDrawList){
			Drawable a=(Drawable)(i.get("itemIcon"));
			a.setCallback(null);
		}
		Drawable bg=this.getResources().getDrawable(R.drawable.game);
		bg.setCallback(null);*/
		super.onDestroy();
	}
}