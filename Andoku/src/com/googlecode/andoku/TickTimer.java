/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009, 2010  Markus Wiederkehr
 *
 * This file is part of Andoku.
 *
 * Andoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Andoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TickTimer {
	private static final String TAG = TickTimer.class.getName();

	private static final int TICK_TIMER_MSG = 0;

	private boolean running = false;

	private long startTime;
	private long stoppedTime = 0;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case TICK_TIMER_MSG:
					long time = sendTick();
					int delay = 1000 - (int) (time % 1000) + 10;
					handler.sendMessageDelayed(handler.obtainMessage(TICK_TIMER_MSG), delay);
					break;

				default:
					super.handleMessage(msg);
			}
		}
	};

	final TickListener listener;

	public TickTimer(TickListener listener) {
		this.listener = listener;
	}

	public void reset() {
		if (Constants.LOG_V)
			Log.v(TAG, "reset()");

		running = false;
		stoppedTime = 0;

		stopTicking();
	}

	public void start() {
		if (Constants.LOG_V)
			Log.v(TAG, "start()");

		if (running)
			return;

		startTime = System.currentTimeMillis() - stoppedTime;
		running = true;

		startTicking();
	}

	public void stop() {
		if (Constants.LOG_V)
			Log.v(TAG, "stop()");

		if (!running)
			return;

		stoppedTime = System.currentTimeMillis() - startTime;
		running = false;

		stopTicking();
	}

	public boolean isRunning() {
		return running;
	}

	public long getTime() {
		if (running)
			return System.currentTimeMillis() - startTime;
		else
			return stoppedTime;
	}

	public void setTime(long time) {
		if (Constants.LOG_V)
			Log.v(TAG, "setTime(" + time + ")");

		if (running)
			startTime = System.currentTimeMillis() - time;
		else
			stoppedTime = time;

		sendTick();
	}

	@Override
	public String toString() {
		return (running ? "R:" : "S:") + getTime();
	}

	private void startTicking() {
		handler.removeMessages(TICK_TIMER_MSG);

		long time = sendTick();
		int delay = 1000 - (int) (time % 1000) + 10;
		handler.sendMessageDelayed(handler.obtainMessage(TICK_TIMER_MSG), delay);
	}

	private void stopTicking() {
		handler.removeMessages(TICK_TIMER_MSG);
		sendTick(); // one last tick to update current time
	}

	private long sendTick() {
		long time = getTime();
		listener.onTick(time);
		return time;
	}
}
