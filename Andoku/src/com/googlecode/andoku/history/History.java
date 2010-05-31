/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2010  Markus Wiederkehr
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

package com.googlecode.andoku.history;

import java.util.ArrayList;
import java.util.List;

public class History {
	private CommandStack undoStack = new CommandStack();
	private CommandStack redoStack = new CommandStack();

	public void clear() {
		undoStack.clear();
		redoStack.clear();
	}

	public boolean execute(Command command) {
		if (!command.isEffective())
			return false;

		if (undoStack.isEmpty()) {
			undoStack.push(command);
		}
		else {
			Command last = undoStack.peek();
			Command merged = command.mergeWith(last);
			if (merged == null) {
				undoStack.push(command);
			}
			else {
				undoStack.pop();
				if (merged.isEffective())
					undoStack.push(merged);
			}
		}

		redoStack.clear();

		command.execute();
		return true;
	}

	public boolean canUndo() {
		return !undoStack.isEmpty();
	}

	public boolean undo() {
		if (undoStack.isEmpty()) {
			return false;
		}
		else {
			Command command = undoStack.pop();
			redoStack.push(command);

			command.undo();
			return true;
		}
	}

	public boolean canRedo() {
		return !redoStack.isEmpty();
	}

	public boolean redo() {
		if (redoStack.isEmpty()) {
			return false;
		}
		else {
			Command command = redoStack.pop();
			undoStack.push(command);

			command.redo();
			return true;
		}
	}

	private static class CommandStack {
		private List<Command> stack = new ArrayList<Command>();

		public void clear() {
			stack.clear();
		}

		public boolean isEmpty() {
			return stack.isEmpty();
		}

		public void push(Command command) {
			stack.add(command);
		}

		public Command peek() {
			return stack.get(stack.size() - 1);
		}

		public Command pop() {
			return stack.remove(stack.size() - 1);
		}
	}
}
