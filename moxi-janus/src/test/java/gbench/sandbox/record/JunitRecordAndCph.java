package gbench.sandbox.record;

import static gbench.common.tree.LittleTree.cph;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static gbench.common.tree.LittleTree.*;
import gbench.common.tree.LittleTree.Tuple2;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.KVPair;
import static gbench.commonApp.jdbc.Neo4jApp.Graph;

public class JunitRecordAndCph {
    
    public static void foo() {
        final var nodes= new LinkedList<KVPair<String,Object>>();
        cph("abcdefg".split(""),"1234567".split(""),"ABCEDEFG".split(""))// 线段节点定义
        .stream().flatMap(e->e.reverse().tuple2Stream())
        .forEach(span->{
            nodes.addAll(span.tt());
            System.out.println(span);
        });
        
        nodes.stream().distinct().forEach(e->{
            System.out.println(e);
        });
        
        System.out.println();
        final var pp = STRING2REC("1,2,3,4,5,6");
        System.out.println(REC("name","张三","sex","man","address","上海 法华镇路","phone","18601690160").kvs().stream()
            .map(Object::toString).collect(Collectors.joining(" --> ")));
        pp.filter("0,1,2,3".split(",")).kvstream().forEach(System.out::println);
    }
    
    /**
     * 
     * @param args
     */
    public static void main(String args[]) {
        var nodes = new LinkedList<KVPair<String,Object>>();
        var edges = new LinkedList<Tuple2<KVPair<String,Object>,KVPair<String,Object>>>();
        
        // 图结构处理
        var lines = cph(series,"1:5","1:5","1:2","1:6");
        lines.forEach(e->{
            e.reverse().tuple2Stream().forEach(span->{ nodes.addAll(span.tt()); edges.add(span); });
        });
        var g = new Graph();
        Stream.of(// 流化处理
            nodes.stream().distinct().map(g.vertex_renderer()).distinct(),
            edges.stream().map(g.edge_renderer())
        ).flatMap(e->e).forEach(System.out::println);
    }
}
