package com.laudandjolynn.mytv.event;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月22日 下午5:22:20
 * @copyright: www.laudandjolynn.com
 */
public interface CrawlEventListener {
	/**
	 * 抓取到电视台、电视节目对象的事件通知
	 * 
	 * @param event
	 */
	public void itemFound(CrawlEvent event);

	/**
	 * 抓取结束，即将返回结果
	 * 
	 * @param event
	 */
	public void crawlEnd(CrawlEvent event);
}
