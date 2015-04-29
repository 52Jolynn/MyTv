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
