package gbench.common.tree.junit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import gbench.common.fs.XlsFile.SimpleExcel;
import gbench.common.tree.LittleTree.IRecord;

import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.SimpleRecord.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitRecord {
	public static void main(String args[]) {
		final IRecord rc = REC2("name","123","time",new Date(),"user",
			REC2("name","zhangsan","age",234,"birth","1983-04-23"));
		String json = Json.obj2json(rc);
		IRecord rec = Json.json2obj(json, IRecord.class);
		System.out.println(rec);
		System.out.println(rec.date("time"));
		System.out.println(rec.rec("user").date("birth"));
		
		String s = "[{\"name\":\"zhangsan\"},{\"name\":\"lisi\"},"+json+"]";
		System.out.println(Json.json2list(s, IRecord.class));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		try {
			System.out.println(sdf.parse("1984523"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String drivier = "com.mysql.cj.jdbc.Driver";
		String user = "root";
		String password = "123456";
		String url = "jdbc:mysql://localhost:3306/sales190329?serverTimezone=GMT%2B8";
		Jdbc jdbc = new Jdbc(drivier,url,user,password);
		IRecord r = jdbc.sql2maybe("select * from t_pct_attribute_set").get();
		List<IRecord> rr =r.map2recs("Attributes");
		System.out.println(rr);
		System.out.println(r);
		
		SimpleExcel excel = new SimpleExcel("c:/slicec/temp/a.xlsx",false);
		String ss[][]= {
			"1,2,3,4,5".split(","),
			"6,7,8,9,10".split(","),
			"6,7,8,9,10".split(","),
		};
		
		// 扩展Fields的读取数据记录
		rr = jdbc.sql2records("select Fields from t_product",REC2("Fields","name,en,thumbnail"));
		ss = IRecord.toStringArray(rr);// 转换成二维字符数组
		excel.write("sheet2!C3",ss,rr.get(0).keys());
		System.out.println(excel.autoDetect("sheet2"));
		excel.save();
		excel.close();
	}
}
