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

import com.caucho.hessian.client.HessianProxyFactory;
import com.laudandjolynn.mytv.service.JolynnTv;

import junit.framework.TestCase;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月30日 下午5:07:35
 * @copyright: www.laudandjolynn.com
 */
public class HessianTest extends TestCase {
	public void testEpg() {
		String url = "http://localhost/epg";
		HessianProxyFactory proxy = new HessianProxyFactory();
		try {
			JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
			System.out.println(tv.getTvStationClassify());
		} catch (MalformedURLException e) {
		}
	}
}
