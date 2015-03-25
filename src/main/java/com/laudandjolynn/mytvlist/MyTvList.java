package com.laudandjolynn.mytvlist;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

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
		initTvList();
	}

	private static void initTvList() {
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		Page page = null;
		try {
			page = webClient.getPage(Constant.EPG_URL);
		} catch (FailingHttpStatusCodeException e) {
			throw new MyTvListException("can't connect to " + Constant.EPG_URL,
					e);
		} catch (MalformedURLException e) {
			throw new MyTvListException("invalid url " + Constant.EPG_URL, e);
		} catch (IOException e) {
			throw new MyTvListException("error occur while connect to "
					+ Constant.EPG_URL, e);
		}
		if (page instanceof HtmlPage) {
			HtmlPage htmlPage = (HtmlPage) page;
			String dir = Constant.PROGRAM_TABLE_FILE_PATH;
			File dirFile = new File(dir);
			if (!dirFile.exists()) {
				dirFile.mkdir();
			}

			String fileName = dir + Utils.today();
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
			try {
				FileUtils.writeWithNIO(htmlPage.asXml(), "UTF-8", fileName);
			} catch (Exception e) {
				logger.error("fail to save debug file ", e);
			}
			List<?> tvList = htmlPage
					.getByXPath("//ul[@class='weishi']/li/a/text()");
			for (int i = 0, size = tvList == null ? 0 : tvList.size(); i < size; i++) {
				Object tv = tvList.get(i);

			}
		}

	}
}
