package com.laudandjolynn.mytv.utils;

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
	// 数据文件
	public final static String MY_TV_DATA_FILE_PATH = MY_TV_DATA_PATH
			+ "mytv.dat";
	// 电视节目表文件路径
	public final static String CRAWL_FILE_DIR = "crawlfiles";
	public static String CRAWL_FILE_PATH = Constant.MY_TV_DATA_PATH
			+ CRAWL_FILE_DIR + File.separator;
	// 数据库文件名
	public final static String SQL_FILE = "db";
	public final static String APP_NAME = "mytv";
	public final static String PROGRAM_TABLE_DATES = "dates";
	public final static String PROGRAM_TABLE_DATE = "date";
	public final static String DOT = ".";
	public final static String UNDERLINE = "_";

	static {
		File file = new File(MY_TV_DATA_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(CRAWL_FILE_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
}
