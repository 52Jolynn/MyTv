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
package com.laudandjolynn.mytv.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.Proxy;
import com.laudandjolynn.mytv.proxy.MyTvProxyManager;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月24日 下午2:25:00
 * @copyright: www.laudandjolynn.com
 */
public class WebCrawler {
	private final static Logger logger = LoggerFactory
			.getLogger(WebCrawler.class);

	private final static BrowserVersion[] USER_AGENTS = new BrowserVersion[] {
			BrowserVersion.CHROME, BrowserVersion.FIREFOX_24,
			BrowserVersion.INTERNET_EXPLORER_11 };

	/**
	 * 使用htmlunit抓取网页
	 * 
	 * @param url
	 * @return
	 */
	public static Page crawl(String url) {
		return crawl(url, randomUserAgent());
	}

	/**
	 * 使用htmlunit抓取网页
	 * 
	 * @param url
	 * @param userAgent
	 * @param cookie
	 * @return
	 */
	public static Page crawl(String url, String userAgent) {
		return crawl(url, new BrowserVersion(WebCrawler.class.getName(), "1.0",
				userAgent, 1.0f));
	}

	/**
	 * 使用htmlunit抓取网页
	 * 
	 * @param url
	 * @param browserVersion
	 * @return
	 */
	private static Page crawl(String url, BrowserVersion browserVersion) {
		Proxy proxy = MyTvProxyManager.getInstance().pickProxy();
		WebClient webClient = new WebClient(browserVersion);
		if (proxy != null) {
			ProxyConfig pc = new ProxyConfig(proxy.getIp(), proxy.getPort());
			webClient.getOptions().setProxyConfig(pc);
		}
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());

		try {
			logger.info("begin to get page: " + url
					+ (proxy != null ? ", using: " + proxy : ""));
			return webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			throw new MyTvException("can't connect to " + url, e);
		} catch (MalformedURLException e) {
			throw new MyTvException("invalid url " + url, e);
		} catch (IOException e) {
			throw new MyTvException("error occur while connect to " + url, e);
		} finally {
			webClient.closeAllWindows();
		}
	}

	/**
	 * 取得随机浏览器标识
	 * 
	 * @return
	 */
	private static BrowserVersion randomUserAgent() {
		Random random = new Random();
		int max = USER_AGENTS.length;
		return USER_AGENTS[random.nextInt(max)];
	}
}
