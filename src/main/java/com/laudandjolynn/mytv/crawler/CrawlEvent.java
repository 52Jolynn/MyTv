package com.laudandjolynn.mytv.crawler;

import java.util.EventObject;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月22日 下午5:23:00
 * @copyright: www.laudandjolynn.com
 */
public class CrawlEvent extends EventObject {
	private static final long serialVersionUID = -5496065547903238418L;

	public CrawlEvent(Object source) {
		super(source);
	}

}
