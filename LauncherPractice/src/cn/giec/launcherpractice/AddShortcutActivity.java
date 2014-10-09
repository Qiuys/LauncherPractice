package cn.giec.launcherpractice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AddShortcutActivity extends Activity {
	private List<AppItem> appList=null;//保存APP pkg name 和 activity name 用于创建Intent
	private ArrayList<HashMap<String,Object>> toDrawList=null;//保存APP Icon 和 label 用于创建图标
	//用于保存从SharedPreferences获取的或者待写入SharedPreferences文件的快捷方式APP信息，内容为activity名-package名键值对集
	private HashMap<String,String> tempMap=null;
	private GridView appGV;//GridView
	MyAdapter myadapter=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_addshortcut);

		setUpList();//获取所有APP
		findAddedApps();//读取SharedPreferences，对tempMap根据当前APP列表筛选出有效APP
		setAdapter();
		setListener();
	}

	//设置GridView的Adapter
	private void setAdapter(){
		appGV=(GridView)findViewById(R.id.appitemGrid);	
		myadapter=new MyAdapter();
		appGV.setAdapter(myadapter);		
	}

	//退出时对快捷方式信息进行保存
	private void saveResult(){
		SharedPreferences sp=this.getSharedPreferences ("shortcut",Context.MODE_PRIVATE);//数据只能被本应用读写
		Editor editor = sp.edit();//打开写文件的Editor对象
		editor.clear();//清除文件原数据

		Iterator iter =tempMap.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			//保存时key为activity名 value为包名
		    editor.putString((String)entry.getKey(), (String)entry.getValue());
		}
		editor.commit();/**执行commit()才真正写文件*/
	}

	//点击GridView item的监听响应
	private void setListener(){
		appGV.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String pn=appList.get(position).getPkgName();
				String an=appList.get(position).getActivityName();
				if(position==0){//第一个图标为保存并退出
					saveResult();
					Intent i=new Intent(AddShortcutActivity.this, GameActivity.class);
					startActivity(i);
					AddShortcutActivity.this.finish();
				}
					
				else if(tempMap.containsKey(an)){//如果tempMap中包含key为该APP的activity名
						tempMap.remove(an);	//移除该APP
					}
					else{
						tempMap.put(an, pn);//没有则添加该APP的activity名-package名键值对到tempMap
					}
				myadapter.notifyDataSetChanged();
			}
		});
	}

	//读取SharedPreferences，对tempMap根据当前APP列表筛选出有效APP
	private void findAddedApps(){
		//从配置文件"shortcut"读取已添加快捷方式的App信息
		SharedPreferences sp=this.getSharedPreferences ("shortcut",Context.MODE_PRIVATE);
		tempMap=(HashMap<String,String>)sp.getAll();
		//筛选出有效的App 
		Iterator iter =tempMap.entrySet().iterator();
		while (iter.hasNext()) {//遍历每个tempMap
			HashMap.Entry entry = (HashMap.Entry) iter.next();
		    boolean finded=false;
		    for(AppItem ai : appList){//对比所有APP
		    	/**不能用appList.contains(ad)判断appList中是否有新建的ad对象。原因参见其 方法实现
		    	 *不能用 == 判断两个字符串是否一样。应用 equals()*/
		    	if(ai.getActivityName().equals((String)entry.getKey())){
		    		finded=true;
		    		break;
		    	}
		    }
		    if(!finded){//APP列表appList中查不到该快捷方式的APP，表示该APP已不存在，剔除
		    	iter.remove();
		    }
		}
	}

	//获取所有APP
	private void setUpList(){
		//第一个item为“退出”图标
		appList=new ArrayList<AppItem>();
		toDrawList=new ArrayList<HashMap<String,Object>>();		
		appList.add(new AppItem("",""));
		HashMap<String, Object> map = new HashMap<String, Object>();		
		Drawable exitImg=getResources().getDrawable(R.drawable.item_img_exit);
		map.put("itemIcon",exitImg);
		map.put("itemName","Save and return");
		toDrawList.add(map);
		//剩下的为App
		AppList al=new AppList(this);
		al.queryAllApps();
		appList.addAll(al.getAppList());
		toDrawList.addAll(al.getToDrawList());	
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		@Override
		public int getCount() {
			return appList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder=null;
			if(convertView==null){//判断convertView是否已存在，对齐设置标签，是提高性能的实现，详情参见网络
				holder=new ViewHolder();
				inflater=LayoutInflater.from(AddShortcutActivity.this);
				convertView=inflater.inflate(R.layout.layout_addshortcut_item,null);
				holder.icon=(ImageView)convertView.findViewById(R.id.addshortcutitemicon);
				holder.sel=(ImageView)convertView.findViewById(R.id.addshortcutitemiconsel);
				holder.name=(TextView)convertView.findViewById(R.id.addshortcutitemname);
				
				convertView.setTag(holder);//对该convertView设置标签
			}else{
				holder=(ViewHolder)convertView.getTag();
			}
			holder.icon.setImageDrawable((Drawable)(toDrawList.get(position).get("itemIcon")));
			holder.name.setText(toDrawList.get(position).get("itemName").toString());
			if(tempMap.containsKey(appList.get(position).getActivityName()))//根据tempMap设置不同的选中图片
				holder.sel.setImageResource(R.drawable.item_img_sel);
			else
				holder.sel.setImageResource(R.drawable.item_img_unsel);

			return convertView;
		}
		//封装view
		public final class ViewHolder{
			public ImageView icon;//app图标
			public ImageView sel;//是否被选中图标
			public TextView name;//app名称
		}
	};

	//监听返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK) {//返回键时也保存内容，再返回到GameActivity
        	saveResult();
			Intent i=new Intent(AddShortcutActivity.this, GameActivity.class);
			startActivity(i);
        	AddShortcutActivity.this.finish();
        	return true;
         }
         return super.onKeyDown(keyCode, event);
     }
	/*@Override
	protected void onDestroy() {
		System.gc();
		super.onDestroy();
	}*/
}