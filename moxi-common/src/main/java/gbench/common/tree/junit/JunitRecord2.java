package gbench.common.tree.junit;

import static gbench.common.tree.LittleTree.LIST;
import static gbench.common.tree.LittleTree.IRecord.REC;
import static java.util.Arrays.*;
import java.util.HashMap;

/**
 * 深度遍历函数
 * @author gbench
 *
 */
public class JunitRecord2 {
	
	/**
	 * 深度遍历的测试函数
	 */
	public static void dfs() {
		// 复杂的对象遍历
		REC(
			"name","zhangsan",
			 "address",asList("shanghai","changning","xuhui"),
			 "contacts",REC("mobile","18601690610","email","gbench@sina.com"),
			 "friends",REC("chinese",asList("lisi","wangwu","zhaoliu"),
					"japanese",asList("hikaru","tanaka","sakura")).toMap(),
			 "misc",new HashMap<String,String>()// 空值
		).dfs((k,aa)->System.out.println(k+"--->"+LIST(aa)));
		
		
		//简单的对象遍历:对于集合元素值取第一元素给予遍历，这个一般是用于可与kvs值进行选取的便捷操作。
		System.out.println("简单的对象遍历");
		REC("name","zhangsan",
			"contract",REC("phone",asList("18601690610","13601670630"),
						"email",asList("gbench@sina.com","xqh@163.com"))
		).dfs2((k,v)->System.out.println(k+"--->"+v));
		
		System.out.println("树形结构的演示示例,一个REC就是一个命名节点,k是节点名,value 是节点的数值 :");
		REC(// 根节点"/"
			"台灯",REC(
					"灯头",REC(
							"灯泡",REC("价格",25),
							"灯罩",REC("价格",25,"产地",REC("城市","上海","街道","徐家汇"))
					)// 灯头
					,"灯臂",REC(
							"上臂",REC("价格",25),
							"下臂",REC("价格",25)
					)// 灯臂
					,"灯座",REC(
							"配重",REC("价格",25),
							"外壳",REC("价格",25)
					)//灯座
					,"附件",REC(
							"线缆",REC("价格",25),
							"开关",REC("价格",25)
					)//灯座
			)// 台灯
		).dfs((label,node)->System.out.println(label+"--->"+LIST(node)));
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		dfs();
	}

}
