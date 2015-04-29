package com.laudandjolynn.mytv.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.model.Proxy;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.NetworkUtils;
import com.laudandjolynn.mytv.utils.WebCrawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月29日 下午4:08:15
 * @copyright: www.laudandjolynn.com
 */
public class XiciNetProxy implements ProxyProvider {
	private final static String XICINE_TURL = "http://www.xici.net.co/";
	private final static Logger logger = LoggerFactory
			.getLogger(XiciNetProxy.class);
	private final static String CN_HIGH_ANONYMOUS = "国内高匿代理IP";

	@Override
	public List<Proxy> getProxies() {
		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(XICINE_TURL);
		Document doc = Jsoup.parse(htmlPage.asXml());
		Elements elements = doc.select("table#ip_list tr");
		List<Proxy> proxies = new ArrayList<Proxy>();
		String today = DateUtils.date2String(new Date());
		boolean cnHighAnonymous = true;
		for (int i = 0, size = elements == null ? 0 : elements.size(); i < size; i++) {
			Element element = elements.get(i);

			if (element.hasClass("subtitle")) {
				continue;
			}

			Elements h2 = element.select("h2");
			if (element.children().size() != 7 && h2 != null
					&& !CN_HIGH_ANONYMOUS.equals(h2.text().trim())) {
				cnHighAnonymous = false;
			}
			if (!cnHighAnonymous) {
				break;
			}

			if (element.children().size() != 7) {
				continue;
			}

			String ip = element.child(1).text().trim();
			int port = Integer.valueOf(element.child(2).text().trim());
			String pos = element.child(3).text().trim();
			String anonymous = element.child(4).text().trim();
			String type = element.child(5).text().trim();
			String update = today + " " + element.child(6).text().trim();
			if (MyTvProxyManager.PROXY_ANONYMOUS_HIGH.equals(anonymous)
					&& NetworkUtils.isReachable(ip, port, 1000)) {
				Proxy proxy = new Proxy(ip, port);
				proxy.setAnonymous(anonymous);
				proxy.setLastCheck(update);
				proxy.setPos(pos);
				proxy.setSock(MyTvProxyManager.PROXY_TYPE_SOCK
						.equalsIgnoreCase(type));
				logger.info("find a proxy: " + proxy);
				proxies.add(proxy);
			} else {
				logger.info("proxy: " + ip + ":" + port + " can't reacheable.");
			}
		}
		return proxies;
	}
}
