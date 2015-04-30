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
package com.laudandjolynn.mytv;

import java.rmi.Naming;

import junit.framework.TestCase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.laudandjolynn.mytv.service.JolynnTv;
import com.laudandjolynn.mytv.utils.Config;
import com.laudandjolynn.mytv.utils.DateUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月8日 下午12:05:51
 * @copyright: www.laudandjolynn.com
 */
public class RmiTest extends TestCase {
	private final static String url = "rmi://" + Config.NET_CONFIG.getIp()
			+ ":" + Config.NET_CONFIG.getRmiPort() + "/epg";

	/**
	 * 测试电视台分类
	 */
	public void testEpgClassify() {
		String classify = null;
		try {
			JolynnTv jolynnTv = (JolynnTv) Naming.lookup(url);
			classify = jolynnTv.getMyTvClassify();
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray array = JSON.parseArray(classify);
		assertNotNull(array);
	}

	/**
	 * 测试电视台
	 */
	public void testEpgStation() {
		String stations = null;
		try {
			JolynnTv jolynnTv = (JolynnTv) Naming.lookup(url);
			stations = jolynnTv.getMyTvByClassify("番禺有线");
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray array = JSON.parseArray(stations);
		assertNotNull(array);
	}

	/**
	 * 测试电视节目
	 */
	public void testEpgProgram() {
		String program = null;
		try {
			JolynnTv jolynnTv = (JolynnTv) Naming.lookup(url);
			program = jolynnTv.getProgramTable("CCTV-1 综合", "番禺有线",
					DateUtils.today());
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray array = JSON.parseArray(program);
		assertNotNull(array);
	}
}
