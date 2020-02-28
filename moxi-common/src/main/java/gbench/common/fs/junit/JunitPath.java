package gbench.common.fs.junit;
import static gbench.common.fs.FileSystem.*;

public class JunitPath {
	public static void main(String args[]) {
		System.out.println(path("",null));
		System.out.println(path("upload",JunitPath.class));
	}
}
