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
package com.laudandjolynn.mytv;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.service.JolynnTv;
import com.laudandjolynn.mytv.service.JolynnTvImpl;
import com.laudandjolynn.mytv.service.JolynnTvRmiImpl;
import com.laudandjolynn.mytv.utils.Config;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月8日 上午10:00:03
 * @copyright: www.laudandjolynn.com
 */
public class RmiServer implements Server {
	private final static Logger logger = LoggerFactory
			.getLogger(RmiServer.class);
	private final static String url = "rmi://" + Config.NET_CONFIG.getIp()
			+ ":" + Config.NET_CONFIG.getRmiPort() + "/epg";

	@Override
	public void start() {
		logger.info("rmi bind to: " + url);
		try {
			JolynnTv jolynnTv = new JolynnTvRmiImpl(new JolynnTvImpl());
			StaticIpSocketFactory factory = new StaticIpSocketFactory();
			LocateRegistry.createRegistry(Config.NET_CONFIG.getRmiPort(),
					factory, factory);
			Naming.bind(url, jolynnTv);
		} catch (RemoteException e) {
			throw new MyTvException("error occur while start RMI server.", e);
		} catch (MalformedURLException e) {
			throw new MyTvException("invalid url: " + url, e);
		} catch (AlreadyBoundException e) {
			throw new MyTvException(url + " is already bind.", e);
		}
		logger.info("RMI server started. " + url);
	}

	private final static class StaticIpSocketFactory implements
			RMIServerSocketFactory, RMIClientSocketFactory {

		@Override
		public ServerSocket createServerSocket(int port) throws IOException {
			return new ServerSocket(Config.NET_CONFIG.getRmiPort(), 0,
					InetAddress.getByName(Config.NET_CONFIG.getIp()));
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException {
			return new Socket(Config.NET_CONFIG.getIp(),
					Config.NET_CONFIG.getRmiPort());
		}
	}
}
