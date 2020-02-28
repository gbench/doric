package gbench.commonApp.jdbc;

import static gbench.common.fs.FileSystem.extensionpicker;
import static gbench.common.fs.FileSystem.lines;
import static gbench.common.fs.FileSystem.pathname2stream;
import static gbench.common.tree.LittleTree.Jdbc.namedsql_processor;
import static gbench.common.tree.LittleTree.Jdbc.namedsql_processor_escape_brace;
import static gbench.common.tree.LittleTree.Jdbc.parse2namedsqls;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import gbench.common.fs.XlsFile.SimpleExcel;
import gbench.common.fs.XlsFile.StrMatrix;
import gbench.common.tree.LittleTree.IRecord;
import gbench.common.tree.LittleTree.Jdbc;
import gbench.common.tree.LittleTree.Jdbc.SqlPatternPreprocessor;

/**
 * JdbcApp的演示 demo
 * @author gbench
 *
 */
public interface IJdbcApp {
    
    /**
     *  获取AppName
     * @return 获取AppName
     */
    public default String getAppName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * NamedSql_Processor Preprocessor (NSPP)
     * SqlPatternPreprocessor 的处理类
     * @param sqlfile  sql 文件的名称,可以为null,但会得到一个不做任何语句处理的spp:SqlPatternPrePreocessor
     * @param clazz sqlfile 相对基路径
     * @return SqlPatternPreprocessor
     */
    public static SqlPatternPreprocessor nspp(String sqlfile,Class<?> clazz) {
        return  namedsql_processor(namedsqls(sqlfile, clazz));
    }
    
    /**
     * Named Sql Preprocessor Escaped Brace (NSPEB)
     * SqlPatternPreprocessor 的处理类   对 ‘{’进行转义，以以提供Neo4j类型的数据库处理。
     * @param sqlfile  sql 文件的名称，可以为null,但会得到一个不做任何语句处理的spp:SqlPatternPrePreocessor
     * @param clazz sqlfile 相对基路径
     * @return SqlPatternPreprocessor
     */
    public static SqlPatternPreprocessor nspeb(String sqlfile,Class<?> clazz) {
        return  namedsql_processor_escape_brace(namedsqls(sqlfile, clazz));
    }
    
    /**
     * 创建数据库接口实例
     * 注意：当sql文件不存在的啥时候，返回一个不含有任何匀速的 空Map，即new HashMap<String,String>() 的 spp.
     * @param <T> 数据库类型
     * @param sqlfile sql语句脚本。 可以为null,但会得到一个不做任何语句处理的spp:SqlPatternPrePreocessor
     * @param dbClazz 数据库类型
     * @return 数据库接口实例
     */
    public static <T> T newNsppDBInstance(String sqlfile,Class<T> dbClazz) {
        return newDBInstance(()->nspp(sqlfile,dbClazz),dbClazz);
    }
    
    /**
     * 创建数据库接口实例
     * 注意：当sql文件不存在的啥时候，返回一个不含有任何匀速的 空Map，即new HashMap<String,String>() 的 spp.
     * @param <T> 数据库类型
     * @param sqlfile sql语句脚本。 可以为null,但会得到一个不做任何语句处理的spp:SqlPatternPrePreocessor
     * @param dbClazz 数据库类型
     * @param sqlFileRelativeClass sqlfile 相对基路径
     * @return 数据库接口实例
     */
    public static <T> T newNsppDBInstance(String sqlfile,Class<T> dbClazz,Class<?>sqlFileRelativeClass) {
        return newDBInstance(()->nspp(sqlfile,sqlFileRelativeClass),dbClazz);
    }
    
    /**
     * NamedSqlpreProcessor Escaped Brace
     * 创建数据库接口实例
     * @param <T> 数据库类型
     * @param sqlfile sql语句脚本,可以为null,但会得到一个不做任何语句处理的spp:SqlPatternPrePreocessor
     * @param dbClazz 数据库类型
     * @return 数据库接口实例
     */
    public static <T> T newNspebDBInstance(String sqlfile,Class<T> dbClazz) {
        return newDBInstance(()->nspeb(sqlfile,dbClazz),dbClazz);
    }
    
    /**
     * NamedSqlpreProcessor Escaped Brace
     * 创建数据库接口实例
     * @param <T> 数据库类型
     * @param sqlfile sql语句脚本,可以为null,但会得到一个不做任何语句处理的spp:SqlPatternPrePreocessor
     * @param dbClazz 数据库类型
     * @param sqlFileRelativeClass sqlfile 相对基路径
     * @return 数据库接口实例
     */
    public static <T> T newNspebDBInstance(String sqlfile,Class<T> dbClazz,Class<?>sqlFileRelativeClass) {
        return newDBInstance(()->nspeb(sqlfile,sqlFileRelativeClass),dbClazz);
    }
    
