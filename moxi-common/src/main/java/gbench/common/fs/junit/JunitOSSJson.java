package gbench.common.fs.junit;


import java.util.List;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSONObject;
import gbench.common.fs.XlsFile.StrMatrix;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.fs.XlsFile.*;

/**
 * EXCEL的 简单处理函数
 * 
 * @author gbench
 *
 */
public class JunitOSSJson {
	
   /**
    * json 格式化
    * @param content
    * @return
    */
   public static String fastJsonFormatter(String content){
	   return JSONObject.toJSONString(JSONObject.parse(content),true);
    }
	
	/**
	 * 测试函数
	 * @param args
	 */
	public static void main(String args[]) {
		String file = "C:\\Users\\gbench\\Desktop\\制度管理.xlsx"; // excel 本地文件路径
		SimpleExcel excel = new SimpleExcel(file);// 生成一个excel 对象
		StrMatrix mx= excel.autoDetect("Sheet3");
		List<IRecord> recs = mx.mapByRow(LinkedRecord::new).collect(Collectors.toList());
		System.out.println(fastJsonFormatter(Json.obj2json(recs)));
	}
}
