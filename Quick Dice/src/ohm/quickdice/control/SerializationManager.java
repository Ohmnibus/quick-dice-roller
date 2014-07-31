package ohm.quickdice.control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;
import ohm.quickdice.entity.DiceCollection;
import ohm.quickdice.entity.ModifierCollection;
import ohm.quickdice.entity.MostRecentFile;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.RollResult;
import ohm.quickdice.entity.Variable;
import ohm.quickdice.entity.VariableCollection;

import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class SerializationManager {
	
	protected static final String TAG = "SerializationManager";
	protected static final int SERIALIZER_VERSION = 4;
	protected static final String CHARSET = "UTF-8";

//	/**
//	 * Serialize a collection of Dice Bags to a JSON string.
//	 * @param diceBags Dice Bags to serialize.
//	 * @return JSON string containing Dice Bags serialization.
//	 * @throws Exception Raised if object cannot be serialized.
//	 */
//	@Deprecated
//	public static String DiceBags(ArrayList<DiceBag> diceBags) throws IOException {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		serializeDiceBags(baos, diceBags);
//		return baos.toString(CHARSET);
//	}
//	
//	/**
//	 * Serialize a collection of Dice Bags to the specified stream.
//	 * @param out Stream to write into
//	 * @param diceBags Dice Bags to serialize.
//	 * @throws IOException Exception Raised if object cannot be serialized.
//	 */
//	@Deprecated
//	public static void DiceBags(OutputStream out, ArrayList<DiceBag> diceBags) throws IOException {
//		serializeDiceBags(out, diceBags);
//	}
//	
//	/**
//	 * Deserialize a collection of Dice Bags from a JSON string
//	 * @param diceBags JSON string containing Dice Bags
//	 * @return Deserialized Dice Bags
//	 * @throws IOException Exception Raised if object cannot be deserialized.
//	 */
//	@Deprecated
//	public static ArrayList<DiceBag> DiceBags(String diceBags) throws IOException {
//		return deserializeDiceBags(diceBags);
//	}
//
//	/**
//	 * Deserialize a collection of Dice Bags from the specified stream
//	 * @param in Stream to read from
//	 * @return Deserialized Dice Bags
//	 * @throws IOException Exception Raised if object cannot be deserialized.
//	 */
//	@Deprecated
//	public static ArrayList<DiceBag> DiceBags(InputStream in) throws IOException {
//		return deserializeDiceBags(in);
//	}

	/* Dice Bag Collection */
	
	/**
	 * Serialize a collection of Dice Bags to a JSON string.
	 * @param diceBagCollection Dice Bags to serialize.
	 * @return JSON string containing Dice Bags serialization.
	 * @throws Exception Raised if object cannot be serialized.
	 */
	public static String DiceBagCollection(DiceBagCollection diceBagCollection) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializeDiceBagCollection(baos, diceBagCollection);
		return baos.toString(CHARSET);
	}
	
	/**
	 * Serialize a collection of Dice Bags to the specified stream.
	 * @param out Stream to write into
	 * @param diceBagCollection Dice Bags to serialize.
	 * @throws IOException Exception Raised if object cannot be serialized.
	 */
	public static void DiceBagCollection(OutputStream out, DiceBagCollection diceBagCollection) throws IOException {
		serializeDiceBagCollection(out, diceBagCollection);
	}
	
	/**
	 * Deserialize a collection of Dice Bags from a JSON string into the specified collection.
	 * @param serializedCollection JSON string containing Dice Bag Collection
	 * @param diceBagCollection {@link DiceBagCollection} to fill with deserialized data.
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static void DiceBagCollection(String serializedCollection, DiceBagCollection diceBagCollection) throws IOException {
		deserializeDiceBagCollection(serializedCollection, diceBagCollection);
	}

	/**
	 * Deserialize a collection of Dice Bags from the specified stream into the specified collection.
	 * @param in Stream to read from
	 * @param diceBagCollection {@link DiceBagCollection} to fill with deserialized data.
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static void DiceBagCollection(InputStream in, DiceBagCollection diceBagCollection) throws IOException {
		deserializeDiceBagCollection(in, diceBagCollection);
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
				return DiceBag(diceBag);
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
				return DiceBag(diceBag);
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
	 * @param dice Variable to serialize.
	 * @return JSON string containing the variable, or {@code null}.
	 */
	public static String VariableSafe(Variable variable) {
		String retVal = null;
		if (variable != null) {
			try {
				return Variable(variable);
			} catch (IOException e) {
				Log.w(TAG, "VariableSafe: Cannot serialize", e);
				retVal = null;
			}
		}
		return retVal;
	}

	/**
	 * Serialize a variable to a JSON string.
	 * @param dice Variable to serialize.
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
	 * @param dice JSON string containing the variable.
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
	 * @param dice JSON string containing the variable
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
				return Dice(dice);
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
				return Dice(dice);
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
	 * @param stream
	 * @param collection
	 */
	public static void DiceCollection(InputStream stream, DiceCollection collection) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(stream, CHARSET));
		
		deserializeDiceCollection(reader, collection);
		
		reader.close();
	}
	
