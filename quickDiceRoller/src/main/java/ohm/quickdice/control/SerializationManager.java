package ohm.quickdice.control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;
import ohm.quickdice.entity.DiceCollection;
import ohm.quickdice.entity.Icon;
import ohm.quickdice.entity.Icon.CustomIcon;
import ohm.quickdice.entity.IconCollection;
import ohm.quickdice.entity.Modifier;
import ohm.quickdice.entity.ModifierCollection;
import ohm.quickdice.entity.MostRecentFile;
import ohm.quickdice.entity.PercentModifier;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.RollResult;
import ohm.quickdice.entity.VarModifier;
import ohm.quickdice.entity.Variable;
import ohm.quickdice.entity.VariableCollection;
import ohm.quickdice.util.Base64;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class SerializationManager {
	
	protected static final String TAG = "SerializationManager";
	protected static final int SERIALIZER_VERSION = 8;
	protected static final String CHARSET = "UTF-8";
	
	/**
	 * This exception is thrown when the given file cannot be read
	 * because is generated by a newer version of the application
	 * and may contains unsupported features.
	 * @author Ohmnibus
	 *
	 */
	public static class InvalidVersionException extends IOException {

		/**
		 * For serialization purpose.
		 */
		private static final long serialVersionUID = -2194639997299753636L;

		/**
		 * Constructs a new {@code InvalidVersionException} with its stack trace filled in. 
		 */
		public InvalidVersionException() {
			super();
		}

		/**
		 * Constructs a new {@code InvalidVersionException} with its stack trace and detail message filled in.
		 * @param detailMessage the detail message for this exception.
		 */
		public InvalidVersionException(String detailMessage) {
			super(detailMessage);
		}

	}

	/* Dice Bag Manager */
	
	/**
	 * Serialize the Dice Bag Manager to the specified stream.
	 * @param out Stream to write into
	 * @param diceBagManager Dice Bag Manager to serialize.
	 * @param export {@code true} if the serialization is for export purpose, {@code false}
	 * if the serialization is to store data internally.
	 * @throws IOException Exception Raised if object cannot be serialized.
	 */
	public static void DiceBagManager(OutputStream out, DiceBagManager diceBagManager, boolean export) throws IOException {
		serializeDiceBagManager(out, diceBagManager, export);
	}
	
	/**
	 * Deserialize a Dice Bag Manager from the specified stream into the specified collection.
	 * @param in Stream to read from
	 * @param diceBagManager {@link DiceBagManager} to fill with deserialized data.
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static void DiceBagManager(InputStream in, DiceBagManager diceBagManager) throws IOException {
		deserializeDiceBagManager(in, diceBagManager);
	}
	
	/* Dice Bag */
	
	/**
	 * Safely serialize a Dice Bag to a JSON string.<br />
	 * If the parameter is {@code null} or an error occur
	 * this method will return a {@code null} value.
	 * @param diceBag Dice Bag to serialize.
	 * @return JSON string containing Dice Bag serialization, or {@code null}.
	 */
	public static String DiceBagSafe(DiceBag diceBag) {
		String retVal = null;
		if (diceBag != null) {
			try {
				retVal = DiceBag(diceBag);
			} catch (IOException e) {
				Log.w(TAG, "DiceBagSafe: Cannot serialize", e);
				retVal = null;
			}
		}
		return retVal;
	}

	/**
	 * Serialize a Dice Bag to a JSON string.
	 * @param diceBag Dice Bag to serialize.
	 * @return JSON string containing Dice Bag serialization.
	 * @throws IOException Exception Raised if object cannot be serialized.
	 */
	public static String DiceBag(DiceBag diceBag) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos, CHARSET));
		
		serializeDiceBag(writer, diceBag);
		
		writer.close();
		
		return baos.toString(CHARSET);
	}
	
	/**
	 * Deserialize a Dice Bag from a JSON string.<br />
	 * If the parameter is {@code null} or an error occur
	 * this method will return a {@code null} value.
	 * @param diceBag JSON string containing Dice Bag
	 * @return Deserialized Dice Bag, or {@code null}.
	 */
	public static DiceBag DiceBagSafe(String diceBag) {
		DiceBag retVal = null;
		if (diceBag != null) {
			try {
				retVal = DiceBag(diceBag);
			} catch (IOException e) {
				Log.w(TAG, "DiceBagSafe: Cannot deserialize", e);
				retVal = null;
			}
		}
		return retVal;
	}
	
	/**
	 * Safely deserialize a Dice Bag from a JSON string
	 * @param diceBag JSON string containing Dice Bag
	 * @return Deserialized Dice Bag
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static DiceBag DiceBag(String diceBag) throws IOException {
		DiceBag retVal;
		ByteArrayInputStream bais = new ByteArrayInputStream(diceBag.getBytes(CHARSET));
		JsonReader reader = new JsonReader(new InputStreamReader(bais, CHARSET));
		
		retVal = deserializeDiceBag(reader);
		
		reader.close();
		
		return retVal;
	}
	
	/* Variable */
	
	/**
	 * Safely serialize a variable to a JSON string.<br />
	 * If the parameter is {@code null} or an error occur
	 * this method will return a {@code null} value.
	 * @param variable Variable to serialize.
	 * @return JSON string containing the variable, or {@code null}.
	 */
	public static String VariableSafe(Variable variable) {
		String retVal = null;
		if (variable != null) {
			try {
				retVal = Variable(variable);
			} catch (IOException e) {
				Log.w(TAG, "VariableSafe: Cannot serialize", e);
				retVal = null;
			}
		}
		return retVal;
	}

	/**
	 * Serialize a variable to a JSON string.
	 * @param variable Variable to serialize.
	 * @return JSON string containing the variable.
	 * @throws IOException Exception Raised if object cannot be serialized.
	 */
	public static String Variable(Variable variable) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos, CHARSET));
		
		serializeVariable(writer, variable);
		
		writer.close();
		
		return baos.toString(CHARSET);
	}
	
	/**
	 * Safely deserialize a variable from a JSON string.<br />
	 * If the parameter is {@code null} or an error occur
	 * this method will return a {@code null} value.
	 * @param variable JSON string containing the variable.
	 * @return Deserialized variable, or {@code null}.
	 */
	public static Variable VariableSafe(String variable) {
		Variable retVal = null;
		if (variable != null) {
			try {
				retVal = Variable(variable);
			} catch (IOException e) {
				Log.w(TAG, "VariableSafe: Cannot deserialize", e);
				retVal = null;
			}
		}
		return retVal;
	}
	
	/**
	 * Deserialize a variable from a JSON string
	 * @param variable JSON string containing the variable
	 * @return Deserialized variable
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static Variable Variable(String variable) throws IOException {
		Variable retVal;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(variable.getBytes(CHARSET));
		JsonReader reader = new JsonReader(new InputStreamReader(bais, CHARSET));
		
		retVal = deserializeVariable(reader);
		
		reader.close();
		
		return retVal;
	}

	/* Dice */

	/**
	 * Safely serialize a dice to a JSON string.<br />
	 * If the parameter is {@code null} or an error occur
	 * this method will return a {@code null} value.
	 * @param dice Dice to serialize.
	 * @return JSON string containing the dice, or {@code null}.
	 */
	public static String DiceSafe(Dice dice) {
		String retVal = null;
		if (dice != null) {
			try {
				retVal = Dice(dice);
			} catch (IOException e) {
				Log.w(TAG, "DiceSafe: Cannot serialize", e);
				retVal = null;
			}
		}
		return retVal;
	}

	/**
	 * Serialize a dice to a JSON string.
	 * @param dice Dice to serialize.
	 * @return JSON string containing the dice.
	 * @throws IOException Exception Raised if object cannot be serialized.
	 */
	public static String Dice(Dice dice) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos, CHARSET));
		
		serializeDice(writer, dice);
		
		writer.close();
		
		return baos.toString(CHARSET);
	}
	
	/**
	 * Safely deserialize a dice from a JSON string.<br />
	 * If the parameter is {@code null} or an error occur
	 * this method will return a {@code null} value.
	 * @param dice JSON string containing the dice.
	 * @return Deserialized dice, or {@code null}.
	 */
	public static Dice DiceSafe(String dice) {
		Dice retVal = null;
		if (dice != null) {
			try {
				retVal = Dice(dice);
			} catch (IOException e) {
				Log.w(TAG, "DiceSafe: Cannot deserialize", e);
				retVal = null;
			}
		}
		return retVal;
	}
	
	/**
	 * Deserialize a dice from a JSON string
	 * @param dice JSON string containing the dice
	 * @return Deserialized dice
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static Dice Dice(String dice) throws IOException {
		Dice retVal;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(dice.getBytes(CHARSET));
		JsonReader reader = new JsonReader(new InputStreamReader(bais, CHARSET));
		
		retVal = deserializeDice(reader);
		
		reader.close();
		
		return retVal;
	}

	/**
	 * Legacy method to load a {@link DiceCollection} from the old file.
	 * @param stream Stream to read from.
	 * @param collection Collection to populate.
	 */
	public static void DiceCollection(InputStream stream, DiceCollection collection) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(stream, CHARSET));
		
		deserializeDiceCollection(reader, collection);
		
		reader.close();
	}
	
	/**
	 * Legacy method to load a {@link ModifierCollection} from the old file.
	 * @param stream Stream to read from.
	 * @param collection Collection to populate.
	 */
	public static void ModifierCollection(InputStream stream, ModifierCollection collection) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(stream, CHARSET));
		
		deserializeModifierCollection(reader, collection);
		
		reader.close();
	}
	
	/* Roll Result */
	
	/**
	 * Serialize a list of roll result trapping any exception.
	 * @param resultList List of roll result to serialize.
	 * @return JSON string containing the list of roll result, or {@code null} if any error occurred.
	 */
	public static String ResultListNoException(ArrayList<RollResult[]> resultList) {
		String retVal;
		try {
			retVal = ResultList(resultList);
		} catch (Exception e) {
			retVal = null;
		}
		return retVal;
	}
	
	/**
	 * Serialize a list of roll result
	 * @param resultList List of roll result to serialize.
	 * @return JSON string containing the list of roll result.
	 * @throws Exception Raised if object cannot be serialized.
	 */
	public static String ResultList(ArrayList<RollResult[]> resultList) throws Exception {
		return serializeResultList(resultList);
	}
	
	/**
	 * Serialize a list of roll result to a stream
	 * @param out Stream to write into
	 * @param resultList List of roll result to serialize.
	 * @throws Exception Raised if object cannot be serialized.
	 */
	public static void ResultList(OutputStream out, ArrayList<RollResult[]> resultList) throws Exception {
		serializeResultList(out, resultList);
	}

	/**
	 * Deserialize a list of roll result trapping any exception.
	 * @param resultList JSON string containing the list of roll result
	 * @return Deserialized list of roll result, or {@code null} if any error occurred.
	 */
	public static ArrayList<RollResult[]> ResultListNoException(String resultList) {
		ArrayList<RollResult[]> retVal;
		
		try {
			retVal = ResultList(resultList);
		} catch (IOException e) {
			retVal = null;
		}
		
		return retVal;
	}
	
	/**
	 * Deserialize a list of roll result
	 * @param resultList JSON string containing the list of roll result
	 * @return Deserialized list of roll result
	 * @throws IOException Raised if object cannot be deserialized from string.
	 */
	public static ArrayList<RollResult[]> ResultList(String resultList) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(resultList.getBytes(CHARSET));
		return deserializeResultList(bais);
	}

	/**
	 * Deserialize a list of roll result from a stream.
	 * @param in Stream to read from.
	 * @return Deserialized list of roll result
	 * @throws IOException Raised if object cannot be deserialized from stream.
	 */
	public static ArrayList<RollResult[]> ResultList(InputStream in) throws IOException {
		return deserializeResultList(in);
	}

	/* Recent Files */
	
	/**
	 * Serialize a list of most recent files
	 * @param mostRecentFileList List of elements to be serialized
	 * @return Set of strings containing serialized objects
	 * @throws Exception Raised if object cannot be serialized.
	 */
	public static String MostRecentFileList(ArrayList<MostRecentFile> mostRecentFileList) throws Exception {
		return serializeMostRecentFileList(mostRecentFileList);
	}

	/**
	 * Deserialize a list of most recent files
	 * @param mostRecentFileList Strings representing serialized items
	 * @return List of deserialized items
	 * @throws IOException Raised if string cannot be deserialized.
	 */
	public static ArrayList<MostRecentFile> MostRecentFileList(String mostRecentFileList) throws Exception {
		return deserializeMostRecentFileList(mostRecentFileList);
	}
	
	/* ***************************** */
	/* Private and protected methods */
	/* ***************************** */
	
	private static final String FIELD_MRU_LIST = "mruList";
	private static final String FIELD_MRU_NAME = "name";
	/** Obsolete */private static final String FIELD_MRU_PATH = "path";
	private static final String FIELD_MRU_URI = "uri";
	private static final String FIELD_MRU_BAGS = "bags";
	private static final String FIELD_MRU_DICE = "dice";
	private static final String FIELD_MRU_MODS = "mods";
	private static final String FIELD_MRU_VARS = "vars";
	private static final String FIELD_MRU_LAST = "last";

	private static void serializeMostRecentFileList(OutputStream out, ArrayList<MostRecentFile> mostRecentFileList) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, CHARSET));
		writer.beginObject();
		writer.name(FIELD_MRU_LIST);
		
		writer.beginArray();
		
		for (MostRecentFile mostRecentFile : mostRecentFileList) {
			serializeMostRecentFile(writer, mostRecentFile);
		}

		writer.endArray();
		
		writer.endObject(); //FIELD_MRU_LIST
		writer.close();
	}

	private static void serializeMostRecentFile(JsonWriter writer, MostRecentFile mostRecentFile) throws IOException {
		writer.beginObject();
		
		writer.name(FIELD_MRU_NAME).value(mostRecentFile.getName());
		//writer.name(FIELD_MRU_PATH).value(mostRecentFile.getPath());
		writer.name(FIELD_MRU_URI).value(mostRecentFile.getUri());
		writer.name(FIELD_MRU_BAGS).value(mostRecentFile.getBagsNum());
		writer.name(FIELD_MRU_DICE).value(mostRecentFile.getDiceNum());
		writer.name(FIELD_MRU_MODS).value(mostRecentFile.getModsNum());
		writer.name(FIELD_MRU_VARS).value(mostRecentFile.getVarsNum());
		writer.name(FIELD_MRU_LAST).value(mostRecentFile.getLastUsed().getTime());
		
		writer.endObject();
	}
	
	private static String serializeMostRecentFileList(ArrayList<MostRecentFile> mostRecentFileList) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializeMostRecentFileList(baos, mostRecentFileList);
		return baos.toString(CHARSET);
	}
	
	private static ArrayList<MostRecentFile> deserializeMostRecentFileList(InputStream in) throws IOException {
		ArrayList<MostRecentFile> retVal;
		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
		String fieldName;
		
		retVal = new ArrayList<MostRecentFile>();
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_MRU_LIST)) {
				reader.beginArray();
				while (reader.hasNext()) {
					retVal.add(deserializeMostRecentFile(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		reader.close();
		return retVal;
	}

	private static MostRecentFile deserializeMostRecentFile(JsonReader reader) throws IOException {
		String fieldName;
		String name = null;
		String uri = null;
		int bags = 0;
		int dice = 0;
		int mods = 0;
		int vars = 0;
		long date = 0;
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_MRU_NAME)) {
				name = reader.nextString();
			} else if (fieldName.equals(FIELD_MRU_PATH)) {
				uri = reader.nextString();
				uri = Uri.fromFile(new File(uri)).toString();
			} else if (fieldName.equals(FIELD_MRU_URI)) {
				uri = reader.nextString();
			} else if (fieldName.equals(FIELD_MRU_BAGS)) {
				bags = reader.nextInt();
			} else if (fieldName.equals(FIELD_MRU_DICE)) {
				dice = reader.nextInt();
			} else if (fieldName.equals(FIELD_MRU_MODS)) {
				mods = reader.nextInt();
			} else if (fieldName.equals(FIELD_MRU_VARS)) {
				vars = reader.nextInt();
			} else if (fieldName.equals(FIELD_MRU_LAST)) {
				date = reader.nextLong();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return new MostRecentFile(
					name,
					uri,
					bags,
					dice,
					mods,
					vars,
					new Date(date)
					);
	}

	private static ArrayList<MostRecentFile> deserializeMostRecentFileList(String mostRecentFileList) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(mostRecentFileList.getBytes(CHARSET));
		return deserializeMostRecentFileList(bais);
	}

	private static final String FIELD_RESULT_LIST = "resList";
	private static final String FIELD_RL_NAME = "name";
	private static final String FIELD_RL_DESCRIPTION = "desc";
	private static final String FIELD_RL_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_RL_RESULT_TXT = "resTxt";
	private static final String FIELD_RL_RESULT_NUM_LEGACY = "resNum";
	private static final String FIELD_RL_RESULT_MAX_LEGACY = "resMax";
	private static final String FIELD_RL_RESULT_MIN_LEGACY = "resMin";
	private static final String FIELD_RL_RESULT_NUM = "val";
	private static final String FIELD_RL_RESULT_MAX = "max";
	private static final String FIELD_RL_RESULT_MIN = "min";

	private static String serializeResultList(ArrayList<RollResult[]> resultList) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializeResultList(baos, resultList);
		return baos.toString(CHARSET);
	}
	
	private static void serializeResultList(OutputStream out, ArrayList<RollResult[]> resultList) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, CHARSET));
		writer.beginObject();
		writer.name(FIELD_RESULT_LIST);
		
		writer.beginArray();
		
		for (RollResult[] rb : resultList) {
			serializeResultBlock(writer, rb);
		}

		writer.endArray();
		
		writer.endObject(); //FIELD_RESULT_LIST
		writer.close();
	}
	
	private static void serializeResultBlock(JsonWriter writer, RollResult[] resultArray) throws IOException {
		writer.beginArray();
		for (RollResult r : resultArray) {
			writer.beginObject();
			
			writer.name(FIELD_RL_NAME).value(r.getName());
			writer.name(FIELD_RL_DESCRIPTION).value(r.getDescription());
			writer.name(FIELD_RL_RESOURCE_INDEX).value(r.getResourceIndex());
			writer.name(FIELD_RL_RESULT_TXT).value(r.getResultText());
			//writer.name(FIELD_RL_RESULT_NUM_LEGACY).value(r.getResultValue());
			//writer.name(FIELD_RL_RESULT_MAX_LEGACY).value(r.getMaxResultValue());
			//writer.name(FIELD_RL_RESULT_MIN_LEGACY).value(r.getMinResultValue());
			writer.name(FIELD_RL_RESULT_NUM).value(r.getRawResultValue());
			writer.name(FIELD_RL_RESULT_MAX).value(r.getMaxRawResultValue());
			writer.name(FIELD_RL_RESULT_MIN).value(r.getMinRawResultValue());
			
			writer.endObject();
		}
		writer.endArray();
	}
	
	private static ArrayList<RollResult[]> deserializeResultList(InputStream in) throws IOException {
		ArrayList<RollResult[]> retVal;
		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
		String fieldName;
		
		retVal = new ArrayList<RollResult[]>();
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_RESULT_LIST)) {
				reader.beginArray();
				while (reader.hasNext()) {
					retVal.add(deserializeResultBlock(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		reader.close();
		return retVal;
	}
	
	private static RollResult[] deserializeResultBlock(JsonReader reader) throws IOException {
		RollResult[] retVal;
		ArrayList<RollResult> rrList;
		String fieldName;
		String name;
		String desc;
		String rest;
		long resn;
		long rema;
		long remi;
		int resx;
		
		rrList = new ArrayList<RollResult>();
		
		reader.beginArray();

		while (reader.hasNext()) {
			name = null;
			desc = null;
			rest = null;
			resn = 0;
			rema = 0;
			remi = 0;
			resx = 0;
			reader.beginObject();
			while (reader.hasNext()) {
				fieldName = reader.nextName();
				if (fieldName.equals(FIELD_RL_NAME)) {
					name = reader.nextString();
				} else if (fieldName.equals(FIELD_RL_DESCRIPTION)) {
					desc = reader.nextString();
				} else if (fieldName.equals(FIELD_RL_RESULT_TXT)) {
					rest = reader.nextString();
				} else if (fieldName.equals(FIELD_RL_RESULT_NUM_LEGACY)) {
					//Legacy value
					resn = reader.nextLong() * RollResult.VALUES_PRECISION_FACTOR;
				} else if (fieldName.equals(FIELD_RL_RESULT_MAX_LEGACY)) {
					//Legacy value
					rema = reader.nextLong() * RollResult.VALUES_PRECISION_FACTOR;
				} else if (fieldName.equals(FIELD_RL_RESULT_MIN_LEGACY)) {
					//Legacy value
					remi = reader.nextLong() * RollResult.VALUES_PRECISION_FACTOR;
				} else if (fieldName.equals(FIELD_RL_RESULT_NUM)) {
					resn = reader.nextLong();
				} else if (fieldName.equals(FIELD_RL_RESULT_MAX)) {
					rema = reader.nextLong();
				} else if (fieldName.equals(FIELD_RL_RESULT_MIN)) {
					remi = reader.nextLong();
				} else if (fieldName.equals(FIELD_RL_RESOURCE_INDEX)) {
					resx = reader.nextInt();
				} else {
					//Unknown element
					reader.skipValue();
				}
			}
			reader.endObject();
			
			rrList.add(new RollResult(
					name,
					desc,
					rest,
					resn,
					rema,
					remi,
					resx));
		}
		
		reader.endArray();
		
		//Since cast to "RollResult[]" occasionally give problems
		//perform manual conversion to array
		retVal = new RollResult[rrList.size()];
		
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = rrList.get(i);
		}
		
		return retVal; //(RollResult[])rrList.toArray();
	}

	private static final String FIELD_VERSION = "version";
	private static final String FIELD_DICE_BAGS = "diceBags";
	private static final String FIELD_ICONS = "icons";
	
	private static void serializeDiceBagManager(OutputStream out, DiceBagManager diceBagManager, boolean full) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, CHARSET));
		
		writer.beginObject();
		
		writer.name(FIELD_VERSION).value(SERIALIZER_VERSION);
		
		serializeDiceBagCollection(writer, diceBagManager.getDiceBagCollection());
		serializeIconCollection(writer, diceBagManager.getIconCollection(), full);
		
		writer.endObject();
		writer.close();
	}
	
	private static void deserializeDiceBagManager(InputStream in, DiceBagManager diceBagManager) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
		String fieldName;
		
		diceBagManager.getDiceBagCollection().clear();
		diceBagManager.getIconCollection().clear();
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_VERSION)) {
				//This can never happen for very old versions.
				//Not a problem, since old file version are always readable.
				int version = reader.nextInt();
				if (version > SERIALIZER_VERSION) {
					reader.close();
					//throw new IOException("Cannot deserialize a file created with an higher app version.");
					throw new InvalidVersionException("Cannot deserialize a file created with an higher app version.");
				}
			} else if (fieldName.equals(FIELD_DICE_BAGS)) {
				deserializeDiceBagCollection(reader, diceBagManager.getDiceBagCollection());
			} else if (fieldName.equals(FIELD_ICONS)) {
				deserializeIconCollection(reader, diceBagManager.getIconCollection(), diceBagManager.getIconFolder());
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		reader.close();
	}
	
	private static final String FIELD_NAME = "name";
	private static final String FIELD_DESCRIPTION = "desc";
	private static final String FIELD_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_BAGS = "bags";
	private static final String FIELD_VARS = "vars";
	private static final String FIELD_MODS = "mods";
	

	private static void serializeDiceBagCollection(JsonWriter writer, DiceBagCollection diceBagCollection) throws IOException {
		writer.name(FIELD_DICE_BAGS);
		
		writer.beginArray();
		
		for (DiceBag b : diceBagCollection) {
			serializeDiceBag(writer, b);
		}

		writer.endArray();
	}
	
	private static void deserializeDiceBagCollection(JsonReader reader, DiceBagCollection diceBagCollection) throws IOException {
		//diceBagCollection.clear();
		
		reader.beginArray();
		while (reader.hasNext()) {
			diceBagCollection.add(deserializeDiceBag(reader));
		}
		reader.endArray();
	}

	private static void serializeDiceBag(JsonWriter writer, DiceBag diceBag) throws IOException {
		writer.beginObject();
		
		writer.name(FIELD_NAME).value(diceBag.getName());
		writer.name(FIELD_DESCRIPTION).value(diceBag.getDescription());
		writer.name(FIELD_RESOURCE_INDEX).value(diceBag.getResourceIndex());
		writer.name(FIELD_BAGS);
		//serializeDiceList(writer, diceBag.getDiceList());
		serializeDiceCollection(writer, diceBag.getDice());
		writer.name(FIELD_VARS);
		//serializeVariableList(writer, diceBag.getVariables());
		serializeVariableCollection(writer, diceBag.getVariables());
		writer.name(FIELD_MODS);
		//serializeModifiers(writer, diceBag.getModifiers());
		serializeModifierCollection(writer, diceBag.getModifiers());
		
		writer.endObject();
	}

	private static DiceBag deserializeDiceBag(JsonReader reader) throws IOException {
		DiceBag retVal;
		String fieldName;
		
		reader.beginObject();
		retVal = new DiceBag();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_NAME)) {
				retVal.setName(reader.nextString());
			} else if (fieldName.equals(FIELD_DESCRIPTION)) {
				retVal.setDescription(reader.nextString());
			} else if (fieldName.equals(FIELD_RESOURCE_INDEX)) {
				retVal.setResourceIndex(reader.nextInt());
			} else if (fieldName.equals(FIELD_BAGS)) {
				deserializeDiceCollection(reader, retVal.getDice());
			} else if (fieldName.equals(FIELD_VARS)) {
				deserializeVariableCollection(reader, retVal.getVariables());
			} else if (fieldName.equals(FIELD_MODS)) {
				deserializeModifierCollection(reader, retVal.getModifiers());
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return retVal;
	}

	private static final String FIELD_DB_ID = "id";
	private static final String FIELD_DB_NAME = "name";
	private static final String FIELD_DB_DESCRIPTION = "desc";
	private static final String FIELD_DB_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_DB_EXPRESSION = "exp";
	private static final String FIELD_DB_DICE_BAG = "diceBag";
	
	private static void serializeDiceCollection(JsonWriter writer, DiceCollection diceList) throws IOException {
		writer.beginObject();
		writer.name(FIELD_DB_DICE_BAG);
		writer.beginArray();
		for (Dice d : diceList) {
			serializeDice(writer, d);
		}
		writer.endArray();
		writer.endObject(); //FIELD_DB_DICE_BAG
	}
	
	private static void serializeDice(JsonWriter writer, Dice dice) throws IOException {
		writer.beginObject();
		
		writer.name(FIELD_DB_ID).value(dice.getID());
		writer.name(FIELD_DB_NAME).value(dice.getName());
		writer.name(FIELD_DB_DESCRIPTION).value(dice.getDescription());
		writer.name(FIELD_DB_RESOURCE_INDEX).value(dice.getResourceIndex());
		writer.name(FIELD_DB_EXPRESSION).value(dice.getExpression());

		writer.endObject();
	}
	
	private static void deserializeDiceCollection(JsonReader reader, DiceCollection diceList) throws IOException {
		String fieldName;
		
		diceList.clear();
		
		reader.beginObject();

		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_DB_DICE_BAG)) {
				reader.beginArray();
				while (reader.hasNext()) {
					diceList.add(deserializeDice(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
	}

	private static Dice deserializeDice(JsonReader reader) throws IOException {
		Dice retVal;
		String fieldName;
		
		reader.beginObject();
		retVal = new Dice();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_DB_ID)) {
				retVal.setID(reader.nextInt());
			} else if (fieldName.equals(FIELD_DB_NAME)) {
				retVal.setName(reader.nextString());
			} else if (fieldName.equals(FIELD_DB_DESCRIPTION)) {
				retVal.setDescription(reader.nextString());
			} else if (fieldName.equals(FIELD_DB_RESOURCE_INDEX)) {
				retVal.setResourceIndex(reader.nextInt());
			} else if (fieldName.equals(FIELD_DB_EXPRESSION)) {
				retVal.setExpression(reader.nextString());
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return retVal;
	}

	private static final String FIELD_VAR_ID = "id";
	private static final String FIELD_VAR_NAME = "name";
	private static final String FIELD_VAR_DESCRIPTION = "desc";
	private static final String FIELD_VAR_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_VAR_LABEL = "lbl";
	private static final String FIELD_VAR_MIN_VAL = "min";
	private static final String FIELD_VAR_MAX_VAL = "max";
	private static final String FIELD_VAR_CUR_VAL = "val";
	private static final String FIELD_VAR_VAR_LIST = "varBag";
	
	private static void serializeVariableCollection(JsonWriter writer, VariableCollection variableList) throws IOException {
		writer.beginObject();
		writer.name(FIELD_VAR_VAR_LIST);
		writer.beginArray();
		for (Variable v : variableList) {
			serializeVariable(writer, v);
		}
		writer.endArray();
		writer.endObject();
	}
	
	private static void serializeVariable(JsonWriter writer, Variable variable) throws IOException {
		writer.beginObject();
		
		writer.name(FIELD_VAR_ID).value(variable.getID());
		writer.name(FIELD_VAR_NAME).value(variable.getName());
		writer.name(FIELD_VAR_DESCRIPTION).value(variable.getDescription());
		writer.name(FIELD_VAR_RESOURCE_INDEX).value(variable.getResourceIndex());
		writer.name(FIELD_VAR_LABEL).value(variable.getLabel());
		writer.name(FIELD_VAR_MIN_VAL).value(variable.getMinVal());
		writer.name(FIELD_VAR_MAX_VAL).value(variable.getMaxVal());
		writer.name(FIELD_VAR_CUR_VAL).value(variable.getCurVal());

		writer.endObject();
	}

	private static void deserializeVariableCollection(JsonReader reader, VariableCollection collection) throws IOException {
		String fieldName;
		
		collection.clear();
		
		reader.beginObject();

		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_VAR_VAR_LIST)) {
				reader.beginArray();
				while (reader.hasNext()) {
					collection.add(deserializeVariable(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
	}

	private static Variable deserializeVariable(JsonReader reader) throws IOException {
		Variable retVal;
		String fieldName;
		
		reader.beginObject();
		retVal = new Variable();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_VAR_ID)) {
				retVal.setID(reader.nextInt());
			} else if (fieldName.equals(FIELD_VAR_NAME)) {
				retVal.setName(reader.nextString());
			} else if (fieldName.equals(FIELD_VAR_DESCRIPTION)) {
				retVal.setDescription(reader.nextString());
			} else if (fieldName.equals(FIELD_VAR_RESOURCE_INDEX)) {
				retVal.setResourceIndex(reader.nextInt());
			} else if (fieldName.equals(FIELD_VAR_LABEL)) {
				retVal.setLabel(reader.nextString());
			} else if (fieldName.equals(FIELD_VAR_MIN_VAL)) {
				retVal.setMinVal(reader.nextInt());
			} else if (fieldName.equals(FIELD_VAR_MAX_VAL)) {
				retVal.setMaxVal(reader.nextInt());
			} else if (fieldName.equals(FIELD_VAR_CUR_VAL)) {
				retVal.setCurVal(reader.nextInt());
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return retVal;
	}

	private static final String FIELD_BB_TYPE = "type";
	private static final String FIELD_BB_NAME = "name";
	private static final String FIELD_BB_DESCRIPTION = "desc";
	private static final String FIELD_BB_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_BB_MODIFIER = "mod";
	private static final String FIELD_BB_LABEL = "lbl";
	private static final String FIELD_BB_BONUS_BAG = "bonusBag";
	
	private static void serializeModifierCollection(JsonWriter writer, ModifierCollection collection) throws IOException {
		writer.beginObject();
		writer.name(FIELD_BB_BONUS_BAG);
		writer.beginArray();
		for (Modifier m : collection) {
			serializeModifier(writer, m);
		}		
		writer.endArray();
		writer.endObject(); //FIELD_BB_BONUS_BAG
	}

	private static void serializeModifier(JsonWriter writer, Modifier modifier) throws IOException {
		writer.beginObject();

		writer.name(FIELD_BB_TYPE).value(modifier.getTypeID());

		if (modifier instanceof VarModifier) {
			writer.name(FIELD_BB_LABEL).value(((VarModifier) modifier).getLabel());
		} else {
			writer.name(FIELD_BB_NAME).value(modifier.getName());
			writer.name(FIELD_BB_DESCRIPTION).value(modifier.getDescription());
			writer.name(FIELD_BB_RESOURCE_INDEX).value(modifier.getResourceIndex());
			writer.name(FIELD_BB_MODIFIER).value(modifier.getValue());
		}
		
		writer.endObject();
	}

	private static void deserializeModifierCollection(JsonReader reader, ModifierCollection collection) throws IOException {
		String fieldName;
		
		collection.clear();
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_BB_BONUS_BAG)) {
				reader.beginArray();
				while (reader.hasNext()) {
					collection.add(deserializeModifier(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
	}
	
	private static Modifier deserializeModifier(JsonReader reader) throws IOException {
		int type = RollModifier.TYPE_ID;
		String fieldName;
		String name = null;
		String desc = null;
		int value = 0;
		int resx = 0;
		String label = null;
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_BB_TYPE)) {
				type = reader.nextInt();
			} else if (fieldName.equals(FIELD_BB_NAME)) {
				name = reader.nextString();
			} else if (fieldName.equals(FIELD_BB_DESCRIPTION)) {
				desc = reader.nextString();
			} else if (fieldName.equals(FIELD_BB_MODIFIER)) {
				value = reader.nextInt();
			} else if (fieldName.equals(FIELD_BB_RESOURCE_INDEX)) {
				resx = reader.nextInt();
			} else if (fieldName.equals(FIELD_BB_LABEL)) {
				label = reader.nextString();
				type = VarModifier.TYPE_ID; //This assignment is for backward compatibility
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();

		Modifier retVal;

		switch (type) {
			case VarModifier.TYPE_ID:
				retVal = new VarModifier(label);
				break;
			case PercentModifier.TYPE_ID:
				retVal = new PercentModifier(
						name,
						desc,
						value,
						resx);
				break;
			default:
				retVal = new RollModifier(
						name,
						desc,
						value,
						resx);
		}

		return retVal;
	}
	
	private static final String FIELD_ICON_ID = "id";
	private static final String FIELD_ICON_COLOR = "color";
	private static final String FIELD_ICON_HASH = "hash";
	private static final String FIELD_ICON_BODY = "body";
	

	private static void serializeIconCollection(JsonWriter writer, IconCollection iconCollection, boolean full) throws IOException {
		writer.name(FIELD_ICONS);
		
		writer.beginArray();
		
		for (Icon i : iconCollection) {
			serializeIcon(writer, i, full);
		}

		writer.endArray();
	}
	
	private static void deserializeIconCollection(JsonReader reader, IconCollection iconCollection, File iconFolder) throws IOException {
		
		reader.beginArray();
		while (reader.hasNext()) {
			iconCollection.add(deserializeIcon(reader, iconFolder));
		}
		reader.endArray();
	}
	
	private static void serializeIcon(JsonWriter writer, Icon icon, boolean full) throws IOException {
		if (icon.isCustom()) {
			Context ctx = QuickDiceApp.getInstance().getApplicationContext();
			CustomIcon ico = (CustomIcon)icon;
			
			writer.beginObject();
			
			writer.name(FIELD_ICON_ID).value(ico.getId());
			writer.name(FIELD_ICON_COLOR).value(ico.getColor(ctx));
			writer.name(FIELD_ICON_HASH).value(ico.getHash());
			if (full) {
				writer.name(FIELD_ICON_BODY);
				writer.beginArray();

				int byteCount;
				byte[] buffer = new byte[510];
				String base64;
//			try {
				FileInputStream fis = new FileInputStream(ico.getIconPath());

				while ((byteCount = fis.read(buffer)) > 0) {
					base64 = new String(
							Base64.encode(buffer, 0, byteCount, Base64.NO_WRAP),
							CHARSET);
					writer.value(base64);
				}

				fis.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

				writer.endArray();
			}
			
			writer.endObject();
		}
	}
	
	private static Icon deserializeIcon(JsonReader reader, File iconFolder) throws IOException {
		Icon retVal;
		String fieldName;
		int id = -1;
		//int color;
		String hash = null;
		File tempFile = null;
		Context ctx = QuickDiceApp.getInstance().getApplicationContext();
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_ICON_ID)) {
				id = reader.nextInt();
				if (tempFile == null) {
					//If no body exists, it means the icon file
					//already exists in its right place.
					tempFile = CustomIcon.getIconFile(iconFolder, id);
				}
			//} else if (fieldName.equals(FIELD_ICON_COLOR)) {
			//	color = reader.nextInt();
			} else if (fieldName.equals(FIELD_ICON_HASH)) {
				hash = reader.nextString();
			} else if (fieldName.equals(FIELD_ICON_BODY)) {
				reader.beginArray();
				tempFile = CustomIcon.getTempFile(ctx);
				
				FileOutputStream fos = new FileOutputStream(tempFile);
				String base64;
				byte[] buffer;
				
				while (reader.hasNext()) {
					base64 = reader.nextString();
					buffer = base64.getBytes(CHARSET);
					buffer = Base64.decode(
							buffer,
							Base64.NO_WRAP);
					fos.write(buffer);
				}
				
				fos.close();
				
				reader.endArray();

			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();

		retVal = Icon.newIcon(hash, tempFile.getAbsolutePath());
		retVal.setId(id);
		
		return retVal;
	}

}
