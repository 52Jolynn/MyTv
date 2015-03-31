package com.laudandjolynn.mytv.utils;

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
	public final static Web WEB_CONFIG = new Web();
	private final static String CONFIG_WEB_IP = "web_ip";
	private final static String CONFIG_WEB_PORT = "web_port";

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
		if (bundle.containsKey(CONFIG_WEB_IP)) {
			WEB_CONFIG.ip = bundle.getString(CONFIG_WEB_IP);
		}
		if (bundle.containsKey(CONFIG_WEB_PORT)) {
			WEB_CONFIG.port = Integer
					.valueOf(bundle.getString(CONFIG_WEB_PORT));
		}
	}

	public final static class Web {
		private String ip = "127.0.0.1";
		private int port = 8080;

		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}
	}
}
