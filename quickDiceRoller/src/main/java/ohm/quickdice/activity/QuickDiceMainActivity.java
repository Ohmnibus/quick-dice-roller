package ohm.quickdice.activity;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
import ohm.quickdice.adapter.ResultListAdapter.ItemViews;
import ohm.quickdice.adapter.VariableAdapter;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.control.PreferenceManager;
import ohm.quickdice.control.SerializationManager;
import ohm.quickdice.control.UndoManager;
import ohm.quickdice.dialog.DialogHelper;
import ohm.quickdice.dialog.DiceDetailDialog;
import ohm.quickdice.dialog.DicePickerDialog;
import ohm.quickdice.dialog.ModifierBuilderDialog;
import ohm.quickdice.dialog.ModifierBuilderDialog.OnCreatedListener;
import ohm.quickdice.dialog.RollDetailDialog;
import ohm.quickdice.dialog.VariableDetailDialog;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.Modifier;
import ohm.quickdice.entity.PercentModifier;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.RollResult;
import ohm.quickdice.entity.VarModifier;
import ohm.quickdice.entity.Variable;
import ohm.quickdice.util.AsyncDrawable;
import ohm.quickdice.util.Behavior;
import ohm.quickdice.util.Helper;
import ohm.quickdice.util.Helper.BackgroundManager;
import ohm.quickdice.util.RollDiceToast;
import ohm.quickdice.util.SynchRunnable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HeaderViewListAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main {@link Activity} of the app.<br />
 * Renamed from {@code QuickDiceActivity} to {@code QuickDiceMainActivity} in
 * order to realize a splash screen. See {@link QuickDiceActivity} for details.
 * @author Ohmnibus
 *
 */
public class QuickDiceMainActivity extends BaseActivity {
	
	private static final String TAG = "QuickDiceMainActivity";
	
	int currentTheme;
	QuickDiceApp app;
	Resources res;
	//GraphicManager graphicManager;
	PreferenceManager pref;
	UndoManager undoManager;
	
	boolean backedUpData = true;
	DiceBagManager diceBagManager;
	/** Currently selected dice bag */
	DiceBag diceBag;
	
	RollResult[] lastResult;
	ArrayList<RollResult[]> resultList;
	
	ListView lvDiceBag;
	ListView lvVariable;
	GridView gvResults;
	GridView gvDice;
	ViewGroup vgModifiers = null;
	
	View addDiceBag;
	View addVariable;
	
	DrawerLayout drawer;
	ActionBarDrawerToggle drawerToggle;
	CompatActionBar actionBar;
	
	//Cache for "modifiers"
	ArrayList<View> modifierViewList;
	View modifiersHolder = null;

	//Cache for "lastResult"
	View lastResHolder;
	ResultListAdapter.ItemViews lastResViews;
	ImageButton linkSwitchButton;
	Button undoAllButton;
	boolean linkRoll = false;

	RollDiceToast rollDiceToast;
	
	private final int LINK_LEVEL_OFF = 0;
	private final int LINK_LEVEL_ON = 1;

	private final String KEY_ROLL_LIST = "KEY_ROLL_LIST";

//	private final String TYPE_MODIFIER = "TYPE_MODIFIER";
//	private final String TYPE_VARIABLE = "TYPE_VARIABLE";
//	private final String TYPE_PERCENTAGE = "TYPE_PERCENTAGE";
	private final int TYPE_MODIFIER = RollModifier.TYPE_ID;
	private final int TYPE_VARIABLE = VarModifier.TYPE_ID;
	private final int TYPE_PERCENTAGE = PercentModifier.TYPE_ID;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		currentTheme = QuickDiceApp.getInstance().getPreferences().getThemeResId();
		setTheme(currentTheme);

		super.onCreate(savedInstanceState);

		//Initializations
		app = QuickDiceApp.getInstance();
		res = getResources();
		//graphicManager = app.getGraphic();
		pref = app.getPreferences();
		undoManager = UndoManager.getInstance();

		diceBagManager = app.getBagManager();
		diceBagManager.init();

		diceBag = diceBagManager.getDiceBagCollection().getCurrent();

		modifierViewList = new ArrayList<View>();

		rollDiceToast = new RollDiceToast(this, pref, diceBagManager);

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
		
