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
package com.laudandjolynn.mytv.epg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.laudandjolynn.mytv.Init;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.Constant;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午1:24:54
 * @copyright: www.laudandjolynn.com
 */
public class EpgDao {

	/**
	 * 获取数据库连接
	 * 
	 * @return
	 */
	public static Connection getConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new MyTvException("db driver class is not found.", e);
		}

		try {
			return DriverManager.getConnection("jdbc:sqlite:"
					+ Constant.MY_TV_DATA_PATH + Constant.DB_NAME);
		} catch (SQLException e) {
			throw new MyTvException("error occur while connection to db.", e);
		}
	}

	/**
	 * 获取电视台分类
	 * 
	 * @return
	 */
	protected static List<String> getTvStationClassify() {
		String sql = "select classify from tv_station group by classify";
		Connection conn = EpgDao.getConnection();
		Statement stmt = null;
		List<String> classifies = new ArrayList<String>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				classifies.add(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new MyTvException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
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
	protected static List<TvStation> getAllStation() {
		String sql = "select id,name,city,classify from tv_station";
		Connection conn = EpgDao.getConnection();
		Statement stmt = null;
		List<TvStation> stations = new ArrayList<TvStation>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				TvStation station = new TvStation();
				station.setId(rs.getInt(1));
				station.setName(rs.getString(2));
				station.setCity(rs.getString(3));
				station.setClassify(rs.getString(4));
				stations.add(station);
			}
		} catch (SQLException e) {
			throw new MyTvException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
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
	protected static TvStation getStation(String stationName) {
		String sql = "select id,name,city,classify from tv_station where name='"
				+ stationName + "'";
		TvStation station = null;
		Connection conn = EpgDao.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				station = new TvStation();
				station.setId(rs.getInt(1));
				station.setName(rs.getString(2));
				station.setCity(rs.getString(3));
				station.setClassify(rs.getString(4));
			}
			rs.close();
		} catch (SQLException e) {
			throw new MyTvException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}
		}
		return station;
	}

	/**
	 * 判断电视台在数据库是否存在
	 * 
	 * @param stations
	 * @return
	 */
	protected static boolean[] isStationExists(TvStation... stations) {
		int length = stations.length;
		boolean[] result = new boolean[length];
		Connection conn = EpgDao.getConnection();
		String sql = "select * from tv_station where name=?";
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			for (int i = 0; i < length; i++) {
				TvStation station = stations[i];
				stmt.setString(1, station.getName());
				ResultSet rs = stmt.executeQuery();
				result[i] = rs.next();
				rs.close();
			}
			return result;
		} catch (SQLException e) {
			throw new MyTvException(
					"error occur while query state of station.", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}
		}
	}

	/**
	 * 判断电视台在数据库是否存在
	 * 
	 * @param stationName
	 *            电视台名称
	 * @return
	 */
	protected static boolean isStationExists(String stationName) {
		String sql = "select * from tv_station where name='" + stationName
				+ "'";
		Connection conn = EpgDao.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			boolean exists = rs.next();
			rs.close();
			return exists;
		} catch (SQLException e) {
			throw new MyTvException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}
		}
	}

	/**
	 * 保存电视台信息
	 * 
	 * @param stations
	 * @return
	 */
	protected static int[] save(TvStation... stations) {
		Connection conn = EpgDao.getConnection();
		String insertSql = "insert into tv_station (name,city,classify) values(?,?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			int len = stations.length;
			for (int i = 0; i < len; i++) {
				TvStation station = stations[i];
				insertStmt.setString(1, station.getName());
				insertStmt.setString(2, station.getCity());
				insertStmt.setString(3, station.getClassify());
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
					throw new MyTvException(e1);
				}
			}
			throw new MyTvException(
					"error occur while save data to tv_station.", e);
		} finally {
			if (insertStmt != null) {
				try {
					insertStmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
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
	protected static int[] save(ProgramTable... programTables) {
		Connection conn = EpgDao.getConnection();
		String insertSql = "insert into program_table (station,stationName,program,airtime,week) values(?,?,?,?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			int len = programTables.length;
			for (int i = 0; i < len; i++) {
				ProgramTable pt = programTables[i];
				String stationName = pt.getStationName();
				int id = 0;
				if (Init.getIntance().isStationExists(stationName)) {
					id = Init.getIntance().getStation(stationName).getId();
				} else {
					id = getStation(stationName).getId();
				}
				insertStmt.setInt(1, id);

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
					throw new MyTvException(e1);
				}
			}
			throw new MyTvException(
					"error occur while save data to program_table.", e);
		} finally {
			if (insertStmt != null) {
				try {
					insertStmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
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
	protected static List<ProgramTable> getProgramTable(String stationName,
			String date) {
		String sql = "select id,station,stationName,program,airtime,week from program_table where stationName='"
				+ stationName + "' and aritime='" + date + "'";
		Connection conn = EpgDao.getConnection();
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
			throw new MyTvException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
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
	protected static boolean isProgramTableExists(String stationName,
			String date) {
		String sql = "select * from program_table where stationName='"
				+ stationName + "' and airtime='" + date + "'";
		Connection conn = EpgDao.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			boolean exists = rs.next();
			rs.close();
			return exists;
		} catch (SQLException e) {
			throw new MyTvException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw new MyTvException(e);
				}
			}
		}
	}

}
