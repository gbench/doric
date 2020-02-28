package gbench.sandbox.neo4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import gbench.commonApp.jdbc.Neo4jApp;
import lombok.AllArgsConstructor;
import lombok.Data;

import static java.util.stream.Stream.*;
import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class GraphFromPaths {
    
    @BeforeEach
    public void beforeEaach() {
        System.out.println("----");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Radar"})
    public void foo(String name) {
        final var neo4jApp = new Neo4jApp();
        System.out.println(name);
        // 定义一个图
        final var lines = LIST(of(
            "1/2/3/4/5", 
            "a/b/c/c/e/f/g/h/i"
        ).map(IRecord::STRING2REC));
        @SuppressWarnings("unused")
        final var g = neo4jApp.graph(lines, false);
        
        lines.forEach(line->{
            //line.tuple2Stream().forEach(e->System.out.println(e));
            line.sliding(3, 3).forEach(e->System.out.println(e));
        });
        
        var user = new User("zhangsan","man");
        System.out.println(IRecord.OBJ2REC(user,"name sex address"));
    }
    
    @Data @AllArgsConstructor
    class User{
        String name;
        String sex;
    }

  
    @AfterAll
    static void afterAll() {
        System.out.println("after all");
    }
}
