/*******************************************************************************
 * Copyright 2015 htd0324@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.laudandjolynn.mytv.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.model.Proxy;
import com.laudandjolynn.mytv.utils.Constant;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月28日 下午5:47:56
 * @copyright: www.laudandjolynn.com
 */
public class MyTvProxyManager {
	private final static Logger logger = LoggerFactory
			.getLogger(MyTvProxyManager.class);
	public final static String PROXY_ANONYMOUS_HIGH = "高匿";
	public final static String PROXY_TYPE_SOCK = "scok5";
	private final static String LOCALHOST = "localhost";
	private final ConcurrentLinkedQueue<Proxy> PROXY_QUEUE = new ConcurrentLinkedQueue<Proxy>();
	private final static Proxy LOCALHOST_PROXY = new Proxy(LOCALHOST, 80);
	private List<ProxyProvider> providerList = new ArrayList<ProxyProvider>();

	private MyTvProxyManager() {
	}

	private final static class MyTvProxySingletonHolder {
		private final static MyTvProxyManager MY_TV_PROXY = new MyTvProxyManager();
	}

	public static MyTvProxyManager getInstance() {
		return MyTvProxySingletonHolder.MY_TV_PROXY;
	}

	public void prepareProxies(ProxyProvider... providers) {
		int length = providers == null ? 0 : providers.length;
		if (length > 0) {
			int maxThreadNum = Constant.CPU_PROCESSOR_NUM;
			ThreadFactory threadFactory = new BasicThreadFactory.Builder()
					.namingPattern("MyTv_Find_Proxies_%d").build();
			ExecutorService executorService = Executors.newFixedThreadPool(
					length > maxThreadNum ? maxThreadNum : length,
					threadFactory);
			CompletionService<List<Proxy>> completionService = new ExecutorCompletionService<List<Proxy>>(
					executorService);
			providerList.clear();
			for (int i = 0; i < length; i++) {
				final ProxyProvider provider = providers[i];
				providerList.add(provider);
				completionService.submit(new Callable<List<Proxy>>() {

					@Override
					public List<Proxy> call() throws Exception {
						return provider.getProxies();
					}
				});
			}
			executorService.shutdown();

			int count = 0;
			List<Proxy> resultList = new ArrayList<Proxy>();
			while (count < length) {
				try {
					Future<List<Proxy>> future = completionService.take();
					List<Proxy> proxies = future.get();
					if (proxies != null) {
						resultList.addAll(proxies);
					}
				} catch (InterruptedException e) {
					logger.error("get proxies thread has interrupted.", e);
				} catch (ExecutionException e) {
					logger.error("get proxies thread has execution fail.", e);
				}
				count++;
			}
			resultList.add(LOCALHOST_PROXY);
			PROXY_QUEUE.clear();
			PROXY_QUEUE.addAll(resultList);
		}
	}

	/**
	 * 刷新代理服务器
	 */
	public void refresh() {
		int size = providerList.size();
		ProxyProvider[] providers = new ProxyProvider[size];
		prepareProxies(providerList.toArray(providers));
	}

	public int getProxySize() {
		return PROXY_QUEUE.size();
	}

	public Proxy pickProxy() {
		Proxy proxy = PROXY_QUEUE.poll();
		if (proxy == null) {
			return null;
		}
		PROXY_QUEUE.offer(proxy);
		if (LOCALHOST.equals(proxy.getIp())) {
			return null;
		}
		return proxy;
	}
}
