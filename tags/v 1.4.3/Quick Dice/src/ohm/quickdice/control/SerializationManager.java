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

import ohm.dexp.DExpression;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.MostRecentFile;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.RollResult;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class SerializationManager {
	
	protected static final int SERIALIZER_VERSION = 3;
	protected static final String CHARSET = "UTF-8";

	/**
	 * Serialize a collection of Dice Bags to a JSON string.
	 * @param diceBags Dice Bags to serialize.
	 * @return JSON string containing Dice Bags serialization.
	 * @throws Exception Raised if object cannot be serialized.
	 */
	public static String DiceBags(ArrayList<DiceBag> diceBags) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializeDiceBags(baos, diceBags);
		return baos.toString(CHARSET);
	}
	
	/**
	 * Serialize a collection of Dice Bags to the specified stream.
	 * @param out Stream to write into
	 * @param diceBags Dice Bags to serialize.
	 * @throws IOException Exception Raised if object cannot be serialized.
	 */
	public static void DiceBags(OutputStream out, ArrayList<DiceBag> diceBags) throws IOException {
		serializeDiceBags(out, diceBags);
	}
	
	/**
	 * Deserialize a collection of Dice Bags from a JSON string
	 * @param diceBags JSON string containing Dice Bags
	 * @return Deserialized Dice Bags
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static ArrayList<DiceBag> DiceBags(String diceBags) throws IOException {
		return deserializeDiceBags(diceBags);
	}

	/**
	 * Deserialize a collection of Dice Bags from the specified stream
	 * @param in Stream to read from
	 * @return Deserialized Dice Bags
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static ArrayList<DiceBag> DiceBags(InputStream in) throws IOException {
		return deserializeDiceBags(in);
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
	 * Deserialize a Dice Bag from a JSON string
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

	/**
	 * Serialize a Die to a JSON string.
	 * @param die Die to serialize.
	 * @return JSON string containing the collection of Dice.
	 * @throws IOException Exception Raised if object cannot be serialized.
	 */
	public static String Die(DExpression die) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos, CHARSET));
		
		serializeDie(writer, die);
		
		writer.close();
		
		return baos.toString(CHARSET);
	}
	
	/**
	 * Deserialize a Die from a JSON string
	 * @param die JSON string containing the Die
	 * @return Deserialized Die
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static DExpression Die(String die) throws IOException {
		DExpression retVal;
		ByteArrayInputStream bais = new ByteArrayInputStream(die.getBytes(CHARSET));
		JsonReader reader = new JsonReader(new InputStreamReader(bais, CHARSET));
		
		retVal = deserializeDie(reader);
		
		reader.close();
		
		return retVal;
	}

//	/**
//	 * Serialize a collection of Dice to a JSON string.
//	 * @param dice Collection of Dice to serialize.
//	 * @return JSON string containing the collection of Dice.
//	 * @throws JSONException Raised if a JSON related problem occur.
//	 */
//	public static String DiceList(ArrayList<DExpression> dice) throws JSONException {
//		return convertDiceListToJSON(dice).toString();
//	}
	
	/**
	 * Deserialize a collection of Dice from the specified stream
	 * @param in Stream to read from
	 * @return Deserialized collection of Dice
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static ArrayList<DExpression> DiceList(InputStream in) throws IOException {
		ArrayList<DExpression> retVal;
		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));

		retVal = deserializeDice(reader);
		
		reader.close();

		return retVal;
	}

//	/**
//	 * Serialize a collection of Modifiers to a JSON string.
//	 * @param bonusBag Collection of Modifiers to serialize.
//	 * @return JSON string containing the collection of Modifiers.
//	 * @throws JSONException Raised if a JSON related problem occur.
//	 */
//	public static String BonusList(ArrayList<RollModifier> bonusBag) throws JSONException {
//		return convertBonusListToJSON(bonusBag).toString();
//	}
	
	/**
	 * Deserialize a collection of Modifiers from the specified stream
	 * @param in Stream to read from.
	 * @return Deserialized collection of Modifiers
	 * @throws IOException Exception Raised if object cannot be deserialized.
	 */
	public static ArrayList<RollModifier> BonusList(InputStream in) throws IOException {
		ArrayList<RollModifier> retVal;
		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));

		retVal = deserializeModifiers(reader);
		
		reader.close();

		return retVal;
	}
	
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
	
	private static final String FIELD_MRU_LIST = "mruList";
	private static final String FIELD_MRU_NAME = "name";
	private static final String FIELD_MRU_PATH = "path";
	private static final String FIELD_MRU_BAGS = "bags";
	private static final String FIELD_MRU_DICE = "dice";
	private static final String FIELD_MRU_MODS = "mods";
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
	private static final String FIELD_MODS = "mods";

	private static void serializeDiceBags(OutputStream out, ArrayList<DiceBag> diceBags) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, CHARSET));
		
		writer.beginObject();
		
		writer.name(FIELD_VERSION).value(SERIALIZER_VERSION);
		writer.name(FIELD_DICE_BAGS);
		
		writer.beginArray();
		
		for (DiceBag b : diceBags) {
			serializeDiceBag(writer, b);
		}

		writer.endArray();
		
		writer.endObject(); //FIELD_DICE_BAGS
		writer.close();
	}
	
	private static ArrayList<DiceBag> deserializeDiceBags(String diceBag) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(diceBag.getBytes(CHARSET));
		return deserializeDiceBags(bais);
	}
	
	private static ArrayList<DiceBag> deserializeDiceBags(InputStream in) throws IOException {
		ArrayList<DiceBag> retVal;
		JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
		String fieldName;
		
		retVal = new ArrayList<DiceBag>();
		
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
					retVal.add(deserializeDiceBag(reader));
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

	private static void serializeDiceBag(JsonWriter writer, DiceBag diceBag) throws IOException {
		writer.beginObject();
		
		writer.name(FIELD_NAME).value(diceBag.getName());
		writer.name(FIELD_DESCRIPTION).value(diceBag.getDescription());
		writer.name(FIELD_RESOURCE_INDEX).value(diceBag.getResourceIndex());
		writer.name(FIELD_BAGS);
		serializeDice(writer, diceBag.getDice());
		writer.name(FIELD_MODS);
		serializeModifiers(writer, diceBag.getModifiers());
		
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
				retVal.setDice(deserializeDice(reader));
			} else if (fieldName.equals(FIELD_MODS)) {
				retVal.setModifiers(deserializeModifiers(reader));
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
	
	private static void serializeDice(JsonWriter writer, ArrayList<DExpression> dice) throws IOException {
		writer.beginObject();
		writer.name(FIELD_DB_DICE_BAG);
		writer.beginArray();
		for (DExpression d : dice) {
			serializeDie(writer, d);
		}
		writer.endArray();
		writer.endObject(); //FIELD_DB_DICE_BAG
	}
	
	private static void serializeDie(JsonWriter writer, DExpression die) throws IOException {
		writer.beginObject();
		
		writer.name(FIELD_DB_ID).value(die.getID());
		writer.name(FIELD_DB_NAME).value(die.getName());
		writer.name(FIELD_DB_DESCRIPTION).value(die.getDescription());
		writer.name(FIELD_DB_RESOURCE_INDEX).value(die.getResourceIndex());
		writer.name(FIELD_DB_EXPRESSION).value(die.getExpression());

		writer.endObject();
	}
	
	private static ArrayList<DExpression> deserializeDice(JsonReader reader) throws IOException {
		ArrayList<DExpression> retVal;
		String fieldName;
		
		reader.beginObject();
		retVal = new ArrayList<DExpression>();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_DB_DICE_BAG)) {
				reader.beginArray();
				while (reader.hasNext()) {
					retVal.add(deserializeDie(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return retVal;
	}

	private static DExpression deserializeDie(JsonReader reader) throws IOException {
		DExpression retVal;
		String fieldName;
		
		reader.beginObject();
		retVal = new DExpression();
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

	private static final String FIELD_BB_NAME = "name";
	private static final String FIELD_BB_DESCRIPTION = "desc";
	private static final String FIELD_BB_RESOURCE_INDEX = "resIdx";
	private static final String FIELD_BB_MODIFIER = "mod";
	private static final String FIELD_BB_BONUS_BAG = "bonusBag";
	
	private static void serializeModifiers(JsonWriter writer, ArrayList<RollModifier> modifiers) throws IOException {
		writer.beginObject();
		writer.name(FIELD_BB_BONUS_BAG);
		writer.beginArray();
		for (RollModifier m : modifiers) {
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

	private static ArrayList<RollModifier> deserializeModifiers(JsonReader reader) throws IOException {
		ArrayList<RollModifier> retVal;
		String fieldName;
		
		reader.beginObject();
		retVal = new ArrayList<RollModifier>();
		while (reader.hasNext()) {
			fieldName = reader.nextName();
			if (fieldName.equals(FIELD_BB_BONUS_BAG)) {
				reader.beginArray();
				while (reader.hasNext()) {
					retVal.add(deserializeModifier(reader));
				}
				reader.endArray();
			} else {
				//Unknown element
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return retVal;
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
