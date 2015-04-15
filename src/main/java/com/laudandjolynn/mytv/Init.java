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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.datasource.DataSourceManager;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.service.TvService;
import com.laudandjolynn.mytv.utils.Config;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;

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

	@SuppressWarnings("unchecked")
	private List<String> loadSql() {
		Object object = DataSourceManager.prop
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
	private void initData() {
		TvService epgService = new TvService();
		List<TvStation> stations = epgService.getAllStation();
		boolean isStationExists = (stations == null ? 0 : stations.size()) > 0;
		String today = DateUtils.today();
		if (isStationExists) {
			this.addAllTvStation2Cache(stations);
		} else {
			// 首次抓取
			stations = Config.TV_CRAWLER.crawlAllTvStation();
			// 写数据到tv_station表
			TvStation[] stationArray = new TvStation[stations.size()];
			epgService.save(stations.toArray(stationArray));
			this.addAllTvStation2Cache(epgService.getAllStation());
		}

		if (!MyTvData.getInstance().isProgramTableOfTodayCrawled()) {
			// 保存当天电视节目表
			logger.info("query program table of today. " + "today is " + today);
			List<ProgramTable> ptList = Config.TV_CRAWLER
					.crawlAllProgramTable(today);
			ProgramTable[] ptArray = new ProgramTable[ptList.size()];
			epgService.save(ptList.toArray(ptArray));
			MyTvData.getInstance().writeData(
					Constant.XML_TAG_PROGRAM_TABLE_DATES,
					Constant.XML_TAG_PROGRAM_TABLE_DATE, today);
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
