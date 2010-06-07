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

import android.os.Bundle;

public class History<C> {
	private CommandStack<C> undoStack = new CommandStack<C>();
	private CommandStack<C> redoStack = new CommandStack<C>();

	private C context;

	public History(C context) {
		this.context = context;
	}

	public void clear() {
		undoStack.clear();
		redoStack.clear();
	}

	public boolean execute(Command<C> command) {
		if (undoStack.isEmpty()) {
			undoStack.push(command);
		}
		else {
			Command<C> last = undoStack.peek();
			Command<C> merged = command.mergeDown(last);
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

		command.execute(context);
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
			Command<C> command = undoStack.pop();
			redoStack.push(command);

			command.undo(context);
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
			Command<C> command = redoStack.pop();
			undoStack.push(command);

			command.redo(context);
			return true;
		}
	}

	public Bundle saveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putBundle("undoStack", undoStack.saveInstanceState());
		bundle.putBundle("redoStack", redoStack.saveInstanceState());
		return bundle;
	}

	public void restoreInstanceState(Bundle bundle) {
		undoStack.restoreInstanceState(bundle.getBundle("undoStack"));
		redoStack.restoreInstanceState(bundle.getBundle("redoStack"));
	}

	private static class CommandStack<C> {
		private List<Command<C>> stack = new ArrayList<Command<C>>();

		public void clear() {
			stack.clear();
		}

		public boolean isEmpty() {
			return stack.isEmpty();
		}

		public void push(Command<C> command) {
			stack.add(command);
		}

		public Command<C> peek() {
			return stack.get(stack.size() - 1);
		}

		public Command<C> pop() {
			return stack.remove(stack.size() - 1);
		}

		public Bundle saveInstanceState() {
			Bundle bundle = new Bundle();

			final int size = stack.size();
			bundle.putInt("size", size);

			for (int i = 0; i < size; i++) {
				Command<C> command = stack.get(i);
				bundle.putParcelable("cmd." + i, command);
			}

			return bundle;
		}

		public void restoreInstanceState(Bundle bundle) {
			stack.clear();

			final int size = bundle.getInt("size");

			for (int i = 0; i < size; i++) {
				Command<C> command = bundle.getParcelable("cmd." + i);
				stack.add(command);
			}
		}
	}
}
