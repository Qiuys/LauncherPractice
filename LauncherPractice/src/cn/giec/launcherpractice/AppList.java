package cn.giec.launcherpractice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/*��ϵͳ��ѯ����ȡ����APP�������*/
public class AppList {
	private Context ct;
	private List<AppItem> appList;//����APP pkg name �� activity name ���ڴ���Intent
	private ArrayList<HashMap<String,Object>> toDrawList;//����APP Icon �� label ���ڴ���ͼ��

	public AppList(Context t){
		ct=t;
		appList=null;
		toDrawList=null;
	}

	public int getAppNum(){
		return appList.size();
	}

	public List<AppItem> getAppList(){
		return appList;
	}

	public ArrayList<HashMap<String,Object>> getToDrawList(){
		return toDrawList;
	}

	public void queryAllApps(){
		PackageManager pm=ct.getPackageManager();
		Intent appsInt=new Intent(Intent.ACTION_MAIN);//��ѯ��������������������APP
		appsInt.addCategory(Intent.CATEGORY_LAUNCHER);//�˴����Ը�������Ĳ�ͬ�����ģ���ȡ��ͬ���͵�APP
		List<ResolveInfo> rl=pm.queryIntentActivities(appsInt,0);//�ڶ�������Ӧ��Ϊ0 why
		Collections.sort(rl,new ResolveInfo.DisplayNameComparator(pm));//�Ի�ȡ��APP��������
		if(appList!=null)
			appList.clear();
		if(toDrawList!=null)
			toDrawList.clear();

		appList=new ArrayList<AppItem>();
		toDrawList=new ArrayList<HashMap<String, Object>>();

		for(ResolveInfo reInfo : rl){
			if(reInfo.activityInfo.packageName.equals("cn.giec.launcherpractice")){
				break;
			}
			AppItem aitem=new AppItem(reInfo.activityInfo.packageName,reInfo.activityInfo.name);
			appList.add(aitem);//����appList����������ӦAPP

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemIcon",reInfo.loadIcon(pm));
			map.put("itemName",(String)reInfo.loadLabel(pm));
			toDrawList.add(map);//����toDrawList���ڻ���GridView
		}
	}
}