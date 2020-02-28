package gbench.appdemo.neo4j;

import gbench.common.tree.LittleTree.Jdbc;
import gbench.common.tree.LittleTree.Jdbc.*;
import gbench.common.tree.LittleTree.KVPair;
import gbench.commonApp.jdbc.*;
import gbench.appdemo.neo4j.GraphParser.TokenType;
import gbench.common.tree.LittleTree.IRecord;
import static gbench.common.tree.LittleTree.IRecord.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static gbench.common.tree.LittleTree.*;
/**
 * 演示实例
 * @author gbench
 *
 */
public class Neo4jAppDemo {
    @JdbcConfig(url="jdbc:neo4j:bolt://localhost/mydb?nossl",
     driver="org.neo4j.jdbc.Driver",user="neo4j",password="123456")
    interface Database extends ISqlDatabase{ // 数据接口
        @JdbcQuery List<IRecord> getCytokines(String name);
    }
    
    /**
     * 
     * @param database
     */
    public static void foo(Database database){
        final var proxy = database.getProxy();// 提取代理对象
        final var jdbc = proxy.findOne(Jdbc.class);
        jdbc.withTransaction( sess -> {
            final var recs = sess.sql2records("#getCytokines",R("name","肥大细胞"));
            System.out.println(FMT(recs));
        }, Jdbc.M(SqlPatternPreprocessor.class,proxy.findOne(SqlPatternPreprocessor.class )) );
        
        System.out.println(FMT(database.getCytokines("成纤维细胞")));
    }
    
    /**
     * 分解行
     * @param line
     */
    public static void parseLine(String line) {
        String[] ln = line.split(";\\s*\n");
        
        for(String s:ln){
            System.out.println(":"+s);
            GraphParser parser = new GraphParser();
            final var tokenizer = parser.new Tokenizer(GraphParser.M(
                   TokenType.ACTION,"[\\]]+",
                   TokenType.NODE,"[^->\\[\\]]+"));
            tokenizer.initialize();
        }
        
    }
    
    /**
     * 程序执行入口
     * @param args
     */
    public static void main(String args[]) {
        final var database = IJdbcApp.newNspebDBInstance("neo4j.sql", Database.class);
        final var proxy = database.getProxy();// 提取代理对象
        final var spp = proxy.findOne(SqlPatternPreprocessor.class);
        final var line = spp.handle(null, null, "#createLine", null);
        parseLine(line);
        System.out.println("\n");
        
        final var nodes = new LinkedList<KVPair<String,Object>>();
        final var ai = new AtomicInteger(1);
        final var lines = cph(series,"1:4","1:3","1:3","1:2","1:5","1:4","1:2")// 线段节点定义
        .stream().map(e->e.reverse())// 字段列表倒序
        .flatMap(e->{
            final var path = new StringBuffer();// 长边
            final var pp = new LinkedList<String>();// 二元边集合
            
            // 路径拆解成二元关系结构。
            e.sliding(2).forEach(kvs->{
                final var p0 = kvs.get(0);// 第一个节点
                final var p1 = kvs.size()>1?kvs.get(1):null;// 第二个节点
                if(p1==null)return;// 不满足窗口大小的直接给予取消。
                
                final var n0 = MFT("(n{0}{1})", p0._1(), p0._2());// 起点
                final var n1 = MFT("(n{0}{1})", p1._1(), p1._2());// 终点
                final var from = MFT("{0}{1}", p0._1(), p0._2());// 开始标记
                final var to = MFT("{0}{1}", p1._1(), p1._2());// 结束标记
                final var name = MFT("{0,number,#}", ai.getAndIncrement(), n0);
                final var pattern = "{0}-[e{1}:Edge '{'name:\"e{1}\",from:\"{2}\",to:\"{3}\"}]->{4}";
                final Function<Boolean, String> span_renderer = (b) -> MFT(pattern, b ? n0 : "", name, from, to, n1);// 线段
        
                path.append(span_renderer.apply(path.length()==0));
                pp.add(span_renderer.apply(true));// 二元边
            });// forEach
            
            //System.out.println(path);
            nodes.addAll(e.kvs());// 记录节点
            var ret = Arrays.asList(path.toString());// 长路径
            ret = pp;// 段路径
            
            return ret.stream();
        });
        
        final Function<KVPair<String,Object>,String> nod_renderer = f->MFT("(n{0}{1}:Node '{'name:\"{2}\"})",
            f.key(),f.value(),MFT("n{0}{1}",f.key(),f.value()));// 节点绘制器
        
        // 边绘制工具
        final var edges_cql = MFT("{0}",IJdbcApp.distinct(LIST(lines),e->{
            final var matcher = Pattern.compile("(from|to)\\s*:\\s*\"([^\"]+)\"").matcher(e);// 提取属性组合
            matcher.find();
            final var from = matcher.group(2);matcher.find();
            final var to = matcher.group(2);
            final var id = MFT("{0}->{1}",from,to);
            //System.out.println(id);
            return id;
        }).collect(Collectors.joining(",\n")));
        
        //节点绘制
        final var nodes_cql = MFT("{0}",IJdbcApp.distinct(nodes,e->e.key()+"_"+e.value())
            .map(nod_renderer)
            .collect(Collectors.joining(",\n")));
        
        final var clear_cql = "match (a)-[e:Edge]->(b) delete a,e,b";
        final var all_cql = "match (a)-[e:Edge]->(b) return a,e,b";
        
        // 脚本打印
        System.out.println("\nCREATE "+Arrays.asList(nodes_cql,edges_cql,clear_cql,all_cql)
            .stream().collect(Collectors.joining(",\n")));
        
        var t = A2REC("1,2,3,4,5,6".split(","));
        var m = t.sliding(2).stream().map(kvs->{
            if(kvs.size()<2)return null;
            final var v0 = kvs.get(0);
            final var v1 = kvs.get(1);
            return MFT("({0})-[e{0}{1}:Edge]->({1})",v0.value(),v1.value()) ;
        }).filter(e->e!=null);
        m.forEach(System.out::println);
        //
        System.out.println(t);
        
    }

}
