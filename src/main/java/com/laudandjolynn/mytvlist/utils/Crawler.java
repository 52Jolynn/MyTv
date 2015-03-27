package com.laudandjolynn.mytvlist.utils;

import java.io.IOException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月24日 下午2:25:00
 * @copyright: www.laudandjolynn.com
 */
public class Crawler {
	private final static Logger logger = LoggerFactory.getLogger(Crawler.class);

	/**
	 * 根据url抓取
	 * 
	 * @param url
	 * @return
	 */
	public static String crawlAsXml(String url) {
		Page page = crawl(url);
		if (page instanceof HtmlPage) {
			return ((HtmlPage) page).asXml();
		}
		throw new MyTvListException("my crawler is only to get html page.");
	}

	/**
	 * 使用htmlunit抓取网页
	 * 
	 * @param url
	 * @return
	 */
	protected static Page crawl(String url) {
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		try {
			logger.debug("begin to get page: " + Constant.EPG_URL);
			return webClient.getPage(Constant.EPG_URL);
		} catch (FailingHttpStatusCodeException e) {
			throw new MyTvListException("can't connect to " + Constant.EPG_URL,
					e);
		} catch (MalformedURLException e) {
			throw new MyTvListException("invalid url " + Constant.EPG_URL, e);
		} catch (IOException e) {
			throw new MyTvListException("error occur while connect to "
					+ Constant.EPG_URL, e);
		}
	}
}
