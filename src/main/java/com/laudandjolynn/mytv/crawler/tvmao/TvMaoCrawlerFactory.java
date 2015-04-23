package com.laudandjolynn.mytv.crawler.tvmao;

import com.laudandjolynn.mytv.crawler.Crawler;
import com.laudandjolynn.mytv.crawler.CrawlerFactory;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午12:29:59
 * @copyright: www.laudandjolynn.com
 */
public class TvMaoCrawlerFactory implements CrawlerFactory {
	@Override
	public Crawler createCrawler() {
		return new TvMaoCrawler();
	}
}
