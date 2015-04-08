package com.laudandjolynn.mytv.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月8日 上午11:01:22
 * @copyright: www.laudandjolynn.com
 */
public class JolynnTvRmiImpl extends UnicastRemoteObject implements JolynnTv {
	private static final long serialVersionUID = -9023130568555215531L;
	private transient JolynnTv jolynnTv = null;

	public JolynnTvRmiImpl(JolynnTv jolynnTv) throws RemoteException {
		this.jolynnTv = jolynnTv;
	}

	@Override
	public String getTvStationClassify() throws RemoteException {
		return jolynnTv.getTvStationClassify();
	}

	@Override
	public String getAllTvStation() throws RemoteException {
		return jolynnTv.getAllTvStation();
	}

	@Override
	public String getProgramTable(String name, String date)
			throws RemoteException {
		return jolynnTv.getProgramTable(name, date);
	}

}
