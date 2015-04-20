package com.laudandjolynn.mytv.crawler.tvmao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.dom4j.DocumentException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBold;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.crawler.AbstractCrawler;
import com.laudandjolynn.mytv.crawler.Parser;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.MyTvUtils;
import com.laudandjolynn.mytv.utils.WebCrawler;

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
		String tvMaoFile = Constant.CRAWL_FILE_PATH + getCrawlerName();
		File file = new File(tvMaoFile);
		ExecutorService executorService = Executors
				.newFixedThreadPool(Constant.CPU_PROCESSOR_NUM * 2);
		CompletionService<List<TvStation>> stationCompletionService = new ExecutorCompletionService<List<TvStation>>(
				executorService);
		int size = 0;
		List<TvStation> resultList = new ArrayList<TvStation>();
		if (file.exists() && file.listFiles().length > 0) {
			File[] files = file.listFiles();
			size = files == null ? 0 : files.length;
			for (int i = 0; i < size; i++) {
				final File f = files[i];
				Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
					@Override
					public List<TvStation> call() throws Exception {
						try {
							String html = MyTvUtils.readAsXml(f);
							return parser.parseTvStation(html);
						} catch (DocumentException e) {
							logger.error("read as xml error: " + f.getPath(), e);
						}
						return null;
					}
				};
				stationCompletionService.submit(task);
			}
		} else {
			Page page = WebCrawler.crawl(getUrl());
			if (page.isHtmlPage()) {
				final HtmlPage htmlPage = (HtmlPage) page;
				List<?> elements = htmlPage
						.getByXPath("//div[@class='pgnav_wrap']/table[@class='pgnav']//a");
				int ssize = size = elements == null ? 0 : elements.size();
				for (int i = 0; i < ssize; i++) {
					final HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
					if (!anchor.getAttribute("href").startsWith("/program/")) {
						size--;
						continue;
					}
					if ("CCTV".equals(anchor.getTextContent())) {
						Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
							@Override
							public List<TvStation> call() throws Exception {
								List<TvStation> stationList = new ArrayList<TvStation>();
								stationList.addAll(parse(htmlPage));
								stationList.addAll(findAllAndParse(htmlPage));
								return stationList;
							}
						};
						stationCompletionService.submit(task);
					} else {
						Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
							@Override
							public List<TvStation> call() throws Exception {
								try {
									HtmlPage hp = anchor.click();
									List<TvStation> stationList = new ArrayList<TvStation>();
									stationList.addAll(parse(hp));
									stationList.addAll(findAllAndParse(hp));
									return stationList;
								} catch (IOException e) {
									logger.error("error occur while click on: "
											+ anchor.getTextContent(), e);
								}
								return null;
							}
						};
						stationCompletionService.submit(task);
					}
				}
			}

		}
		int count = 0;
		while (count < size) {
			try {
				Future<List<TvStation>> future = stationCompletionService.poll(
						5, TimeUnit.MINUTES);
				if (future != null) {
					List<TvStation> stationList = future.get();
					if (stationList != null) {
						resultList.addAll(stationList);
					}
				}
			} catch (InterruptedException e) {
				logger.error("crawl task of all station was interrupted.", e);
			} catch (ExecutionException e) {
				logger.error(
						"error occur while execute crawl task of all station.",
						e);
			}
			count++;
		}
		executorService.shutdown();
		return resultList;
	}

	private List<TvStation> findAllAndParse(HtmlPage htmlPage) {
		List<TvStation> resultList = new ArrayList<TvStation>();
		List<?> elements = htmlPage
				.getByXPath("//div[@class='chlsnav']//div[@class='plst']/parent::*");
		for (int i = 0, size = elements == null ? 0 : elements.size(); i < size; i++) {
			HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
			try {
				HtmlPage p = anchor.click();
				resultList.addAll(parse(p));
			} catch (IOException e) {
				logger.error("error occur while " + anchor.getTextContent()
						+ " on click. ", e);
				continue;
			}
		}
		return resultList;
	}

	private List<TvStation> parse(HtmlPage htmlPage) {
		String html = htmlPage.asXml();
		List<?> elements = htmlPage
				.getByXPath("//div[@class='chlsnav']/div[@class='pbar']/b");
		HtmlBold hb = (HtmlBold) elements.get(0);
		String classify = hb.getTextContent();
		List<TvStation> stationList = parser.parseTvStation(html);
		MyTvUtils.outputCrawlData(getCrawlerName(), html, getCrawlerName()
				+ Constant.UNDERLINE + classify);
		return stationList;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(String date, TvStation station) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(TvStation station) {
		String tvMaoFile = Constant.CRAWL_FILE_PATH + getCrawlerName();
		File file = new File(tvMaoFile);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (File f : files) {
				String html;
				try {
					html = MyTvUtils.readAsXml(f);
				} catch (DocumentException e) {
					continue;
				}
				Document doc = Jsoup.parse(html);
				Elements classifyElements = doc
						.select("div.chlsnav div.pbar b");
				String classify = classifyElements.get(0).text().trim();
				Elements channelElements = doc.select("div.chlsnav ul.r li");
				for (Element element : channelElements) {
					Element channel = element.child(0);
					String displayName = channel.text().trim();
					if (classify == null) {
						if (station.getClassify() != null) {
							continue;
						}
					} else if (displayName.equals(station.getName())) {
						return true;
					}
				}
			}
		}

		Page page = WebCrawler.crawl(TV_MAO_URL);
		if (page.isHtmlPage()) {
			HtmlPage htmlPage = (HtmlPage) page;
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		new TvMaoCrawler(new TvMaoParser()).crawlAllTvStation();
	}
}
