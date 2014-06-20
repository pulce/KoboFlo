package com.koboflo;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

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
public class MainActivity extends Activity {

	private static final String VERSIONID = "0.1";
	private boolean originalWifiState = false;
	private WifiManager wifi;
	private Method[] wmMethods;
	public static final int USER_PREF = 1;
	private static String koboIp;
	private static String userDir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wmMethods = wifi.getClass().getDeclaredMethods();
		printInfoRow(getString(R.string.welcome_string));

		final ImageButton wifiButton = (ImageButton) findViewById(R.id.wifiImageButton);
		if (wifiCheck()) {
			wifiButton.setImageResource(R.drawable.ic_action_network_wifi);
		} else {
			wifiButton.setImageResource(R.drawable.ic_action_network_wifi_off);
		}
		wifiButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (wifiCheck()) {
					wifiTurn(false);
					wifiButton
							.setImageResource(R.drawable.ic_action_network_wifi_off);

				} else {
					wifiTurn(true);
					wifiButton
							.setImageResource(R.drawable.ic_action_network_wifi);
				}
			}
		});

		final ImageButton syncButton = (ImageButton) findViewById(R.id.syncImageButton);
		syncButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (wifiCheck()) {
					ftpSync();
				} else {
					printErrorRow(getString(R.string.message_no_hotspot));
				}
			}
		});
	}

	public boolean wifiCheck() {
		boolean isWifiAPenabled = false;
		for (Method method : wmMethods) {
			if (method.getName().equals("isWifiApEnabled")) {
				try {
					isWifiAPenabled = (Boolean) method.invoke(wifi);
				} catch (Exception e) {
					printErrorRow(e.toString());
				}
			}
		}
		return isWifiAPenabled;
	}

	public void wifiTurn(boolean on) {
		if (on) {
			originalWifiState = wifi.isWifiEnabled();
			if (wifi.isWifiEnabled()) {
				wifi.setWifiEnabled(false);
				printInfoRow(getString(R.string.message_wifi_turned_off));
			}
		}

		for (Method method : wmMethods) {
			if (method.getName().equals("setWifiApEnabled")) {
				try {
					method.invoke(wifi, null, on);
					printInfoRow(getString(R.string.message_hotspot)
							+ (on ? getString(R.string.message_on)
									: getString(R.string.message_off)));
				} catch (Exception e) {
					printErrorRow(e.toString());
					;
				}
			}
		}

		if (!on) {
			if (originalWifiState) {
				wifi.setWifiEnabled(true);
				printInfoRow(getString(R.string.message_wifi_on));
			}
		}
	}

	public void ftpSync() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		koboIp = sharedPrefs.getString("kobo_ip", "192.168.43.247");
		userDir = sharedPrefs.getString("user_dir",
				getString(R.string.home_dir));
		AsyncTask<String, Integer, ArrayList<Message>> ftpS = new FtpSync(
				koboIp, userDir).execute("");
		try {
			ArrayList<Message> res = ftpS.get();
			for (Message result : res) {
				String mess = result.getMessage() == 0 ? result.getInit()
						: result.getInit() + getString(result.getMessage());
				printTableRow(mess, result.getIcon());
			}
		} catch (Exception e) {
			printErrorRow(e.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent userPref = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivityForResult(userPref, USER_PREF);

			break;
		case R.id.action_about:
			AboutDialog.makeDialog(this, VERSIONID);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case USER_PREF:
			Intent mainAct = new Intent(getApplicationContext(),
					MainActivity.class);
			startActivity(mainAct);
		}
	}

	public void printErrorRow(String line) {
		printTableRow(line, "error");
	}

	public void printInfoRow(String line) {
		printTableRow(line, "info");
	}

	public void printTableRow(String line, String image) {
		TableLayout tl = (TableLayout) findViewById(R.id.tableLayout);
		tl.setColumnShrinkable(0, false);
		tl.setColumnShrinkable(1, true);
		tl.setColumnStretchable(0, false);
		tl.setColumnStretchable(1, true);

		TableRow tr = new TableRow(this);
		TableRow.LayoutParams params = new TableRow.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.FILL_HORIZONTAL;
		tr.setLayoutParams(params);
		if (image != null) {
			int resID = getResources().getIdentifier(image, "drawable",
					"com.koboflo");
			ImageView img = new ImageView(this);
			img.setImageResource(resID);
			TableRow.LayoutParams para = new TableRow.LayoutParams(
					TableRow.LayoutParams.WRAP_CONTENT,
					TableRow.LayoutParams.WRAP_CONTENT);
			para.gravity = Gravity.LEFT;
			para.gravity = Gravity.CENTER_VERTICAL;
			para.height = 30;
			img.setLayoutParams(para);
			tr.addView(img, 0);
		}
		TextView txv = new TextView(this);
		txv.setLayoutParams(new TableRow.LayoutParams(
				TableRow.LayoutParams.WRAP_CONTENT,
				TableRow.LayoutParams.WRAP_CONTENT));
		txv.setText(line);
		txv.setGravity(Gravity.LEFT);
		tr.addView(txv, 1);
		tl.addView(tr);
		final ScrollView scv = (ScrollView) findViewById(R.id.scrollView1);
		scv.post(new Runnable() {
	        @Override
	        public void run() {
	            scv.fullScroll(ScrollView.FOCUS_DOWN);
	        }
	    });

	}

}
