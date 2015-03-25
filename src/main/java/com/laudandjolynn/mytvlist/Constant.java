package com.laudandjolynn.mytvlist;

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
	public final static String PROGRAM_TABLE_FILE_DIR = "program_tables";
	public final static String PROGRAM_TABLE_FILE_NAME = "program_table";
	public final static String CLASS_PATH = Constant.class.getResource("/")
			.getPath();
	public final static String PROGRAM_TABLE_FILE_PATH = CLASS_PATH
			+ PROGRAM_TABLE_FILE_DIR;
	public final static String MY_TV_LIST_FILE_NAME = CLASS_PATH
			+ "mytvlist.dat";
	public final static String SQL_FILE = "db";
	public final static String APP_NAME = "mytvlist";
}
