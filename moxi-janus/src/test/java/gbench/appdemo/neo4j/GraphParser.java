package gbench.appdemo.neo4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.Tuple2.*;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 
 * @author gbench
 *
 */
public class GraphParser {
    
    //分词器
    public class Tokenizer{
        /**
         * 构造函数
         * @param patterns 关键词式样
         */
        public Tokenizer(Map<TokenType,Object> patterns) {
            this.patterns = new HashMap<>();
            patterns.forEach((type,pattern)->{
                if(pattern instanceof String) {
                    final var p = (String) pattern;
                    patterns.put(type,p);
                }else if(pattern instanceof Pattern) {
                    final var p = (Pattern) pattern;
                    patterns.put(type,p);
                }else {
                    // do nothing;
                }//if
            });//forEach
        }// Tokenizer
        
        /**
         * 
         * @param line
         * @return
         */
       public Map<TokenType,Token> getToken(final String line){
           Map<TokenType,Token> map = new HashMap<TokenType,Token>();
           patterns.forEach((type,pattern)->{
               final var matcher = pattern.matcher(line);
               if(matcher.find()) {
                   final var text = matcher.group();
                   final var s = matcher.start();
                   final var e = matcher.end();
                   final var token = new Token(text,type,TUP2(s,e));
                   map.compute(type, (k,v)->token);
               }//while
           });
           return map;
        }
        
        /**
         *  初始化
         */
        public void initialize() {
            String line = source;
            long pre = -1;
            
            while(tokens.size()-pre>0) {// 
                final var tks = this.getToken(line);
                final var holder = new AtomicReference<Token>(null);
                tks.forEach((type,token)->{
                    holder.set(token);
                });
                final var token = holder.get();
                if(token==null)break;
                tokens.compute(token.getType(),(k,v)->{
                   if(v==null)v=new LinkedList<>();
                   v.add(token);
                   return v;
                });
                pre = tokens.size();
                line = line.substring(token.location._2());
            }
            
            /**
             * 
             */
            tokens.forEach((type,token)->{
                System.out.println(token);
            });
            
        }
       
        private Map<TokenType,Pattern> patterns = null;
        private Map<TokenType,List<Token>> tokens = new HashMap<>();
    }
    
    /**
     * 
     * @author gbench
     *
     */
    @Data @AllArgsConstructor
    public static class Token{
        private String text;
        private TokenType type;
        private Tuple2<Integer,Integer> location;
    }
    
    /**
     * 快速构造一个Map&ltObject,Object&gt的对象:快送构造Map的方法
     * @param oo key1,value1,key2,value2的序列
     * @return Map&ltObject,Object&gt的对象
     */
    public static Map<TokenType,Object> M(Object ...oo){
        final var map = new LinkedHashMap<TokenType,Object>();
        if(oo!=null&&oo.length>0) {
            for(int i=0;i+1<oo.length;i+=2) {
                map.put((TokenType)oo[i],oo[i+1]);
            }//for
        }//if
        return map;
    }
    
    public enum TokenType{NODE,ACTION};
    private String source;// 文本

}
