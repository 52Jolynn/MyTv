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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.epg.EpgCrawler;
import com.laudandjolynn.mytv.epg.EpgDao;
import com.laudandjolynn.mytv.epg.EpgParser;
import com.laudandjolynn.mytv.epg.EpgService;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.MyTvUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月26日 下午1:33:14
 * @copyright: www.laudandjolynn.com
 */
public class Init {
	private final static Logger logger = LoggerFactory.getLogger(Init.class);
	private final static Map<String, TvStation> ALL_TV_STATION = new HashMap<String, TvStation>();

	private Init() {
	}

	public static Init getIntance() {
		return InitSingltonHolder.INIT;
	}

	private final static class InitSingltonHolder {
		private final static Init INIT = new Init();
	}

	/**
	 * 初始化应用基础数据
	 */
	public void init() {
		// 加载应用数据
		MyTvData.getInstance().loadData();
		// 初始化数据库
		this.initDb();
		// 初始化其他数据
		this.initData();
	}

	private List<String> loadSql() {
		ResourceBundle bundle = ResourceBundle.getBundle(Constant.SQL_FILE);
		Enumeration<String> enumeration = bundle.getKeys();
		SortedSet<String> sqlSet = new TreeSet<String>(
				new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						if (o1.startsWith("create table")) {
							return -1;
						} else {
							return 1;
						}
					}
				});
		while (enumeration.hasMoreElements()) {
			sqlSet.add(bundle.getString(enumeration.nextElement()));
		}
		return new ArrayList<String>(sqlSet);
	}

	/**
	 * 初始化数据库
	 */
	private void initDb() {
		if (MyTvData.getInstance().isDbInited()) {
			logger.debug("db have already init.");
			return;
		}

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new MyTvException("db driver class is not found.", e);
		}

		File myTvDataFilePath = new File(Constant.MY_TV_DATA_FILE_PATH);
		Connection conn = EpgDao.getConnection();
		Statement stmt = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			List<String> sqlList = loadSql();
			for (String sql : sqlList) {
				stmt.addBatch(sql);
				logger.info("execute sql: " + sql);
			}
			stmt.executeBatch();
			conn.commit();

			MyTvData.getInstance().writeData(null, Constant.SQL_FILE, "true");
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
	private void initData() {
		List<TvStation> stations = EpgService.getAllStation();
		boolean isStationExists = (stations == null ? 0 : stations.size()) > 0;
		boolean isProgramTableOfTodayCrawled = MyTvData.getInstance()
				.isProgramTableOfTodayCrawled();
		if (isStationExists && isProgramTableOfTodayCrawled) {
			this.addAllTvStation2Cache(stations);
			return;
		}
		// 首次抓取
		Page page = Crawler.crawl(Constant.EPG_URL);
		if (!page.isHtmlPage()) {
			return;
		}
		HtmlPage htmlPage = (HtmlPage) page;
		String today = DateUtils.today();
		if (!isStationExists) {
			String html = htmlPage.asXml();
			stations = EpgParser.parseTvStation(html);
			// 写数据到tv_station表
			TvStation[] stationArray = new TvStation[stations.size()];
			EpgService.save(stations.toArray(stationArray));
			MyTvUtils.outputCrawlData(today, html,
					Constant.CRAWL_FILE_STATION_TAG);
			this.addAllTvStation2Cache(EpgService.getAllStation());
		}

		if (!isProgramTableOfTodayCrawled) {
			// 保存当天电视节目表
			logger.info("query program table of today. " + "today is " + today);
			List<ProgramTable> ptList = EpgCrawler.crawlAllProgramTable(today);
			ProgramTable[] ptArray = new ProgramTable[ptList.size()];
			EpgService.save(ptList.toArray(ptArray));
			MyTvData.getInstance().writeData(Constant.PROGRAM_TABLE_DATES,
					Constant.PROGRAM_TABLE_DATE, today);
		}
	}

	public Collection<TvStation> getAllCacheTvStation() {
		return ALL_TV_STATION.values();
	}

	/**
	 * 缓存所有电视台
	 * 
	 * @param stations
	 */
	private void addAllTvStation2Cache(List<TvStation> stations) {
		for (TvStation station : stations) {
			ALL_TV_STATION.put(station.getName(), station);
		}
	}

	/**
	 * 判断电视台是否已经在数据库中存在
	 * 
	 * @param stationName
	 * @return
	 */
	public boolean isStationExists(String stationName) {
		return ALL_TV_STATION.containsKey(stationName);
	}

	/**
	 * 根据名称获取电视台對象
	 * 
	 * @param stationName
	 * @return
	 */
	public TvStation getStation(String stationName) {
		return ALL_TV_STATION.get(stationName);
	}

}
