package com.googlecode.andoku;

class GameStatistics {
	public final int numGamesSolved;
	public final long sumTime;
	public final long minTime;

	public GameStatistics(int numGamesSolved, long sumTime, long minTime) {
		this.numGamesSolved = numGamesSolved;
		this.sumTime = sumTime;
		this.minTime = minTime;
	}

	public long getAverageTime() {
		return sumTime / numGamesSolved;
	}
}
