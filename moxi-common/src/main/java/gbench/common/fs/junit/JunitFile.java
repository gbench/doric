package gbench.common.fs.junit;

import static gbench.common.fs.FileSystem.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitFile {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		/*String fullname = "C:\\Users\\gbench\\Pictures\\Saved Pictures\\avatar.jpg";
		String base64str = file2base64str(fullname);// 文件转base64文件对象
		System.out.println(base64str);
		base64str2file(base64str,"C:\\Users\\gbench\\Pictures\\Saved Pictures\\new file.jpg");*/
		
		System.out.println(unixpath("c:\\a\\b\\\\c"));
		System.out.println("'x/abc.exe' 的扩展名:'"+extensionpicker("x/abc.exe")+"'");
		System.out.println("'x\\abc' 的扩展名:'"+extensionpicker("x\\abc")+"'");
		System.out.println("'' 的扩展名:'"+extensionpicker("")+"'");
		System.out.println("'.' 的扩展名:'"+extensionpicker(".")+"'");
		
		System.out.println("'x/abc.exe' 的文件名:'"+namepicker("x/abc.exe")+"'");
		System.out.println("'x\\abc' 的文件名:'"+namepicker("x\\abc")+"'");
		System.out.println("'' 的文件名:'"+namepicker("")+"'");
		System.out.println("'.' 的文件名:'"+namepicker(".")+"'");
		System.out.println(unixpath("c:\\a\\b\\c.jpg"));
		
		String dest= "f:/a/b/c/d.tar.gz";
		while(dest.matches(".+\\.[^\\.]+$")) {
			System.out.println(dest);
			dest = dest.replaceFirst("\\.[^\\.]+$", "");
		}
		System.out.println("--------"+dest);
	}

}
