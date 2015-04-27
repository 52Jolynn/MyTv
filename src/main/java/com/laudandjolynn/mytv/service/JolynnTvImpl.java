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

import java.rmi.RemoteException;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.laudandjolynn.mytv.CrawlAction;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.MyTv;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.utils.MyTvUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月8日 上午11:03:39
 * @copyright: www.laudandjolynn.com
 */
public class JolynnTvImpl implements JolynnTv {
	private TvServiceImpl tvService = new TvServiceImpl();

	@Override
	public String getMyTvClassify() throws RemoteException {
		List<String> classifies = tvService.getMyTvClassify();
		return JSON.toJSONString(classifies);
	}

	@Override
	public String getProgramTable(String stationName, String classify,
			String date) throws RemoteException {
		if (!MyTvUtils.checkInvalidString(stationName)
				|| !MyTvUtils.checkInvalidString(classify)) {
			throw new MyTvException("invalid stationName: " + stationName);
		}
		List<ProgramTable> ptList = CrawlAction.getIntance().queryProgramTable(
				stationName, classify, date);
		return JSON.toJSONString(ptList);
	}

	@Override
	public String getMyTvByClassify(String classify) throws RemoteException {
		List<MyTv> stationList = tvService.getMyTvByClassify(classify);
		return JSON.toJSONString(stationList);
	}

}