		Helper.setWakeLock(this, pref.getWakeLock());
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
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
	protected void onDestroy() {
		if (rollDiceToast != null) {
			rollDiceToast.shutdown();
		}
		super.onDestroy();
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
		menu.findItem(R.id.mmAddDice).setVisible(app.canAddDiceBag());
		menu.findItem(R.id.mmAddModifier).setVisible(pref.getShowModifiers());

		return retVal;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean retVal;

		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		retVal = true;

		switch (item.getItemId()) {
			case R.id.mmOpenDiceBagDrawer:
				drawer.openDrawer(lvDiceBag);
				break;
			case R.id.mmOpenVariableDrawer:
				drawer.openDrawer(lvVariable);
				break;
//			case R.id.mmAddDiceBag:
//				EditBagActivity.callInsert(this);
//				break;
			case R.id.mmAddDice:
				//callEditDice(EditDiceActivity.ACTIVITY_ADD, null, EditDiceActivity.POSITION_UNDEFINED);
				EditDiceActivity.callInsert(this);
				break;
//			case R.id.mmAddVariable:
//				EditVariableActivity.callInsert(this);
//				break;
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
			case R.id.mmQuickStart:
				Intent i = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse(getString(R.string.urlQuickStart)));

				//startActivity(i);
				Helper.checkAndStartActivity(this, i, R.string.err_cannot_show_help);
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

		switch (requestCode) {
			case EditBagActivity.ACTIVITY_ADD:
				if (resultCode == RESULT_OK) {
					//Add new dice bag
					//DiceBag newBag = getDiceBagFromIntent(data);
					DiceBag newBag = EditBagActivity.getDiceBag(data);
					if (newBag != null) {
						//int position = getDiceBagPositionFromIntent(data);
						int position = EditBagActivity.getDiceBagPosition(data);
						//diceBagManager.addDiceBag(position, newBag);
						position = diceBagManager.getDiceBagCollection().add(position, newBag);
						diceBagManager.setCurrentIndex(position);
						refreshAllDiceContainers();
					}
				}
				break;
			case EditBagActivity.ACTIVITY_EDIT:
				if (resultCode == RESULT_OK) {
					//Edit dice bag
					//DiceBag newBag = getDiceBagFromIntent(data);
					DiceBag newBag = EditBagActivity.getDiceBag(data);
					if (newBag != null) {
						//int position = getDiceBagPositionFromIntent(data);
						int position = EditBagActivity.getDiceBagPosition(data);
						//diceBagManager.editDiceBag(position, newBag);
						diceBagManager.getDiceBagCollection().edit(position, newBag);
						refreshBagsList();
					}
				}
				break;
			case EditDiceActivity.ACTIVITY_ADD:
				if (resultCode == RESULT_OK) {
					//Add new die
					//Dice newExp = getDiceFromIntent(data);
					Dice newExp = EditDiceActivity.getDice(data);
					if (newExp != null) {
						//int position = getExpressionPositionFromIntent(data);
						int position = EditDiceActivity.getDicePosition(data);
						//diceBagManager.addDice(position, newExp);
						diceBag.getDice().add(position, newExp);
						refreshDiceList();
					}
				}
				break;
			case EditDiceActivity.ACTIVITY_EDIT:
				if (resultCode == RESULT_OK) {
					//Edit die
					//Dice newExp = getDiceFromIntent(data);
					Dice newExp = EditDiceActivity.getDice(data);
					if (newExp != null) {
						//int position = getExpressionPositionFromIntent(data);
						int position = EditDiceActivity.getDicePosition(data);
						//diceBagManager.editDice(position, newExp);
						diceBag.getDice().edit(position, newExp);
						refreshDiceList();
					}
				}
				break;
			case EditVariableActivity.ACTIVITY_ADD:
				if (resultCode == RESULT_OK) {
					//Add new Variable
					Variable newVar = EditVariableActivity.getVariableData(data);
					if (newVar != null) {
						int position = EditVariableActivity.getVariablePosition(data);
						//diceBagManager.addVariable(position, newVar);
						diceBag.getVariables().add(position, newVar);
						refreshVariablesList();
					}
				}
				break;
			case EditVariableActivity.ACTIVITY_EDIT:
				if (resultCode == RESULT_OK) {
					//Edit Variable
					Variable newVar = EditVariableActivity.getVariableData(data);
					if (newVar != null) {
						int position = EditVariableActivity.getVariablePosition(data);
						//diceBagManager.editVariable(position, newVar);
						diceBag.getVariables().edit(position, newVar);
						refreshVariablesList();
					}
				}
				break;
			case ImportExportActivity.ACTIVITY_IMPORT_EXPORT:
				if (resultCode == ImportExportActivity.RESULT_EXPORT) {
					Toast.makeText(this, R.string.msgExported, Toast.LENGTH_SHORT).show();
				} else if (resultCode == ImportExportActivity.RESULT_IMPORT) {
					Toast.makeText(this, R.string.msgImported, Toast.LENGTH_SHORT).show();
					refreshAllDiceContainers(true);
					//} else if (resultCode == ImportExportActivity.RESULT_IMPORT_FAILED) {
					//NOOP
					//(Error is already notified by persistence manager)
				}
				break;
			case PrefDiceActivity.ACTIVITY_EDIT_PREF:
				//Apply new preferences
				
				//Request for backup.
				backedUpData = false;
	
				//Reset configuration cache
				pref.resetCache();

				//Check if needed a new initialization
				boolean reInit = false;

				//If custom background was disabled, then we need a new initialization (no more)
				//reInit = reInit || (customBackground && ! pref.getCustomBackground());
				
				//If theme is changed, then we need a new initialization
				reInit = reInit || (pref.getThemeResId() != currentTheme);
	
				if (reInit) {
					app.getPersistence().saveResultList(lastResult, resultList);
					finish();
					Intent intent = new Intent(this, QuickDiceMainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} else {
					//Number of columns
					gvResults.setNumColumns(pref.getGridResultColumn());
		
					//Swap name and results
					ResultListAdapter.setSwapNameResult(pref.getSwapNameResult());
		
					//Pop Up
					rollDiceToast.refreshConfig();
					
					//Wake Lock
					Helper.setWakeLock(this, pref.getWakeLock());
		
					//Modifiers bar
					initModifierList();
					refreshResultList();
					refreshLastResult();
					
					//Activate or change Background
					initBackground();
				}
				break;
		}
	}
	
	private static final long CONTEXT_MENU_COOL_DOWN = 900; //0.9 seconds cool down
	long contextMenuCooledDown = Long.MIN_VALUE;
	
	/**
	 * Check for cool down.<br />
	 * Some actions, like rich context menus, can trigger a click if
	 * touch is released between long click invocation and the display
	 * of it's pop-up.<br />
	 * This method check if it is passed enough time.
	 * @return {@code true} if action can be performed,
	 * {@code false} if not cooled down yet.
	 */
	private boolean checkCoolDown() {
		return System.currentTimeMillis() >= contextMenuCooledDown;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		contextMenuCooledDown = System.currentTimeMillis() + CONTEXT_MENU_COOL_DOWN;
		
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		int index = info == null ? 0 : (int)info.id;
		//int type = v.getTag(R.id.key_type) == null ? -1 :
		
		if (v.getId() == lvDiceBag.getId()) {
			//Context menu for the dice bags list
			setupDiceBagMenu(menu, index);
		} else if (v.getId() == lvVariable.getId()) {
			//Context menu for the variable
			setupVariableMenu(menu, index, false, null);
		} else if (v.getId() == gvDice.getId()) {
			//Context menu for the dice bag
			setupDiceMenu(menu, index);
		} else if (v.getId() == gvResults.getId()) {
			//Context menu for the result list
			setupRollMenu(menu, index);
		} else if (((Integer)TYPE_MODIFIER).equals(v.getTag(R.id.key_type))) {
			//Context menu for a modifier
			setupModifierMenu(menu, (Integer)v.getTag(R.id.key_value));
		} else if (((Integer)TYPE_PERCENTAGE).equals(v.getTag(R.id.key_type))) {
			//Context menu for a percent modifier
			setupModifierMenu(menu, (Integer)v.getTag(R.id.key_value));
		} else if (((Integer)TYPE_VARIABLE).equals(v.getTag(R.id.key_type))) {
			//Context menu for a modifier
			int vmIndex;
			VarModifier vm;
			Variable var = null;
			vmIndex = (Integer)v.getTag(R.id.key_value);
			modifierOpeningMenu = vmIndex;
			SparseBooleanArray hidden = getHiddenModifiersMenuItems(vmIndex);
			vm = (VarModifier)diceBag.getModifiers().get(vmIndex);
			if (vm != null) {
				var = diceBag.getVariables().getByLabel(vm.getLabel());
			}
			if (var != null) {
				setupVariableMenu(menu, var.getID(), true, hidden);
			} else {
				setupModifierMenu(menu, (Integer)v.getTag(R.id.key_value));
			}
		} else {
			//Context menu for the last result item
			if (lastResult.length > 0) {
				setupRollMenu(menu, -1);
			}
		}
	}

	protected void setupDiceBagMenu(ContextMenu menu, int index) {
		DiceBag bag;
		int bagNum;
		
		//bag = (DiceBag)lvDiceBag.getItemAtPosition(index);
		bag = diceBagManager.getDiceBagCollection().get(index);
		bagNum = diceBagManager.getDiceBagCollection().size();
		

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_dice_bag, menu);
		
		//Get the dice icon and resize it to fit the menu header icon size.
//		Drawable diceBagIcon = graphicManager.getResizedDiceIcon(
//				bag.getResourceIndex(), 32, 32);
		Drawable diceBagIcon = diceBagManager.getIconDrawable(
				bag.getResourceIndex(),
				R.dimen.header_icon_size,
				R.dimen.header_icon_size);
		
		menu.setHeaderIcon(diceBagIcon);
		menu.setHeaderTitle(bag.getName());
		
		if (bagNum == 1) {
			//Only one element
			menu.findItem(R.id.mdbRemove).setVisible(false);
		}
		if (! app.canAddDiceBag()) { // if (bagNum >= pref.getMaxDiceBags()) {
			//Maximum number of allowed dice bags reached
			menu.findItem(R.id.mdbAddHere).setVisible(false);
			menu.findItem(R.id.mdbClone).setVisible(false);
		}
		if (index == 0) {
			//First element
			menu.findItem(R.id.mdbSwitchPrev).setVisible(false);
		}
		if (index == bagNum - 1) {
			//Last element
			menu.findItem(R.id.mdbSwitchNext).setVisible(false);
		}
	}
	
