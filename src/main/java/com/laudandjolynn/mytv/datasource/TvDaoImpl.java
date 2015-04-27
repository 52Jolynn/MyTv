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
package com.laudandjolynn.mytv.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.MyTv;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午1:24:54
 * @copyright: www.laudandjolynn.com
 */
public class TvDaoImpl implements TvDao {
	@Override
	public List<String> getMyTvClassify() {
		String sql = "select classify from my_tv group by classify order by sequence asc";
		Connection conn = getConnection();
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

	@Override
	public List<MyTv> getMyTvByClassify(String classify) {
		String sql = "select id,stationName,displayName,classify,channel,sequence from my_tv where classify='"
				+ classify + "' order by sequence asc";
		List<MyTv> tvList = new ArrayList<MyTv>();
		Connection conn = getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				MyTv myTv = new MyTv();
				int index = 1;
				myTv.setId(rs.getInt(index++));
				myTv.setStationName(rs.getString(index++));
				myTv.setDisplayName(rs.getString(index++));
				myTv.setClassify(rs.getString(index++));
				myTv.setChannel(rs.getString(index++));
				myTv.setSequence(rs.getInt(index++));
				tvList.add(myTv);
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
		return tvList;
	}

	@Override
	public List<TvStation> getAllCrawlableStation() {
		String sql = "select id,name,city,classify,sequence from tv_station order by sequence asc";
		Connection conn = getConnection();
		Statement stmt = null;
		List<TvStation> stations = new ArrayList<TvStation>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int index = 1;
				TvStation station = new TvStation();
				station.setId(rs.getInt(index++));
				station.setName(rs.getString(index++));
				station.setCity(rs.getString(index++));
				station.setClassify(rs.getString(index++));
				station.setSequence(rs.getInt(index++));
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

	@Override
	public List<TvStation> getStation(String stationName) {
		String sql = "select id,name,city,classify,sequence from tv_station where stationName='"
				+ stationName + "' order by sequence asc";
		List<TvStation> stationList = new ArrayList<TvStation>();
		Connection conn = getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int index = 1;
				TvStation station = new TvStation();
				station.setId(rs.getInt(index++));
				station.setName(rs.getString(index++));
				station.setCity(rs.getString(index++));
				station.setClassify(rs.getString(index++));
				station.setSequence(rs.getInt(index++));
				stationList.add(station);
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
		return stationList;
	}

	@Override
	public TvStation getStationByDisplayName(String displayName, String classify) {
		String sql = "select b.id,b.name,b.city,b.classify,b.sequence from my_tv a, tv_station b where a.stationName=b.name and a.displayName='"
				+ displayName
				+ "' and a.classify='"
				+ classify
				+ "' order by sequence asc";
		TvStation station = null;
		Connection conn = getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				int index = 1;
				station = new TvStation();
				station.setId(rs.getInt(index++));
				station.setName(rs.getString(index++));
				station.setCity(rs.getString(index++));
				station.setClassify(rs.getString(index++));
				station.setSequence(rs.getInt(index++));
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

	@Override
	public int[] save(TvStation... stations) {
		Connection conn = getConnection();
		String insertSql = "insert into tv_station (name,city,classify,sequence) values(?,?,?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			int len = stations.length;
			for (int i = 0; i < len; i++) {
				TvStation station = stations[i];
				int index = 1;
				insertStmt.setString(index++, station.getName());
				insertStmt.setString(index++, station.getCity());
				insertStmt.setString(index++, station.getClassify());
				insertStmt.setInt(index++, station.getSequence());
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

	@Override
	public int[] save(ProgramTable... programTables) {
		Connection conn = getConnection();
		String insertSql = "insert into program_table (stationName,program,airdate,airtime,week) values(?,?,?,?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			int len = programTables.length;
			for (int i = 0; i < len; i++) {
				ProgramTable pt = programTables[i];
				insertStmt.setString(1, pt.getStationName());
				insertStmt.setString(2, pt.getProgram());
				insertStmt.setString(3, pt.getAirDate());
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

	@Override
	public List<ProgramTable> getProgramTable(String stationName, String date) {
		String sql = "select id,stationName,program,airdate,airtime,week from program_table a where stationName='"
				+ stationName
				+ "' and airdate='"
				+ date
				+ "' order by airtime asc";
		Connection conn = getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			List<ProgramTable> resultList = new ArrayList<ProgramTable>();
			while (rs.next()) {
				ProgramTable pt = new ProgramTable();
				pt.setId(rs.getLong(1));
				pt.setStationName(rs.getString(2));
				pt.setProgram(rs.getString(3));
				pt.setAirDate(rs.getString(4));
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

	@Override
	public boolean isProgramTableExists(String stationName, String date) {
		String sql = "select * from program_table where stationName='"
				+ stationName + "' and airdate='" + date + "'";
		Connection conn = getConnection();
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
	 * 获取数据库连接
	 * 
	 * @return
	 */
	private Connection getConnection() {
		try {
			return DataSourceManager.getConnection();
		} catch (SQLException e) {
			throw new MyTvException("error occur while connection to db.", e);
		}
	}
}
