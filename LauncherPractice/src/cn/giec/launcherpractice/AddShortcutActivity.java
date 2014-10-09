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
	private List<AppItem> appList=null;//����APP pkg name �� activity name ���ڴ���Intent
	private ArrayList<HashMap<String,Object>> toDrawList=null;//����APP Icon �� label ���ڴ���ͼ��
	//���ڱ����SharedPreferences��ȡ�Ļ��ߴ�д��SharedPreferences�ļ��Ŀ�ݷ�ʽAPP��Ϣ������Ϊactivity��-package����ֵ�Լ�
	private HashMap<String,String> tempMap=null;
	private GridView appGV;//GridView
	MyAdapter myadapter=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_addshortcut);

		setUpList();//��ȡ����APP
		findAddedApps();//��ȡSharedPreferences����tempMap���ݵ�ǰAPP�б�ɸѡ����ЧAPP
		setAdapter();
		setListener();
	}

	//����GridView��Adapter
	private void setAdapter(){
		appGV=(GridView)findViewById(R.id.appitemGrid);	
		myadapter=new MyAdapter();
		appGV.setAdapter(myadapter);		
	}

	//�˳�ʱ�Կ�ݷ�ʽ��Ϣ���б���
	private void saveResult(){
		SharedPreferences sp=this.getSharedPreferences ("shortcut",Context.MODE_PRIVATE);//����ֻ�ܱ���Ӧ�ö�д
		Editor editor = sp.edit();//��д�ļ���Editor����
		editor.clear();//����ļ�ԭ����

		Iterator iter =tempMap.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			//����ʱkeyΪactivity�� valueΪ����
		    editor.putString((String)entry.getKey(), (String)entry.getValue());
		}
		editor.commit();/**ִ��commit()������д�ļ�*/
	}

	//���GridView item�ļ�����Ӧ
	private void setListener(){
		appGV.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String pn=appList.get(position).getPkgName();
				String an=appList.get(position).getActivityName();
				if(position==0){//��һ��ͼ��Ϊ���沢�˳�
					saveResult();
					Intent i=new Intent(AddShortcutActivity.this, GameActivity.class);
					startActivity(i);
					AddShortcutActivity.this.finish();
				}
					
				else if(tempMap.containsKey(an)){//���tempMap�а���keyΪ��APP��activity��
						tempMap.remove(an);	//�Ƴ���APP
					}
					else{
						tempMap.put(an, pn);//û������Ӹ�APP��activity��-package����ֵ�Ե�tempMap
					}
				myadapter.notifyDataSetChanged();
			}
		});
	}

	//��ȡSharedPreferences����tempMap���ݵ�ǰAPP�б�ɸѡ����ЧAPP
	private void findAddedApps(){
		//�������ļ�"shortcut"��ȡ����ӿ�ݷ�ʽ��App��Ϣ
		SharedPreferences sp=this.getSharedPreferences ("shortcut",Context.MODE_PRIVATE);
		tempMap=(HashMap<String,String>)sp.getAll();
		//ɸѡ����Ч��App 
		Iterator iter =tempMap.entrySet().iterator();
		while (iter.hasNext()) {//����ÿ��tempMap
			HashMap.Entry entry = (HashMap.Entry) iter.next();
		    boolean finded=false;
		    for(AppItem ai : appList){//�Ա�����APP
		    	/**������appList.contains(ad)�ж�appList���Ƿ����½���ad����ԭ��μ��� ����ʵ��
		    	 *������ == �ж������ַ����Ƿ�һ����Ӧ�� equals()*/
		    	if(ai.getActivityName().equals((String)entry.getKey())){
		    		finded=true;
		    		break;
		    	}
		    }
		    if(!finded){//APP�б�appList�в鲻���ÿ�ݷ�ʽ��APP����ʾ��APP�Ѳ����ڣ��޳�
		    	iter.remove();
		    }
		}
	}

	//��ȡ����APP
	private void setUpList(){
		//��һ��itemΪ���˳���ͼ��
		appList=new ArrayList<AppItem>();
		toDrawList=new ArrayList<HashMap<String,Object>>();		
		appList.add(new AppItem("",""));
		HashMap<String, Object> map = new HashMap<String, Object>();		
		Drawable exitImg=getResources().getDrawable(R.drawable.item_img_exit);
		map.put("itemIcon",exitImg);
		map.put("itemName","Save and return");
		toDrawList.add(map);
		//ʣ�µ�ΪApp
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
			if(convertView==null){//�ж�convertView�Ƿ��Ѵ��ڣ��������ñ�ǩ����������ܵ�ʵ�֣�����μ�����
				holder=new ViewHolder();
				inflater=LayoutInflater.from(AddShortcutActivity.this);
				convertView=inflater.inflate(R.layout.layout_addshortcut_item,null);
				holder.icon=(ImageView)convertView.findViewById(R.id.addshortcutitemicon);
				holder.sel=(ImageView)convertView.findViewById(R.id.addshortcutitemiconsel);
				holder.name=(TextView)convertView.findViewById(R.id.addshortcutitemname);
				
				convertView.setTag(holder);//�Ը�convertView���ñ�ǩ
			}else{
				holder=(ViewHolder)convertView.getTag();
			}
			holder.icon.setImageDrawable((Drawable)(toDrawList.get(position).get("itemIcon")));
			holder.name.setText(toDrawList.get(position).get("itemName").toString());
			if(tempMap.containsKey(appList.get(position).getActivityName()))//����tempMap���ò�ͬ��ѡ��ͼƬ
				holder.sel.setImageResource(R.drawable.item_img_sel);
			else
				holder.sel.setImageResource(R.drawable.item_img_unsel);

			return convertView;
		}
		//��װview
		public final class ViewHolder{
			public ImageView icon;//appͼ��
			public ImageView sel;//�Ƿ�ѡ��ͼ��
			public TextView name;//app����
		}
	};

	//�������ؼ�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK) {//���ؼ�ʱҲ�������ݣ��ٷ��ص�GameActivity
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