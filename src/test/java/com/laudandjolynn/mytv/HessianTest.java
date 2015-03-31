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

import java.net.MalformedURLException;

import org.json.JSONArray;

import com.caucho.hessian.client.HessianProxyFactory;
import com.laudandjolynn.mytv.service.JolynnTv;
import com.laudandjolynn.mytv.utils.DateUtils;

import junit.framework.TestCase;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月30日 下午5:07:35
 * @copyright: www.laudandjolynn.com
 */
public class HessianTest extends TestCase {
	/**
	 * 测试电视台分类
	 */
	public void testEpgClassify() {
		String url = "http://localhost/epg";
		HessianProxyFactory proxy = new HessianProxyFactory();
		try {
			JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
			String classify = tv.getTvStationClassify();
			JSONArray array = new JSONArray(classify);
			assertTrue(array.length() == 6);
		} catch (MalformedURLException e) {
		}
	}

	/**
	 * 测试电视台
	 */
	public void testEpgStation() {
		String url = "http://localhost/epg";
		HessianProxyFactory proxy = new HessianProxyFactory();
		try {
			JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
			String stations = tv.getAllTvStation();
			JSONArray array = new JSONArray(stations);
			assertTrue(array.length() == 145);
		} catch (MalformedURLException e) {
		}
	}

	/**
	 * 测试电视节目
	 */
	public void testEpgProgram() {
		String url = "http://localhost/epg";
		HessianProxyFactory proxy = new HessianProxyFactory();
		try {
			JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
			String program = tv.getProgramTable("江苏卫视", DateUtils.today());
			JSONArray array = new JSONArray(program);
			System.out.println(array);
		} catch (MalformedURLException e) {
		}
	}
}
