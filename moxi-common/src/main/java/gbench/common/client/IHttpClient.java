package gbench.common.client;

import gbench.common.tree.LittleTree.IRecord;

public interface  IHttpClient{
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V sync(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V get(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V syncGet(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V post(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V syncPost(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V delete(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V syncDelete(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V put(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V syncPut(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V  patch(IRecord param) ;
	
	/**
	 * 同步请求
	 *  控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V syncPatch(IRecord param) ;
	
	/**
	 * 异步请求,返回类型由用户指定为V,异步模式时候, 为Boolean
	 * 控制操作从参数以$开头:
	 * 	method: get,post,默认为 get
	 * 	encoding: gbk,utf8, 默认为 utf8
	 *     unescape: 是否使用 StringEscapeUtils.unescapeJava
	 * @param param
	 */
	public <V> V ajax(IRecord param);
}
