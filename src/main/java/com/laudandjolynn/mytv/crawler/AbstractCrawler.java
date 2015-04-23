package com.laudandjolynn.mytv.crawler;

import java.util.ArrayList;
import java.util.List;

import com.laudandjolynn.mytv.event.CrawlEventListener;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午12:16:12
 * @copyright: www.laudandjolynn.com
 */
public abstract class AbstractCrawler implements Crawler {
	protected List<CrawlEventListener> listeners = new ArrayList<CrawlEventListener>();

	@Override
	public void registerCrawlEventListener(CrawlEventListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeCrawlEventListener(CrawlEventListener listener) {
		this.listeners.remove(listener);
	}
}