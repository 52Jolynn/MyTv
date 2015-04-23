package com.laudandjolynn.mytv.event;

import java.util.List;

import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月23日 下午1:12:01
 * @copyright: www.laudandjolynn.com
 */
public class AllTvStationCrawlEndEvent extends CrawlEndEvent<List<TvStation>> {
	private static final long serialVersionUID = -3789839197425037620L;

	public AllTvStationCrawlEndEvent(Object source, List<TvStation> returnValue) {
		super(source, returnValue);
	}

}
