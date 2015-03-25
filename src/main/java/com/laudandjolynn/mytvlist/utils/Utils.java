package com.laudandjolynn.mytvlist.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午1:24:54
 * @copyright: www.laudandjolynn.com
 */
public class Utils {
	public static String today() {
		return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return
	 */
	public static Connection getConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new MyTvListException("db driver class is not found.", e);
		}

		try {
			return DriverManager.getConnection("jdbc:sqlite:"
					+ Constant.CLASS_PATH + "mytvlist.db");
		} catch (SQLException e) {
			throw new MyTvListException("error occur while connection to db.",
					e);
		}
	}
}
