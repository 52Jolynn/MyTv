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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.laudandjolynn.mytv.utils.Constant;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月14日 上午9:39:20
 * @copyright: www.laudandjolynn.com
 */
public class Sqlite implements TvDataSource {
	@Override
	public Connection getConnection(Properties prop) throws SQLException {
		String dbFileName = prop
				.getProperty(DataSourceManager.RES_KEY_DB_FILE_NAME);
		return DriverManager.getConnection("jdbc:sqlite:"
				+ Constant.MY_TV_DATA_PATH + dbFileName);
	}

}
