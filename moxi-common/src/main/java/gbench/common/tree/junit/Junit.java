package gbench.common.tree.junit;

import java.io.*;
import java.util.function.Function;

public class Junit {
	
	public static void tranverse(File file,Function<File,Object> handler) {
		if(null == file)return;
		if(file.isFile()) {
			handler.apply(file);
		}else {
			for(File f:file.listFiles())tranverse(f,handler);
		}
	}
	
	public static void main(String args[]) {
		tranverse(new File("D:/sliced/sandbox/ITF/marketingplatform/src/main/resources/config"),
		file->{
			System.out.println("---------------------------"+"/"+file);
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				br.lines().forEach(e->{
					System.out.println(e);
				});
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		
		System.out.println("裁剪图片保存路径");
		System.out.println("#合同路径");
		System.out.println("#chapter 电子签章存放路径");
	}

}
