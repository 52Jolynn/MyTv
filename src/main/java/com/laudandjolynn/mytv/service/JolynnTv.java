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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午6:18:24
 * @copyright: www.laudandjolynn.com
 */
public interface JolynnTv extends Remote {
	/**
	 * 获取电视台分类
	 * 
	 * @throws RemoteException
	 * @return
	 */
	public String getMyTvClassify() throws RemoteException;

	/**
	 * 根据电视台分类获取分类下的所有电视台
	 * 
	 * @param classify
	 * @return
	 * @throws RemoteException
	 */
	public String getMyTvByClassify(String classify) throws RemoteException;

	/**
	 * 获取指定电视台、日期的节目表
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param classify
	 *            电视台分类
	 * @param date
	 *            日期, yyyy-MM-dd
	 * @throws RemoteException
	 * @return
	 */
	public String getProgramTable(String stationName, String classify,
			String date) throws RemoteException;
}
