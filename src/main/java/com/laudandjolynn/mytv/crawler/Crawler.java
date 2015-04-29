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
package com.laudandjolynn.mytv.crawler;

import java.util.List;

import com.laudandjolynn.mytv.event.CrawlEventListener;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午3:36:28
 * @copyright: www.laudandjolynn.com
 */
public interface Crawler {
	/**
	 * 抓取所有电视台
	 * 
	 * @return
	 */
	public List<TvStation> crawlAllTvStation();

	/**
	 * 根据电视台名称、日期抓取电视节目表
	 * 
	 * @param date
	 * @param station
	 * @return
	 */
	public List<ProgramTable> crawlProgramTable(String date, TvStation station);

	/**
	 * 判断指定电视台是否可抓取
	 * 
	 * @param station
	 * @return
	 */
	public boolean exists(TvStation station);

	/**
	 * 获取抓取器名称
	 * 
	 * @return
	 */
	public String getCrawlerName();

	/**
	 * 获取抓取地址
	 * 
	 * @return
	 */
	public String getUrl();

	/**
	 * 注册抓取事件监听器
	 * 
	 * @param listener
	 */
	public void registerCrawlEventListener(CrawlEventListener listener);

	/**
	 * 删除抓取事件监听器
	 * 
	 * @param listener
	 */
	public void removeCrawlEventListener(CrawlEventListener listener);
}
