package com.laudandjolynn.mytvlist;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytvlist.utils.Constant;
import com.laudandjolynn.mytvlist.utils.FileUtils;
import com.laudandjolynn.mytvlist.utils.MyTvListException;
import com.laudandjolynn.mytvlist.utils.Utils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月26日 下午1:33:14
 * @copyright: www.laudandjolynn.com
 */
public class Init implements Before {
	private final static Logger logger = LoggerFactory.getLogger(Init.class);
	private final AtomicBoolean ready = new AtomicBoolean(false);
	private final static Init instance = new Init();

	private Init() {
	}

	public static Init getIntance() {
		return instance;
	}

	/**
	 * 初始化应用基础数据
	 */
	public void init() {
		this.initDb();
		this.initData();
		ready.set(true);
	}

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

	private void initData() {
		File dirFile = new File(Constant.PROGRAM_TABLES_FILE_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		String fileName = Constant.PROGRAM_TABLE_FILE_PATH + Utils.today();
		File file = new File(fileName);
		if (file.exists()) {
			logger.debug(fileName + " have already exists.");
			return;
		}

		try {
			logger.debug("write html to file: " + fileName);
			FileUtils.writeWithNIO(Crawler.crawlAsXml(Constant.EPG_URL),
					"UTF-8", fileName);
		} catch (Exception e) {
			logger.error("fail to save debug file ", e);
		}
	}

	@Override
	public boolean isReady() {
		return ready.get();
	}

}
