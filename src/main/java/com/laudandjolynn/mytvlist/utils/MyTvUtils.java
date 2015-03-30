package com.laudandjolynn.mytvlist.utils;

import java.io.File;
import java.io.IOException;

import com.laudandjolynn.mytvlist.exception.MyTvListException;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月30日 上午9:34:41
 * @copyright: www.laudandjolynn.com
 */
public class MyTvUtils {
	/**
	 * 输出抓取数据到文件
	 * 
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @param data
	 *            数据
	 */
	public static void outputCrawlData(String date, String data) {
		String crawlFileDir = Constant.CRAWL_FILE_PATH + DateUtils.today()
				+ File.separator;
		File file = new File(crawlFileDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		String crawlFilePath = crawlFileDir + date + Constant.UNDERLINE
				+ System.nanoTime();
		try {
			FileUtils.writeWithNIO(data, FileUtils.DEFAULT_CHARSET_NAME,
					crawlFilePath);
		} catch (IOException e) {
			throw new MyTvListException(
					"error occur while write crawled data to disk. filepaht ["
							+ crawlFilePath + "].", e);
		}
	}

}
