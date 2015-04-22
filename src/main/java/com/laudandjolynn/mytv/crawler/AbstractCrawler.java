package com.laudandjolynn.mytv.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.MyTvUtils;
import com.laudandjolynn.mytv.utils.WebCrawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午12:16:12
 * @copyright: www.laudandjolynn.com
 */
public abstract class AbstractCrawler implements Crawler {
	protected Parser parser = null;
	private List<CrawlEventListener> listeners = new ArrayList<CrawlEventListener>();

	public AbstractCrawler(Parser parser) {
		this.parser = parser;
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		String epgFile = Constant.CRAWL_FILE_PATH + getCrawlerName()
				+ File.separator + getCrawlerName();
		File file = new File(epgFile);
		String html = null;
		if (file.exists()) {
			try {
				html = MyTvUtils.readAsHtml(epgFile);
				return parser.parseTvStation(html);
			} catch (IOException e) {
				// do nothing
			}
			return null;
		}
		Page page = WebCrawler.crawl(getUrl());
		if (page.isHtmlPage()) {
			HtmlPage htmlPage = (HtmlPage) page;
			html = htmlPage.asXml();
			List<TvStation> stationList = parser.parseTvStation(html);
			MyTvUtils.outputCrawlData(getCrawlerName(), html, getCrawlerName());
			return stationList;
		}

		return null;
	}

	@Override
	public void registerCrawlEventListener(CrawlEventListener listener) {
		this.listeners.add(listener);
	}
}