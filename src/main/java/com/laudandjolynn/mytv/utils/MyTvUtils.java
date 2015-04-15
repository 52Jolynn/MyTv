/*******************************************************************************
 * Copyright 2015 htd0324@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.laudandjolynn.mytv.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import com.laudandjolynn.mytv.exception.MyTvException;

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
	 * @param tag
	 *            文件名标识
	 */
	public static void outputCrawlData(String date, String data, String tag) {
		String crawlFileDir = Constant.CRAWL_FILE_PATH + date + File.separator;
		File file = new File(crawlFileDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		String crawlFilePath = crawlFileDir + date + Constant.UNDERLINE
				+ (tag == null ? System.nanoTime() : tag);
		try {
			FileUtils.writeWithNIO(data, FileUtils.DEFAULT_CHARSET_NAME,
					crawlFilePath);
		} catch (IOException e) {
			throw new MyTvException(
					"error occur while write crawled data to disk. filepaht ["
							+ crawlFilePath + "].", e);
		}
	}

	/**
	 * 获取运行路径
	 * 
	 * @return
	 */
	public static String getRunningPath(Class<?> clazz) {
		URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = URLDecoder.decode(url.getPath(), "utf-8");// 转化为utf-8编码
		} catch (Exception e) {
			throw new MyTvException("invalid file path: " + url.getPath(), e);
		}
		if (filePath.endsWith(".jar")) {// 可执行jar包运行的结果里包含".jar"
			// 截取路径中的jar包名
			filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		}

		File file = new File(filePath);

		// /If this abstract pathname is already absolute, then the pathname
		// string is simply returned as if by the getPath method. If this
		// abstract pathname is the empty abstract pathname then the pathname
		// string of the current user directory, which is named by the system
		// property user.dir, is returned.
		filePath = file.getAbsolutePath();// 得到windows下的正确路径
		if (!filePath.endsWith(File.separator)) {
			filePath += File.separator;
		}

		return filePath;
	}

	public static boolean checkStationName(String stationName) {
		if (stationName.indexOf("'") != -1) {
			return false;
		}
		return true;
	}
}
