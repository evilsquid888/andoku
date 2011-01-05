package com.googlecode.andoku.source;

import java.io.IOException;

public class AssetsPuzzleSourceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	AssetsPuzzleSourceException(IOException cause) {
		super(cause);
	}
}
