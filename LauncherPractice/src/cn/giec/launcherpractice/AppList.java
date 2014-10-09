package cn.giec.launcherpractice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/*从系统查询并获取所需APP数组的类*/
public class AppList {
	private Context ct;
	private List<AppItem> appList;//保存APP pkg name 和 activity name 用于创建Intent
	private ArrayList<HashMap<String,Object>> toDrawList;//保存APP Icon 和 label 用于创建图标

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
		Intent appsInt=new Intent(Intent.ACTION_MAIN);//查询满足这两个条件的所有APP
		appsInt.addCategory(Intent.CATEGORY_LAUNCHER);//此处可以根据需求的不同做更改，获取不同类型的APP
		List<ResolveInfo> rl=pm.queryIntentActivities(appsInt,0);//第二个参数应该为0 why
		Collections.sort(rl,new ResolveInfo.DisplayNameComparator(pm));//对获取的APP进行排序
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
			appList.add(aitem);//存入appList用于启动响应APP

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemIcon",reInfo.loadIcon(pm));
			map.put("itemName",(String)reInfo.loadLabel(pm));
			toDrawList.add(map);//存入toDrawList用于绘制GridView
		}
	}
}