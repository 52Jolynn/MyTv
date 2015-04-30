package com.laudandjolynn.mytv.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.model.Proxy;
import com.laudandjolynn.mytv.utils.Config;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.NetworkUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月30日 下午1:43:01
 * @copyright: www.laudandjolynn.com
 */
public class ConfigProxy implements ProxyProvider {
	private final static Logger logger = LoggerFactory
			.getLogger(ConfigProxy.class);

	@Override
	public List<Proxy> getProxies() {
		List<Proxy> proxies = new ArrayList<Proxy>();
		ResourceBundle bundle = ResourceBundle.getBundle(Config
				.getConfigFileName());
		if (bundle.containsKey(Config.getResKeyConfigProxies())) {
			String value = bundle.getString(Config.getResKeyConfigProxies());
			if (!StringUtils.isEmpty(value)) {
				String[] values = value.split(Constant.COMMA);
				for (int i = 0, length = values == null ? 0 : values.length; i < length; i++) {
					String[] o = values[i].split(Constant.COLON);
					String ip = o[0];
					int port = Integer.valueOf(o[1]);
					Proxy proxy = new Proxy(ip, port);
					if (NetworkUtils.isReachable(ip, port, 10000)) {
						proxies.add(proxy);
						logger.info("find a proxy: " + proxy);
					} else {
						logger.info("proxy can't reachable. " + proxy);
					}
				}
			}
		}
		return proxies;
	}

}
