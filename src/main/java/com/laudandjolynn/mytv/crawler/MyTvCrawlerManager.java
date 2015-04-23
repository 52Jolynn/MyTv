package com.laudandjolynn.mytv.crawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午3:01:20
 * @copyright: www.laudandjolynn.com
 */
public class MyTvCrawlerManager {
	private CrawlerFactory crawlerFactory = new MyTvCrawlerFactory();

	private MyTvCrawlerManager() {
	}

	/**
	 * 获取抓取器管理器实例，单例
	 * 
	 * @return
	 */
	public static MyTvCrawlerManager getInstance() {
		return CrawlerManagerSingleton.CRAWLER_MANAGER;
	}

	private final static class CrawlerManagerSingleton {
		private final static MyTvCrawlerManager CRAWLER_MANAGER = new MyTvCrawlerManager();
	}

	/**
	 * 设置抓取器生成工厂
	 * 
	 * @param crawlerFactory
	 */
	public void setCrawlerFactory(CrawlerFactory crawlerFactory) {
		this.crawlerFactory = crawlerFactory;
	}

	/**
	 * 创建新的抓取器
	 * 
	 * @return
	 */
	public Crawler newCrawler() {
		return this.crawlerFactory.createCrawler();
	}

}
