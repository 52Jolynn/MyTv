package com.laudandjolynn.mytv.crawler;

import java.io.File;
import java.util.List;

import org.dom4j.DocumentException;

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
	protected String crawlerName = null;

	public AbstractCrawler(Parser parser) {
		this.parser = parser;
	}

	public AbstractCrawler(Parser parser, String crawlerName) {
		super();
		this.parser = parser;
		this.crawlerName = crawlerName;
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		String epgFile = Constant.CRAWL_FILE_PATH + getCrawlerName()
				+ File.separator + getCrawlerName();
		File file = new File(epgFile);
		String html = null;
		if (file.exists()) {
			try {
				html = MyTvUtils.readAsXml(epgFile);
				return parser.parseTvStation(html);
			} catch (DocumentException e) {
				// do nothing
			}
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
}