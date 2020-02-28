package gbench.common.fs.junit;

import gbench.common.fs.XlsFile.SimpleExcel;

public class JunitExcelWrite {
	public static void main(String args[]) {
		SimpleExcel excel = new SimpleExcel("c:/slicec/temp/a.xlsx",false);
		String ss[][] = new String[][] {
			"1,2,3,4,5".split("[,]+"),
			"1,2,3,4,9".split("[,]+"),
			"89,2888888,99,4,9,ds,sdf,s,sdsafsaf,asdfsafsa,,asdfsaf,".split("[,]+"),
			"89,20,909,4,9,99,434,3232".split("[,]+")
		};
		excel.write("ADC!D23",ss);
		excel.write("Sheet1!D23",ss);
		excel.write("Sheet8!D23",ss);
		excel.write("Sheet80!D23",ss);
		excel.save();
		excel.close();
	}

}
