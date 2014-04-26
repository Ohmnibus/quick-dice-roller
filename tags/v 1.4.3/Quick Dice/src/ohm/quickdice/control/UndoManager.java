package ohm.quickdice.control;

import java.util.ArrayList;
import java.util.Stack;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.entity.RollResult;

/**
 * Manager for the Undo operations.<br />
 * For personal convenience this manager is handled as a singleton.
 * @author Ohmnibus
 *
 */
public class UndoManager {

	public class RollResultUndo {
		int position;
		RollResult[] res;
		
		public RollResultUndo(int position, RollResult[] result) {
			this.position = position;
			this.res = result;
		}

		/**
		 * @return the position
		 */
		public int getPosition() {
			return position;
		}

		/**
		 * @return the res
		 */
		public RollResult[] getRes() {
			return res;
		}
	}
	
	private static UndoManager self = null;
	private PreferenceManager pref;

	private Stack<RollResultUndo> undoList = new Stack<UndoManager.RollResultUndo>();
	private ArrayList<RollResult[]> undoAll = null;

	private UndoManager() {
		pref = QuickDiceApp.getInstance().getPreferences();
	}
	
	public static UndoManager getInstance() {
		if (self == null) {
			self = new UndoManager();
		}
		return self;
	}
	
	public void addToUndoList(int pos, RollResult[] res) {
		if (canUndoAll()) {
			resetUndoList();
			resetUndoAll();
		}
		undoList.push(new RollResultUndo(pos, res));
		if (undoList.size() > pref.getMaxResultUndo()) {
			undoList.remove(undoList.size() - 1);
		}
	}
	
	public void resetUndoList() {
		undoList.clear();
	}

	public boolean canUndo() {
		return (undoList != null && undoList.size() > 0 && canUndoAll() == false);
	}
	
	public RollResultUndo restoreFromUndoList() {
		if (! canUndo())
			return null;
		
		return undoList.pop();
	}
	
	public void addToUndoAll(RollResult[] lastResult, ArrayList<RollResult[]> resList) {
		undoAll = new ArrayList<RollResult[]>(resList);
		undoAll.add(0, lastResult);
	}

	public ArrayList<RollResult[]> restoreFromUndoAll() {
		ArrayList<RollResult[]> retVal = undoAll;
		resetUndoAll();
		return retVal;
	}
	
	public void resetUndoAll() {
		undoAll = null;
	}
	
	public boolean canUndoAll() {
		return undoAll != null;
	}
}
