package com.laudandjolynn.mytvlist.utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.laudandjolynn.mytvlist.Init;
import com.laudandjolynn.mytvlist.exception.MyTvListException;
import com.laudandjolynn.mytvlist.model.ProgramTable;
import com.laudandjolynn.mytvlist.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午1:24:54
 * @copyright: www.laudandjolynn.com
 */
public class Utils {
	/**
	 * 获取当天的日期字符串，yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String today() {
		return DateUtils.date2String(Calendar.getInstance().getTime(),
				"yyyy-MM-dd");
	}

	/**
	 * 获取明天的日期字符串，yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String tommorow() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return DateUtils.date2String(calendar.getTime(), "yyyy-MM-dd");
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
					+ Constant.MY_TV_DATA_PATH + "mytvlist.db");
		} catch (SQLException e) {
			throw new MyTvListException("error occur while connection to db.",
					e);
		}
	}

	/**
	 * 获取电视台分类
	 * 
	 * @return
	 */
	public static List<String> getTvStationClassify() {
		String sql = "select classify from tv_station group by classify";
		Connection conn = Utils.getConnection();
		Statement stmt = null;
		List<String> classifies = new ArrayList<String>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				classifies.add(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new MyTvListException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}
		}

		return classifies;
	}

	/**
	 * 获取所有电视台
	 * 
	 * @return
	 */
	public static List<TvStation> getAllStation() {
		String sql = "select * from tv_station";
		Connection conn = Utils.getConnection();
		Statement stmt = null;
		List<TvStation> stations = new ArrayList<TvStation>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				TvStation station = new TvStation();
				station.setId(rs.getInt(1));
				station.setName(rs.getString(2));
				station.setClassify(rs.getString(3));
				stations.add(station);
			}
		} catch (SQLException e) {
			throw new MyTvListException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}
		}

		return stations;
	}

	/**
	 * 根据电视台名称查询
	 * 
	 * @param stationName
	 * @return
	 */
	public static TvStation getStation(String stationName) {
		String sql = "select * from tv_station where name='" + stationName
				+ "'";
		TvStation station = null;
		Connection conn = Utils.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				station = new TvStation();
				station.setId(rs.getInt(1));
				station.setName(rs.getString(2));
				station.setClassify(rs.getString(3));
			}
			rs.close();
		} catch (SQLException e) {
			throw new MyTvListException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}
		}
		return station;
	}

	/**
	 * 保存电视台信息
	 * 
	 * @param stations
	 * @return
	 */
	public static int[] save(TvStation... stations) {
		int len = stations == null ? 0 : stations.length;
		if (len <= 0) {
			return null;
		}
		Connection conn = Utils.getConnection();
		String insertSql = "insert into tv_station (name,classify) values(?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			for (int i = 0; i < len; i++) {
				TvStation station = stations[i];
				String stationName = station.getName();
				if (Init.getIntance().isStationExists(stationName)) {
					continue;
				}
				insertStmt.setString(1, station.getName());
				insertStmt.setString(2, station.getClassify());
				insertStmt.addBatch();
			}
			int[] r = insertStmt.executeBatch();
			conn.commit();
			return r;
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new MyTvListException(e1);
				}
			}
			throw new MyTvListException(
					"error occur while save data to tv_station.", e);
		} finally {
			if (insertStmt != null) {
				try {
					insertStmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}
		}
	}

	/**
	 * 保存电视节目表
	 * 
	 * @param programTables
	 * @return
	 */
	public static int[] save(ProgramTable... programTables) {
		int len = programTables == null ? 0 : programTables.length;
		if (len <= 0) {
			return null;
		}
		Connection conn = Utils.getConnection();
		String insertSql = "insert into program_table (station,stationName,program,airtime,week) values(?,?,?,?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			for (int i = 0; i < len; i++) {
				ProgramTable pt = programTables[i];
				String stationName = pt.getStationName();
				if (Init.getIntance().isStationExists(stationName)) {
					insertStmt.setInt(1,
							Init.getIntance().getStation(stationName).getId());
				}

				insertStmt.setString(2, pt.getStationName());
				insertStmt.setString(3, pt.getProgram());
				insertStmt.setString(4, pt.getAirTime());
				insertStmt.setInt(5, pt.getWeek());
				insertStmt.addBatch();
			}
			int[] r = insertStmt.executeBatch();
			conn.commit();
			return r;
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new MyTvListException(e1);
				}
			}
			throw new MyTvListException(
					"error occur while save data to program_table.", e);
		} finally {
			if (insertStmt != null) {
				try {
					insertStmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}
		}
	}

	/**
	 * 获取指定电视台节目表
	 * 
	 * @param stationName
	 *            电视台
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public static List<ProgramTable> getProgramTable(String stationName,
			String date) {
		String sql = "select id,station,stationName,program,airtime,week from program_table where stationName='"
				+ stationName + "' and aritime='" + date + "'";
		Connection conn = Utils.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			List<ProgramTable> resultList = new ArrayList<ProgramTable>();
			while (rs.next()) {
				ProgramTable pt = new ProgramTable();
				pt.setId(rs.getLong(1));
				pt.setStation(rs.getInt(2));
				pt.setStationName(rs.getString(3));
				pt.setProgram(rs.getString(4));
				pt.setAirTime(rs.getString(5));
				pt.setWeek(rs.getInt(6));
				resultList.add(pt);
			}
			rs.close();
			return resultList;
		} catch (SQLException e) {
			throw new MyTvListException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}
		}
	}

	/**
	 * 判断电视节目表是否已抓取
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public static boolean isProgramTableExists(String stationName, String date) {
		String sql = "select * from program_table where stationName='"
				+ stationName + "' and aritime='" + date + "'";
		Connection conn = Utils.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			boolean exists = rs.next();
			rs.close();
			return exists;
		} catch (SQLException e) {
			throw new MyTvListException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvListException(e);
				}
			}
		}
	}

	/**
	 * 输出抓取数据到文件
	 * 
	 * @param date
	 * @param data
	 */
	public static void outputCrawlData(String date, String data) {
		String crawlFileDir = Constant.CRAWL_FILE_PATH + Constant.UNDERLINE
				+ Utils.today() + File.separator;
		File crawlFile = new File(crawlFileDir);
		if (!crawlFile.exists()) {
			crawlFile.mkdirs();
		}
		String crawlFilePath = crawlFileDir + date + Constant.UNDERLINE
				+ System.nanoTime();
		try {
			FileUtils.writeWithNIO(data, FileUtils.DEFAULT_CHARSET_NAME,
					crawlFilePath);
		} catch (IOException e) {
			throw new MyTvListException(
					"error occur while write crawled data to disk. filepaht ["
							+ crawlFilePath + "].", e);
		}
	}
}
