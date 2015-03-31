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
package com.laudandjolynn.mytv.service;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午6:18:24
 * @copyright: www.laudandjolynn.com
 */
public interface JolynnTv {
	/**
	 * 获取电视台分类
	 * 
	 * @return
	 */
	public String getTvStationClassify();

	/**
	 * 获取所有电视台
	 * 
	 * @return
	 */
	public String getAllTvStation();

	/**
	 * 获取指定电视台、日期的节目表
	 * 
	 * @param name
	 *            电视台名称
	 * @param date
	 *            日期, yyyy-MM-dd
	 * @return
	 */
	public String getProgramTable(String name, String date);
}
