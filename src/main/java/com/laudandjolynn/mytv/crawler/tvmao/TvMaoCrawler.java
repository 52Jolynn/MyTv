package com.laudandjolynn.mytv.crawler.tvmao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.crawler.AbstractCrawler;
import com.laudandjolynn.mytv.crawler.Parser;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午3:32:56
 * @copyright: www.laudandjolynn.com
 */
class TvMaoCrawler extends AbstractCrawler {
	private final static Logger logger = LoggerFactory
			.getLogger(TvMaoCrawler.class);
	// tvmao节目表地址
	private final static String TV_MAO_URL = "http://www.tvmao.com/program/channels";
	private final static String TV_MAO_NAME = "tvmao";

	public TvMaoCrawler(Parser parser) {
		super(parser);
	}

	@Override
	public String getCrawlerName() {
		return TV_MAO_NAME;
	}

	@Override
	public String getUrl() {
		return TV_MAO_URL;
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(String date, TvStation station) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(TvStation station) {
		// TODO Auto-generated method stub
		return false;
	}
}
