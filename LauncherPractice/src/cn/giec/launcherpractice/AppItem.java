package cn.giec.launcherpractice;

/*AppList��applist��һ�����package����activity��*/
public class AppItem {
	private String pkgName;	
	private String activityName;	

	public AppItem (String pn,String actn){
		setPkgName(pn);
		setActivityName(actn);
	}
	public String getPkgName(){
		return pkgName;
	}
	public String getActivityName(){
		return activityName;
	}
	public void setPkgName(String pn){
		pkgName=pn;
	}
	public void setActivityName(String actn){
		activityName=actn;
	}
}