package gbench.common.chinese;

import java.util.Arrays;

public class JunitPinyin {
	
	public static void main(String args[]) {
		
		 String str = "中国银行长沙分行";  
		 //String str = "龙港巷店";  
	          
		 PinyinUtil.initPinyin("/duoyin_dic");  
	          
	     String py = PinyinUtil.convertChineseToPinyin(str);  
	    
	     System.out.println(str+" = "+py);  
	     str="行走的点唱机";
	     System.out.println(str+" = "+Arrays.asList(PinyinUtil.getPinyinShort(str)));
	     System.out.println(str+" = "+Arrays.asList(PinyinUtil.getPinyinLong(str)));
	     System.out.println(str+" = "+Arrays.asList(PinyinUtil.getPinyins(str)));
	}

}
