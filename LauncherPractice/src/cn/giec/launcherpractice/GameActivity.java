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
	private int NUMBER_PER_SCREEN=24;//ÿ����ʾ��APPͼ����Ŀ
	private ArrayList<AppItem> appList=new ArrayList<AppItem>();//��ݷ�ʽAPP package�� Activity�� ����
	private ArrayList<HashMap<String,Object>> toDrawList=new ArrayList<HashMap<String,Object>>();//��ݷ�ʽAPP iconͼ�� label ����
	private int screenNo=0;//��ǰ�����
	private int screenCount;//ȫ��������
	private ViewSwitcher switcher;//�л�����GridView����һ ViewSwitcher
	private LayoutInflater inflater;//��ǰ��Ļ����layout
	private HashMap<String,String> tempMap=new HashMap<String,String>();//��ݷ�ʽAPP activity��-package�� ��ֵ��
	private float touchDownX , touchUpX;//������Ļ�¼����� �뿪��Ļ�¼�����
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_game);

		try{
			findViewById(R.id.gameLayout).setBackgroundResource(R.drawable.game);
		}catch (OutOfMemoryError e) {//���ڱ���ͼ���󣬾���������ط�OutOfMemory
			System.out.println("�ڴ治�㣡��");
			System.gc();//֪ͨ����������ڴ棬�����������뱳��ͼ
			findViewById(R.id.gameLayout).setBackgroundResource(R.drawable.game);
		}

		findAdded();//��ȡ��������ӿ�ݷ�ʽAPP
		setSwitcher();//����ViewSwitcher
		setDot();//����СԲ��
	}

	//��ȡ��������ӿ�ݷ�ʽAPP
	private void findAdded(){
		//���첢��ӵ�һ��ͼ��
		HashMap<String, Object> editIcon = new HashMap<String, Object>();
		Drawable exitImg=getResources().getDrawable(R.drawable.item_img_add);
		editIcon.put("itemIcon",exitImg);
		editIcon.put("itemName","Edit APP list");
		toDrawList.add(editIcon);
		appList.add(new AppItem("",""));//package��activityΪ��

		inflater=LayoutInflater.from(this);
		//��SharedPreferences�ļ�"shortcut"��û�и��ļ����½�������Ϊ��
		SharedPreferences sp=this.getSharedPreferences ("shortcut",Context.MODE_PRIVATE);
		PackageManager pm=getPackageManager();//PackageManager���ڲ�ѯapp��ز���
		tempMap=(HashMap<String,String>)sp.getAll();//"shortcut"�����ļ��ж�ȡ���м�ֵ�Ե�tempMap��
		
/*** ���� ��ȡ��������ӿ�ݷ�ʽAPP������********************************************************************/
		List<AppItem> altemp=null;//ϵͳ������APP pkg name �� activity name
		ArrayList<HashMap<String,Object>> tdtemp=null;//���Ӧ��APP Icon �� label
		AppList al=new AppList(this);
		al.queryAllApps();
		altemp=al.getAppList();
		tdtemp=al.getToDrawList();
		boolean mark[] = new boolean[altemp.size()];
		for(int i=0;i<altemp.size();i++){
			mark[i]=false;//�����Щ������˿�ݷ�ʽ��APP
		}
		Iterator iter =tempMap.entrySet().iterator();//tempMap�ĵ����� ���ڱ���
		while (iter.hasNext()) {//����tempMap����altemp�в��Ҷ�ӦAPP������ֹ����ЧAPP����GridView
			HashMap.Entry entry = (HashMap.Entry) iter.next();//��ȡHashMapÿһ����ֵ�Զ���entry
			for(int i=0;i<altemp.size();i++){
				if(altemp.get(i).getPkgName().equals(entry.getValue())//������activity����Ҫ��ͬ�ű�ʾ�ҵ�
						&&altemp.get(i).getActivityName().equals(entry.getKey())){
					mark[i]=true;
					break;
				}
			}
		}
		for(int i=0;i<altemp.size();i++){//��ԭ����˳���б�ǵ�APP��Ϣ��������list
			if(mark[i]){
				appList.add(altemp.get(i));
				toDrawList.add(tdtemp.get(i));
			}
		}
		screenCount=appList.size()%NUMBER_PER_SCREEN==0? appList.size()/NUMBER_PER_SCREEN : appList.size()/NUMBER_PER_SCREEN+1;
	}

/*** ���� ��ȡ��������ӿ�ݷ�ʽAPP������********************************************************************/

