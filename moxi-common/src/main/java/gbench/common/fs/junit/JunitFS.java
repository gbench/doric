package gbench.common.fs.junit;

import java.util.stream.Stream;
import java.util.Properties;
import java.util.stream.Collectors;
import static gbench.common.fs.FileSystem.*;

public class JunitFS {
	/**
	 * 	寻找特定标记的文件行
	 * @param args
	 */
	public static void main(String args[]) {
		/*String path = "D:\\sliced\\sandbox\\svn\\daji\\trunk\\ITF\\marketingplatform\\src\\main\\resources\\mapper";
		File home = new File(path);
		tranverse(home,file->{
			AtomicLong lng = new AtomicLong(1L);
			StringBuffer buffer = new StringBuffer();
			utf8Lines(file).map(e->lng.getAndIncrement()+")"+e)
				.filter(line->line.contains("goods"))// 添加行号
				.forEach(line->buffer.append(line+"\n"));
			if(buffer.length()>0) {
				System.out.println("\n"+file+"\n"+buffer);
			}
		});*/
		
		Properties vars = defvars("a","zhangsan","b","lisi");// 变量集合
		final String src = "C:\\Users\\gbench\\Desktop\\bak\\a.conf";
		final String dest = "C:\\Users\\gbench\\Desktop\\bak\\b.conf";
		Pipe<String,Boolean> pipe = new Pipe<>(a->a,lines(src,"utf8")) // 注意这是最后pipe返回的类型
			.pipe(lines->(Stream<String>)lines.map(line->inject(line,vars))) // 按行进行数据处理
			.pipe(lines->lines.collect(Collectors.joining("\n"))) //  合并成一行问及那
			.pipe(line->utf8write(dest,()->line));
		System.out.println(pipe.evaluate());// 文件处理
		
		shell("cmd /c nginx -s stop","D:\\sliced\\develop\\nginx-1.14.1",e->{});
		System.out.println("over");
	}
}
