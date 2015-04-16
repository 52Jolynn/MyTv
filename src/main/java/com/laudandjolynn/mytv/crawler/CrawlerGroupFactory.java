package com.laudandjolynn.mytv.crawler;

import com.laudandjolynn.mytv.crawler.epg.EpgCrawlerFactory;
import com.laudandjolynn.mytv.crawler.tvmao.TvMaoCrawlerFactory;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午2:57:53
 * @copyright: www.laudandjolynn.com
 */
public class CrawlerGroupFactory implements CrawlerFactory {
	@Override
	public Crawler createCrawler() {
		CrawlerGroup cralwerGroup = new CrawlerGroup();
		cralwerGroup.addCrawler(new EpgCrawlerFactory().createCrawler());
		cralwerGroup.addCrawler(new TvMaoCrawlerFactory().createCrawler());
		return cralwerGroup;
	}

}
