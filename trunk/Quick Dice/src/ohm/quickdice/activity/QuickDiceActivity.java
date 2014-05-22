package ohm.quickdice.activity;

import java.util.ArrayList;

import ohm.dexp.DExpression;
import ohm.dexp.exception.DException;
import ohm.library.compat.CompatActionBar;
import ohm.library.compat.CompatClipboard;
import ohm.library.gesture.SwipeDismissGridViewTouchListener;
import ohm.library.gesture.SwipeDismissTouchListener;
import ohm.library.widget.SplitView;
import ohm.library.widget.SplitView.ResizeListener;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.DiceBagAdapter;
import ohm.quickdice.adapter.GridExpressionAdapter;
import ohm.quickdice.adapter.ResultListAdapter;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.control.GraphicManager;
import ohm.quickdice.control.PreferenceManager;
import ohm.quickdice.control.SerializationManager;
import ohm.quickdice.control.UndoManager;
import ohm.quickdice.dialog.DialogHelper;
import ohm.quickdice.dialog.DiceDetailDialog;
import ohm.quickdice.dialog.DicePickerDialog;
import ohm.quickdice.dialog.ModifierBuilderDialog;
import ohm.quickdice.dialog.ModifierBuilderDialog.ReadyListener;
import ohm.quickdice.dialog.RollDetailDialog;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.RollResult;
import ohm.quickdice.util.Behavior;
import ohm.quickdice.util.Helper;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class QuickDiceActivity extends BaseActivity {
	
	QuickDiceApp app;
	Resources res;
	GraphicManager graphicManager;
	PreferenceManager pref;
	UndoManager undoManager;
	
	boolean backedUpData = true;
	DiceBagManager diceBagManager;
	ArrayList<DExpression> diceBag;
	ArrayList<RollModifier> bonusBag;
	
	RollResult[] lastResult;
	ArrayList<RollResult[]> resultList;
	
	ListView lvDiceBag;
	GridView gvResults;
	GridView gvDice;
	ViewGroup vgModifiers = null;
	DrawerLayout diceBagDrawer;
	ActionBarDrawerToggle diceBagDrawerToggle;
	CompatActionBar actionBar;
	
	//Cache for "modifiers"
	ArrayList<View> modifierViewList;
	View modifiersHolder = null;

	//Cache for "lastResult"
	View lastResHolder;
	ImageView lastResDieImage;
	TextView lastResName;
	TextView lastResText;
	TextView lastResValue;
	ImageView lastResResultImage;
	ImageButton linkSwitchButton;
	Button undoAllButton;
	boolean linkRoll = false;

	//Cache for effects
	Animation rollDiceAnimation = null;
	MediaPlayer rollDiceSound = null;
	MediaPlayer rollDiceCriticalSound = null;
	MediaPlayer rollDiceFumbleSound = null;

	//Cache for "rollDiceToast"
	Toast rollDiceToast = null;
	View rollDiceView = null;
	ImageView rollDiceImage = null;
	TextView rollDiceText = null;
	
	private final int LINK_LEVEL_OFF = 0;
	private final int LINK_LEVEL_ON = 1;

	private final String KEY_ROLL_LIST = "KEY_ROLL_LIST";

	private final String TYPE_MODIFIER = "TYPE_MODIFIER";

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Initializations
		app = QuickDiceApp.getInstance();
		res = getResources();
		graphicManager = app.getGraphic();
		pref = app.getPreferences();
		undoManager = UndoManager.getInstance();

		diceBagManager = app.getBagManager();
		diceBagManager.initBagManager();

		diceBag = diceBagManager.getDice();
		bonusBag = diceBagManager.getModifiers();

		modifierViewList = new ArrayList<View>();

		if (savedInstanceState != null) {
			lastResult = null;
			resultList = null;

			String jsonData = savedInstanceState.getString(KEY_ROLL_LIST);
			if (jsonData != null) {
				resultList = SerializationManager.ResultListNoException(jsonData);
				if (resultList != null) {
					lastResult = resultList.remove(0);
				}
			}
		} else {
			int currentVersion = app.getCurrentVersion();
			int lastVersionExecuted = app.getLastVersionExecuted();
			if (lastVersionExecuted < 0) {
				//First execution
				DialogHelper.ShowAbout(this);
				app.setLastVersionExecuted(currentVersion);
			} else if (lastVersionExecuted < currentVersion) {
				//Execution after update
				DialogHelper.ShowWhatsNew(this);
				app.setLastVersionExecuted(currentVersion);
			}
		}
		if (lastResult == null) {
			resultList = app.getPersistence().loadResultList();
			if (resultList != null) {
				lastResult = resultList.remove(0);
			}
		}
		if (lastResult == null) {
			lastResult = new RollResult[0];
		}

		if (resultList == null) {
			resultList = new ArrayList<RollResult[]>();
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		initViews();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		diceBagDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		diceBagDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//outState.putSerializable(KEY_LAST_ROLL, lastResult);
		//outState.putSerializable(KEY_ROLL_LIST, resultList);
		resultList.add(0, lastResult);
		String jsonData = SerializationManager.ResultListNoException(resultList);
		resultList.remove(0);
		outState.putString(KEY_ROLL_LIST, jsonData);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onPause() {
		if (diceBagManager.isDataChanged()) {
			diceBagManager.saveAll();
			backedUpData = false;
		}

		if (isFinishing()) {
			app.getPersistence().saveResultList(lastResult, resultList);
		}

		super.onPause();
	}

	@Override
	protected void onStop() {
		if (! backedUpData) {
			//Online Backup
			Helper.requestBackup(app);
			backedUpData = true;
		}
		
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean retVal = super.onPrepareOptionsMenu(menu);

		menu.findItem(R.id.mmUndoClearAllResults).setVisible(undoManager.canUndoAll());
		menu.findItem(R.id.mmUndoClearResult).setVisible(undoManager.canUndo());
		menu.findItem(R.id.mmAddModifier).setVisible(pref.getShowModifiers());

		return retVal;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean retVal;

		if (diceBagDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		retVal = true;

		switch (item.getItemId()) {
			case R.id.mmAddDiceBag:
				callEditDiceBag(EditBagActivity.ACTIVITY_ADD, null, EditBagActivity.POSITION_UNDEFINED);
				break;
			case R.id.mmAddDice:
				callEditDice(EditDiceActivity.ACTIVITY_ADD, null, EditDiceActivity.POSITION_UNDEFINED);
				break;
			case R.id.mmAddModifier:
				callAddModifier(ModifierBuilderDialog.POSITION_UNDEFINED);
				break;
			case R.id.mmSettings:
				callPreferences();
				break;
			case R.id.mmUndoClearResult:
				restoreFromUndoList();
				break;
			case R.id.mmClearAllResults:
				clearAllRollResult();
				break;
			case R.id.mmUndoClearAllResults:
				restoreFromUndoAll();
				break;
			case R.id.mmImportExport:
				callImportExport();
				break;
			case R.id.mmAbout:
				DialogHelper.ShowAbout(this);
				break;
			case R.id.mmExit:
				this.finish();
				break;
			default:
				//Unknown: pass to parent
				retVal=super.onOptionsItemSelected(item);
				break;
		}
		return retVal;
	}

	/**
	 * Handle the result given by a previously called activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == EditBagActivity.ACTIVITY_ADD && resultCode == EditBagActivity.RESULT_OK) {
			//Add new dice bag
			DiceBag newBag = getDiceBagFromIntent(data);
			if (newBag != null) {
				int position = getDiceBagPositionFromIntent(data);
				diceBagManager.addDiceBag(position, newBag);
				diceBagManager.setCurrentDiceBag(position);
				refreshAllDiceContainers();
			}
		} else if (requestCode == EditBagActivity.ACTIVITY_EDIT && resultCode == EditBagActivity.RESULT_OK) {
			//Edit dice bag
			DiceBag newBag = getDiceBagFromIntent(data);
			if (newBag != null) {
				int position = getDiceBagPositionFromIntent(data);
				diceBagManager.editDiceBag(position, newBag);
				refreshBagsList();
			}
		} else if (requestCode == EditDiceActivity.ACTIVITY_ADD && resultCode == EditDiceActivity.RESULT_OK) {
			//Add new die
			DExpression newExp = getExpressionFromIntent(data);
			if (newExp != null) {
				int position = getExpressionPositionFromIntent(data);
				diceBagManager.addDie(position, newExp);
				refreshDiceList();
			}
		} else if (requestCode == EditDiceActivity.ACTIVITY_EDIT && resultCode == EditDiceActivity.RESULT_OK) {
			//Edit die
			DExpression newExp = getExpressionFromIntent(data);
			if (newExp != null) {
				int position = getExpressionPositionFromIntent(data);
				diceBagManager.editDie(position, newExp);
				refreshDiceList();
			}
		} else if (requestCode == ImportExportActivity.ACTIVITY_IMPORT_EXPORT) {
			if (resultCode == ImportExportActivity.RESULT_EXPORT) {
				Toast.makeText(this, R.string.msgExported, Toast.LENGTH_SHORT).show();
			} else if (resultCode == ImportExportActivity.RESULT_IMPORT) {
				Toast.makeText(this, R.string.msgImported, Toast.LENGTH_SHORT).show();
				refreshAllDiceContainers(true);
				//} else if (resultCode == ImportExportActivity.RESULT_IMPORT_FAILED) {
				//NOOP
				//(Error is already notified by persistence manager)
			}
		} else if (requestCode == PrefDiceActivity.ACTIVITY_EDIT_PREF) {
			//Apply new preferences

			//Request for backup.
			backedUpData = false;

			pref.resetCache();

			//Number of columns
			gvResults.setNumColumns(pref.getGridResultColumn());

			//Swap name and results
			ResultListAdapter.setSwapNameResult(pref.getSwapNameResult());

			//Roll Pop Up
			if (! pref.getShowToast()) {
				//Free cache for "rollDiceToast"
				rollDiceToast = null;
				//rollDiceLayout = null;
				rollDiceView = null;
				rollDiceImage = null;
				rollDiceText = null;
			}
			//Roll Animation
			if (! pref.getShowAnimation()) {
				//Free resources
				rollDiceAnimation = null;
			}
			//Roll sound
			if (! pref.getSoundEnabled()) {
				//Free resources
				if (rollDiceSound != null) {
					rollDiceSound.release();
					rollDiceSound = null;
				}
			}
			//Roll ext sound
			if (! pref.getSoundEnabled() || ! pref.getExtSoundEnabled()) {
				//Free resources
				if (rollDiceCriticalSound != null) {
					rollDiceCriticalSound.release();
					rollDiceCriticalSound = null;
				}
				if (rollDiceFumbleSound != null) {
					rollDiceFumbleSound.release();
					rollDiceFumbleSound = null;
				}
			}

			//Modifiers bar
			initModifierList();
			refreshResultList();
			refreshLastResult();
		}
	}
	
	private DiceBag getDiceBagFromIntent(Intent data) {
		DiceBag retVal;
		Bundle extras = data.getExtras();
		if (extras != null) {
			retVal = (DiceBag)extras.getSerializable(EditBagActivity.BUNDLE_DICE_BAG);
		} else {
			retVal = null;
		}
		return retVal;
	}

	private int getDiceBagPositionFromIntent(Intent data) {
		int retVal;
		Bundle extras = data.getExtras();
		if (extras != null && extras.containsKey(EditBagActivity.BUNDLE_POSITION)) {
			retVal = extras.getInt(EditBagActivity.BUNDLE_POSITION);
		} else {
			retVal = EditBagActivity.POSITION_UNDEFINED;
		}
		return retVal;
	}

	private DExpression getExpressionFromIntent(Intent data) {
		DExpression retVal;
		Bundle extras = data.getExtras();
		if (extras != null) {
			retVal = (DExpression)extras.getSerializable(EditDiceActivity.BUNDLE_DICE_EXPRESSION);
		} else {
			retVal = null;
		}
		return retVal;
	}
	
	private int getExpressionPositionFromIntent(Intent data) {
		int retVal;
		Bundle extras = data.getExtras();
		if (extras != null && extras.containsKey(EditDiceActivity.BUNDLE_POSITION)) {
			retVal = extras.getInt(EditDiceActivity.BUNDLE_POSITION);
		} else {
			retVal = EditDiceActivity.POSITION_UNDEFINED;
		}
		return retVal;
	}
	
	/**
	 * Initialize context menus.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if (v.getId() == lvDiceBag.getId()) {
			//Context menu for the dice bags list
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
			
			setupDiceBagMenu(info.position, menu);
		} else if (v.getId() == gvDice.getId()) {
			//Context menu for the dice bag
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
			
			setupDiceMenu(info.position, menu);
		} else if (v.getId() == gvResults.getId()) {
			//Context menu for the result list
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;

			setupRollMenu(info.position, menu);
		} else if (v.getTag(R.id.key_type) == TYPE_MODIFIER) {
			//Context menu for a modifier
			setupModifierMenu((Integer)v.getTag(R.id.key_value), menu);
		} else {
			//Context menu for the last result item
			if (lastResult.length > 0) {
				setupRollMenu(-1, menu);
			}
		}
	}

	protected void setupDiceBagMenu(int index, ContextMenu menu) {
		DiceBag bag;
		
		bag = (DiceBag)lvDiceBag.getItemAtPosition(index);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_dice_bag, menu);
		
		//Get the dice icon and resize it to fit the menu header icon size.
		Drawable diceBagIcon = graphicManager.getResizedDiceIcon(
				bag.getResourceIndex(), 32, 32);
		menu.setHeaderIcon(diceBagIcon);
		menu.setHeaderTitle(bag.getName());
		
		if (lvDiceBag.getCount() == 1) {
			//Only one element
			menu.findItem(R.id.mdbRemove).setVisible(false);
		}
		if (lvDiceBag.getCount() >= pref.getMaxDiceBags()) {
			//Maximum number of allowed dice bags reached
			menu.findItem(R.id.mdbAddHere).setVisible(false);
			menu.findItem(R.id.mdbClone).setVisible(false);
		}
		if (index == 0) {
			//First element
			menu.findItem(R.id.mdbSwitchPrev).setVisible(false);
		}
		if (index == lvDiceBag.getCount() - 1) {
			//Last element
			menu.findItem(R.id.mdbSwitchNext).setVisible(false);
		}
	}

	protected void setupDiceMenu(int index, ContextMenu menu) {
		DExpression exp;
		
		exp = (DExpression)gvDice.getItemAtPosition(index);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_dice, menu);
		
		//Get the dice icon and resize it to fit the menu header icon size.
		Drawable diceIcon = graphicManager.getResizedDiceIcon(
				exp.getResourceIndex(), 32, 32);
		menu.setHeaderIcon(diceIcon);
		menu.setHeaderTitle(exp.getName());
		
		if (gvDice.getCount() == 1) {
			//Only one element
			menu.findItem(R.id.mdRemove).setVisible(false);
			menu.findItem(R.id.mdMoveTo).setVisible(false);
		}
		if (diceBag.size() >= pref.getMaxDice()) {
			//Maximum number of allowed dice reached
			menu.findItem(R.id.mdAddHere).setVisible(false);
			menu.findItem(R.id.mdClone).setVisible(false);
		}
	}
	
	protected int modifierOpeningMenu;
	
	protected void setupModifierMenu(int index, ContextMenu menu) {
		RollModifier modifier;
		Drawable modIcon;
		
		modifierOpeningMenu = index;
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_modifier, menu);

		modifier = bonusBag.get(index);
		modIcon = graphicManager.getResizedDiceIcon(
				modifier.getResourceIndex(), 32, 32);

		menu.setHeaderIcon(modIcon);
		menu.setHeaderTitle(modifier.getName());

		if (lastResult.length == 0) {
			//No rolls to add bonus to
			menu.findItem(R.id.moApply).setVisible(false);
		}
		if (bonusBag.size() == 1) {
			//Only one element
			menu.findItem(R.id.moRemove).setVisible(false);
		}
		if (bonusBag.size() >= pref.getMaxModifiers()) {
			//Maximum number of allowed modifiers reached
			menu.findItem(R.id.moAddHere).setVisible(false);
		}
		if (index == 0) {
			//First element
			menu.findItem(R.id.moSwitchPrev).setVisible(false);
		}
		if (index == bonusBag.size() - 1) {
			//Last element
			menu.findItem(R.id.moSwitchNext).setVisible(false);
		}
	}

	protected void setupRollMenu(int index, ContextMenu menu) {
		RollResult[] rollItem;
		RollResult[] nextItem;
		RollResult mergedRoll;
		
		if (index < 0) {
			rollItem = lastResult;
		} else {
			rollItem = resultList.get(index); //This seem to never throw ClassCastException!!
		}
		if (index + 1 < resultList.size()) {
			try {
				nextItem = resultList.get(index + 1);
			} catch (ClassCastException ex) {
				//Really don't know why, but sometime
				//a cast exception occur.
				nextItem = null;
			}
		} else {
			nextItem = null;
		}

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_roll, menu);

		//Get the dice icon and resize it to fit the menu header icon size.
		mergedRoll = RollResult.mergeResultList(rollItem);
		Drawable diceIcon = graphicManager.getResizedDiceIcon(
				mergedRoll.getResourceIndex(), 32, 32);
		menu.setHeaderIcon(diceIcon);
		menu.setHeaderTitle(mergedRoll.getName());

		if (rollItem.length <= 1) {
			//Cannot split
			menu.findItem(R.id.mrSplit).setVisible(false);
		}
		if (nextItem == null || rollItem.length + nextItem.length > pref.getMaxResultLink()) {
			//Cannot link (Last element or too much results)
			menu.findItem(R.id.mrMerge).setVisible(false);
		}
	}

	protected int targetItem;
	
	/**
	 * Handle a context menu selection.
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean retVal;
		DiceBag bag;
		DExpression exp;
		RollResult[] result;
		AlertDialog.Builder builder;
		RollModifier mod;
		
		retVal = true;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
			case R.id.mdbSelect:
				diceBagManager.setCurrentDiceBag(info.position);
				refreshAllDiceContainers();
				diceBagDrawer.closeDrawer(lvDiceBag);
				break;
			case R.id.mdbEdit:
				bag = (DiceBag)lvDiceBag.getItemAtPosition(info.position);
				callEditDiceBag(EditBagActivity.ACTIVITY_EDIT, bag, info.position);
				break;
			case R.id.mdbAddHere:
				callEditDiceBag(EditBagActivity.ACTIVITY_ADD, null, info.position);
				break;
			case R.id.mdbClone:
				if (lvDiceBag.getCount() >= pref.getMaxDiceBags()) {
					//Maximum number of allowed dice bags reached
					Toast.makeText(this, R.string.msgMaxBagsReach, Toast.LENGTH_LONG).show();
				} else {
					diceBagManager.cloneDiceBag(info.position);
					refreshAllDiceContainers();
				}
				break;
			case R.id.mdbRemove:
				//Ask confirmation prior to delete a dice bag.
				targetItem = info.position;
				bag = (DiceBag)lvDiceBag.getItemAtPosition(info.position);
				builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.msgRemoveDiceBagTitle);
				builder.setMessage(Helper.getString(res, R.string.msgRemoveDiceBag, bag.getName(), bag.getDice().size(), bag.getModifiers().size()));
				builder.setPositiveButton(
						R.string.lblYes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								diceBagManager.removeDiceBag(targetItem);
								refreshAllDiceContainers();
							}
						});
				builder.setNegativeButton(
						R.string.lblNo,
						cancelDialogClickListener);
				builder.create().show();
				break;
			case R.id.mdbSwitchPrev:
				diceBagManager.moveDiceBag(info.position, info.position - 1);
				refreshAllDiceContainers();
				break;
			case R.id.mdbSwitchNext:
				diceBagManager.moveDiceBag(info.position, info.position + 1);
				refreshAllDiceContainers();
				break;
			case R.id.mdDetails:
				exp = (DExpression)gvDice.getItemAtPosition(info.position);
				new DiceDetailDialog(this, exp).show();
				break;
			case R.id.mdRoll:
				exp = (DExpression)gvDice.getItemAtPosition(info.position);
				doRoll(exp);
				break;
			case R.id.mdEdit:
				exp = (DExpression)gvDice.getItemAtPosition(info.position);
				callEditDice(EditDiceActivity.ACTIVITY_EDIT, exp, info.position);
				break;
			case R.id.mdRemove:
				//Ask confirmation prior to delete a dice.
				targetItem = info.position;
				exp = (DExpression)gvDice.getItemAtPosition(info.position);
				builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.msgRemoveDiceTitle);
				builder.setMessage(Helper.getString(res, R.string.msgRemoveDice, exp.getName()));
				builder.setPositiveButton(
						R.string.lblYes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								diceBagManager.removeDie(targetItem);
								refreshDiceList();
							}
						});
				builder.setNegativeButton(
						R.string.lblNo,
						cancelDialogClickListener);
				builder.create().show();
				break;
			case R.id.mdAddHere:
				callEditDice(EditDiceActivity.ACTIVITY_ADD, null, info.position);
				break;
			case R.id.mdClone:
				if (diceBag.size() >= pref.getMaxDice()) {
					//Maximum number of allowed dice reached
					Toast.makeText(this, R.string.msgMaxDiceReach, Toast.LENGTH_LONG).show();
				} else {
					diceBagManager.cloneDie(info.position);
					refreshAllDiceContainers();
				}
				break;
			case R.id.mdMoveTo:
				targetItem = info.position;
				new DicePickerDialog(
						this,
						R.string.lblSelectDiceDest,
						diceBagManager.getCurrentDiceBag(),
						info.position,
						DicePickerDialog.DIALOG_SELECT_DESTINATION,
						new DicePickerDialog.ReadyListener() {
							@Override
							public void ready(boolean confirmed, int groupId, int itemId) {
								if (confirmed) {
									moveDice(
											diceBagManager.getCurrentDiceBag(),
											targetItem,
											groupId,
											itemId);
								}
							}
						}).show();
				break;
			case R.id.mrDetails:
				if (info != null) {
					result = (RollResult[])gvResults.getItemAtPosition(info.position);
				} else {
					result = lastResult;
				}
				new RollDetailDialog(this, result).show();
				break;
			case R.id.mrClear:
				clearRollResult(info == null ? -1 : info.position);
				break;
			case R.id.mrClearAll:
				clearAllRollResult();
				break;
			case R.id.mrSplit:
				if (info != null) {
					//Split result at given list position
					result = (RollResult[])gvResults.getItemAtPosition(info.position);
					resultList.remove(info.position);
					for (int i = 0; i < result.length; i++) {	
						resultList.add(info.position, new RollResult[] {result[i]});
					}
					refreshResultList();
				} else {
					//Split result in lastResult box
					for (int i = 0; i < lastResult.length - 1; i++) {
						resultList.add(0, new RollResult[] {lastResult[i]});
					}
					lastResult = new RollResult[] {lastResult[lastResult.length - 1]};
					refreshLastResult();
					refreshResultList();
				}
				invalidateUndo();
				break;
			case R.id.mrMerge:
				if (info != null) {
					//Merge result at given list position with previous
					result = linkResult(resultList.get(info.position), resultList.get(info.position + 1));
					resultList.remove(info.position + 1);
					resultList.remove(info.position);
					resultList.add(info.position, result);
					refreshResultList();
				} else {
					//Merge result in lastResult box with previous
					lastResult = linkResult(lastResult, resultList.get(0));
					resultList.remove(0);
					refreshLastResult();
					refreshResultList();
				}
				invalidateUndo();
				break;
			case R.id.moApply:
				doModifier(bonusBag.get(modifierOpeningMenu));
				invalidateUndo();
				break;
			case R.id.moRemove:
				// Ask confirmation prior to delete a modifier.
				targetItem = modifierOpeningMenu;
				mod = bonusBag.get(modifierOpeningMenu);
				builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.msgRemoveModTitle);
				builder.setMessage(Helper.getString(res, R.string.msgRemoveMod, mod.getName()));
				builder.setPositiveButton(
						R.string.lblYes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								diceBagManager.removeModifier(targetItem);
								refreshModifierList();
							}
						});
				builder.setNegativeButton(
						R.string.lblNo,
						cancelDialogClickListener);
				builder.create().show();
				break;
			case R.id.moAddHere:
				callAddModifier(modifierOpeningMenu);
				break;
			case R.id.moSwitchPrev:
				diceBagManager.moveModifier(modifierOpeningMenu, modifierOpeningMenu - 1);
				refreshModifierList();
				break;
			case R.id.moSwitchNext:
				diceBagManager.moveModifier(modifierOpeningMenu, modifierOpeningMenu + 1);
				refreshModifierList();
				break;
			default:
				retVal = super.onContextItemSelected(item);
				break;
		}
		return retVal;
	}

	private void initViews() {
		setContentView(R.layout.quick_dice_activity);
		if (pref.getPlainBackground()) {
			findViewById(R.id.mBgLogo).setVisibility(View.GONE); //Remove the logo
			findViewById(R.id.mRoot).setBackgroundResource(R.color.main_bg); //Remove the background
			findViewById(R.id.mDiceBagList).setBackgroundResource(R.color.main_bg); //Remove the background from bag list
		}

		actionBar = CompatActionBar.createInstance(this);

		//Initialize SplitView to last known size
		SplitView sw = (SplitView)findViewById(R.id.mSplitView);
		if (sw.getOrientation() == SplitView.VERTICAL) {
			if (pref.getSplitPanelHeight() >= 0) {
				sw.setContentSize(SplitView.FIRST, pref.getSplitPanelHeight());
			}
		} else {
			if (pref.getSplitPanelWidth() >= 0) {
				sw.setContentSize(SplitView.FIRST, pref.getSplitPanelWidth());
			}
		}
		sw.setOnResizeListener(new ResizeListener() {
			@Override
			public void onResize(int orientation, int newSize) {
				if (orientation == SplitView.VERTICAL) {
					pref.setSplitPanelHeight(newSize);
				} else {
					pref.setSplitPanelWidth(newSize);
				}
			}
		});

		//Dice bag list
		initDiceBagList();
		
		//Dice bag
		initDiceGrid();
		
		//Modifier list
		initModifierList();
		
		//Result list
		initResultList();
		
		// References to the last roll panel
		// Must be called at last
		initLastResultView();
		
		introAnimation();
	}
	
	private void initDiceBagList() {
		//openDrawerContentDescRes = R.string.mnuSelectDiceBag;
		//closeDrawerContentDescRes = R.string.app_name;
		
		diceBagDrawer = (DrawerLayout)findViewById(R.id.mProfileDrawer);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			diceBagDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		} else {
			diceBagDrawer.setDrawerShadow(R.drawable.ic_handle_bar, GravityCompat.START);
		}

		diceBagDrawerToggle = new ActionBarDrawerToggle(
				this,
				diceBagDrawer,
				R.drawable.ic_drawer,
				R.string.mnuSelectDiceBag, //openDrawerContentDescRes,
				R.string.app_name /* closeDrawerContentDescRes */) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				//actionBar.setTitle(R.string.app_name);
				actionBar.setTitle(diceBagManager.getDiceBag().getName());
				//invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				ActivityCompat.invalidateOptionsMenu(QuickDiceActivity.this);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				actionBar.setTitle(R.string.mnuSelectDiceBag);
				//invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				ActivityCompat.invalidateOptionsMenu(QuickDiceActivity.this);
			}
		};

		// Set the drawer toggle as the DrawerListener
		diceBagDrawer.setDrawerListener(diceBagDrawerToggle);

		actionBar.setTitle(diceBagManager.getDiceBag().getName());
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		lvDiceBag = (ListView)findViewById(R.id.mDiceBagList);
		lvDiceBag.setAdapter(new DiceBagAdapter(
				this,
				R.layout.dice_bag_item,
				diceBagManager.getDiceBags()));
		lvDiceBag.setOnItemClickListener(diceBagClickListener);
		registerForContextMenu(lvDiceBag);
	}
	
	private void initDiceGrid() {
		gvDice = (GridView)findViewById(R.id.mDiceSet);

		gvDice.setAdapter(new GridExpressionAdapter(
				this,
				R.layout.dice_item,
				diceBag));

		gvDice.setOnItemClickListener(diceClickListener);

		gvDice.setSelector(android.R.drawable.list_selector_background);

		registerForContextMenu(gvDice);
	}
	
	private void initModifierList() {
		LayoutInflater inflater;
		RollModifier modifier;
		View modView;
		//Button modButton;
		TextView modText;
		ImageView modIcon;
		
		if (modifiersHolder == null) {
			modifiersHolder = (View)findViewById(R.id.mModifierContainer);
		}

		if (! pref.getShowModifiers()) {
			modifiersHolder.setVisibility(View.GONE);
			return;
		} else {
			modifiersHolder.setVisibility(View.VISIBLE);
		}

		if (vgModifiers == null) {
			vgModifiers = (ViewGroup)findViewById(R.id.mModifierList);
		}

		inflater = LayoutInflater.from(this);

		for (int i = 0; i < modifierViewList.size(); i++) {
			modView = modifierViewList.get(i);
			unregisterForContextMenu(modView);
		}
		
		vgModifiers.removeAllViews();
		modifierViewList.clear();
		
		for (int i = 0; i < bonusBag.size(); i++) {
			modifier = bonusBag.get(i);
			modView = inflater.inflate(R.layout.modifier_item, null);
			
			modIcon = (ImageView)modView.findViewById(R.id.miIcon);
			modText = (TextView)modView.findViewById(R.id.miValue);
			modIcon.setImageDrawable(graphicManager.getDiceIcon(modifier.getResourceIndex()));
			modText.setText(modifier.getValueString());

			modView.setTag(R.id.key_type, TYPE_MODIFIER);
			modView.setTag(R.id.key_value, i);
			
			modView.setOnClickListener(modifierClickListener);
			
			registerForContextMenu(modView);
			
			modView.setOnTouchListener(Behavior.listOnTouchListener);
			
			vgModifiers.addView(modView);
			modifierViewList.add(modView);
		}
		
		centerInParent(vgModifiers);
	}
	
	private void initResultList() {
		gvResults = (GridView)findViewById(R.id.mRollList);

		gvResults.setNumColumns(pref.getGridResultColumn());

		ResultListAdapter.setSwapNameResult(pref.getSwapNameResult());

		gvResults.setAdapter(new ResultListAdapter(
				this,
				R.layout.roll_item,
				resultList));

		SwipeDismissGridViewTouchListener touchListener = new SwipeDismissGridViewTouchListener(
				gvResults,
				new SwipeDismissGridViewTouchListener.DismissCallbacks(){
					@Override
					public void onDismiss(GridView gridView, int[] reverseSortedPositions) {
						clearRollResult(reverseSortedPositions);
					}

					@Override
					public boolean canDismiss(int position) {
						return true;
					}
				},
				SwipeDismissGridViewTouchListener.DIRECTION_RTOL);

		gvResults.setOnTouchListener(touchListener);
		gvResults.setOnScrollListener(touchListener.makeScrollListener());

		gvResults.setSelector(android.R.drawable.list_selector_background);

		registerForContextMenu(gvResults);
	}

	private void initLastResultView() {
		lastResHolder = findViewById(R.id.mLastRollContainer);

		registerForContextMenu(lastResHolder);

		lastResHolder.setOnTouchListener(new Behavior.RollResultTouchListener(
				lastResHolder,
				null,
				new SwipeDismissTouchListener.DismissCallbacks(){
					public void onDismiss(View view, Object token) {
						clearRollResult(-1);
					}

					@Override
					public boolean canDismiss(Object token) {
						return lastResult.length > 0;
					}
				}));

		lastResDieImage = (ImageView)lastResHolder.findViewById(R.id.riImage);
		lastResName = (TextView)lastResHolder.findViewById(R.id.riName);
		lastResText = (TextView)lastResHolder.findViewById(R.id.riResultText);
		lastResValue = (TextView)lastResHolder.findViewById(R.id.riResult);
		lastResResultImage = (ImageView)lastResHolder.findViewById(R.id.riResultIcon);

		linkSwitchButton = (ImageButton)lastResHolder.findViewById(R.id.mLinkButton);
		linkSwitchButton.setOnClickListener(linkSwitchButtonClickListener);
		refreshLinkSwitchButton();

		undoAllButton = (Button)findViewById(R.id.mUndoDeleteAll);
		undoAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				restoreFromUndoAll();				
			}
		});

		refreshLastResult();
	}
	
	private void introAnimation() {
		//Only if result list is empty
		if (resultList.size() == 0) {
			lastResHolder.startAnimation(AnimationUtils.loadAnimation(this, R.anim.last_roll_intro));
			((SplitView)findViewById(R.id.mSplitView)).getHandle().startAnimation(AnimationUtils.loadAnimation(this, R.anim.split_handle_intro));
			gvDice.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dice_grid_intro));
			modifiersHolder.startAnimation(AnimationUtils.loadAnimation(this, R.anim.modifier_list_intro));
		}
	}
	
	OnItemClickListener diceBagClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			diceBagManager.setCurrentDiceBag(position);
			refreshAllDiceContainers();
			diceBagDrawer.closeDrawer(lvDiceBag);
		}
	};
	
	OnItemClickListener diceClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			doRoll(diceBag.get((int) id));
		}
	};
	
	OnClickListener modifierClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			doModifier(bonusBag.get((Integer)v.getTag(R.id.key_value)));
		}
	};
	
	OnClickListener linkSwitchButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			linkRoll = !linkRoll;
			refreshLinkSwitchButton();
		}
	};
	
	private void refreshLinkSwitchButton() {
		linkSwitchButton.getBackground().setLevel(linkRoll ? LINK_LEVEL_ON : LINK_LEVEL_OFF);
		//linkSwitchButton.setImageLevel(linkRoll ? 1 : 0);
		//linkSwitchButton.refreshDrawableState();
	}
	
	/**
	 * Generic listener to handle a "cancel" or "no" button on dialogs.
	 */
	DialogInterface.OnClickListener cancelDialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
		}
	};

	private void doModifier(RollModifier modifier) {
		RollResult modRes;

		if (lastResult.length > 0) { //Apply only if a result exist
			modRes = new RollResult(
					modifier.getName(), 
					modifier.getDescription(), 
					modifier.getValueString(), 
					modifier.getValue(), 
					modifier.getValue(), 
					modifier.getValue(), 
					modifier.getResourceIndex());

			addResult(modRes, true);
		}
	}

	private void doRoll(DExpression exp) {
		try {
			handleResult(new RollResult(exp.getResult()));
		} catch (DException ex) {
			handleErrResult(ex);
		}
	}

	private void handleResult(RollResult res) {
		String resultValue;
		
		resultValue = Long.toString(res.getResultValue());
		
		//ClipboardManager clpMng;
		switch (pref.getClipboardUsage()) {
		case PreferenceManager.CLIPBOARD_TYPE_VALUE:
			//clpMng = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
			//clpMng.setText(resultValue);
			CompatClipboard.getInstance(app).setText(
					getString(R.string.lblResultValue),
					resultValue);
			break;
		case PreferenceManager.CLIPBOARD_TYPE_EXT:
			//clpMng = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
			//clpMng.setText(res.getResultText() + " = " + resultValue);
			CompatClipboard.getInstance(app).setText(
					getString(R.string.lblResultText),
					res.getResultText() + " = " + resultValue);
			break;
		default:
			//NOOP
			break;
		}
		
		performRoll(res);
		
		//Link result if:
		//- Linking is enabled
		//- Link chain is not too long
		//- Previous roll is not too far
		//long now = System.currentTimeMillis();
		
//		addResult(res, pref.getLinkEnabled()
//				&& lastResult.length < pref.getMaxResultLink()
//				&& (now - startInterval) <= pref.getLinkDelay());
//		
//		startInterval = now;
		
		addResult(res, checkLinkRoll() //This condition must be evaluated so it must appear on first position
				&& lastResult.length < pref.getMaxResultLink());
	}
	
	/**
	 * Check if roll has to be linked and update the status of the button.
	 * @return
	 */
	private boolean checkLinkRoll() {
		boolean retVal = linkRoll;
		if (pref.getAutoLinkEnabled()) {
			//Automatic linking enabled
			//(re)set to true, reset to false after delay.
			linkRoll = true;
			resetLinkSwitch(pref.getLinkDelay());
		} else {
			//If was true, set to false.
			linkRoll = false;
		}
		if (retVal != linkRoll) {
			refreshLinkSwitchButton();
		}
		return retVal;
	}
	
	private void addResult(RollResult res, boolean linked) {
		if (linked) {
			//Result is linked
			RollResult[] newRes = new RollResult[lastResult.length + 1];
			for (int i = 0; i < lastResult.length; i++) {
				newRes[i] = lastResult[i];
			}
			lastResult = newRes;
		} else {
			//Result is stacked
			if (lastResult.length > 0) {
				resultList.add(0, lastResult);
				refreshResultList();
			}
			lastResult = new RollResult[1];
		}
		lastResult[lastResult.length - 1] = res;
		
		invalidateUndo();
		
		refreshLastResult();
	}
	
	private void handleErrResult(DException ex) {
		Toast.makeText(this, Helper.getErrorMessage(this, ex), Toast.LENGTH_SHORT).show();
	}
	
	protected void performRoll(RollResult res) {

		if (pref.getShowToast()) {
			performRollPopup(res);
		
			if (pref.getShowAnimation())
				performRollAnimation(res);
		}
		
		if (pref.getSoundEnabled())
			performRollSound(res);
	}
	
	private void performRollSound(RollResult res) {
		if (res.isCritical() && pref.getExtSoundEnabled()) {
			if (rollDiceCriticalSound == null) {
				rollDiceCriticalSound = MediaPlayer.create(app, R.raw.critical);
			}
			if (rollDiceCriticalSound != null) {
				rollDiceCriticalSound.start();
			}
		} else if (res.isFumble() && pref.getExtSoundEnabled()) {
			if (rollDiceFumbleSound == null) {
				rollDiceFumbleSound = MediaPlayer.create(app, R.raw.fumble);
			}
			if (rollDiceFumbleSound != null) {
				rollDiceFumbleSound.start();
			}
		} else {
			if (rollDiceSound == null) {
				rollDiceSound = MediaPlayer.create(app, R.raw.roll);
			}
			if (rollDiceSound != null) {
				rollDiceSound.start();
			}
		}
	}
	
	private void performRollPopup(RollResult res) {
		//Create the references to the roll toast if not exist
		if (rollDiceToast == null) {
			View rollDiceLayout = getLayoutInflater().inflate(
					R.layout.dice_roll_toast,
					null);

			rollDiceView = (View)rollDiceLayout.findViewById(R.id.drtRolling);
			rollDiceImage = (ImageView)rollDiceLayout.findViewById(R.id.drtImg);
			rollDiceText = (TextView)rollDiceLayout.findViewById(R.id.drtText);

			rollDiceToast = new Toast(getApplicationContext());
			rollDiceToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			rollDiceToast.setDuration(Toast.LENGTH_SHORT);
			rollDiceToast.setView(rollDiceLayout);
		}
		
		//#######################################################
		//Following block is needed only with rotating animations
		//#######################################################
//		//Check if the result is uniquely composed by 6 or 9 or 0
//		//and thus require a dot to tell if is upside down.
//		String result = Long.toString(res.getResultValue());
//		boolean dot = true;
//		for (int i = 0; i < result.length() && dot == true; i++) {
//			dot = result.charAt(i) == '6' || result.charAt(i) == '9' || result.charAt(i) == '0';
//		}
//		if (dot) {
//			rollDiceText.setText(result + ".");
//		} else {
//			rollDiceText.setText(result);
//		}
		//#######################################################
		rollDiceText.setText(Long.toString(res.getResultValue()));
		//#######################################################

		//Create the shape of the dice
		int resIndex = res.getResourceIndex();
		//int color = graphicManager.getDiceColor(resIndex);
		
		//rollDiceImage.setImageBitmap(graphicManager.getShape(resIndex, color));
		rollDiceImage.setImageDrawable(graphicManager.getDiceIconShape(resIndex, resIndex));

		rollDiceToast.show();
	}
	
	private void performRollAnimation(RollResult res) {
		if (rollDiceAnimation == null) {
			rollDiceAnimation = AnimationUtils.loadAnimation(app, R.anim.dice_roll);
		}
		rollDiceView.startAnimation(rollDiceAnimation);
	}

	/**
	 * Update the content of the last roll result.
	 */
	private void refreshLastResult() {
		lastResHolder.setEnabled(lastResult.length > 0);
		
		ResultListAdapter.bindData(
				this, 
				lastResult, 
				lastResDieImage, 
				lastResName, 
				lastResText, 
				lastResValue,
				lastResResultImage);
		
		if (undoManager.canUndoAll()) {
			undoAllButton.setVisibility(View.VISIBLE);
		} else {
			undoAllButton.setVisibility(View.GONE);
		}
	}

	/**
	 * Refresh the result grid layout after the data are changed.
	 */
	private void refreshResultList() {
		while (resultList.size() > pref.getMaxResultList()) {
			resultList.remove(resultList.size() - 1);
		}
		((ResultListAdapter)gvResults.getAdapter()).notifyDataSetChanged();
	}
	
	/**
	 * Refresh the dice bags grid layout after the data are changed.
	 */
	private void refreshBagsList() {
		((DiceBagAdapter)lvDiceBag.getAdapter()).notifyDataSetChanged();
		//lvDiceBag.invalidateViews();
	}
	
	/**
	 * Refresh the dice grid layout after the data are changed.
	 */
	private void refreshDiceList() {
		((GridExpressionAdapter)gvDice.getAdapter()).notifyDataSetChanged();
	}
	
	/**
	 * Refresh the modifier list layout after the data are changed.
	 */
	private void refreshModifierList() {
		initModifierList();
	}
	
	private void refreshAllDiceContainers() {
		refreshAllDiceContainers(false);
	}
	
	private void refreshAllDiceContainers(boolean afterImport) {
		diceBag = diceBagManager.getDice();
		bonusBag = diceBagManager.getModifiers();

		//refreshDiceList();
		gvDice.setAdapter(new GridExpressionAdapter(
				QuickDiceActivity.this,
				R.layout.dice_item,
				diceBag));

		initModifierList();

		if (afterImport) {
			lvDiceBag.setAdapter(new DiceBagAdapter(
					this,
					R.layout.dice_bag_item,
					diceBagManager.getDiceBags()));
		} else {
			refreshBagsList();
		}
	}
	
	private void callEditDiceBag(int requestType, DiceBag bag, int position) {
		if (requestType == EditBagActivity.ACTIVITY_ADD 
				&& diceBagManager.getDiceBags().size() >= pref.getMaxDiceBags()) {
			//Maximum number of allowed bags reached
			Toast.makeText(this, R.string.msgMaxBagsReach, Toast.LENGTH_LONG).show();
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt(EditBagActivity.BUNDLE_REQUEST_TYPE, requestType);
		bundle.putSerializable(EditBagActivity.BUNDLE_DICE_BAG, bag);
		bundle.putInt(EditBagActivity.BUNDLE_POSITION, position);
		Intent i = new Intent(this, EditBagActivity.class);
		i.putExtras(bundle);
		startActivityForResult(i, requestType);
	}
	
	private void callEditDice(int requestType, DExpression exp, int position) {
		if (requestType == EditDiceActivity.ACTIVITY_ADD 
				&& diceBag.size() >= pref.getMaxDice()) {
			//Maximum number of allowed dice reached
			Toast.makeText(this, R.string.msgMaxDiceReach, Toast.LENGTH_LONG).show();
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt(EditDiceActivity.BUNDLE_REQUEST_TYPE, requestType);
		bundle.putSerializable(EditDiceActivity.BUNDLE_DICE_EXPRESSION, exp);
		bundle.putInt(EditDiceActivity.BUNDLE_POSITION, position);
		Intent i = new Intent(this, EditDiceActivity.class);
		i.putExtras(bundle);
		startActivityForResult(i, requestType);
	}
	
	private void callPreferences() {
		startActivityForResult(new Intent(this, PrefDiceActivity.class), PrefDiceActivity.ACTIVITY_EDIT_PREF);
	}
	
	private void clearRollResult(int position) {
		clearRollResult(new int[] {position});
	}
	
	private void clearRollResult(int[] positions) {
		boolean refreshLast = false;
		RollResult[] removed;

		//Element indexes are ordered in growing order
		for (int i = 0; i < positions.length; i++) {
			if (positions[i] >= 0) {
				//Clear result at given list position
				removed = resultList.remove(positions[i]);
			} else {
				//Clear result in lastResult box
				removed = lastResult;
				if (resultList.size() > 0) {
					lastResult = resultList.remove(0);
				} else {
					lastResult = new RollResult[0];
				}
				refreshLast = true;
			}
			undoManager.addToUndoList(positions[i], removed);
		}
		if (refreshLast) refreshLastResult();
		refreshResultList();
	}
	
	private void invalidateUndo() {
		undoManager.resetUndoList();
		undoManager.resetUndoAll();
	}
	
	private void restoreFromUndoList() {
		UndoManager.RollResultUndo undo = undoManager.restoreFromUndoList();
		
		if (undo != null) {
			if (undo.getPosition() < 0) {
				//Top element
				if (lastResult.length > 0) {
					resultList.add(0, lastResult);
					refreshResultList();
				}
				lastResult = undo.getRes();
				refreshLastResult();
			} else {
				//Grid element
				if (undo.getPosition() < resultList.size()) {
					resultList.add(undo.getPosition(), undo.getRes());
					refreshResultList();
				} else {
					//Last element
					resultList.add(undo.getRes());
					refreshResultList();
				}
			}
		}
	}

	private void clearAllRollResult() {
		if (lastResult.length > 0) {
			undoManager.addToUndoAll(lastResult, resultList);
			lastResult = new RollResult[0];
			resultList.clear();
			refreshLastResult();
			refreshResultList();
		}
	}
	
	private void restoreFromUndoAll() {
		if (undoManager.canUndoAll()) {
			resultList.clear();
			resultList.addAll(undoManager.restoreFromUndoAll());
	
			if (resultList.size() > 0) {
				lastResult = resultList.remove(0);
			} else {
				lastResult = new RollResult[0];
			}
	
			refreshLastResult();
			refreshResultList();
		}
	}
	
//	private void sendSuggestion() {
//		Intent i = new Intent(Intent.ACTION_SEND);
//		i.setType("message/rfc822");
//		i.putExtra(Intent.EXTRA_EMAIL, new String[]{"ohmnibus@gmail.com"});
//		i.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.lblMailSubject));
//		//i.putExtra(Intent.EXTRA_TEXT, "body");
//		try {
//			startActivity(Intent.createChooser(i, res.getString(R.string.lblSendMail)));
//		} catch (android.content.ActivityNotFoundException ex) {
//			Toast.makeText(this, R.string.err_no_email_client, Toast.LENGTH_SHORT).show();
//		}
//	}
	
	private void callImportExport() {
		Bundle bundle = new Bundle();
		bundle.putInt(ImportExportActivity.BUNDLE_REQUEST_TYPE, ImportExportActivity.ACTIVITY_IMPORT_EXPORT);
		Intent i = new Intent(this, ImportExportActivity.class);
		i.putExtras(bundle);
		startActivityForResult(i, ImportExportActivity.ACTIVITY_IMPORT_EXPORT);
	}

	private RollResult[] linkResult(RollResult[] prevResult, RollResult[] nextResult) {
		RollResult[] retVal;
		retVal = new RollResult[prevResult.length + nextResult.length];
		
		//Older rolls to the left
		for (int i = 0; i < nextResult.length; i++) {
			retVal[i] = nextResult[i];
		}

		for (int i = 0; i < prevResult.length; i++) {
			retVal[nextResult.length + i] = prevResult[i];
		}

		return retVal;
	}
	
	private void callAddModifier(int position) {
		if (bonusBag.size() >= pref.getMaxModifiers()) {
			//Maximum number of allowed modifiers reached
			Toast.makeText(this, R.string.msgMaxModifiersReach, Toast.LENGTH_LONG).show();
		} else {
			new ModifierBuilderDialog(this, position, modifierBuilderReadyListener).show();
		}
	}
	
	private ModifierBuilderDialog.ReadyListener modifierBuilderReadyListener = new ReadyListener() {
		@Override
		public void ready(boolean confirmed, int modifier, int position) {
			if (confirmed) {
				addModifier(
						modifier,
						position == ModifierBuilderDialog.POSITION_UNDEFINED ? -1 : position);
			}
		}
	};
	
	private void moveDice(int fromDiceBagIndex, int fromPosition, int toDiceBagIndex, int toPosition) {
		if (diceBagManager.moveDie(
					fromDiceBagIndex,
					fromPosition,
					toDiceBagIndex,
					toPosition)) {
			
			refreshDiceList();
		}
	}
	
	private void addModifier(int modifier, int position) {
		RollModifier newMod;
		boolean duplicate = false;

		if (modifier == 0) {
			//Neutral modifier
			Toast.makeText(this, R.string.lblNeutralModifier, Toast.LENGTH_LONG).show();
			return;
		}
		
		for (RollModifier mod : bonusBag) {
			if (mod.getValue() == modifier) {
				duplicate = true;
				break;
			}
		}
		if (duplicate) {
			//Duplicate modifier
			Toast.makeText(this, R.string.lblDuplicateModifier, Toast.LENGTH_LONG).show();
		} else {
			newMod = new RollModifier(app, modifier);
			diceBagManager.addModifier(position, newMod);
			refreshModifierList();
		}
	}
	
	CenterInParent centerInParentAgent = null;
	ResetLinkSwitch resetLinkSwitchAgent = null;
	//Handler myHandler = null;
	Handler myHandler = new Handler();

	private void resetLinkSwitch(int timeout) {
		if (resetLinkSwitchAgent == null) {
			resetLinkSwitchAgent = new ResetLinkSwitch();
		}
		myHandler.removeCallbacks(resetLinkSwitchAgent);
		myHandler.postDelayed(resetLinkSwitchAgent, timeout);
	}
	
	class ResetLinkSwitch implements Runnable {
		public void run() {
			if (linkRoll) {
				linkRoll = false;
				refreshLinkSwitchButton();
			}
		}
	}

	private void centerInParent(View view) {
		if (centerInParentAgent == null) {
			centerInParentAgent = new CenterInParent();
		}
		centerInParentAgent.setView(view);
//		if (myHandler == null) {
//			myHandler = new Handler();
//		}
		myHandler.removeCallbacks(centerInParentAgent);
		myHandler.postDelayed(centerInParentAgent, 500);
	}
	
	class CenterInParent implements Runnable {
		View mChild;

		public void setView(View child) {
			mChild = child;
		}

		public void run() {
			if (mChild.getParent() instanceof HorizontalScrollView) {
				HorizontalScrollView parent = (HorizontalScrollView) mChild.getParent();
				if (parent.getWidth() < mChild.getWidth()) {
					int scroll = (mChild.getWidth() - parent.getWidth()) / 2;
					parent.scrollTo(scroll, 0);
				}
			}
		}
	}

}