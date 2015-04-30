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

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:14:59
 * @copyright: www.laudandjolynn.com
 */
public class Constant {
	// 应用名称
	public final static String APP_NAME = "mytv";
	// CPU核数
	public static final int CPU_PROCESSOR_NUM = Runtime.getRuntime()
			.availableProcessors();
	// 数据文件目录
	public final static String MY_TV_DATA_PATH = Config.getDataFilePath()
			+ "data" + File.separator;
	// 数据文件
	public final static String MY_TV_DATA_FILE_PATH = MY_TV_DATA_PATH
			+ "mytv.dat";
	// 电视节目表文件路径
	public final static String CRAWL_FILE_PATH = Constant.MY_TV_DATA_PATH
			+ "crawlfiles" + File.separator;

	public final static String XML_TAG_DB = "db";
	public final static String XML_TAG_DATA = "data";
	public final static String XML_TAG_STATION = "station";
	public final static String XML_TAG_PROGRAM = "program";

	public final static String TV_STATION_INIT_DATA_FILE_NAME = "tv_station.properties";
	public final static String TV_STATION_ALIAS_INIT_DATA_FILE_NAME = "tv_station_alias.properties";

	public final static String DOT = ".";
	public final static String COMMA = ",";
	public final static String UNDERLINE = "_";
	public final static String COLON = ":";

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
