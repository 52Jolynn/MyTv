package com.laudandjolynn.mytv.crawler;

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

}