	protected void setupVariableMenu(ContextMenu menu, int index, boolean treatAsModifier, SparseBooleanArray hiddenMenuItems) {
		MenuInflater inflater = getMenuInflater();
		if (treatAsModifier) {
			inflater.inflate(R.menu.menu_modifier, menu);
		} else {
			inflater.inflate(R.menu.menu_variable, menu);
		}
		
		//VariableDetailDialog dlg = new VariableDetailDialog(this, menu, diceBagManager.getDiceBag(), index);
		VariableDetailDialog dlg = new VariableDetailDialog(this, menu, diceBag, index, treatAsModifier, hiddenMenuItems);
		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				//A variable value may have been changed
				//so a refresh is needed.
				refreshVariablesList();
				//A variable can be used in modifiers list.
				//If so, refresh modifier list, too.
				if (diceBag.getModifiers().containVariables()) {
					refreshModifierList();
				}
			}
		});
		dlg.show();
		menu.clear();
	}

	protected void setupDiceMenu(ContextMenu menu, int index) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_dice, menu);
		
		//DiceDetailDialog dlg = new DiceDetailDialog(this, diceBagManager.getDiceBag(), index, menu);
		DiceDetailDialog dlg = new DiceDetailDialog(this, diceBag, index, menu);
		dlg.show();
		menu.clear();
	}
	
	protected int modifierOpeningMenu;
	
	protected void setupModifierMenu(ContextMenu menu, int index) {
		Modifier modifier;
		Drawable modIcon;
		
		modifierOpeningMenu = index;
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_modifier, menu);

		modifier = diceBag.getModifiers().get(index);
//		modIcon = graphicManager.getResizedDiceIcon(
//				modifier.getResourceIndex(), 32, 32);
		modIcon = diceBagManager.getIconDrawable(
				modifier.getResourceIndex(),
				R.dimen.header_icon_size,
				R.dimen.header_icon_size);
		
		menu.setHeaderIcon(modIcon);
		menu.setHeaderTitle(modifier.getName());

