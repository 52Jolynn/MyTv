package com.laudandjolynn.mytv.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月17日 下午5:11:46
 * @copyright: www.laudandjolynn.com
 */
public class ObjectDaoImpl implements ObjectDao {

	@Override
	public int executeUpdate(String sql) throws SQLException {
		Connection conn = DataSourceManager.getConnection();
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
		int r = stmt.executeUpdate(sql);
		conn.commit();
		conn.setAutoCommit(true);
		stmt.close();
		conn.close();
		return r;
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		Connection conn = DataSourceManager.getConnection();
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		conn.commit();
		conn.setAutoCommit(true);
		stmt.close();
		conn.close();
		return rs;
	}

}
