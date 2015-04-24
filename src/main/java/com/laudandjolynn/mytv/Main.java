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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.crawler.RequestHandler;
import com.laudandjolynn.mytv.datasource.DataSourceManager;
import com.laudandjolynn.mytv.event.CrawlEvent;
import com.laudandjolynn.mytv.event.CrawlEventListener;
import com.laudandjolynn.mytv.event.CrawlEventListenerAdapter;
import com.laudandjolynn.mytv.event.TvStationFoundEvent;
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
			data.writeData(null, Constant.XML_TAG_DATA, "true");
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new MyTvException(
							"error occur while rollback transaction.", e1);
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
		// 启动抓取任务
		new Thread(new Runnable() {

			@Override
			public void run() {
				createCrawlTask(data, tvService);
			}
		}, "Mytv crawl task thread where init data").start();
	}

	/**
	 * 抓取任务
	 * 
	 * @param data
	 */
	private static void createCrawlTask(final MyTvData data,
			final TvService tvService) {
		CrawlEventListener listener = null;
		final String today = DateUtils.today();
		ThreadFactory threadFactory = new BasicThreadFactory.Builder()
				.namingPattern(
						"Mytv crawl program table task where init data[%d]")
				.build();
		final ExecutorService executorService = Executors.newFixedThreadPool(3,
				threadFactory);
		if (!data.isProgramCrawlerInited()) {
			listener = new CrawlEventListenerAdapter() {
				@Override
				public void itemFound(CrawlEvent event) {
					if (event instanceof TvStationFoundEvent) {
						final TvStation item = (TvStation) ((TvStationFoundEvent) event)
								.getItem();
						executorService.execute(new Runnable() {
							@Override
							public void run() {
								RequestHandler.getIntance().queryProgramTable(
										item.getName(), item.getClassify(),
										today);
							}
						});
					}
				}
			};
		}
		if (!data.isStationCrawlerInited()) {
			// 首次抓取
			tvService.crawlAllTvStation(listener);
			data.writeData(null, Constant.XML_TAG_STATION, "true");
			data.writeData(null, Constant.XML_TAG_PROGRAM, "true");
			final String[] weeks = DateUtils.getWeek(new Date(), "yyyy-MM-dd");
			for (final String date : weeks) {
				if (date.compareTo(today) < 1) {
					continue;
				}
				executorService.submit(new Runnable() {

					@Override
					public void run() {
						crawlAllProgramTable(date, tvService);
					}
				});
			}
			executorService.shutdown();
		}

		// 启动每天定时任务
		logger.info("create everyday crawl task.");
		createEverydayCron(data, tvService);
	}

	/**
	 * 创建每天定时任务
	 */
	private static void createEverydayCron(final MyTvData data,
			final TvService tvService) {
		ThreadFactory threadFactory = new BasicThreadFactory.Builder()
				.namingPattern("Mytv scheduled crawl task").build();
		ScheduledExecutorService scheduled = new ScheduledThreadPoolExecutor(1,
				threadFactory);
		Date today = new Date();
		String nextWeek = DateUtils.date2String(DateUtils.nextWeek(today),
				"yyyy-MM-dd");
		long initDelay = (DateUtils.string2Date(nextWeek + " 00:00:00")
				.getTime() - today.getTime()) / 1000;
		logger.info("cron crawler task will be automatic start after "
				+ initDelay + " seconds at " + nextWeek);
		scheduled.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				Date[] weeks = DateUtils.getWeek(new Date());
				logger.info("begin to crawl program table of "
						+ Arrays.deepToString(weeks));
				for (Date date : weeks) {
					crawlAllProgramTable(
							DateUtils.date2String(date, "yyyy-MM-dd"),
							tvService);
				}
			}
		}, initDelay, 7, TimeUnit.DAYS);
	}

	/**
	 * 抓取所有电视台节目表
	 * 
	 * @param date
	 * @param tvService
	 */
	private static void crawlAllProgramTable(final String date,
			TvService tvService) {
		List<TvStation> stationList = tvService.getAllCrawlableStation();
		ThreadFactory threadFactory = new BasicThreadFactory.Builder()
				.namingPattern(
						"Mytv crawl all program table where init data[%d]")
				.build();
		ExecutorService executorService = Executors.newFixedThreadPool(2,
				threadFactory);
		int size = stationList == null ? 0 : stationList.size();
		for (int i = 0; i < size; i++) {
			final TvStation tvStation = stationList.get(i);
			executorService.submit(new Runnable() {

				@Override
				public void run() {
					RequestHandler.getIntance().queryProgramTable(
							tvStation.getName(), tvStation.getClassify(), date);
				}
			});
		}
		executorService.shutdown();
	}
}
