package com.laudandjolynn.mytvlist.utils;

import java.io.File;
import java.util.ResourceBundle;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午5:00:00
 * @copyright: www.laudandjolynn.com
 */
public class Config {
	private final static String CRAWL_FILE_PATH_NAME = "crawl_file_path";
	private final static String CONFIG_FILE_NAME = "config";

	static {
		ResourceBundle bundle = ResourceBundle.getBundle(CONFIG_FILE_NAME);
		if (bundle.containsKey(CRAWL_FILE_PATH_NAME)) {
			String value = bundle.getString(CRAWL_FILE_PATH_NAME);
			if (!Constant.DOT.equals(value)) {
				if (!value.endsWith(File.separator)) {
					value += File.separator;
				}
				Constant.CRAWL_FILE_PATH = value + Constant.CRAWL_FILE_DIR;
				File file = new File(Constant.CRAWL_FILE_PATH);
				if (!file.exists()) {
					file.mkdirs();
				}
			}
		}
	}
}
