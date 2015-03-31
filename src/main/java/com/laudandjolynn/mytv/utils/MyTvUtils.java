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
			throw new MyTvException(
					"error occur while write crawled data to disk. filepaht ["
							+ crawlFilePath + "].", e);
		}
	}

}
