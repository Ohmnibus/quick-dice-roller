/***
 Copyright (c) 2015 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.
 From _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */
package ohm.library.compat;

import static android.provider.MediaStore.MediaColumns.DATA;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Build;

public abstract class CompatCursorWrapper extends CursorWrapper {

	protected boolean hasDataColumn=false;

	/**
	 * Get the correct instance of {@link CompatCursorWrapper}
	 * based on the version of the OS.
	 * @param cursor The underlying cursor to wrap.
	 * @return Instance of {@link CompatCursorWrapper} based on the version of the OS.
	 */
	public static CompatCursorWrapper createInstance(Cursor cursor) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new CompatCursorWrapperHoneycomb(cursor);
		} else {
			return new CompatCursorWrapperGingerbread(cursor);
		}
	}

	/**
	 * Creates a cursor wrapper.
	 * @param cursor The underlying cursor to wrap.
	 */
	public CompatCursorWrapper(Cursor cursor) {
		super(cursor);
		
		hasDataColumn=(cursor.getColumnIndex(DATA)>=0);
	}

	@Override
	public int getColumnCount() {
		if (hasDataColumn) {
			return(super.getColumnCount());
		}

		return(super.getColumnCount()+1);
	}

	@Override
	public int getColumnIndex(String columnName) {
		if (hasDataColumn || DATA.equalsIgnoreCase(columnName)) {
			return(super.getColumnCount());
		}

		return(super.getColumnIndex(columnName));
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (!hasDataColumn && columnIndex==super.getColumnCount()) {
			return(DATA);
		}

		return(super.getColumnName(columnIndex));
	}

	@Override
	public String[] getColumnNames() {
		if (hasDataColumn) {
			return(super.getColumnNames());
		}

		String[] orig = super.getColumnNames();
		//String[] result = Arrays.copyOf(orig, orig.length + 1);
		String[] result = new String[orig.length + 1];
		System.arraycopy(orig, 0, result, 0, orig.length);

		result[orig.length]=DATA;

		return(result);
	}

	@Override
	public String getString(int columnIndex) {
		if (!hasDataColumn && columnIndex==super.getColumnCount()) {
			return(null); // yes, we have no _data, we have no _data today
		}

		return(super.getString(columnIndex));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class CompatCursorWrapperHoneycomb extends CompatCursorWrapper {

		public CompatCursorWrapperHoneycomb(Cursor cursor) {
			super(cursor);
		}
		
		@Override
		public int getType(int columnIndex) {
			if (!hasDataColumn && columnIndex==super.getColumnCount()) {
				return(Cursor.FIELD_TYPE_STRING);
			}
	
			return(super.getType(columnIndex));
		}
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static class CompatCursorWrapperGingerbread extends CompatCursorWrapper {

		public CompatCursorWrapperGingerbread(Cursor cursor) {
			super(cursor);
		}
		
	}
}
