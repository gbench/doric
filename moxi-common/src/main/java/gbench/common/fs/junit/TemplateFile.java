package gbench.common.fs.junit;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import gbench.common.fs.junit.TemplateFile.FileSystem.Pipe;
import java.util.function.Supplier;

/**
 * 
 * @author gbench
 *
 */
public class TemplateFile {
	

	/**
	 * 
	 * @author gbench
	 *
	 */
	public static class FileSystem {
		/**
		 * 	生成当前类的默认数据存放路径
		 * @param file 文件路径
		 * @param clazz 与file文件同级的目录的class文件,null 则返回file本身
		 * @return
		 */
		public static String path(String file,Class<?>clazz) {
			String path = null;
			if(clazz==null)return file;
			File temp = new File(file);
			if(temp.exists())return temp.getAbsolutePath();
			try {
				path = clazz.getClassLoader()
					.getResource("").toURI().getPath();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}// 获取当前路径
			
			path+=clazz.getPackage().getName()
				.replace(".", "/")+"/"+file;
			//System.out.println(path);
			return path;
		}
		
		/**
		 * 	读取文件所有行
		 * 
		 * @param file 文件对象
		 * @param encoding 文件编码
		 * @return
		 */
		public static Stream<String> lines(String file,String encoding){
			return lines(new File(file),encoding);
		}
		
		/**
		 * 	尝试读取cls同级位置所存在文件信息
		 * 
		 * @param file 文件对象
		 * @param cls 类文件位置
		 * @param encoding 文件编码
		 * @return
		 */
		public static Stream<String> localLines(String file,Class<?>cls,String encoding){
			return lines(new File(path(file,cls)),encoding);
		}
		
		/**
		 * 	尝试读取cls同级位置所存在文件信息
		 * 
		 * @param file 文件对象
		 * @param cls 类文件位置
		 * @param encoding 文件编码
		 * @return
		 */
		public static Stream<String> localUtf8Lines(String file,Class<?>cls){
			var f = new File(path(file,cls));
			String encoding = "utf8";
			if(!f.exists()) {
				var stream = cls.getResourceAsStream(file);
				return lines(stream,encoding);
			}else {
				return lines(f,encoding);
			}
		}
		
		/**
		 * 	读取文件所有行
		 * 
		 * @param file 文件对象
		 * @param encoding 文件编码
		 * @return
		 */
		public static Stream<String> lines(InputStream is,String encoding){
			Stream<String> stream = null;
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(is,encoding));
				stream = br.lines().collect(Collectors.toList()).stream();
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return stream;
		}
		
		/**
		 * 	读取文件所有行
		 * 
		 * @param file 文件对象
		 * @param encoding 文件编码
		 * @return
		 */
		public static Stream<String> lines(File file,String encoding){
			Stream<String> stream = null;
			try {stream = lines(new FileInputStream(file),encoding);}catch(Exception e) {e.printStackTrace();}
			
			return stream;
		}
		
		/**
		 * 	读取文件所有行
		 * 
		 * @param file 文件对象
		 * @param encoding 文件编码
		 * @return
		 */
		public static Stream<String> utf8lines(File file){
			return FileSystem.lines(file,"utf8");
		}
		
		/**
		 * 	分段读取
		 * @param file 文件路径
		 * @param pattern 分段标记
		 * @param n 提取key的标记位置
		 * @return 分段key->lines Map
		 */
		public static Map<String,List<String>> split(String file,String pattern,int n){
			Map<String,List<String>> map = new HashMap<>();
			List<String> keys = new ArrayList<String>();
			final Pattern pat = Pattern.compile(pattern);
			FileSystem.utf8lines(new File(file)).forEach(e->{
				Matcher mat = pat.matcher(e);
				if(mat.find()) {
					String key = mat.group(1);
					map.computeIfAbsent(key, k-> new LinkedList<>());
					keys.add(key);
				}else if(!e.matches("^\\s*$")) {
					String key = keys.size()<1?"-":keys.get(keys.size()-1);
					if(map.get(key)!=null)map.get(key).add(e);
				}//if
			});
			return map;
		}
		
		/**
		 * 	文件遍历
		 * @param file
		 * @param cs
		 */
		public static void tranverse(File file,Consumer<File>cs) {
			if(file==null||!file.exists())return;
			if(file.isFile())cs.accept(file);
			else if(file.isDirectory()) Arrays.stream(file.listFiles()).forEach(cs);
		}
		
