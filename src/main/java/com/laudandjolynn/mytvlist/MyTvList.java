package com.laudandjolynn.mytvlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:12:56
 * @copyright: www.laudandjolynn.com
 */
public class MyTvList {
	private final static Logger logger = LoggerFactory
			.getLogger(MyTvList.class);

	public static void main(String[] args) {
		logger.info("start My TV Program Table crawler.");
		start();
	}

	private static void start() {
		Init.getIntance().init();
	}

}
