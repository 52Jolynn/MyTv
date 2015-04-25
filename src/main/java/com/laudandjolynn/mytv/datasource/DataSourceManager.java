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
package com.laudandjolynn.mytv.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;

import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.utils.Config;
import com.laudandjolynn.mytv.utils.Config.DbType;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月14日 上午10:15:11
 * @copyright: www.laudandjolynn.com
 */
@SuppressWarnings("unchecked")
public class DataSourceManager {
	private static Class<TvDataSource> dsClass = null;
	public final static String RES_KEY_DS_CLASS_NAME = "ds_class_name";
	public final static String RES_KEY_DB_DRIVER_CLASS = "db_driver_class";
	public final static String RES_KEY_DB_URL = "db_url";
	public final static String RES_KEY_DB_USER_NAME = "db_username";
	public final static String RES_KEY_DB_PASSWORD = "db_password";
	public final static String RES_KEY_DB_FILE_NAME = "db_file_name";
	public final static String RES_KEY_DB_SQL_LIST = "db_sql_stmt";
	// 数据源配置文件前缀，如db_sqlite、db_mysql
	private final static String DB_PROPERTY_FILE_PREFIX = "db_";
	// 配置文件中的sql语句前缀
	private final static String DB_PROPERTY_FILE_SQL_PREFIX = "sql_";
	// DBCP连接池配置
	private final static String DBCP_FILE_NAME = "dbcp.properties";
	private final static String DBCP_URL = "url";
	private final static String DBCP_USER_NAME = "username";
	private final static String DBCP_PASSWORD = "password";
	private final static String DBCP_DRIVER_CLASS_NAME = "driverClassName";

	public final static Properties DATA_SOURCE_PROP = new Properties();
	private final static Properties DBCP_PROP = new Properties();
	private final static Pattern PATTERN_DB_PROPERTY_FILE_SQL_SUFFIX = Pattern
			.compile("_(\\d+)$");

	static {
		// dbcp
		InputStream is = DataSourceManager.class.getResourceAsStream("/"
				+ DBCP_FILE_NAME);
		try {
			DBCP_PROP.load(is);
		} catch (IOException e) {
			throw new MyTvException("error occur while load properties file: "
					+ DBCP_FILE_NAME, e);
		}

		// 非连接池
		ResourceBundle bundle = ResourceBundle
				.getBundle(DB_PROPERTY_FILE_PREFIX
						+ Config.getDbType().getValue());
		if (bundle.containsKey(RES_KEY_DS_CLASS_NAME)) {
			String dsClassName = bundle.getString(RES_KEY_DS_CLASS_NAME);
			try {
				dsClass = (Class<TvDataSource>) Class.forName(dsClassName);
			} catch (ClassNotFoundException e) {
				throw new MyTvException(dsClassName + " isn't found.", e);
			}
			DATA_SOURCE_PROP.setProperty(RES_KEY_DS_CLASS_NAME, dsClassName);
		}
		if (bundle.containsKey(RES_KEY_DB_DRIVER_CLASS)) {
			String dbDriverClass = bundle.getString(RES_KEY_DB_DRIVER_CLASS);
			try {
				Class.forName(dbDriverClass);
			} catch (ClassNotFoundException e) {
				throw new MyTvException(dbDriverClass + " isn't found.", e);
			}
			DATA_SOURCE_PROP
					.setProperty(RES_KEY_DB_DRIVER_CLASS, dbDriverClass);
			DBCP_PROP.setProperty(DBCP_DRIVER_CLASS_NAME, dbDriverClass);
		}
		if (bundle.containsKey(RES_KEY_DB_URL)) {
			String dbUrl = bundle.getString(RES_KEY_DB_URL);
			DATA_SOURCE_PROP.setProperty(RES_KEY_DB_URL, dbUrl);
			DBCP_PROP.setProperty(DBCP_URL, dbUrl);
		}
		if (bundle.containsKey(RES_KEY_DB_USER_NAME)) {
			String dbUserName = bundle.getString(RES_KEY_DB_USER_NAME);
			DATA_SOURCE_PROP.setProperty(RES_KEY_DB_USER_NAME, dbUserName);
			DBCP_PROP.setProperty(DBCP_USER_NAME, dbUserName);
		}
		if (bundle.containsKey(RES_KEY_DB_PASSWORD)) {
			String dbPassword = bundle.getString(RES_KEY_DB_PASSWORD);
			DATA_SOURCE_PROP.setProperty(RES_KEY_DB_PASSWORD, dbPassword);
			DBCP_PROP.setProperty(DBCP_PASSWORD, dbPassword);
		}
		if (bundle.containsKey(RES_KEY_DB_FILE_NAME)) {
			String dbFileName = bundle.getString(RES_KEY_DB_FILE_NAME);
			DATA_SOURCE_PROP.setProperty(RES_KEY_DB_FILE_NAME, dbFileName);
		}
		List<String> keyList = new ArrayList<String>(bundle.keySet());
		Collections.sort(keyList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int s1 = 0;
				int s2 = 0;
				Matcher matcher = PATTERN_DB_PROPERTY_FILE_SQL_SUFFIX
						.matcher(o1);
				if (matcher.find()) {
					s1 = Integer.valueOf(matcher.group(1));
				}
				matcher = PATTERN_DB_PROPERTY_FILE_SQL_SUFFIX.matcher(o2);
				if (matcher.find()) {
					s2 = Integer.valueOf(matcher.group(1));
				}
				return s1 - s2;
			}
		});
		List<String> sqlList = new ArrayList<String>();
		for (String key : keyList) {
			if (key.startsWith(DB_PROPERTY_FILE_SQL_PREFIX)) {
				String value = bundle.getString(key);
				sqlList.add(value);
			}
		}

		DATA_SOURCE_PROP.put(RES_KEY_DB_SQL_LIST, sqlList);

	}

	/**
	 * 获取数据源连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		if (dsClass == null) {
			throw new MyTvException("load db driver class error.");
		}

		try {
			if (Config.getDbType() == DbType.SQLITE) {
				return dsClass.newInstance().getConnection(DATA_SOURCE_PROP);
			}

			DataSource ds = BasicDataSourceFactory.createDataSource(DBCP_PROP);
			Connection conn = ds.getConnection();
			return conn;
		} catch (InstantiationException e) {
			throw new MyTvException("can't instantce db driver: " + dsClass, e);
		} catch (IllegalAccessException e) {
			throw new MyTvException(e);
		} catch (Exception e) {
			throw new MyTvException(e);
		}
	}
}
