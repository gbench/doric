package gbench.sandbox.matrix;

import org.junit.Test;
import static gbench.common.tree.LittleTree.frt;
import static gbench.common.tree.LittleTree.IRecord.A;
import static gbench.common.tree.LittleTree.IRecord.L;
import static gbench.common.tree.LittleTree.IRecord.REC;
import static gbench.common.tree.LittleTree.DataFrame.*;

public class JunitDataFrame {

    @Test
    public void foo() {
        final var dfm = DFM("A", L("a", "b", "c"), // 第一列
            "B", L(1, 2, 3), // 第二列
            "C", A(2, 4, 6, 10), // 第三列
            "D", REC(0, 3, 1, 6, 2, 9), // 第四列
            "E", REC(0, 31, 1, 61, 2, 91).toMap()// 第五列
        );// DataFrame
        System.out.println(dfm.toString2(frt(2)));
        // dfm.rows().forEach(System.out::println);
        final var df = dfm.melt(L("A", "B"), L("C", "D"));
        System.out.println(df.toString2());
    }
}
