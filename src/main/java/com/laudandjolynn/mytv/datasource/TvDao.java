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

import java.util.List;

import com.laudandjolynn.mytv.model.MyTv;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午12:28:15
 * @copyright: www.laudandjolynn.com
 */
public interface TvDao {
	/**
	 * 获取所有电视台分类
	 * 
	 * @return
	 */
	public List<String> getMyTvClassify();

	/**
	 * 获取指定分类下的所有电视台
	 * 
	 * @param classify
	 * @return
	 */
	public List<MyTv> getMyTvByClassify(String classify);

	/**
	 * 获取所有可用于抓取的电视台对象列表
	 * 
	 * @return
	 */
	public List<TvStation> getAllCrawlableStation();

	/**
	 * 根据电视台名称得到电视台对象
	 * 
	 * @param stationName
	 *            电视台名称
	 * @return
	 */
	public List<TvStation> getStation(String stationName);

	/**
	 * 根据电视台显示名得到电视台对象
	 * 
	 * @param displayName
	 *            电视台显示名
	 * @param classify
	 *            电视台分类
	 * @return
	 */
	public TvStation getStationByDisplayName(String displayName, String classify);

	/**
	 * 保存电视台
	 * 
	 * @param stations
	 * @return
	 */
	public int[] save(TvStation... stations);

	/**
	 * 保存电视节目表
	 * 
	 * @param programTables
	 * @return
	 */
	public int[] save(ProgramTable... programTables);

	/**
	 * 根据电视台、日期获取电视节目表
	 * 
	 * @param stationName
	 * @param date
	 * @return
	 */
	public List<ProgramTable> getProgramTable(String stationName, String date);

	/**
	 * 判断指定电视台、日期的电视节目表是否已存在
	 * 
	 * @param stationName
	 * @param date
	 * @return
	 */
	public boolean isProgramTableExists(String stationName, String date);

	/**
	 * 获取所有在客户端显示的电视台
	 * 
	 * @return
	 */
	public List<TvStation> getDisplayedTvStation();

	/**
	 * 获取所有我的电视台
	 * 
	 * @return
	 */
	public List<MyTv> getMyTv();

}
