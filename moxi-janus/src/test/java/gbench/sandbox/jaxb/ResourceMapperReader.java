package gbench.sandbox.jaxb;

import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.Test;

import gbench.common.fs.FileSystem;
import static gbench.common.tree.LittleTree.*;

public class ResourceMapperReader {
    
    public static void dfs(File file,Consumer<File> cons) {
        if(file.isFile())cons.accept(file);
        else Arrays.stream(file.listFiles()).forEach(e->dfs(e,cons));
    }
    
    @Test
    public void foo(){
        dfs(new File("D:\\sliced\\sandbox\\saas-hp\\marketingplatform\\src\\main\\resources\\mapper"),file->{
            final var extension = FileSystem.extensionpicker(file.getName());
            //System.out.println(file.getAbsolutePath());
            if(extension.equals("xml")) {
                final var path = file.getAbsolutePath();
                System.out.println(MFT("{0}\t{1}",path,JavaXBParser.get(path).namespace));
            }
        });
    }

}