//	/**
//	 * Deserialize a collection of Dice from the specified stream
//	 * @param in Stream to read from
//	 * @return Deserialized collection of Dice
//	 * @throws IOException Exception Raised if object cannot be deserialized.
//	 */
//	@Deprecated
//	public static ArrayList<Dice> DiceList(InputStream in) throws IOException {
//		ArrayList<Dice> retVal;
//		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
//
//		retVal = deserializeDiceList(reader);
//		
//		reader.close();
//
//		return retVal;
//	}

	/* Modifier */
	
//	/**
//	 * Deserialize a collection of Modifiers from the specified stream
//	 * @param in Stream to read from.
//	 * @return Deserialized collection of Modifiers
//	 * @throws IOException Exception Raised if object cannot be deserialized.
//	 */
//	@Deprecated
//	public static ArrayList<RollModifier> BonusList(InputStream in) throws IOException {
//		ArrayList<RollModifier> retVal;
//		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
//
//		retVal = deserializeModifiers(reader);
//		
//		reader.close();
//
//		return retVal;
//	}
	
	/**
	 * Legacy method to load a {@link ModifierCollection} from the old file.
	 * @param stream
	 * @param collection
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
	private static final String FIELD_MRU_PATH = "path";
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
		writer.name(FIELD_MRU_PATH).value(mostRecentFile.getPath());
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
		String path = null;
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
				path = reader.nextString();
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
					path,
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
	private static final String FIELD_RL_RESULT_NUM = "resNum";
	private static final String FIELD_RL_RESULT_TXT = "resTxt";
	private static final String FIELD_RL_RESULT_MAX = "resMax";
	private static final String FIELD_RL_RESULT_MIN = "resMin";

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
			writer.name(FIELD_RL_RESULT_NUM).value(r.getResultValue());
			writer.name(FIELD_RL_RESULT_TXT).value(r.getResultText());
			writer.name(FIELD_RL_RESULT_MAX).value(r.getMaxResultValue());
			writer.name(FIELD_RL_RESULT_MIN).value(r.getMinResultValue());
			
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
	private static final String FIELD_NAME = "name";
	private static final String FIELD_DESCRIPTION = "desc";
	private static final String FIELD_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_BAGS = "bags";
	private static final String FIELD_VARS = "vars";
	private static final String FIELD_MODS = "mods";

//	@Deprecated
//	private static void serializeDiceBags(OutputStream out, ArrayList<DiceBag> diceBags) throws IOException {
//		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, CHARSET));
//		
//		writer.beginObject();
//		
//		writer.name(FIELD_VERSION).value(SERIALIZER_VERSION);
//		writer.name(FIELD_DICE_BAGS);
//		
//		writer.beginArray();
//		
//		for (DiceBag b : diceBags) {
//			serializeDiceBag(writer, b);
//		}
//
//		writer.endArray();
//		
//		writer.endObject(); //FIELD_DICE_BAGS
//		writer.close();
//	}
	
	private static void serializeDiceBagCollection(OutputStream out, DiceBagCollection diceBagCollection) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, CHARSET));
		
		writer.beginObject();
		
		writer.name(FIELD_VERSION).value(SERIALIZER_VERSION);
		writer.name(FIELD_DICE_BAGS);
		
		writer.beginArray();
		
		for (DiceBag b : diceBagCollection) {
			serializeDiceBag(writer, b);
		}

		writer.endArray();
		
		writer.endObject(); //FIELD_DICE_BAGS
		writer.close();
	}
	
//	@Deprecated
//	private static ArrayList<DiceBag> deserializeDiceBags(String diceBag) throws IOException {
//		ByteArrayInputStream bais = new ByteArrayInputStream(diceBag.getBytes(CHARSET));
//		return deserializeDiceBags(bais);
//	}
//	
//	@Deprecated
//	private static ArrayList<DiceBag> deserializeDiceBags(InputStream in) throws IOException {
//		ArrayList<DiceBag> retVal;
//		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
//		String fieldName;
//		
//		retVal = new ArrayList<DiceBag>();
//		
//		reader.beginObject();
//		while (reader.hasNext()) {
//			fieldName = reader.nextName();
//			if (fieldName.equals(FIELD_VERSION)) {
//				//This can never happen for very old versions.
//				//Not a problem, since old file version are always readable.
//				int version = reader.nextInt();
//				if (version > SERIALIZER_VERSION) {
//					reader.close();
//					throw new IOException("Cannot deserialize a file created with an higher app version.");
//				}
//			} else if (fieldName.equals(FIELD_DICE_BAGS)) {
//				reader.beginArray();
//				while (reader.hasNext()) {
//					retVal.add(deserializeDiceBag(reader));
//				}
//				reader.endArray();
//			} else {
//				//Unknown element
//				reader.skipValue();
//			}
//		}
//		reader.endObject();
//		reader.close();
//		
//		return retVal;
//	}
	
	private static void deserializeDiceBagCollection(String serializedCollection, DiceBagCollection diceBagCollection) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(serializedCollection.getBytes(CHARSET));
		deserializeDiceBagCollection(bais, diceBagCollection);
	}
	
	private static void deserializeDiceBagCollection(InputStream in, DiceBagCollection diceBagCollection) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
		String fieldName;
		
		diceBagCollection.clear();
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_VERSION)) {
				//This can never happen for very old versions.
				//Not a problem, since old file version are always readable.
				int version = reader.nextInt();
				if (version > SERIALIZER_VERSION) {
					reader.close();
					throw new IOException("Cannot deserialize a file created with an higher app version.");
				}
			} else if (fieldName.equals(FIELD_DICE_BAGS)) {
				reader.beginArray();
				while (reader.hasNext()) {
					diceBagCollection.add(deserializeDiceBag(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		reader.close();
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
				//retVal.setDiceList(deserializeDiceList(reader));
				deserializeDiceCollection(reader, retVal.getDice());
			} else if (fieldName.equals(FIELD_VARS)) {
				//retVal.setVariables(deserializeVariableList(reader));
				deserializeVariableCollection(reader, retVal.getVariables());
			} else if (fieldName.equals(FIELD_MODS)) {
				//retVal.setModifiers(deserializeModifiers(reader));
				deserializeModifierCollection(reader, retVal.getModifiers());
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
//		if (retVal.getVariables() == null) {
//			retVal.setVariables(new ArrayList<Variable>());
//		}
		
		return retVal;
	}

	private static final String FIELD_DB_ID = "id";
	private static final String FIELD_DB_NAME = "name";
	private static final String FIELD_DB_DESCRIPTION = "desc";
	private static final String FIELD_DB_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_DB_EXPRESSION = "exp";
	private static final String FIELD_DB_DICE_BAG = "diceBag";
	
//	@Deprecated
//	private static void serializeDiceList(JsonWriter writer, ArrayList<Dice> diceList) throws IOException {
//		writer.beginObject();
//		writer.name(FIELD_DB_DICE_BAG);
//		writer.beginArray();
//		for (Dice d : diceList) {
//			serializeDice(writer, d);
//		}
//		writer.endArray();
//		writer.endObject(); //FIELD_DB_DICE_BAG
//	}
	
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
	
//	@Deprecated
//	private static ArrayList<Dice> deserializeDiceList(JsonReader reader) throws IOException {
//		ArrayList<Dice> retVal;
//		String fieldName;
//		
//		reader.beginObject();
//		retVal = new ArrayList<Dice>();
//		while (reader.hasNext()) {
//			fieldName = reader.nextName();
//			if (fieldName.equals(FIELD_DB_DICE_BAG)) {
//				reader.beginArray();
//				while (reader.hasNext()) {
//					retVal.add(deserializeDice(reader));
//				}
//				reader.endArray();
//			} else {
//				//Unknown element
//				reader.skipValue();
//			}
//		}
//		reader.endObject();
//		
//		return retVal;
//	}

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
	
//	private static void serializeVariableList(JsonWriter writer, ArrayList<Variable> variableList) throws IOException {
//		writer.beginObject();
//		writer.name(FIELD_VAR_VAR_LIST);
//		writer.beginArray();
//		for (Variable v : variableList) {
//			serializeVariable(writer, v);
//		}
//		writer.endArray();
//		writer.endObject();
//	}
	
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

//	@Deprecated
//	private static ArrayList<Variable> deserializeVariableList(JsonReader reader) throws IOException {
//		ArrayList<Variable> retVal;
//		String fieldName;
//		
//		reader.beginObject();
//		retVal = new ArrayList<Variable>();
//		while (reader.hasNext()) {
//			fieldName = reader.nextName();
//			if (fieldName.equals(FIELD_VAR_VAR_LIST)) {
//				reader.beginArray();
//				while (reader.hasNext()) {
//					retVal.add(deserializeVariable(reader));
//				}
//				reader.endArray();
//			} else {
//				//Unknown element
//				reader.skipValue();
//			}
//		}
//		reader.endObject();
//		
//		return retVal;
//	}

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
	
	private static final String FIELD_BB_NAME = "name";
	private static final String FIELD_BB_DESCRIPTION = "desc";
	private static final String FIELD_BB_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_BB_MODIFIER = "mod";
	private static final String FIELD_BB_BONUS_BAG = "bonusBag";
	
//	@Deprecated
//	private static void serializeModifiers(JsonWriter writer, ArrayList<RollModifier> modifiers) throws IOException {
//		writer.beginObject();
//		writer.name(FIELD_BB_BONUS_BAG);
//		writer.beginArray();
//		for (RollModifier m : modifiers) {
//			serializeModifier(writer, m);
//		}		
//		writer.endArray();
//		writer.endObject(); //FIELD_BB_BONUS_BAG
//	}
	
	private static void serializeModifierCollection(JsonWriter writer, ModifierCollection collection) throws IOException {
		writer.beginObject();
		writer.name(FIELD_BB_BONUS_BAG);
		writer.beginArray();
		for (RollModifier m : collection) {
			serializeModifier(writer, m);
		}		
		writer.endArray();
		writer.endObject(); //FIELD_BB_BONUS_BAG
	}

	private static void serializeModifier(JsonWriter writer, RollModifier modifier) throws IOException {
		writer.beginObject();
		
		writer.name(FIELD_BB_NAME).value(modifier.getName());
		writer.name(FIELD_BB_DESCRIPTION).value(modifier.getDescription());
		writer.name(FIELD_BB_RESOURCE_INDEX).value(modifier.getResourceIndex());
		writer.name(FIELD_BB_MODIFIER).value(modifier.getValue());
		
		writer.endObject();
	}

//	@Deprecated
//	private static ArrayList<RollModifier> deserializeModifiers(JsonReader reader) throws IOException {
//		ArrayList<RollModifier> retVal;
//		String fieldName;
//		
//		reader.beginObject();
//		retVal = new ArrayList<RollModifier>();
//		while (reader.hasNext()) {
//			fieldName = reader.nextName();
//			if (fieldName.equals(FIELD_BB_BONUS_BAG)) {
//				reader.beginArray();
//				while (reader.hasNext()) {
//					retVal.add(deserializeModifier(reader));
//				}
//				reader.endArray();
//			} else {
//				//Unknown element
//				reader.skipValue();
//			}
//		}
//		reader.endObject();
//		
//		return retVal;
//	}
	
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
	
	private static RollModifier deserializeModifier(JsonReader reader) throws IOException {
		String fieldName;
		String name = null;
		String desc = null;
		int value = 0;
		int resx = 0;
		
		reader.beginObject();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_BB_NAME)) {
				name = reader.nextString();
			} else if (fieldName.equals(FIELD_BB_DESCRIPTION)) {
				desc = reader.nextString();
			} else if (fieldName.equals(FIELD_BB_MODIFIER)) {
				value = reader.nextInt();
			} else if (fieldName.equals(FIELD_BB_RESOURCE_INDEX)) {
				resx = reader.nextInt();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return new RollModifier(
				name, 
				desc, 
				value, 
				resx);
	}
}