//		if (lastResult.length == 0) {
//			//No rolls to add bonus to
//			menu.findItem(R.id.moApply).setVisible(false);
//		}
//		if (diceBag.getModifiers().size() == 1) {
//			//Only one element
//			menu.findItem(R.id.moRemove).setVisible(false);
//		}
//		if (diceBag.getModifiers().size() >= pref.getMaxModifiers()) {
//			//Maximum number of allowed modifiers reached
//			menu.findItem(R.id.moAddHere).setVisible(false);
//		}
//		if (index == 0) {
//			//First element
//			menu.findItem(R.id.moSwitchPrev).setVisible(false);
//		}
//		if (index == diceBag.getModifiers().size() - 1) {
//			//Last element
//			menu.findItem(R.id.moSwitchNext).setVisible(false);
//		}
		SparseBooleanArray hidden = getHiddenModifiersMenuItems(index);
		for (int i = 0; i < hidden.size(); i++) {
			menu.findItem(hidden.keyAt(i)).setVisible(false);
		}
	}

	private SparseBooleanArray getHiddenModifiersMenuItems(int index) {
		SparseBooleanArray retVal = new SparseBooleanArray();

		if (lastResult.length == 0) {
			//No rolls to add bonus to
			retVal.append(R.id.moApply, false);
		}
		if (diceBag.getModifiers().size() == 1) {
			//Only one element
			retVal.append(R.id.moRemove, false);
		}
		if (diceBag.getModifiers().size() >= pref.getMaxModifiers()) {
			//Maximum number of allowed modifiers reached
			retVal.append(R.id.moAddHere, false);
		}
		if (index == 0) {
			//First element
			retVal.append(R.id.moSwitchPrev, false);
		}
		if (index == diceBag.getModifiers().size() - 1) {
			//Last element
			retVal.append(R.id.moSwitchNext, false);
		}
		return retVal;
	}

	protected void setupRollMenu(ContextMenu menu, int index) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_roll, menu);
		
		RollDetailDialog dlg = new RollDetailDialog(this, menu, lastResult, resultList, index);
		dlg.show();
		menu.clear();
	}

	protected int targetItem;
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean retVal;
		
		//Menu was closed, reset cooldown
		contextMenuCooledDown = System.currentTimeMillis();
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		//When an handler consume the event and return true,
		//retVal become also true and short circuiting boolean logic
		//avoid to call other handlers
		
		retVal = false;

		retVal = retVal || onDiceBagContextItemSelected(item, info);
		
		retVal = retVal || onDiceContextItemSelected(item, info);
		
		retVal = retVal || onResultContextItemSelected(item, info);
		
		retVal = retVal || onModifierContextItemSelected(item, info);
		
		retVal = retVal || onVariableContextItemSelected(item, info);
		
		retVal = retVal || super.onContextItemSelected(item);
		
		return retVal;
	}
	
	public boolean onDiceBagContextItemSelected(MenuItem item, AdapterContextMenuInfo info) {
		boolean retVal;
		int index;
		DiceBag bag;
		AlertDialog.Builder builder;
		
		retVal = true;
		
		index = info == null ? 0 : (int)info.id;

		switch (item.getItemId()) {
			case R.id.mdbSelect:
				diceBagManager.setCurrentIndex(index);
				refreshAllDiceContainers();
				drawer.closeDrawer(lvDiceBag);
				break;
			case R.id.mdbEdit:
				bag = (DiceBag)lvDiceBag.getItemAtPosition(info.position);
				//callEditDiceBag(EditBagActivity.ACTIVITY_EDIT, bag, info.position);
				EditBagActivity.callEdit(this, index, bag);
				break;
			case R.id.mdbAddHere:
				//callEditDiceBag(EditBagActivity.ACTIVITY_ADD, null, info.position);
				EditBagActivity.callInsert(this, index);
				break;
			case R.id.mdbClone:
				if (lvDiceBag.getCount() >= pref.getMaxDiceBags()) {
					//Maximum number of allowed dice bags reached
					Toast.makeText(this, R.string.msgMaxBagsReach, Toast.LENGTH_LONG).show();
				} else {
					//diceBagManager.cloneDiceBag(info.position);
					diceBagManager.getDiceBagCollection().duplicate(index);
					refreshAllDiceContainers();
				}
				break;
			case R.id.mdbRemove:
				//Ask confirmation prior to delete a dice bag.
				targetItem = index;
				bag = (DiceBag)lvDiceBag.getItemAtPosition(info.position);
				builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.msgRemoveDiceBagTitle);
				builder.setMessage(res.getString(
						R.string.msgRemoveDiceBagNew,
						bag.getName(),
						bag.getDice().size(),
						bag.getVariables().size(),
						bag.getModifiers().size()));
				builder.setPositiveButton(
						R.string.lblYes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								//diceBagManager.removeDiceBag(targetItem);
								diceBagManager.getDiceBagCollection().remove(targetItem);
								refreshAllDiceContainers();
							}
						});
				builder.setNegativeButton(
						R.string.lblNo,
						cancelDialogClickListener);
				builder.create().show();
				break;
			case R.id.mdbSwitchPrev:
				//diceBagManager.moveDiceBag(info.position, info.position - 1);
				diceBagManager.getDiceBagCollection().move(index, index - 1);
				refreshAllDiceContainers();
				break;
			case R.id.mdbSwitchNext:
				//diceBagManager.moveDiceBag(info.position, info.position + 1);
				diceBagManager.getDiceBagCollection().move(index, index + 1);
				refreshAllDiceContainers();
				break;
			default:
				retVal = false;
				break;
		}

		return retVal;
	}
	
	public boolean onDiceContextItemSelected(MenuItem item, AdapterContextMenuInfo info) {
		boolean retVal;
		Dice dice;
		AlertDialog.Builder builder;
		
		retVal = true;

		switch (item.getItemId()) {
			case R.id.mdRoll:
				dice = (Dice)gvDice.getItemAtPosition(info.position);
				doRoll(dice);
				break;
			case R.id.mdEdit:
				dice = (Dice)gvDice.getItemAtPosition(info.position);
				//callEditDice(EditDiceActivity.ACTIVITY_EDIT, dice, info.position);
				EditDiceActivity.callEdit(this, info.position, dice);
				break;
			case R.id.mdRemove:
				//Ask confirmation prior to delete a dice.
				targetItem = info.position;
				dice = (Dice)gvDice.getItemAtPosition(info.position);
				builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.msgRemoveDiceTitle);
				builder.setMessage(res.getString(R.string.msgRemoveDice, dice.getName()));
				builder.setPositiveButton(
						R.string.lblYes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								//diceBagManager.removeDice(targetItem);
								diceBag.getDice().remove(targetItem);
								refreshDiceList();
							}
						});
				builder.setNegativeButton(
						R.string.lblNo,
						cancelDialogClickListener);
				builder.create().show();
				break;
			case R.id.mdAddHere:
				//callEditDice(EditDiceActivity.ACTIVITY_ADD, null, info.position);
				EditDiceActivity.callInsert(this, info.position);
				break;
			case R.id.mdClone:
				if (diceBag.getDice().size() >= pref.getMaxDice()) {
					//Maximum number of allowed dice reached
					Toast.makeText(this, R.string.msgMaxDiceReach, Toast.LENGTH_LONG).show();
				} else {
					//diceBagManager.cloneDice(info.position);
					diceBag.getDice().duplicate(info.position);
					refreshAllDiceContainers();
				}
				break;
			case R.id.mdMoveTo:
				targetItem = info.position;
				new DicePickerDialog(
						this,
						R.string.lblSelectDiceDest,
						diceBagManager.getCurrentIndex(),
						info.position,
						DicePickerDialog.DIALOG_SELECT_DESTINATION,
						new DicePickerDialog.ReadyListener() {
							@Override
							public void ready(boolean confirmed, int groupId, int itemId) {
								if (confirmed) {
									moveDice(
											diceBagManager.getCurrentIndex(),
											targetItem,
											groupId,
											itemId);
								}
							}
						}).show();
				break;
			default:
				retVal = false;
				break;
		}

		return retVal;
	}

	public boolean onResultContextItemSelected(MenuItem item, AdapterContextMenuInfo info) {
		boolean retVal;
		RollResult[] result;
		
		retVal = true;

		switch (item.getItemId()) {
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
			default:
				retVal = false;
				break;
		}

		return retVal;
	}
	
	public boolean onModifierContextItemSelected(MenuItem item, AdapterContextMenuInfo info) {
		boolean retVal;
		AlertDialog.Builder builder;
		Modifier mod;
		
		retVal = true;

		switch (item.getItemId()) {
			case R.id.moApply:
				//doModifier(bonusBag.get(modifierOpeningMenu));
				doModifier(diceBag.getModifiers().get(modifierOpeningMenu));
				invalidateUndo();
				break;
			case R.id.moRemove:
				// Ask confirmation prior to delete a modifier.
				targetItem = modifierOpeningMenu;
				//mod = bonusBag.get(modifierOpeningMenu);
				mod = diceBag.getModifiers().get(modifierOpeningMenu);
				builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.msgRemoveModTitle);
				builder.setMessage(res.getString(R.string.msgRemoveMod, mod.getName()));
				builder.setPositiveButton(
						R.string.lblYes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								//diceBagManager.removeModifier(targetItem);
								diceBag.getModifiers().remove(targetItem);
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
				//diceBagManager.moveModifier(modifierOpeningMenu, modifierOpeningMenu - 1);
				diceBag.getModifiers().move(modifierOpeningMenu, modifierOpeningMenu - 1);
				refreshModifierList();
				break;
			case R.id.moSwitchNext:
				//diceBagManager.moveModifier(modifierOpeningMenu, modifierOpeningMenu + 1);
				diceBag.getModifiers().move(modifierOpeningMenu + 1, modifierOpeningMenu);
				refreshModifierList();
				break;
			default:
				retVal = false;
				break;
		}
		
		return retVal;
	}
	
	public boolean onVariableContextItemSelected(MenuItem item, AdapterContextMenuInfo info) {
		boolean retVal;
		int index;
		Variable var;
		AlertDialog.Builder builder;
		
		retVal = true;
		
		index = info == null ? 0 : (int)info.id;

		switch (item.getItemId()) {
			case R.id.mvEdit:
				var = (Variable)lvVariable.getItemAtPosition(info.position);
				EditVariableActivity.callEdit(this, index, var);
				break;
			case R.id.mvAddHere:
				EditVariableActivity.callInsert(this, index);
				break;
			case R.id.mvSwitchPrev:
				diceBag.getVariables().move(index, index - 1);
				refreshVariablesList();
				break;
			case R.id.mvSwitchNext:
				diceBag.getVariables().move(index + 1, index);
				refreshVariablesList();
				break;
			case R.id.mvRemove:
				// Ask confirmation prior to delete a variable.
				targetItem = index;
				var = diceBag.getVariables().get(targetItem);
				builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.msgRemoveModTitle);
				Dice[] affected = var.requiredBy();
				if (affected.length > 0) {
					String diceNames = "";
					for (Dice dice : affected) {
						if (diceNames.length() > 0) {
							diceNames += "\", \"";
						}
						diceNames += dice.getName();
					}
					if (affected.length == 1) {
						builder.setMessage(getString(R.string.msgRemoveUsedVar, var.getName(), diceNames));
					} else {
						builder.setMessage(getString(R.string.msgRemoveMultiUsedVar, var.getName(), diceNames));
					}
				} else {
					builder.setMessage(getString(R.string.msgRemoveVar, var.getName()));
				}
				builder.setPositiveButton(
						R.string.lblYes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								Variable removed = diceBag.getVariables().remove(targetItem);
								//Remove from modifier list
								if (removed != null && removed.getLabel() != null) {
									boolean modListChanged = false;
									for (int i = diceBag.getModifiers().size() - 1; i >= 0; i--) {
										Modifier mod = diceBag.getModifiers().get(i);
										if (mod instanceof VarModifier && removed.getLabel().equals(((VarModifier) mod).getLabel())) {
											diceBag.getModifiers().remove(i);
											modListChanged = true;
										}
									}
									if (modListChanged) {
										refreshModifierList();
									}
								}
								refreshVariablesList();
							}
						});
				builder.setNegativeButton(
						R.string.lblNo,
						cancelDialogClickListener);
				builder.show();
				break;
			default:
				retVal = false;
				break;
		}

		return retVal;
	}
	
	private void initViews() {
		setContentView(R.layout.quick_dice_activity);

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

		//Custom Background
		initBackground();
		
		//Drawers
		initDrawers();

		//Dice bag list
		initDiceBagList();
		
		//Variable list
		initVariableList();
		
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
	
	private class ActionBarDoubleDrawerToggle extends ActionBarDrawerToggle {

		private DrawerLayout drawerLayout;
		
		public ActionBarDoubleDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes) {
			super(activity, drawerLayout, drawerImageRes, R.string.app_name, R.string.app_name);
			
			this.drawerLayout = drawerLayout;
		}
		
		// android.R.id.home as defined by public API in v11
		private static final int ID_HOME = 0x0102002c;
		
		private static final int GRAVITY_FIRST = Gravity.LEFT;
		private static final int GRAVITY_SECOND = Gravity.RIGHT;

		/** Called when a drawer has settled in a completely closed state. */
		public void onDrawerClosed(View drawerView) {
			if (drawerView.getId() == R.id.mDiceBagList) {
				actionBar.setTitle(diceBag.getName());
			}
			ActivityCompat.invalidateOptionsMenu(QuickDiceMainActivity.this);
		}

		/** Called when a drawer has settled in a completely open state. */
		public void onDrawerOpened(View drawerView) {
			if (drawerView.getId() == R.id.mDiceBagList) {
				actionBar.setTitle(R.string.mnuSelectDiceBag);
			}
			ActivityCompat.invalidateOptionsMenu(QuickDiceMainActivity.this);
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item != null && item.getItemId() == ID_HOME && isDrawerIndicatorEnabled()) {
				if (drawerLayout.isDrawerVisible(GRAVITY_SECOND)) {
					drawerLayout.closeDrawer(GRAVITY_SECOND);
				} else {
					if (drawerLayout.isDrawerVisible(GRAVITY_FIRST)) {
						drawerLayout.closeDrawer(GRAVITY_FIRST);
					} else {
						drawerLayout.openDrawer(GRAVITY_FIRST);
					}
				}
				return true;
			}
			return false;
		}
	}
	
	private void initBackground() {
		//Set Custom Background
//		if (pref.getCustomBackground() && BackgroundManager.exists(this)) {
//			View v = findViewById(R.id.mRoot);
//			if (v != null) {
//				final String path = BackgroundManager.getBackgroundImagePath(this);
//				AsyncDrawable.setBackgroundDrawable(v, new AsyncDrawable.PathDrawableProvider(path));
//			}
//		}
		ImageView bg = (ImageView) findViewById(R.id.mCutomBackground);
		if (pref.getCustomBackground() && BackgroundManager.exists(this)) {
			final String path = BackgroundManager.getBackgroundImagePath(this);
			AsyncDrawable.setDrawable(bg, new AsyncDrawable.PathDrawableProvider(path));
			bg.setVisibility(View.VISIBLE);
		} else {
			bg.setVisibility(View.GONE);
		}
	}
	
	private void initDrawers() {
		drawer = (DrawerLayout)findViewById(R.id.mProfileDrawer);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			drawer.setDrawerShadow(R.drawable.drawer_shadow_left, Gravity.LEFT);
			drawer.setDrawerShadow(R.drawable.drawer_shadow_right, Gravity.RIGHT);
		} else {
			drawer.setDrawerShadow(R.drawable.ic_handle_bar_left, Gravity.LEFT);
			drawer.setDrawerShadow(R.drawable.ic_handle_bar_right, Gravity.RIGHT);
		}

		drawerToggle = new ActionBarDoubleDrawerToggle(
				this,
				drawer,
				R.drawable.ic_drawer);

		// Set the drawer toggle as the DrawerListener
		drawer.setDrawerListener(drawerToggle);
	}
	
	private void initDiceBagList() {
		actionBar.setTitle(diceBag.getName());
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		lvDiceBag = (ListView)findViewById(R.id.mDiceBagList);
		
		View title = getLayoutInflater().inflate(R.layout.inc_list_title, lvDiceBag, false);
		((TextView)title.findViewById(R.id.lblTitle)).setText(R.string.lblDiceBags);
		addDiceBag = title.findViewById(R.id.cmdAddNew);
		addDiceBag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (app.canAddDiceBag()) { //Redundant control
					EditBagActivity.callInsert(QuickDiceMainActivity.this);
				}
			}
		});
		addDiceBag.setVisibility(app.canAddDiceBag() ? View.VISIBLE : View.GONE);
		
		lvDiceBag.addHeaderView(title, null, false);

		lvDiceBag.setAdapter(new DiceBagAdapter(
				this,
				R.layout.dice_bag_item,
				diceBagManager.getDiceBagCollection()));
		lvDiceBag.setOnItemClickListener(diceBagClickListener);
		registerForContextMenu(lvDiceBag);
	}
	
	
	private void initVariableList() {

		lvVariable = (ListView)findViewById(R.id.mVariableList);
		
		View title = getLayoutInflater().inflate(R.layout.inc_list_title, lvVariable, false);
		((TextView)title.findViewById(R.id.lblTitle)).setText(R.string.lblVariables);
		addVariable = title.findViewById(R.id.cmdAddNew);
		addVariable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (app.canAddVariable()) { //Redundant control
					EditVariableActivity.callInsert(QuickDiceMainActivity.this);
				}
			}
		});
		addVariable.setVisibility(app.canAddVariable() ? View.VISIBLE : View.GONE);
		
		lvVariable.addHeaderView(title, null, false);

		lvVariable.setAdapter(new VariableAdapter(
				this,
				R.layout.item_variable,
				diceBag.getVariables()));
		lvVariable.setOnItemClickListener(variableClickListener);
		registerForContextMenu(lvVariable);
	}

	private void initDiceGrid() {

		gvDice = (GridView)findViewById(R.id.mDiceSet);

		gvDice.setAdapter(new GridExpressionAdapter(
				this,
				R.layout.dice_item,
				diceBag.getDice()));

		gvDice.setOnItemClickListener(diceClickListener);

		gvDice.setSelector(android.R.drawable.list_selector_background);

		registerForContextMenu(gvDice);
	}
	
	private void initModifierList() {
		LayoutInflater inflater;
		Modifier modifier;
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

		String label;
		for (int i = 0; i < diceBag.getModifiers().size(); i++) {
			modifier = diceBag.getModifiers().get(i);
			modView = inflater.inflate(R.layout.modifier_item, vgModifiers, false);
			
			modIcon = (ImageView)modView.findViewById(R.id.miIcon);
			modText = (TextView)modView.findViewById(R.id.miValue);
			//modIcon.setImageDrawable(graphicManager.getDiceIcon(modifier.getResourceIndex()));
			//modIcon.setImageDrawable(diceBagManager.getIconDrawable(modifier.getResourceIndex()));
			diceBagManager.setIconDrawable(modIcon, modifier.getResourceIndex());
			label = modifier.getValueString();
			modText.setText(modifier.getValueString());
			modText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ResultListAdapter.getFontSize(app, label));

			//modView.setTag(R.id.key_type, modifier instanceof VarModifier ? TYPE_VARIABLE : TYPE_MODIFIER);
			modView.setTag(R.id.key_type, modifier.getTypeID());
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
		
		gvResults.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (checkCoolDown()) {
					parent.showContextMenuForChild(view);
				}
			}
		});
	}
	
	/**
	 * This is used to pass result list item position to
	 * {@link #onCreateContextMenu(ContextMenu, View, ContextMenuInfo)}
	 * and {@link #onContextItemSelected(MenuItem)} in case of
	 * single click.<br />
	 * Use of global variables to pass parameters makes me sick, 
	 * but can't find a better way, so for now tat's it.
	 */
	//private AdapterView.AdapterContextMenuInfo mMenuInfo = null;

	private void initLastResultView() {
		lastResHolder = findViewById(R.id.mLastRollContainer);

		registerForContextMenu(lastResHolder);

		lastResHolder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//mMenuInfo = null;
				if (checkCoolDown()) {
					openContextMenu(lastResHolder);
				}
			}
		});
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

		lastResViews = new ItemViews();
		lastResViews.diceIcon = (ImageView)lastResHolder.findViewById(R.id.riImage);
		lastResViews.name = (TextView)lastResHolder.findViewById(R.id.riName);
		lastResViews.resultText = (TextView)lastResHolder.findViewById(R.id.riResultText);
		lastResViews.resultValue = (TextView)lastResHolder.findViewById(R.id.riResult);
		lastResViews.resultIcon = (ImageView)lastResHolder.findViewById(R.id.riResultIcon);

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
			diceBagManager.setCurrentIndex((int)id);
			refreshAllDiceContainers();
			drawer.closeDrawer(lvDiceBag);
		}
	};
	
	OnItemClickListener variableClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//mMenuInfo = new AdapterContextMenuInfo(view, position, id);
			//openContextMenu(lvVariable);
			if (checkCoolDown()) {
				parent.showContextMenuForChild(view);
			}
		}
	};
	
	OnItemClickListener diceClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			doRoll(diceBag.getDice().get((int) id));
		}
	};
	
	OnClickListener modifierClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			doModifier(diceBag.getModifiers().get((Integer)v.getTag(R.id.key_value)));
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
//		//linkSwitchButton.setImageLevel(linkRoll ? 1 : 0);
//		//linkSwitchButton.refreshDrawableState();
//		linkSwitchButton.getBackground().setLevel(linkRoll ? LINK_LEVEL_ON : LINK_LEVEL_OFF);
		linkSwitchButton.setImageLevel(linkRoll ? LINK_LEVEL_ON : LINK_LEVEL_OFF);
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

	private void doModifier(Modifier modifier) {
		RollResult modRes;

		if (lastResult.length > 0) { //Apply only if a result exist
			long value = modifier.getValue();
			RollResult res = RollResult.mergeResultList(lastResult);
			if (modifier instanceof PercentModifier) {
				//Value is a percentage, and it have to be applyied to current result.
				value = ((res.getRawResultValue() * value) / 100);
			} else {
				value = value * RollResult.VALUES_PRECISION_FACTOR;
			}
			modRes = new RollResult(
					modifier.getName(),
					modifier.getDescription(),
					modifier.getValueString(),
					value,
					value,
					value,
					res.getResourceIndex() // modifier.getResourceIndex()
			);

			addResult(modRes, true);
		}
	}

	private void doRoll(Dice dice) {
		if (checkCoolDown()) {
//			try {
//				handleResult(new RollResult(exp.getResult()));
//			} catch (DException ex) {
//				handleErrResult(ex);
//			}

			synchronized (this) {
				if (! roller.checkAndSetWorkingState()) {
					//If current roller is working
					//create new roller.
					roller = new DoRoll();
					android.util.Log.i(TAG, "New roller!");
				}
	
				//Set data
				roller.setDice(dice);

				//Execute
				executor.execute(roller);
			}
		}
	}
	
	private ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
	
	private DoRoll roller = new DoRoll();
	
	class DoRoll extends SynchRunnable {

		private Dice mDice;
		private DispatchOutcome dispatchOutcome = new DispatchOutcome();

		public void setDice(Dice dice) {
			mDice = dice;
		}

		@Override
		public void execute() {
			synchronized (this) {
				if (! dispatchOutcome.checkAndSetWorkingState()) {
					//If current dispatcher is working
					//create new dispatcher.
					dispatchOutcome = new DispatchOutcome();
				}
	
				//Set data
				try {
					//handleResult(new RollResult(mDie.getResult()));
					//dispatchOutcome.setResult(new RollResult(mDice.getResult()));
					//mDice.setContext(diceBagManager.getVariables());
					dispatchOutcome.setResult(mDice.getNewResult());
				} catch (DException ex) {
					//handleErrResult(ex);
					dispatchOutcome.setError(ex);
				}

				//Dispatch
				QuickDiceMainActivity.this.runOnUiThread(dispatchOutcome);
			}
		}
		
	}
	
	class DispatchOutcome extends SynchRunnable {
		
		private RollResult res;
		private DException ex;
		
		public void setResult(RollResult result) {
			res = result;
			ex = null;
		}
		
		public void setError(DException error) {
			res = null;
			ex = error;
		}
		
		@Override
		public void execute() {
			if (res != null) {
				handleResult(res);
			} else {
				handleErrResult(ex);
			}
		}
	}

	private void handleResult(RollResult res) {
		String resultValue;

		//resultValue = Long.toString(res.getResultValue());
		resultValue = res.getResultString();

		switch (pref.getClipboardUsage()) {
			case PreferenceManager.CLIPBOARD_TYPE_VALUE:
				CompatClipboard.getInstance(app).setText(
						getString(R.string.lblResultValue),
						resultValue);
				break;
			case PreferenceManager.CLIPBOARD_TYPE_EXT:
				CompatClipboard.getInstance(app).setText(
						getString(R.string.lblResultText),
						res.getResultText() + " = " + resultValue);
				break;
			default:
				//NOOP
				break;
		}

		//performRoll(res);
		rollDiceToast.performRoll(res);

		addResult(res, checkLinkRoll());
	}
	
	/**
	 * Check if roll has to be linked. It also update the status of the button.<br />
	 * Link result if: <br />
	 * - Linking is enabled<br />
	 * - Previous roll is not too far<br />
	 * - Link chain is not too long
	 * @return {@code true} if roll must be linked, {@code false} otherwise.
	 */
	private boolean checkLinkRoll() {
		return checkAutoLinkRoll() //This method must be invoked, so leave it at left of boolean operator
				&& lastResult.length < pref.getMaxResultLink();
	}
	
	/**
	 * Check if roll has to be linked and update the status of the button.
	 * @return {@code true} if rolls has to be linked.
	 */
	private boolean checkAutoLinkRoll() {
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
		//If status changed, update button
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
	
	/**
	 * Update the content of the last roll result.
	 */
	private void refreshLastResult() {
		lastResHolder.setEnabled(lastResult.length > 0);
		
		ResultListAdapter.bindData(
				this, 
				lastResult, 
				lastResViews);
		
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
		//((ResultListAdapter)gvResults.getAdapter()).notifyDataSetChanged();
		notifyDataSetChanged(gvResults);
	}
	
	/**
	 * Refresh the dice bags grid layout after the data are changed.
	 */
	private void refreshBagsList() {
		//((DiceBagAdapter)lvDiceBag.getAdapter()).notifyDataSetChanged();
		////lvDiceBag.invalidateViews();
		notifyDataSetChanged(lvDiceBag);
		addDiceBag.setVisibility(app.canAddDiceBag() ? View.VISIBLE : View.GONE);
	}
	
	/**
	 * Refresh the dice grid layout after the data are changed.
	 */
	private void refreshDiceList() {
		//((GridExpressionAdapter)gvDice.getAdapter()).notifyDataSetChanged();
		notifyDataSetChanged(gvDice);
	}
	
	/**
	 * Refresh the variable list after the data are changed.
	 */
	private void refreshVariablesList() {
		//((VariableAdapter)lvVariable.getAdapter()).notifyDataSetChanged();
		notifyDataSetChanged(lvVariable);
		addVariable.setVisibility(app.canAddVariable() ? View.VISIBLE : View.GONE);
	}
	
	private void notifyDataSetChanged(AbsListView listView) {
		notifyDataSetChanged(listView.getAdapter());
	}
	
	private void notifyDataSetChanged(ListAdapter adapter) {
		if (adapter instanceof HeaderViewListAdapter) {
			adapter = ((HeaderViewListAdapter)adapter).getWrappedAdapter();
		}
		if (adapter instanceof BaseAdapter) {
			((BaseAdapter)adapter).notifyDataSetChanged();
		} else {
			throw new InvalidParameterException("Not supported: " + adapter.getClass().getCanonicalName());
		}
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
		//diceBag = diceBagManager.getDiceList();
		//bonusBag = diceBagManager.getModifiers();
		
		diceBag = diceBagManager.getDiceBagCollection().getCurrent();

		//refreshDiceList();
		gvDice.setAdapter(new GridExpressionAdapter(
				QuickDiceMainActivity.this,
				R.layout.dice_item,
				diceBag.getDice()));
		
		lvVariable.setAdapter(new VariableAdapter(
				this,
				R.layout.item_variable,
				diceBag.getVariables()));
		addVariable.setVisibility(app.canAddVariable() ? View.VISIBLE : View.GONE);

		initModifierList();

		if (afterImport) {
			lvDiceBag.setAdapter(new DiceBagAdapter(
					this,
					R.layout.dice_bag_item,
					diceBagManager.getDiceBagCollection()));
			addDiceBag.setVisibility(app.canAddDiceBag() ? View.VISIBLE : View.GONE);
		} else {
			refreshBagsList();
		}
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
		if (diceBag.getModifiers().size() >= pref.getMaxModifiers()) {
			//Maximum number of allowed modifiers reached
			Toast.makeText(this, R.string.msgMaxModifiersReach, Toast.LENGTH_LONG).show();
		} else {
			new ModifierBuilderDialog(this, position, modifierCreatedListener).show();
		}
	}
	
	private OnCreatedListener modifierCreatedListener = new OnCreatedListener() {
		@Override
		public void onCreated(boolean confirmed, int position, int type, int value, String label) {
			if (confirmed) {
				switch (type) {
					case VarModifier.TYPE_ID:
						addVarModifier(
								label,
								position == ModifierBuilderDialog.POSITION_UNDEFINED ? -1 : position);
						break;
					case PercentModifier.TYPE_ID:
						addPercModifier(
								value,
								position == ModifierBuilderDialog.POSITION_UNDEFINED ? -1 : position);
						break;
					default:
						addRollModifier(
								value,
								position == ModifierBuilderDialog.POSITION_UNDEFINED ? -1 : position);
				}
			}
		}
	};
	
	private void moveDice(int fromDiceBagIndex, int fromPosition, int toDiceBagIndex, int toPosition) {
		if (diceBag.getDice().move(
				fromDiceBagIndex,
				fromPosition,
				toDiceBagIndex,
				toPosition)) {
		
			refreshDiceList();
		}
	}
	
	private void addRollModifier(int modifier, int position) {
		RollModifier newMod;
		boolean duplicate = false;

		if (modifier == 0) {
			//Neutral modifier
			Toast.makeText(this, R.string.lblNeutralModifier, Toast.LENGTH_LONG).show();
			return;
		}

		for (Modifier mod : diceBag.getModifiers()) {
			if (mod instanceof RollModifier && mod.getValue() == modifier) {
				duplicate = true;
				break;
			}
		}

		if (duplicate) {
			//Duplicate modifier
			Toast.makeText(this, R.string.lblDuplicateModifier, Toast.LENGTH_LONG).show();
		} else {
			newMod = new RollModifier(app, modifier);
			diceBag.getModifiers().add(position, newMod);
			refreshModifierList();
		}
	}

	private void addPercModifier(int percent, int position) {
		PercentModifier newMod;
		boolean duplicate = false;

		if (percent == 0) {
			//Neutral modifier
			Toast.makeText(this, R.string.lblNeutralModifier, Toast.LENGTH_LONG).show();
			return;
		}

		for (Modifier mod : diceBag.getModifiers()) {
			if (mod instanceof PercentModifier && mod.getValue() == percent) {
				duplicate = true;
				break;
			}
		}

		if (duplicate) {
			//Duplicate modifier
			Toast.makeText(this, R.string.lblDuplicateModifier, Toast.LENGTH_LONG).show();
		} else {
			newMod = new PercentModifier(app, percent);
			diceBag.getModifiers().add(position, newMod);
			refreshModifierList();
		}
	}

	private void addVarModifier(String label, int position) {
		VarModifier newMod;
		boolean duplicate = false;

		if (label == null || label.length() == 0) {
			//Where did it go?
			return;
		}

		for (Modifier mod : diceBag.getModifiers()) {
			if (mod instanceof VarModifier && label.equals(((VarModifier) mod).getLabel())) {
				duplicate = true;
				break;
			}
		}

		if (duplicate) {
			//Duplicate modifier
			//Fix text
			Toast.makeText(this, R.string.lblDuplicateModifier, Toast.LENGTH_LONG).show();
		} else {
			newMod = new VarModifier(label);
			diceBag.getModifiers().add(position, newMod);
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