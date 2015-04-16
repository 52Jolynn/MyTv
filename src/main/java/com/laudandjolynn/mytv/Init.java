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
package com.laudandjolynn.mytv;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.datasource.DataSourceManager;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.service.TvService;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.MemoryCache;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月26日 下午1:33:14
 * @copyright: www.laudandjolynn.com
 */
public class Init {
	private final static Logger logger = LoggerFactory.getLogger(Init.class);
	private final static String TV_STATION_INIT_DATA_FILE_NAME = "tv_station.properties";
	private final static String TV_STATION_ALIAS_INIT_DATA_FILE_NAME = "tv_station_alias.properties";

	private Init() {
	}

	public static Init getIntance() {
		return InitSingletonHolder.INIT;
	}

	private final static class InitSingletonHolder {
		private final static Init INIT = new Init();
	}

	/**
	 * 初始化应用基础数据
	 */
	public void init() {
		// 加载应用数据
		MyTvData.getInstance().loadData();
		// 创建数据库及表结构
		this.initDb();
		// 初始化数据库数据
		this.initDbData();
		this.initDbData2();
		// 启动每天定时任务
		logger.info("create everyday crawl task.");
		createEverydayCron();
	}

	@SuppressWarnings("unchecked")
	private List<String> loadSql() {
		Object object = DataSourceManager.DATA_SOURCE_PROP
				.get(DataSourceManager.RES_KEY_DB_SQL_LIST);
		if (object instanceof List) {
			return (List<String>) object;
		}
		throw new MyTvException("error occur while trying to init db.");
	}

	/**
	 * 初始化数据库
	 */
	private void initDb() {
		if (MyTvData.getInstance().isDbInited()) {
			logger.debug("db have already init.");
			return;
		}

		File myTvDataFilePath = new File(Constant.MY_TV_DATA_FILE_PATH);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DataSourceManager.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			List<String> sqlList = loadSql();
			for (String sql : sqlList) {
				stmt.addBatch(sql);
				logger.info("execute sql: " + sql);
			}
			stmt.executeBatch();
			conn.commit();
			DataSourceManager.DATA_SOURCE_PROP
					.remove(DataSourceManager.RES_KEY_DB_SQL_LIST);
			MyTvData.getInstance().writeData(null, Constant.XML_TAG_DB, "true");
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new MyTvException(
							"error occur while rollback transaction.", e);
				}
			}
			myTvDataFilePath.deleteOnExit();
			throw new MyTvException("error occur while execute sql on db.", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(
							"error occur while close statement.", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(
							"error occur while close sqlite connection.", e);
				}
			}
		}
	}

	/**
	 * 初始化应用数据
	 */
	private void initDbData() {
		TvService tvService = new TvService();
		List<TvStation> stationList = null;
		if (MyTvData.getInstance().isDataInited()) {
			stationList = tvService.getAllStation();
		}
		boolean isStationExists = (stationList == null ? 0 : stationList.size()) > 0;
		String today = DateUtils.today();
		if (isStationExists) {
			MemoryCache.getInstance().addCache(stationList);
		} else {
			// 首次抓取
			stationList = tvService.crawlAllTvStation();
			MemoryCache.getInstance().addCache(stationList);
		}

		if (!MyTvData.getInstance().isProgramTableOfTodayCrawled()) {
			// 保存当天电视节目表
			logger.info("query program table of today. " + "today is " + today);
			tvService.crawlAllProgramTable(today);
			MyTvData.getInstance().writeData(
					Constant.XML_TAG_PROGRAM_TABLE_DATES,
					Constant.XML_TAG_PROGRAM_TABLE_DATE, today);
		}
	}

	/**
	 * 初始化数据库数据
	 */
	private void initDbData2() {
		Properties tvStationProp = new Properties();
		try {
			tvStationProp.load(Init.class.getResourceAsStream("/"
					+ TV_STATION_INIT_DATA_FILE_NAME));
		} catch (IOException e) {
			throw new MyTvException("error occur while load property file: "
					+ TV_STATION_INIT_DATA_FILE_NAME, e);
		}

		Collection<Object> values = tvStationProp.values();
		List<String> insertSqlList = new ArrayList<String>(values.size());
		String insertSql = "insert into tv_station (name,displayName,city,classify,channel,sequence)";
		for (Object value : values) {
			insertSqlList.add(insertSql + " values (" + value.toString() + ")");
		}

		Properties tvStationAliasProp = new Properties();
		try {
			tvStationAliasProp.load(Init.class.getResourceAsStream("/"
					+ TV_STATION_ALIAS_INIT_DATA_FILE_NAME));
		} catch (IOException e) {
			throw new MyTvException("error occur while load property file: "
					+ TV_STATION_ALIAS_INIT_DATA_FILE_NAME, e);
		}
		values = tvStationAliasProp.values();
		insertSql = "insert into tv_station_alias (stationName,alias)";
		for (Object value : values) {
			insertSqlList.add(insertSql + " values (" + value.toString() + ")");
		}
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DataSourceManager.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();

			for (String sql : insertSqlList) {
				stmt.addBatch(sql);
				logger.info("execute sql: " + sql);
			}
			stmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new MyTvException(
							"error occur while rollback transaction.", e);
				}
			}
			throw new MyTvException("error occur while execute sql on db.", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(
							"error occur while close statement.", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(
							"error occur while close sqlite connection.", e);
				}
			}
		}
		MyTvData.getInstance().writeData(null, Constant.XML_TAG_DATA, "true");
	}

	/**
	 * 创建每天定时任务
	 */
	private static void createEverydayCron() {
		ScheduledExecutorService scheduled = new ScheduledThreadPoolExecutor(1);
		final String cronDate = DateUtils.tommorow();
		long initDelay = (DateUtils.string2Date(cronDate + " 00:00:00")
				.getTime() - new Date().getTime()) / 1000;
		logger.info("cron crawler task will be automatic start after "
				+ initDelay + " seconds.");
		scheduled.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				TvService epgService = new TvService();
				epgService.crawlAllProgramTable(cronDate);
				MyTvData.getInstance().writeData(
						Constant.XML_TAG_PROGRAM_TABLE_DATES,
						Constant.XML_TAG_PROGRAM_TABLE_DATE, cronDate);
			}
		}, initDelay, 86400, TimeUnit.SECONDS);
	}

}