/*** ���� ��ȡ��������ӿ�ݷ�ʽAPP����һ�������е�APP�������������治һ�µ����*******************************/		
		//����/* */ע�ʹ�Ϊ��ȡ��ӿ�ݷ�ʽ��APP���ж�APP�Ƿ���Ч������Ĺ���
		//    /** */ע�ʹ�Ϊ�ʼ�
		/*Iterator iter =tempMap.entrySet().iterator();//tempMap�ĵ����� ���ڱ���
		while (iter.hasNext()) {//����tempMap����ֵ����ֹ����ЧAPP����GridView
			HashMap.Entry entry = (HashMap.Entry) iter.next();//��ȡHashMapÿһ����ֵ�Զ���entry
			AppItem ai=new AppItem((String)entry.getValue(),(String)entry.getKey());//keyΪactivity�� valueΪ����
			ActivityInfo info;
			try {
				ComponentName comp= new ComponentName(ai.getPkgName(),ai.getActivityName());
				info=pm.getActivityInfo(comp, 0);//PackageManager��ѯָ��app��Ϣ
				Drawable icon = info.loadIcon(pm);//iconͼ��
				String name =info.applicationInfo.loadLabel(pm).toString();*/	//app����					
				/**info.loadLabel(pm)ʱ��Ҫ��toString()��������(String)�����ȡ����ĳЩapp��labelʱ���������
				 *android.text.SpannedString cannot be cast to java.lang.String*/

				/*HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("itemIcon",icon);
				map.put("itemName",name);
				appList.add(ai);
				toDrawList.add(map);
				
			} catch (NameNotFoundException e) {
				//���ﲻ����ЧAPP��ʾ��������û�и���SharedPreferences�ļ�
				//�ڽ���AddShortcutActivityʱ��ɾ����ЧAPP
				//e.printStackTrace();
				System.out.println("��ȡAPPʧ��-�´ν���AddShortcutʱ��������"+ai.getPkgName());
			}*/
				
				/**��������
				 * ComponentName comp = new ComponentName(ai.getPkgName(),ai.getActivityName());  
				 * Intent appInt =new Intent();
				 * appInt.setComponent(comp);//?Ҫ��Ҫ 
				 * ResolveInfo reInfo=null;
				 * reInfo=pm.resolveActivity(appInt, 0);
				 * if(reInfo==null){
				 * 		continue;
				 * }
				 * Drawable icon = reInfo.loadIcon(pm);
				 * String name =(String)reInfo.activityInfo.applicationInfo.loadLabel(pm);*/
			
				/**������ע��ָ��APP������ResolveInfo reInfo��ȡӦ�����ƣ����¼���������
				 * reInfo.loadLabel(pm)
				 * reInfo.activityInfo.loadLabel(pm)
				 * reInfo.activityInfo.name
				 * reInfo.activityInfo.applicationInfo.name
				 * reInfo.activityInfo.applicationInfo.loadLabel(pm)
				 *  ֻ��reInfo.activityInfo.applicationInfo.loadLabel(pm)�������κ��������ȷ��ȡ*/
		/*}
		sort();//����
		screenCount=appList.size()%NUMBER_PER_SCREEN==0? appList.size()/NUMBER_PER_SCREEN : appList.size()/NUMBER_PER_SCREEN+1;		
	}*/

	/*private void sort(){
		if(toDrawList.size()>1){
			for(int i=0;i<toDrawList.size();i++){
				toDrawList.get(i).put("num", i);
			}
			//String��compareTo�����ǰ���ƴ���������ﰴ��ƴ������ĸ����
			Collections.sort(toDrawList.subList(1, toDrawList.size()-1),new MyDisplayNameComparator());
			ArrayList<AppItem> tempAppList=new ArrayList<AppItem>();
			for(int i=0;i<toDrawList.size();i++){//��Ӧ����appList
				tempAppList.add(appList.get((Integer) toDrawList.get(i).get("num")));
			}
			appList=tempAppList;
			tempAppList=null;
		}
	}*/
