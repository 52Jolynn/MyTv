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
import java.util.ResourceBundle;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午5:00:00
 * @copyright: www.laudandjolynn.com
 */
public class Config {
	private final static String RES_KEY_DATA_FILE_PATH = "data_file_path";
	private final static String CONFIG_FILE_NAME = "config";
	public final static NetConfig NET_CONFIG = new NetConfig();
	private final static String RES_KEY_CONFIG_IP = "ip";
	private final static String RES_KEY_CONFIG_HESSIAN_PORT = "hessian_port";
	private final static String RES_KEY_CONFIG_RMI_PORT = "rmi_port";
	private final static String RES_KEY_CONFIG_DB_TYPE = "db_type";

	private static DbType dbType = DbType.SQLITE;
	private static String dataFilePath = MyTvUtils.getRunningPath(Config.class);

	static {
		ResourceBundle bundle = ResourceBundle.getBundle(CONFIG_FILE_NAME);
		// 读取抓取的文件存放路径
		if (bundle.containsKey(RES_KEY_DATA_FILE_PATH)) {
			String value = bundle.getString(RES_KEY_DATA_FILE_PATH);
			if (!Constant.DOT.equals(value)) {
				if (!value.endsWith(File.separator)) {
					value += File.separator;
				}
				dataFilePath = value;
			}
		}
		if (bundle.containsKey(RES_KEY_CONFIG_IP)) {
			NET_CONFIG.ip = bundle.getString(RES_KEY_CONFIG_IP);
		}
		if (bundle.containsKey(RES_KEY_CONFIG_HESSIAN_PORT)) {
			NET_CONFIG.hessianPort = Integer.valueOf(bundle
					.getString(RES_KEY_CONFIG_HESSIAN_PORT));
		}
		if (bundle.containsKey(RES_KEY_CONFIG_RMI_PORT)) {
			NET_CONFIG.rmiPort = Integer.valueOf(bundle
					.getString(RES_KEY_CONFIG_RMI_PORT));
		}
		if (bundle.containsKey(RES_KEY_CONFIG_DB_TYPE)) {
			dbType = DbType.valueOf(bundle.getString(RES_KEY_CONFIG_DB_TYPE)
					.toUpperCase());
		}
	}

	public static String getDataFilePath() {
		return dataFilePath;
	}

	public final static class NetConfig {
		private String ip = "127.0.0.1";
		private int hessianPort = 33117;
		private int rmiPort = 33118;

		public String getIp() {
			return ip;
		}

		public int getHessianPort() {
			return hessianPort;
		}

		public int getRmiPort() {
			return rmiPort;
		}
	}

	public static DbType getDbType() {
		return dbType;
	}

	public enum DbType {
		SQLITE("sqlite"), MYSQL("mysql"), POSTGRESQL("postgresql"), DB2("db2"), ORACLE(
				"oracle"), SQL_SERVER("sqlserver"), SYBASE("sybase");

		private String value;

		private DbType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
