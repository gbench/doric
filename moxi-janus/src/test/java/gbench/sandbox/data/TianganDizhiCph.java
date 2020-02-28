package gbench.sandbox.data;

import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TianganDizhiCph {
    
    @Test
    public void foo() {
        final var ai = new AtomicInteger(1);
        cph(series,"1:5","1:5","1:5","1:5","1:5").stream()
        //cph("子丑寅卯辰巳午未申酉戌亥".split(""),"甲乙丙丁戊己庚辛壬癸".split("")).stream()
        .map(line->line.vvjoin(""))
        .forEach(e->System.out.println(MFT("{0,number,#}-->{1}",ai.getAndIncrement(),e)));
    }
    
    @Test
    public void b() {
        final var t =  LIST(cph(series,"1:5","1:5","1:5","1:5","1:50").stream().map(e->e.reverse()));
        System.out.println(FMT2(t));
    }
}
