package com.koboflo;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

import java.io.File;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * This Class is part of KoboFlo.
 * 
 * It adds an About Dialog to the given context.
 * 
 * @author Florian Hauser Copyright (C) 2014
 * 
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 * 
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 * 
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class FtpSync extends AsyncTask<String, Integer, ArrayList<Message>> {
	private String koboIp;
	private String userDir;
	
	public FtpSync(String koboIp, String userDir) {
		this.koboIp = koboIp;
		this.userDir = userDir;
	}
	
	@Override
	protected ArrayList<Message> doInBackground(String... params) {
		ArrayList<Message> res = new ArrayList<Message>();
		FTPClient client = new FTPClient();
		File localDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + userDir);
		if (!localDir.exists()) {
			localDir.mkdirs();
		}
		Log.e("Check", Environment.getExternalStorageDirectory().getAbsolutePath());
		File[] localList = localDir.listFiles();
		try {
			client.connect(koboIp);
			client.login("", "");
			res.add(new Message("", R.string.message_connected, "info"));
			client.changeDirectory("XCSoarData/logs");
			FTPFile[] list = client.list();
			for (FTPFile file : list) {
				if (isInList(file.getName(), localList)) {
					res.add(new Message(file.getName(), R.string.message_not_copied, "left_yell"));
				} else {
					File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + userDir + "/" + file.getName());
					newFile.createNewFile();
					client.download(file.getName(), newFile);
					res.add(new Message(file.getName(), R.string.message_copied, "left_green"));
				}
			}
			client.disconnect(true);
			res.add(new Message("", R.string.message_syncing_completed, "info"));
		} catch (Exception e) {
			res.add(new Message("Error: " + e.toString(), 0, "error"));
		}
		return res;
	}
	
	public static boolean isInList(String name, File[] list) {
		for (File file : list) {
			if (name.equals(file.getName()))
				return true;
		}
		return false;
	}


}