		/**
		 * 
		 * @param file 文件绝对路径
		 * @param contentSuppler  文件书写内容
		 */
		public static boolean utf8write(String file,Supplier<String> contentSuppler) {
			return write(new File(file),"utf8",contentSuppler);
		}
		
		/**
		 * 
		 * @param file 文件绝对路径
		 * @param contentSuppler  文件书写内容
		 */
		public static boolean write(String file,String encoding,Supplier<String> contentSuppler) {
			return write(new File(file),encoding,contentSuppler);
		}
		
		/**
		 * 
		 * @param file 文件对象
		 * @param contentSuppler  文件书写内容
		 */
		public static boolean write(File file,String encoding,Supplier<String> contentSuppler) {
			if(contentSuppler==null || file == null )return false;
			BufferedWriter bw = null;
			try {
				File pfile = file.getParentFile();
				if(!file.exists())pfile.mkdirs();
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),encoding));
				bw.write(contentSuppler.get());
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				try {bw.close();} catch (IOException e) {e.printStackTrace(); return false;}
			}
			
			return true;
		}
		
		/**
		 * 	管道处理
		 * @author gbench
		 *
		 * @param <T> 输入类型
		 * @param <U> 输出类型
		 */
		public static class Pipe<T,U>{
			
			/**
			 * 	创建一条管道
			 * @param tu_handler 管道的处理方式
			 * @param input 起始点的管道数据输入
			 */
			public Pipe(Function<T,U> tu_handler,Supplier<T> supplier){
				this.tu_handler = tu_handler;
				this.supplier = supplier;
			}
			
			/**
			 * 	创建一条管道
			 * @param tu_handler 管道的处理方式
			 * @param input 起始点的管道数据输入
			 */
			public Pipe(Function<T,U> tu_handler,T input){
				this.tu_handler = tu_handler;
				this.supplier = ()->input;
			}
	       
			/**
			*	 管道的处理连接
			* @param tu_handler 管道连接处理方式
			*/
			private Pipe(Function<T,U> tu_handler){
				this.tu_handler = tu_handler;
			}
			       
			/**
			* 	管道连接，连接处两个管道节点，在原来的管道出口接上一段新的管道节点，使用tx_handler
			* 	作为管道节点的处理函数
			* @param tx_handler 管道连接处理方式
			* @return
			*/
			public <X> Pipe<U,X> pipe(Function<U,X> tx_handler){
				Pipe<U,X> p = new Pipe<>(tx_handler);
				p.pre = this;
				return p;
			}
	
			/**
			* 	使用指定的入口值input尽心管道计算
			* @param input 指定的管道入口之
			* @return 整条 管道链的计算结果
			*/
			@SuppressWarnings("unchecked")
			public U evaluate(Object input){
				Stack<Pipe<?,?>> stack = new Stack<>();// 管道计算，擦除掉管道的计算类型信息,以便进行管道的连接的尾巴连接。
				stack.push(this);
				Pipe<?,?> p = this.pre;// 获取前一个处理节点
				while(p!=null){stack.push(p); p=p.pre;}
					Pipe<?,?> piper = stack.pop();// init pipe
					Object v = input==null?piper.input():input;// 确认输入信息: v是Value的缩写
					if(v==null)return null;// 没有有效输入项
					do{//从这里开始依次执行
					v=((Pipe<Object,Object>)piper).tu_handler.apply(v);// 管道计算覆写先前的value.
				    piper = stack.empty()?null:stack.pop();
				}while(piper!=null);// do
				
				return (U)v;// 结果返回，恢复到指定类型
			}
			       
			/**
			* 	使用初始管道入口值进行数计算
			* @return 整条 管道链的计算结果
			*/
			public U evaluate(){
				return evaluate(null);
			}
			
			public T input() {
				return supplier.get();
			}
	   
			private Function<T,U> tu_handler = null;// 管道的处理方式
			private Pipe<?,T> pre = null;// 前一段的管道节点
			private Supplier<T> supplier;//管道的接口收入
		};
		
		/**
		 * 	定义一个变量集合
		 * @param objs
		 * @return
		 */
		public static Properties defvars(Object ... objs){
			Properties props = new Properties();
			for(int i=0;i<objs.length-1;i++)props.put(objs[i], objs[i+1]);
			return props;
		}
		
		/**
		 * 
		 * @param pattern 页面模式
		 * @param line 行数据
		 * @return
		 */
		public static Matcher matcher(String pattern,String line) {
			if(pattern==null || line==null )return null;
			return Pattern.compile(pattern).matcher(line);
		}
		
		/**
		 * case sensive
		 * @param pattern
		 * @return
		 */
		public static Pattern P(String pattern) {
			return Pattern.compile(pattern);
		}

		/**
		 * case insensive
		 * @param pattern
		 * @return
		 */
		public static Pattern P2(String pattern) {
			return Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
		}

		/**
		 * 	内容替换,把模式中的数据给与替换
		 * @param pattern 页面模式
		 * @param line 行数据
		 * @return
		 */
		public static String replace(String pattern,String line,Map<String,String> map) {
			Properties props = new Properties();
			map.forEach(props::put);
			return replace(pattern,line,props);
		}
		
		/**
		 * 	内容替换,把模式中的数据给与替换
		 * @param pattern 页面模式
		 * @param line 行数据
		 * @return
		 */
		public static String replace(String pattern,String line,Properties props) {
			return replacen(P(pattern),line,props,null);
		}
		
		/**
		 * 	内容替换,把模式中的数据给与替换
		 * @param pattern 页面模式
		 * @param line 行数据
		 * @return
		 */
		public static String replace(Pattern pattern,String line,Map<String,String> map) {
			Properties props = new Properties();
			map.forEach(props::put);
			return replace(pattern,line,props);
		}
		/**
		 * 	内容替换,把模式中的数据给与替换
		 * @param pattern 页面模式
		 * @param line 行数据
		 * @return
		 */
		public static String replace(Pattern pattern,String line,Properties props) {
			return replacen(pattern,line,props,null);
		}
		
		/**
		 * map -->Properties
		 * @param map
		 * @return
		 */
		public static Properties map2props(Map<String,String> map) {
			Properties props = new Properties();
			map.forEach(props::put);
			return props;
		}
		
		/**
		 * 	内容替换,把模式中的数据给与替换
		 * @param pattern 页面模式
		 * @param line 行数据
		 * @return
		 */
		public static String replacen(Pattern pattern,String line,Map<String,String> map,Integer n) {
			return replacen(pattern,line,map2props(map),n);
		}
		/**
		 * 	内容替换,把模式中的数据给与替换
		 * @param pattern 页面模式
		 * @param line 行数据
		 * @return
		 */
		public static String replacen(Pattern pattern,String line,Properties props,Integer n) {
			String buffer = line;
			Matcher matcher = pattern.matcher(new String(line));
			StringBuilder builder = new StringBuilder();// 返回值
			while(buffer.length()>0) {
				if(!matcher.find()) {builder.append(buffer);break;}
				String key = n==null?matcher.group():matcher.group(n);
				builder.append(buffer.substring(0,matcher.start()));
				builder.append(props.getOrDefault(key, key));	
				buffer = buffer.substring(matcher.end());// 截取
				matcher = pattern.matcher(buffer);
			}
			return builder.toString();
		}
		
		/**
		 * 	变量注入
		 * @param line 行数据
		 * @param vars 变量集合
		 * @return
		 */
		public static String inject(String line,Properties vars) {
			String buffer = line;
			String pattern = "\\$*\\{\\s*([^\\}]*)\\s*\\}";
			Matcher matcher = matcher(pattern,new String(line));
			StringBuilder builder = new StringBuilder();// 返回值
			while(buffer.length()>0) {
				if(!matcher.find()) {builder.append(buffer);break;}
				String key = matcher.group(1);
				builder.append(buffer.substring(0,matcher.start()));
				builder.append(vars.getOrDefault(key, matcher.group()));	
				buffer = buffer.substring(matcher.end());// 截取
				matcher = matcher(pattern,buffer);
			}
			return builder.toString();
		}
		
		/**
		 * 	执行一个shell命令，并返回字符串值
		 *
		 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
		 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
		 * @return 执行结果组成的字符串
		 * @throws IOException
		 */
		public static synchronized void shell(String cmds) {
			shell(cmds.split("[\\s+]"),null,System.out::println,System.out::println);
		}
		
		/**
		 * 	执行一个shell命令，并返回字符串值
		 *
		 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
		 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
		 * @return 执行结果组成的字符串
		 * @throws IOException
		 */
		public static synchronized void shell(String cmds,String workingdirectory) {
			shell(cmds.split("[\\s+]"),workingdirectory,System.out::println,System.out::println);
		}
		
		/**
		 * 	执行一个shell命令，并返回字符串值
		 *
		 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
		 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
		 * @return 执行结果组成的字符串
		 * @throws IOException
		 */
		public static synchronized void shell(String cmds,final Consumer<String> stdin,final Consumer<String> stderr) {
			shell(cmds.split("[\\s+]"),null,stdin,stderr);
		}
		
		/**
		 * 	执行一个shell命令，并返回字符串值
		 *
		 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
		 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
		 * @return 执行结果组成的字符串
		 * @throws IOException
		 */
		public static synchronized void shell(String cmds, String wd, final Consumer<String> stdin, final Consumer<String> stderr) {
			shell(cmds.split("[\\s+]"),wd,stdin,stderr);
		}
	
		/**
		 * 	执行一个shell命令，并返回字符串值
		 *
		 * @param cmd           命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
		 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
		 * @return 执行结果组成的字符串
		 * @throws IOException
		 */
		public static synchronized void shell(String[] cmd, final String workingdirectory, 
			final Consumer<String> stdin,Consumer<String> stderr) {
			new Thread(()->{
				try {
					final ProcessBuilder builder = new ProcessBuilder(cmd);
					String wd = workingdirectory;// 工作目录
					if (wd == null) wd = System.getProperty("user.dir"); // 设置一个路径（绝对路径了就不一定需要）
					builder.directory(new File(wd)); // 设置工作目录（同上）
					builder.redirectErrorStream(true); // 合并标准错误和标准输出
					Process process = builder.start(); // 启动一个新进程
					BiConsumer<InputStream,Consumer<String>> handle = (inputstream,consumer)->{
						BufferedReader br = null;
						try {
							br = new BufferedReader(new InputStreamReader(inputstream,"gbk"));
							String line = null; while ((line = br.readLine()) != null) consumer.accept(line);
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							if(br!=null)try {br.close();}catch(Exception ex) {ex.printStackTrace();};
						}//try
					};
					// 结果处理
					handle.accept(process.getInputStream(),stdin); handle.accept(process.getErrorStream(),stderr);
		            process.waitFor(5, TimeUnit.SECONDS);// 等待5秒钟
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					// do nothing
				}// try
			}).start();
		}
	}

	/**
	 * 	更具文件模板，船舰一个目标文件
	 * @param src 源文件
	 * @param vars 模板变量
	 * @return 是否注入成功
	 */
	public static Boolean tpl2target(String tpl,String target,Properties vars) {
		String src = tpl;// 源文件
		String dest = target; // 目标文件
		Pipe<String,Boolean> pipe = new Pipe<>(a->a,FileSystem.lines(src,"utf8")) // 注意这是最后pipe返回的类型
			.pipe(lines->(Stream<String>)lines.map(line->FileSystem.inject(line,vars))) // 按行进行数据处理
			.pipe(lines->lines.collect(Collectors.joining("\n"))) //  合并成一行问及那
			.pipe(line->FileSystem.utf8write(dest,()->line));
		return pipe.evaluate();
	}
	
	/**
	 * 	创建模板配置文件
	 * @param tentants 租户系统名称
	 * @param confHome nginx 配置文件路径
	 * @param templateFile 模板文件路径
	 */
	public static void newTentantsConfFile(String [] tentants,String confHome,String templateFile) {
		for(String tentant : tentants ) {
			String src = templateFile ;// 模板文件位置
			String dest = confHome+tentant+".yichem365.conf";// 输出的实例文件位置
			// 模板文件的变量:${VARIABLE}
			Properties vars = FileSystem.defvars("SERVER-NAME",tentant+".yichem365.com",
				"INDEX1",tentant+".index.html","INDEX2",tentant+".index.htm");// 变量集合
			TemplateFile.tpl2target(src, dest, vars);// 模板文件实例化
			//生成的文件内容
			System.out.println("---------------"+dest+"---------------");
			System.out.println(TemplateFile.FileSystem.lines(dest, "utf8").collect(Collectors.joining("\n")));	
		}//for
	}
	
	/**
	 *  	方法使用示例
	 * @param args
	 */
	public static void main(String args[]) {
		String[] tentants = "tentant001,tentant002".split("[,]+");// 租户系统名称
		// 创建租户系统
		TemplateFile.newTentantsConfFile(tentants, 
			"D:\\sliced\\tmp\\nginx\\conf\\servers\\", 
			"D:\\sliced\\tmp\\nginx\\conf\\tentant-template.conf");
		String nginxHome = "D:\\sliced\\develop\\nginx-1.14.1";
		TemplateFile.FileSystem.shell("cmd /c nginx -s reload",nginxHome);
		System.out.println("运行结束！");
	}
	
}
