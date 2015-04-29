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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.exception.MyTvException;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月30日 上午9:34:41
 * @copyright: www.laudandjolynn.com
 */
public class MyTvUtils {
	private final static Logger logger = LoggerFactory
			.getLogger(MyTvUtils.class);

	/**
	 * 读入html文件
	 * 
	 * @param path
	 * @return
	 */
	public static String readAsHtml(String path) throws IOException {
		return new String(FileUtils.readWithNIO(path, "UTF-8"), "UTF-8");
	}

	/**
	 * 输出抓取数据到文件
	 * 
	 * @param dirName
	 *            目录名称，如日期，yyyy-MM-dd
	 * @param data
	 *            数据
	 * @param fileName
	 *            文件名
	 */
	public static void outputCrawlData(String dirName, String data,
			String fileName) {
		String crawlFileDir = Constant.CRAWL_FILE_PATH + dirName
				+ File.separator;
		File file = new File(crawlFileDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		String crawlFilePath = crawlFileDir + fileName;
		// 若文件已存在，则删除
		file = new File(crawlFilePath);
		if (file.exists()) {
			file.delete();
		}
		try {
			logger.info("write data to file: " + crawlFilePath);
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

	public static boolean checkInvalidString(String string) {
		if (string.indexOf("'") != -1) {
			return false;
		}
		return true;
	}
	
}
