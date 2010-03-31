/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
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

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayErrorActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Util.setFullscreenMode(this);

		setContentView(R.layout.error);

		TextView titleView = (TextView) findViewById(R.id.errorTitle);
		TextView messageView = (TextView) findViewById(R.id.errorMessage);

		setTitleAndMessage(titleView, messageView);
	}

	private void setTitleAndMessage(TextView titleView, TextView messageView) {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		String title = extras.getString(Constants.EXTRA_ERROR_TITLE);
		titleView.setText(title);

		String message = extras.getString(Constants.EXTRA_ERROR_MESSAGE);
		Throwable throwable = (Throwable) extras.getSerializable(Constants.EXTRA_ERROR_THROWABLE);
		if (throwable != null) {
			StringWriter stringWriter = new StringWriter();
			throwable.printStackTrace(new PrintWriter(stringWriter));
			String trace = stringWriter.toString();

			message = message + "\n\n" + getResources().getString(R.string.error_message_stack_trace)
					+ "\n" + trace;
		}

		messageView.setText(message);
	}
}
