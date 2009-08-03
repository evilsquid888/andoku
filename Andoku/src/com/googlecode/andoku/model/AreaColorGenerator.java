/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku.model;

import java.util.HashSet;

public class AreaColorGenerator {
	private final Puzzle puzzle;

	public AreaColorGenerator(Puzzle puzzle) {
		this.puzzle = puzzle;
	}

	public int[][] generate() {
		Node[] graph = buildGraph();
		paintGraph(graph);
		return buildAreaColors(graph);
	}

	private Node[] buildGraph() {
		final int size = puzzle.getSize();

		Node[] nodes = new Node[size];
		for (int i = 0; i < size; i++)
			nodes[i] = new Node();

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				int areaCode = puzzle.getAreaCode(row, col);
				if (row > 0)
					link(nodes, areaCode, puzzle.getAreaCode(row - 1, col));
				if (row < size - 1)
					link(nodes, areaCode, puzzle.getAreaCode(row + 1, col));
				if (col > 0)
					link(nodes, areaCode, puzzle.getAreaCode(row, col - 1));
				if (col < size - 1)
					link(nodes, areaCode, puzzle.getAreaCode(row, col + 1));
			}
		}

		return nodes;
	}

	// TODO: could be a lot more sophisticated
	private void paintGraph(Node[] graph) {
		for (int i = 0; i < graph.length; i++) {
			graph[i].color = findFreeColor(graph[i]);
		}
	}

	private int findFreeColor(Node node) {
		HashSet<Integer> occupied = new HashSet<Integer>();
		for (Node neighbor : node.neighbors) {
			if (neighbor.color != -1)
				occupied.add(neighbor.color);
		}

		for (int color = 0;; color++) {
			if (!occupied.contains(color))
				return color;
		}
	}

	private int[][] buildAreaColors(Node[] graph) {
		final int size = puzzle.getSize();

		int[][] areaColors = new int[size][size];
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				int areaCode = puzzle.getAreaCode(row, col);
				int color = graph[areaCode].color;
				areaColors[row][col] = color;
			}
		}

		return areaColors;
	}

	private void link(Node[] nodes, int areaCode1, int areaCode2) {
		if (areaCode1 == areaCode2)
			return;

		Node node1 = nodes[areaCode1];
		Node node2 = nodes[areaCode2];
		node1.neighbors.add(node2);
		node2.neighbors.add(node1);
	}

	private static final class Node {
		public HashSet<Node> neighbors = new HashSet<Node>();
		public int color = -1;
	}
}
