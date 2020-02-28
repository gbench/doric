package gbench.common.fs.junit;
import static java.util.Arrays.*;

import java.util.LinkedList;

import static gbench.common.fs.XlsFile.*;
import static gbench.common.fs.XlsFile.DataMatrix.*;

/**
 * EXCEL的 简单处理函数
 * 
 * @author gbench
 *
 */
public class JunitExcel {
	
	/**
	 * 	简单的数据操作
	 * @param excel
	 */
	public static void manipulate(SimpleExcel excel) {
		//计算指定区域:这里的吹很简单就是变换一个数据类型，其实可以做更为负载的操作。mapper 可以是一个函数f(object)
		System.out.println(excel.evaluate("Sheet1!I11:W26",asList("1,2".split(",")), e->(Double)e));
		//计算指定区域
		System.out.println(excel.evaluate("Sheet1!I11:W26", new LinkedList<>(),e->(Double)e));//首行作为标题了
		//计算指定区域-简单的矩阵计算,默认表头
		DataMatrix<Double> m2x2 = excel.evaluate("Sheet1!I11:W26",e->(Double)e).submx("A1:B10");
		DataMatrix<Double> m2x1 = dbl_vec2mx(ones(2));
		System.out.println(m2x2.mmult(m2x1).mmult(dbl_vec2mx(ones(10)).transpose()));//首行作为标题了
		//显示一个全一矩阵
		System.out.println(dbl_vec2mx(ones(10)));
	}
	
	/**
	 * 测试函数
	 * @param args
	 */
	public static void main(String args[]) {
		String file = "C:\\Users\\gbench\\Desktop\\home\\MSDS数据.xls"; // excel 本地文件路径
		SimpleExcel excel = new SimpleExcel(file);// 生成一个excel 对象
		
		// 产看excel的各个sheet数据
		excel.sheets().forEach(e->{
			System.out.println(e.getSheetName());
		});
		
		//自动定位数据区间
		StrMatrix mm = excel.autoDetect(1);
		System.out.println(mm);
		mm = excel.range("Sheet2!B2:D10");// 读取指定范围的excel 内容
		System.out.println(mm);
		mm = excel.autoDetect("Sheet2");// 自动从Sheet2中读取数块
		mm.rfor(row->{// 数据行遍历
			System.out.println(row);
		});
		
		mm.transpose().forEach(e->{
			System.out.println(e);
		});
		
		System.out.println("表头数据："+mm.header());
		//System.out.println(mm.submx(0, 0, 2, 2).tranpose());
		System.out.println(mm.submx("A1:H100"));
		//使用字符串矩阵二维数组构建矩阵
		System.out.println(new StrMatrix(mm.sub_2darray("A1:A10 "),asList("1,2".split(","))));
		
		//数据操作
		manipulate(excel);
		
	}
}
