package com.laudandjolynn.mytv.crawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午12:16:12
 * @copyright: www.laudandjolynn.com
 */
public abstract class AbstractCrawler implements Crawler {
	protected Parser parser = null;

	public AbstractCrawler(Parser parser) {
		this.parser = parser;
	}
}
