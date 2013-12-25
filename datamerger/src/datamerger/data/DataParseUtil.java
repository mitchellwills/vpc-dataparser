package datamerger.data;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.joda.time.*;
import org.joda.time.format.*;

import au.com.bytecode.opencsv.*;

import com.google.common.base.*;

public class DataParseUtil {

	
	public static <T> void tryForEach(Iterable<T> objects, String source, Function<T, Void> action){
		for(T o:objects){
			try{
				action.apply(o);
			} catch(TableQuery.BadQueryException e){
				System.err.println("BadQuery - "+e.getMessage()+" - <"+source+">: "+o);
			} catch(DataFormatException e){
				System.err.println("Bad data format - "+e.getMessage()+" - <"+source+">: "+o);
			} catch(Exception e){
				System.err.println("Unknown exception while parsing <"+source+">: "+o);
				e.printStackTrace();
			}
		}
	}

	
	public static <T extends DataObject & AlternateIdDataObject> void parseAlternateIdsInput(Reader input, final DatabaseTable<T> database, final Class<T> type) throws FileNotFoundException, IOException{
		try(CSVReader reader = new CSVReader(input)){
			List<String[]> allRows = reader.readAll();
			for(String[] row:allRows){
				if(row.length==0)
					continue;
				String id = row[0].trim();
				if(id.isEmpty())
					continue;
				T entry = database.create(type, id);
				for(int i = 1; i<row.length; ++i){
					String alternateId = row[i].trim();
					if(!alternateId.isEmpty())
						entry.alternateIds().get().add(alternateId);
				}
			}
		}
	}

	
	public static String[] fromStringArray(String arrayString){
		String withoutSquareBraces = arrayString.substring(1, arrayString.length()-1);
		if(withoutSquareBraces.length()==0)
			return new String[0];
		return withoutSquareBraces.split(",\\s");
	}
	


	public static Measurement parseMeasurement(String asString, String stringUnit, Unit unit){
		if(asString==null)
			return null;
		if(asString.isEmpty())
			return null;
		if(asString.equals(stringUnit))
			return null;
		double value = 0;
		
		Matcher unitMatcher = null;
		if(stringUnit!=null){
			Pattern UNIT_PATTERN = Pattern.compile("([+-]?\\d*\\.?\\d*) "+Pattern.quote(stringUnit));
			unitMatcher = UNIT_PATTERN.matcher(asString);
		}
		if(unitMatcher!=null && unitMatcher.find()){
			value = Double.parseDouble(unitMatcher.group(1));
		}
		else{
			value = Double.parseDouble(asString);
		}
		
		return new Measurement(value, unit);
	}
	public static Measurement parsePositiveMeasurement(String asString, String stringUnit, Unit unit){
		Measurement val = parseMeasurement(asString, stringUnit, unit);
		if(val == null)
			return null;
		if(val.value <= 0){
			return null;
		}
		return val;
	}
	public static Measurement parseNegativeMeasurement(String asString, String stringUnit, Unit unit){
		Measurement val = parseMeasurement(asString, stringUnit, unit);
		if(val == null)
			return null;
		if(val.value >= 0){
			return null;
		}
		return val;
	}

	private static final DateTimeFormatter DATE_FORMAT_1 = DateTimeFormat.forPattern("MM/dd/yyyy");
	private static final DateTimeFormatter DATE_FORMAT_2 = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm");
	private static final DateTimeFormatter DATE_FORMAT_3 = DateTimeFormat.forPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATE_FORMAT_4 = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a");
	public static LocalDate parseLocalDate(String asString){
		if(asString==null)
			return null;
		if(asString.isEmpty())
			return null;
		if(asString.equals("None"))
			return null;
		try{
			return DATE_FORMAT_1.parseLocalDate(asString);
		} catch(IllegalArgumentException e1){
			try{
				return DATE_FORMAT_2.parseLocalDate(asString);
			} catch(IllegalArgumentException e2){
				try{
					return DATE_FORMAT_3.parseLocalDate(asString);
				} catch(IllegalArgumentException e3){
					try{
						return DATE_FORMAT_4.parseLocalDate(asString);
					} catch(IllegalArgumentException e4){
						throw new DataFormatException("Unable to parse date: "+asString);
					}
				}
			}
		}
	}
	
	public static String notEmpty(String... values){
		for(String value:values){
			if(!value.isEmpty())
				return value;
		}
		return null;
	}
	
	public static Integer parseInt(String asString) {
		if(asString==null)
			return null;
		if(asString.isEmpty())
			return null;
		try{
			return Integer.parseInt(asString);
		} catch(NumberFormatException e){
			throw new DataFormatException("Unable to parse integer: "+asString);
		}
	}
	
	public static Integer parsePositiveInt(String asString) {
		if(asString==null)
			return null;
		if(asString.isEmpty())
			return null;
		try{
			int val = Integer.parseInt(asString);
			if(val<=0)
				return null;
			return val;
		} catch(NumberFormatException e){
			throw new DataFormatException("Unable to parse integer: "+asString);
		}
	}
	

	public static Boolean parseYesNo(String asString) {
		if(asString==null || asString.isEmpty())
			return null;
		asString = asString.toLowerCase();
		switch(asString){
		case "no":
			return false;
		case "yes":
		case "si":
			return true;
		default:
			throw new DataFormatException("Yes/No value was not yes or no: "+asString);
		}
	}

	public static Boolean parseTF(String asString) {
		if(asString==null || asString.isEmpty())
			return null;
		asString = asString.toLowerCase();
		switch(asString){
		case "f":
			return false;
		case "t":
			return true;
		default:
			throw new DataFormatException("T/F value was not T or F: "+asString);
		}
	}
	
}