    /**
     * 创建数据库接口实例
     * @param <T> 数据库类型
     * @param spp_supplier SqlPatternPreprocessor Supplier
     * @param dbClazz 数据库类型
     * @return 数据库接口实例
     */
    public static <T> T newDBInstance(Supplier<SqlPatternPreprocessor> spp_supplier,Class<T> dbClazz) {
        return Jdbc.newInstance(dbClazz,spp_supplier.get());
    }
    
    /**
     * 相对于clazz的存在位置的sqlfile的namedsql
     * @param sqlfile sql 文件的名称，当sql文件不存在的啥时候，返回一个不含有任何匀速的 空Map，即new HashMap<String,String>() 
     * @param clazz sqlfile 相对基路径
     * @return {name->sql},当sql文件不存在的啥时候，返回一个不含有任何匀速的 空Map，即new HashMap<String,String>() 
     */
    public static Map<String,String> namedsqls(String sqlfile,Class<?> clazz){
        if(sqlfile==null)return new HashMap<String,String>();// 返回一个空的namedsqls
        final var lines = lines(pathname2stream(sqlfile,clazz),"utf8");
        if(lines==null) return new HashMap<String,String>();// 返回一个空的namedsqls
        final var namedsqls = parse2namedsqls(lines);
        return namedsqls;
    }
    
    /**
     * 加载EXCEL文件
     * @param excelFile excel文件名称
     * @param relativeClass excelFile的相对位置：以class的文件为基准
     * @return SimpleExcel 对象
     */
    public static SimpleExcel loadExcel(final String excelFile,final Class<?>relativeClass) {
        final var excel = new SimpleExcel();// EXCEL 文件的对象
        final var pathname = excelFile;// chart of account 的定义
        excel.loadWithExtension(pathname2stream(pathname,relativeClass),// 文件流
            extensionpicker(pathname));// 文件的扩展名
        return excel;
    }
    
    /**
     * 自动检测excel中的数据区域内容
     * @param excelFile excel文件名称
     * @param sheetName sheet名称
     * @param relativeClass excelFile的相对位置：以class的文件为基准
     * @return StrMatrix 对象
     */
    public static StrMatrix xlsAutoDetect(final String excelFile,String sheetName,final Class<?>relativeClass) {
        final var excel = loadExcel(excelFile,relativeClass);
        final var strmx = excel.autoDetect(sheetName);
        excel.close();
        return strmx;
    }
    
    /**
     * 自动检测excel中的数据区域内容
     * @param excelFile excel文件名称
     * @param sheetName sheet名称
     * @param relativeClass excelFile的相对位置：以class的文件为基准
     * @return StrMatrix 对象
     */
    public static StrMatrix xlsAutoDetect(final String excelFile,int shtid,final Class<?>relativeClass) {
        final var excel = loadExcel(excelFile,relativeClass);
        final var strmx = excel.autoDetect(shtid);
        excel.close();
        return strmx;
    }
    
    /**
     * 自动检测excel中的数据区域内容
     * @param sheetAddress path!sheetName,当没有sheetName默认为第一号sheet 例如：clinics.xlsx!cytokines
     * @param relativeClass excelFile的相对位置：以class的文件为基准
     * @return StrMatrix 对象
     */
    public static StrMatrix xlsAutoDetect(final String sheetAddress,final Class<?>relativeClass) {
        final var ss = sheetAddress.split("[!]+");// 分解出地址内容:[0] 文件路径,[1]:sheetName
        final var excelFile = ss[0];
        final var excel = loadExcel(excelFile,relativeClass);
        final StrMatrix strmx = ss.length>1
            ?   excel.autoDetect(ss[1])
            :   excel.autoDetect(0);
        excel.close();
        return strmx;
    }
    
    /**
     * 自动检测excel中的数据区域内容:并把结果转换 成IRecord类型的 Stream<IRecord>
     * @param sheetAddress path!sheetName,当没有sheetName默认为第一号sheet 例如：clinics.xlsx!cytokines
     * @param relativeClass excelFile的相对位置：以class的文件为基准
     * @return IRecord类型的Stream<IRecord> 对象
     */
    public static Stream<IRecord> sht2recs(final String sheetAddress,final Class<?>relativeClass) {
        return xlsAutoDetect(sheetAddress,relativeClass).rowStream(IRecord::REC);
    }
    
    /**
     * 去除重复数据
     * @param <T> 元素类型
     * @param <U> ID类型
     * @param cc 数据源
     * @param t2id 唯一值 id
     * @return 唯一值的数据源流
     */
    public static <T,U> Stream<T> distinct(Collection<T>cc,Function<T,U>t2id){
        @SuppressWarnings("unchecked")
        final var collector = Collector.of(()->new AtomicReference<T>(null),
            (atom,a)->atom.set((T)a),
            (aa,bb)->aa.get()==null?bb:aa);
        final var mm = cc.stream().collect(Collectors.groupingBy(t2id,collector));
        return mm.values().stream().map(e->e.get());
     };
}
