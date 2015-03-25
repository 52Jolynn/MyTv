package com.laudandjolynn.mytvlist;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午1:24:54
 * @copyright: www.laudandjolynn.com
 */
public class Utils {
	public static String today() {
		return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
	}
}
