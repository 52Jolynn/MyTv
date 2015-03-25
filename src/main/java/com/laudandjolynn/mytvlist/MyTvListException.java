package com.laudandjolynn.mytvlist;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:18:03
 * @copyright: www.laudandjolynn.com
 */
public class MyTvListException extends RuntimeException {
	private static final long serialVersionUID = -2699920699817552410L;

	public MyTvListException() {
		super();
	}

	public MyTvListException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyTvListException(String message) {
		super(message);
	}

	public MyTvListException(Throwable cause) {
		super(cause);
	}

}
