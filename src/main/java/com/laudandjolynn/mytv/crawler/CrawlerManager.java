package com.laudandjolynn.mytv.crawler;

import java.util.Iterator;

import com.laudandjolynn.mytv.event.CrawlEventListener;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午3:01:20
 * @copyright: www.laudandjolynn.com
 */
public class CrawlerManager {
	private Crawler crawler = new MyTvCrawlerFactory().createCrawler();

	private CrawlerManager() {
	}

	public static CrawlerManager getInstance() {
		return CrawlerManagerSingleton.CRAWLER_MANAGER;
	}

	private final static class CrawlerManagerSingleton {
		private final static CrawlerManager CRAWLER_MANAGER = new CrawlerManager();
	}

	/**
	 * 添加抓取器
	 * 
	 * @param crawler
	 */
	public void addCrawler(Crawler crawler) {
		((CrawlerGroup) this.crawler).addCrawler(crawler);
	}

	/**
	 * 移除抓取器
	 * 
	 * @param crawler
	 */
	public void removeCrawler(Crawler crawler) {
		((CrawlerGroup) this.crawler).removeCrawler(crawler);
	}

	/**
	 * 添加抓取事件监听器
	 * 
	 * @param listener
	 */
	public void addCrawlEventListener(CrawlEventListener listener) {
		this.crawler.registerCrawlEventListener(listener);
	}

	/**
	 * 删除抓取事件监听器
	 * 
	 * @param listener
	 */
	public void removeCrawlEventListener(CrawlEventListener listener) {
		this.crawler.removeCrawlEventListener(listener);
	}

	/**
	 * 根据抓取器名称移除抓取器
	 * 
	 * @param crawlerName
	 */
	public void removeCrawler(String crawlerName) {
		CrawlerGroup cg = (CrawlerGroup) this.crawler;
		Iterator<Crawler> iterator = cg.getCrawlers().iterator();
		while (iterator.hasNext()) {
			Crawler crawler = iterator.next();
			if (crawler.getCrawlerName().equals(crawlerName)) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * 获取抓取器对象
	 * 
	 * @return
	 */
	public Crawler getCrawler() {
		return crawler;
	}

}
