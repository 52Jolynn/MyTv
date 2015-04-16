package com.laudandjolynn.mytv.crawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午12:26:54
 * @copyright: www.laudandjolynn.com
 */
public interface CrawlerFactory {
	/**
	 * 创建抓取器
	 * 
	 * @return
	 */
	public Crawler createCrawler();
}
