package com.laudandjolynn.mytv.crawler.tvmao;

import java.util.List;

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
public class TvMaoCrawler extends AbstractCrawler {
	// tvmao节目表地址
	public final static String TV_MAO_URL = "http://www.tvmao.com/program/channels";

	public TvMaoCrawler(Parser parser) {
		super(parser);
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProgramTable> crawlAllProgramTable(String date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(String stationName, String date) {
		// TODO Auto-generated method stub
		return null;
	}

}
