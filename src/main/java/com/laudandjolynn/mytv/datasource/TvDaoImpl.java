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
	public List<String> getTvStationClassify() {
		String sql = "select classify from tv_station group by classify order by sequence asc";
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
	public List<TvStation> getTvStationByClassify(String classify) {
		String sql = "select id,name,displayName,city,classify,channel,sequence from tv_station where classify='"
				+ classify + "' order by sequence asc";
		List<TvStation> stationList = new ArrayList<TvStation>();
		Connection conn = getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				TvStation station = new TvStation();
				int index = 1;
				station.setId(rs.getInt(index++));
				station.setName(rs.getString(index++));
				station.setDisplayName(rs.getString(index++));
				station.setCity(rs.getString(index++));
				station.setClassify(rs.getString(index++));
				station.setChannel(rs.getString(index++));
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
	public List<TvStation> getAllStation() {
		String sql = "select id,name,displayName,city,classify,channel,sequence from tv_station order by sequence asc";
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
				station.setDisplayName(rs.getString(index++));
				station.setCity(rs.getString(index++));
				station.setClassify(rs.getString(index++));
				station.setChannel(rs.getString(index++));
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
	public TvStation getStation(String stationName) {
		String sql = "select id,name,displayName,city,classify,channel,sequence from tv_station where name='"
				+ stationName + "' order by sequence asc";
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
				station.setDisplayName(rs.getString(index++));
				station.setCity(rs.getString(index++));
				station.setClassify(rs.getString(index++));
				station.setChannel(rs.getString(index++));
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
	public TvStation getStationByDisplayName(String displayName, String classify) {
		String sql = "select id,name from tv_station a where (displayName='"
				+ displayName
				+ "' and classify='"
				+ classify
				+ "'"
				+ ") or exists (select alias from tv_station_alias b where a.name=b.stationName and b.alias='"
				+ displayName + "')" + " order by sequence asc";
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
				station.setDisplayName(displayName);
				station.setClassify(classify);
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
	public boolean[] isStationExists(TvStation... stations) {
		int length = stations.length;
		boolean[] result = new boolean[length];
		Connection conn = getConnection();
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

	@Override
	public boolean isStationExists(String stationName) {
		String sql = "select * from tv_station where name='" + stationName
				+ "'";
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

	@Override
	public int[] save(TvStation... stations) {
		Connection conn = getConnection();
		String insertSql = "insert into tv_station (name,displayName,city,classify,channel,sequence) values(?,?,?,?,?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			int len = stations.length;
			for (int i = 0; i < len; i++) {
				TvStation station = stations[i];
				int index = 1;
				insertStmt.setString(index++, station.getName());
				insertStmt.setString(index++, station.getDisplayName());
				insertStmt.setString(index++, station.getCity());
				insertStmt.setString(index++, station.getClassify());
				insertStmt.setString(index++, station.getChannel());
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
		String insertSql = "insert into program_table (station,stationName,program,airdate,airtime,week) values(?,?,?,?,?,?)";
		PreparedStatement insertStmt = null;
		try {
			conn.setAutoCommit(false);
			insertStmt = conn.prepareStatement(insertSql);
			int len = programTables.length;
			for (int i = 0; i < len; i++) {
				ProgramTable pt = programTables[i];
				insertStmt.setInt(1, pt.getStation());
				insertStmt.setString(2, pt.getStationName());
				insertStmt.setString(3, pt.getProgram());
				insertStmt.setString(4, pt.getAirDate());
				insertStmt.setString(5, pt.getAirTime());
				insertStmt.setInt(6, pt.getWeek());
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
		String sql = "select id,station,stationName,program,airdate,airtime,week from program_table where stationName='"
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
				pt.setStation(rs.getInt(2));
				pt.setStationName(rs.getString(3));
				pt.setProgram(rs.getString(4));
				pt.setAirDate(rs.getString(5));
				pt.setAirTime(rs.getString(6));
				pt.setWeek(rs.getInt(7));
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
