package com.laudandjolynn.mytv.crawler.tvmao;

import java.util.List;

import com.gargoylesoftware.htmlunit.Page;
import com.laudandjolynn.mytv.crawler.AbstractCrawler;
import com.laudandjolynn.mytv.crawler.Parser;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.WebCrawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午3:32:56
 * @copyright: www.laudandjolynn.com
 */
class TvMaoCrawler extends AbstractCrawler {
	// tvmao节目表地址
	private final static String TV_MAO_URL = "http://www.tvmao.com/program/channels";
	private final static String TV_MAO_NAME = "tvmao";

	public TvMaoCrawler(Parser parser) {
		super(parser);
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		Page page = WebCrawler.crawl(TV_MAO_URL);
		if (page.isHtmlPage()) {

		}
		return null;
	}

	@Override
	public String getCrawlerName() {
		return TV_MAO_NAME;
	}

	@Override
	public List<ProgramTable> crawlAllProgramTable(String date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(TvStation station, String date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(TvStation station) {
		// TODO Auto-generated method stub
		return false;
	}
}
