package cn.giec.launcherpractice;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
//����APP���Ƶĺ���ƴ����������
public class MyDisplayNameComparator implements
		Comparator<HashMap<String, Object>> {

	@Override
	public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
		return sCollator.compare(lhs.get("itemName").toString(), rhs.get("itemName").toString());
	}

    private final Collator   sCollator = Collator.getInstance();
}
