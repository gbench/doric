package gbench.sandbox.matrix.algebra;

import org.junit.Test;

import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import java.util.stream.Collectors;
import gbench.commonApp.data.DataMatrixApp;

public class JunitPloynomial extends DataMatrixApp {
    @Test
    public void foo() {
        cph2(RPTA(3,"ABCDEFG".split(""))).map(e-> LIST(e.values().stream().sorted()))
        .collect(Collectors.groupingBy(e->e))
        .forEach((key,vv)->{
            var item = key.stream().map(Object::toString).collect(Collectors.joining(""));
            var cnt = vv.size();
            System.out.println(MFT("{0}:{1}",cnt,item));
        });
    }
}
