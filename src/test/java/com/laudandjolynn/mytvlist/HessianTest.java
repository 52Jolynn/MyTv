package com.laudandjolynn.mytvlist;

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
