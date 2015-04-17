package com.laudandjolynn.mytv.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月17日 下午5:10:37
 * @copyright: www.laudandjolynn.com
 */
public interface ObjectDao {
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public int executeUpdate(String sql) throws SQLException;

	/**
	 * 
	 * @param sql
	 * @return
	 */
	public ResultSet executeQuery(String sql) throws SQLException;
}
