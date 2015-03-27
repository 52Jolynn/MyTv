package com.laudandjolynn.mytvlist;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytvlist.epg.EpgParser;
import com.laudandjolynn.mytvlist.exception.MyTvListException;
import com.laudandjolynn.mytvlist.model.TvStation;
import com.laudandjolynn.mytvlist.utils.Constant;
import com.laudandjolynn.mytvlist.utils.FileUtils;
import com.laudandjolynn.mytvlist.utils.Utils;

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
		// 初始化数据库
		this.initDb();
		// 初始化其他数据
		this.initData();
	}

	/**
	 * 初始化数据库
	 */
	private void initDb() {
		File mytvlist = new File(Constant.MY_TV_LIST_FILE_NAME);
		if (mytvlist.exists()) {
			logger.debug("db have already init.");
			return;
		}

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new MyTvListException("db driver class is not found.", e);
		}

		Connection conn = Utils.getConnection();
		Statement stmt = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			ResourceBundle bundle = ResourceBundle.getBundle(Constant.SQL_FILE);
			Enumeration<String> enumeration = bundle.getKeys();
			while (enumeration.hasMoreElements()) {
				String sql = bundle.getString(enumeration.nextElement());
				stmt.addBatch(sql);
				logger.info("execute sql: " + sql);
			}
			stmt.executeBatch();
			conn.commit();
			try {
				FileUtils.write(Constant.APP_NAME.getBytes(),
						Constant.MY_TV_LIST_FILE_NAME);
			} catch (IOException e) {
				FileUtils.delete(Constant.MY_TV_LIST_FILE_NAME);
				throw new MyTvListException(e);
			}
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new MyTvListException(
							"error occur while rollback transaction.", e);
				}
			}
			throw new MyTvListException("error occur while execute sql on db.",
					e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(
							"error occur while close statement.", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(
							"error occur while close sqlite connection.", e);
				}
			}
		}
	}

	/**
	 * 初始化应用数据
	 */
	private void initData() {
		// 首次抓取
		String html = Crawler.crawlAsXml(Constant.EPG_URL);
		List<TvStation> stations = EpgParser.parseTvStation(html);
		// 写或更新数据到tv_station表
		TvStation[] stationArray = new TvStation[stations.size()];
		Utils.save(stations.toArray(stationArray));
		this.addAllTvStation2Cache(stations);
		Utils.outputCrawlData(Utils.today(), html);
	}

	/**
	 * 缓存所有电视台
	 * 
	 * @param stations
	 */
	protected void addAllTvStation2Cache(List<TvStation> stations) {
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
