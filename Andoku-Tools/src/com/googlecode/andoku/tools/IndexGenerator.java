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

package com.googlecode.andoku.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the index file that has to accompany a .adk level file.
 */
public class IndexGenerator {
	private static final String PUZZLES_DIR = "../Andoku/assets/puzzles";

	public static void main(String[] args) throws IOException {
		File[] listFiles = new File(PUZZLES_DIR).listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".adk");
			}
		});
		for (File file : listFiles) {
			String filename = file.getName();
			String idxFilename = filename.substring(0, filename.length() - 4) + ".idx";
			File idxFile = new File(file.getParentFile(), idxFilename);
			generateIndex(file, idxFile);
		}
	}

	private static void generateIndex(File file, File idxFile) throws IOException {
		List<Integer> offsets = scanOffsets(file);

		writeOffsets(idxFile, offsets);
	}

	private static List<Integer> scanOffsets(File file) throws IOException {
		System.out.println(file);

		InputStream in = new FileInputStream(file);
		Reader reader = new InputStreamReader(in, "us-ascii");
		BufferedReader br = new BufferedReader(reader);

		List<Integer> offsets = new ArrayList<Integer>();

		int offset = 0;
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			if (line.length() != 0 && !line.startsWith("#")) {
				// System.out.println(offset);
				offsets.add(offset);
			}

			offset += line.length();
			offset++; // \n
		}

		br.close();

		return offsets;
	}

	private static void writeOffsets(File idxFile, List<Integer> offsets) throws IOException {
		OutputStream out = new FileOutputStream(idxFile);
		DataOutputStream dout = new DataOutputStream(out);

		dout.writeInt(offsets.size());
		for (int offset : offsets) {
			dout.writeInt(offset);
		}

		dout.close();
	}
}
