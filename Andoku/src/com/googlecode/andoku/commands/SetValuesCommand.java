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

package com.googlecode.andoku.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.googlecode.andoku.history.Command;
import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.ValueSet;

public class SetValuesCommand extends AbstractCommand {
	private final Position cell;
	private final ValueSet values;
	private ValueSet originalValues;

	public SetValuesCommand(Position cell, ValueSet values) {
		this.cell = cell;
		this.values = values;
	}

	private SetValuesCommand(Position cell, ValueSet values, ValueSet originalValues) {
		this.cell = cell;
		this.values = values;
		this.originalValues = originalValues;
	}

	public void execute(AndokuContext context) {
		originalValues = context.getPuzzle().getValues(cell.row, cell.col);
		redo(context);
	}

	public void undo(AndokuContext context) {
		context.getPuzzle().setValues(cell.row, cell.col, originalValues);
	}

	public void redo(AndokuContext context) {
		context.getPuzzle().setValues(cell.row, cell.col, values);
	}

	@Override
	public Command<AndokuContext> mergeDown(Command<AndokuContext> last) {
		if (!(last instanceof SetValuesCommand))
			return null;

		SetValuesCommand other = (SetValuesCommand) last;
		if (!cell.equals(other.cell))
			return null;

		return new SetValuesCommand(cell, values, other.originalValues);
	}

	@Override
	public boolean isEffective() {
		return !values.equals(originalValues);
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(cell.row);
		dest.writeInt(cell.col);
		dest.writeInt(values.toInt());
		if (originalValues != null)
			dest.writeInt(originalValues.toInt());
	}

	public static final Parcelable.Creator<SetValuesCommand> CREATOR = new Parcelable.Creator<SetValuesCommand>() {
		public SetValuesCommand createFromParcel(Parcel in) {
			int row = in.readInt();
			int col = in.readInt();
			Position cell = new Position(row, col);
			ValueSet values = new ValueSet(in.readInt());
			ValueSet originalValues = in.dataAvail() > 0 ? new ValueSet(in.readInt()) : null;
			return new SetValuesCommand(cell, values, originalValues);
		}

		public SetValuesCommand[] newArray(int size) {
			return new SetValuesCommand[size];
		}
	};
}
