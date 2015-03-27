package com.laudandjolynn.mytvlist.utils;

import java.io.File;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:14:59
 * @copyright: www.laudandjolynn.com
 */
public class Constant {
	public final static String CHROME_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36";
	public final static String FIREFOX_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0";
	public final static String IE_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";
	public final static String EPG_URL = "http://tv.cntv.cn/epg";
	// 数据文件目录
	public final static String MY_TV_DATA_PATH = Constant.class
			.getResource("/").getPath() + "data" + File.separator;
	public final static String MY_TV_LIST_FILE_NAME = MY_TV_DATA_PATH
			+ "mytvlist.dat";
	// 电视节目表目录
	public final static String CRAWL_FILE_NAME = "crawlfile";
	public static String CRAWL_FILE_PATH = Constant.MY_TV_DATA_PATH
			+ CRAWL_FILE_NAME;
	// 数据库文件名
	public final static String SQL_FILE = "db";
	public final static String APP_NAME = "mytvlist";
	public final static String DOT = ".";
	public final static String UNDERLINE = "_";

	static {
		File file = new File(MY_TV_DATA_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
}
