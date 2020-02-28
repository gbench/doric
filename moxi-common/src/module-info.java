/**
 * gbench 的共同模块
 */
module gbench.common{
	requires com.fasterxml.jackson.core;
	requires org.apache.httpcomponents;
	requires org.apache.poi;
	exports gbennch.commmon.tree.LittleTree;
	exports gbennch.commmon.chinese;
	exports gbennch.commmon.fs;
	exports gbennch.commmon.fs.FileSystem;
	exports gbennch.commmon.fs.XlsFile;
	exports gbennch.commmon.client.AbstractHttpClient;
	exports gbennch.commmon.client.IHttpClient;
	exports gbennch.commmon.client.ServiceSite;
	exports gbennch.commmon.client.SimpleClient;
	exports gbench.common.tree.LittleTree.IRecord;
	exports gbench.common.tree.LittleTree.CronTime;
	exports gbench.common.tree.LittleTree.Jdbc;
}