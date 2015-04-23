package com.laudandjolynn.mytv.event;

import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月23日 上午9:30:39
 * @copyright: www.laudandjolynn.com
 */
public class TvStationFoundEvent extends ItemFoundEvent<TvStation> {
	private static final long serialVersionUID = -4729651908093312009L;

	public TvStationFoundEvent(Object source, TvStation item) {
		super(source, item);
	}

}