/*** ���� ��ȡ��������ӿ�ݷ�ʽAPP����һ�������е�APP�������������治һ�µ����*******************************/

	//����ViewSwitcher
	private void setSwitcher(){
		switcher=(ViewSwitcher)findViewById(R.id.gameViewSwitcher);
		switcher.setFactory(new ViewFactory(){////ViewSwitcher�Ĺؼ����� setFactory
			@Override
			public View makeView() {//��̬����һ��GridView�����ظ�GridView
				View gv=inflater.inflate(R.layout.layout_game_gridview, null);
				GridView gg=(GridView)gv.findViewById(R.id.gameGrid);
				//GridView����ע�ᴥ���������������޷���ȡ�������¼� ���Ǹ����¼��ַ�������д��ط���
				gg.setOnTouchListener(new GridTouchListener());
				gg.setOnItemClickListener(new OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if((screenNo*NUMBER_PER_SCREEN+position)!=0){//����screenNo��GridView���λ��ӳ�䵽appList��ʵ��λ��
							Intent intent=new Intent();
							intent.setComponent(new ComponentName(appList.get(screenNo*NUMBER_PER_SCREEN+position).getPkgName(),  
									appList.get(screenNo*NUMBER_PER_SCREEN+position).getActivityName()));
							startActivity(intent);
							
						}else if((screenNo*NUMBER_PER_SCREEN+position)==0){//��һ��ͼ��Ϊ��AddShortcutActivity
							Intent i=new Intent(GameActivity.this, AddShortcutActivity.class);
							startActivity(i);
							GameActivity.this.finish();
						}
					}
				});
				return gv;
				}
		});
		switcher.setOnTouchListener(new STouchListener());//GridView�հ״�Ҳ���л�����Ļ�ļ���
		//��ʾ��һ��
		switcher.setInAnimation(this,R.anim.slide_in_right);//switcher���붯��
		switcher.setOutAnimation(this,R.anim.slide_out_left);//switcher�˳�����
		((GridView)switcher.getNextView()).setAdapter(adapter);//��ý����һ����������adapter�ṩ
		switcher.showNext();//���������ʾ
	}

	//����СԲ��
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

	//����СԲ��
	private void changeDot(int a,int b){//aΪ�˳����ı�� bΪ�������ı�� 
		LinearLayout dotline=(LinearLayout)findViewById(R.id.gameDot);
		ImageView i=(ImageView)dotline.getChildAt(a);
		i.setImageResource(R.drawable.page_bg);
		i=(ImageView)dotline.getChildAt(b);
		i.setImageResource(R.drawable.page_focus_bg);
	}

	//GridView������Ӧ �л�����
	private class GridTouchListener implements OnTouchListener {
		/**GridView���ǵ�����switcher��ȡ����onTouch�¼�*/
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				touchDownX=event.getX();
				/**���ﷵ��true��GridViewÿ�ζ��Ǽ�����ÿһ�е�һ��*/
			}else if(event.getAction()== MotionEvent.ACTION_UP){
				touchUpX=event.getX();
				//�жϻ�������
				if(touchDownX-touchUpX >50){//ǰ������50
					next();
				}else if(touchUpX-touchDownX>50){//���˻���50
					prev();
				}
			}
			return false;
		}
	};

	//Switcher������Ӧ �л�����
	private class STouchListener implements OnTouchListener {
	/**GridView Item����ʱ�����һ�����µĿհ������޷���GridView������ ����switcherҲע��listener*/
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				touchDownX=event.getX();
				/**���ﷵ��true�������޷�������ACTION_UP*/
				return true;
			}
			if(event.getAction()== MotionEvent.ACTION_UP){
				touchUpX=event.getX();
				//�жϻ�������
				if(touchDownX-touchUpX >50){//ǰ������50
					next();
				}else if(touchUpX-touchDownX>50){//���˻���50
					prev();
				}
			}
			return false;
		}
	}

	//�л�����һ��
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
		if(screenCount!=1 && screenNo==screenCount-1){//���Ϊ���һ����ѭ������
			changeDot(screenNo,0);
			screenNo=0;
			switcher.setInAnimation(this,R.anim.slide_in_right);
			switcher.setOutAnimation(this,R.anim.slide_out_left);
			((GridView)switcher.getNextView()).setAdapter(adapter);
			switcher.showNext();
		}
	}

	//�л�����һ��
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
		if(screenCount!=1 && screenNo==0){//���Ϊ��һ����ѭ������
			changeDot(screenNo,screenCount-1);
			screenNo=screenCount-1;
			switcher.setInAnimation(this,R.anim.slide_in_left);
			switcher.setOutAnimation(this,R.anim.slide_out_right);
			((GridView)switcher.getNextView()).setAdapter(adapter);
			switcher.showNext();
		}
	}

	//�����Զ���AdapterΪGridView�ṩ����
	private BaseAdapter adapter = new BaseAdapter(){

		@Override
		public int getCount() {//����screenNo���ظ�GridView��item����
			if(screenNo == screenCount-1 && appList.size()%NUMBER_PER_SCREEN !=0){
				return toDrawList.size()%NUMBER_PER_SCREEN;
			}
			return NUMBER_PER_SCREEN;
		}

		@Override
		public HashMap<String, Object> getItem(int position) {
			//����screenNo���ظ�λ��item��toDrawList��ʵ��λ��
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

	//��������
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
        /**��ȡ��ǰ���������item�ı��
         * GridView ggg=(GridView)switcher.getChildAt(switcher.getDisplayedChild());
         *�������Itemλ��Ϊ   +ggg.getSelectedItemPosition()*/
         return super.onKeyDown(keyCode, event);
     }
	@Override
	protected void onDestroy() {
		//�˳�ʱ֪ͨ���������������Դ
		/*for(HashMap<String,Object>  i:toDrawList){
			Drawable a=(Drawable)(i.get("itemIcon"));
			a.setCallback(null);
		}
		Drawable bg=this.getResources().getDrawable(R.drawable.game);
		bg.setCallback(null);*/
		super.onDestroy();
	}
}