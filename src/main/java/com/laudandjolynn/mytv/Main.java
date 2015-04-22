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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.crawler.CrawlerTaskManager;
import com.laudandjolynn.mytv.datasource.DataSourceManager;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.service.TvService;
import com.laudandjolynn.mytv.service.TvServiceImpl;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.MemoryCache;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:12:56
 * @copyright: www.laudandjolynn.com
 */
public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		// 初始化应用状态、启动内部任务
		startService();
		com.laudandjolynn.mytv.Server hessian = new HessianServer();
		try {
			hessian.start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new MyTvException(e);
		}
		com.laudandjolynn.mytv.Server rmi = new RmiServer();
		try {
			rmi.start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new MyTvException(e);
		}
		logger.info("My TV Program Table Crawler is running.");
	}

	/**
	 * 启动应用
	 */
	private static void startService() {
		logger.info("start My TV Program Table Crawler.");
		// 加载应用数据
		MyTvData data = new MyTvData();
		// 创建数据库及表结构
		initDb(data);
		// 初始化数据库数据
		initDbData0(data);
		initDbData(data);
	}

	@SuppressWarnings("unchecked")
	private static List<String> loadSql() {
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
	private static void initDb(MyTvData data) {
		if (data.isDbInited()) {
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
			data.writeData(null, Constant.XML_TAG_DB, "true");
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
	 * 初始化数据库数据
	 */
	private static void initDbData0(MyTvData data) {
		if (data.isDataInited()) {
			logger.info("init data had insert into db.");
			return;
		}
		Properties tvStationProp = new Properties();
		try {
			tvStationProp.load(Main.class.getResourceAsStream("/"
					+ Constant.TV_STATION_INIT_DATA_FILE_NAME));
		} catch (IOException e) {
			throw new MyTvException("error occur while load property file: "
					+ Constant.TV_STATION_INIT_DATA_FILE_NAME, e);
		}

		Collection<Object> values = tvStationProp.values();
		List<String> insertSqlList = new ArrayList<String>(values.size());
		String insertSql = "insert into tv_station (name,displayName,city,classify,channel,sequence)";
		for (Object value : values) {
			insertSqlList.add(insertSql + " values (" + value.toString() + ")");
		}

		Properties tvStationAliasProp = new Properties();
		try {
			tvStationAliasProp.load(Main.class.getResourceAsStream("/"
					+ Constant.TV_STATION_ALIAS_INIT_DATA_FILE_NAME));
		} catch (IOException e) {
			throw new MyTvException("error occur while load property file: "
					+ Constant.TV_STATION_ALIAS_INIT_DATA_FILE_NAME, e);
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
			data.writeData(null, Constant.XML_TAG_STATION, "true");
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

	}

	/**
	 * 初始化应用数据
	 */
	private static void initDbData(final MyTvData data) {
		final TvService tvService = new TvServiceImpl();
		List<TvStation> stationList = tvService.getAllStation();
		if (stationList != null) {
			MemoryCache.getInstance().addCache(stationList);
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				ExecutorService executorService = Executors
						.newFixedThreadPool(1);
				CompletionService<Void> completionService = new ExecutorCompletionService<Void>(
						executorService);
				Callable<Void> stationTask = null;
				if (!data.isStationCrawlerInited()) {
					stationTask = new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							// 首次抓取
							List<TvStation> stationList = tvService
									.crawlAllTvStation();
							if (stationList != null) {
								MemoryCache.getInstance().addCache(stationList);
							}
							data.writeData(null, Constant.XML_TAG_STATION,
									"true");
							return null;
						}
					};
					completionService.submit(stationTask);
				}

				if (!data.isProgramCrawlerInited()) {
					Callable<Void> programTask = new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							// 保存当天电视节目表
							String[] weeks = DateUtils.getWeek(new Date(),
									"yyyy-MM-dd");
							for (String date : weeks) {
								logger.info("query program table of " + date);
								CrawlerTaskManager.getIntance()
										.queryAllProgramTable(date);
							}
							data.writeData(null, Constant.XML_TAG_PROGRAM,
									"true");
							return null;
						}
					};
					if (stationTask != null) {
						try {
							completionService.take().get();
						} catch (InterruptedException e) {
							throw new MyTvException(
									"the crawl task of station is interrupted.",
									e);
						} catch (ExecutionException e) {
							throw new MyTvException(
									"the crawl task of station execute fail.",
									e);
						}
						completionService.submit(programTask);
					} else {
						completionService.submit(programTask);
					}
					try {
						completionService.take().get();
					} catch (InterruptedException e) {
						throw new MyTvException(
								"the crawl task of program table is interrupted.",
								e);
					} catch (ExecutionException e) {
						throw new MyTvException(
								"the crawl task of program table is interrupted.",
								e);
					}
					// 启动每天定时任务
					logger.info("create everyday crawl task.");
					createEverydayCron(data);
					executorService.shutdown();
				}
			}
		}).start();

	}

	/**
	 * 创建每天定时任务
	 */
	private static void createEverydayCron(final MyTvData data) {
		ScheduledExecutorService scheduled = new ScheduledThreadPoolExecutor(1);
		long initDelay = (DateUtils.string2Date(
				DateUtils.tommorow() + " 00:00:00").getTime() - new Date()
				.getTime()) / 1000;
		logger.info("cron crawler task will be automatic start after "
				+ initDelay + " seconds.");
		scheduled.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				CrawlerTaskManager.getIntance().queryAllProgramTable(
						DateUtils.today());
			}
		}, initDelay, 86460, TimeUnit.SECONDS);
	}
}
