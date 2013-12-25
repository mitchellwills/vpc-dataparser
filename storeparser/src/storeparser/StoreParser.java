package storeparser;

import java.io.*;
import java.util.*;

import au.com.bytecode.opencsv.*;

import com.google.common.collect.*;

public class StoreParser {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Map<String, String[]> data = Maps.newHashMap();
		String[] locationHeader;
		try(CSVReader reader = new CSVReader(new FileReader("Store_locations.csv"))){
			locationHeader = reader.readNext();//header
			String[] line;
			while((line = reader.readNext())!=null){
				data.put(line[0], line);
			}
		}

		Map<String, CSVWriter> locationWriters = Maps.newHashMap();
		Map<String, CSVWriter> typesWriters = Maps.newHashMap();
		
		try(CSVReader reader = new CSVReader(new FileReader("Store types (all) - 2013.12.2 vFINAL.csv"))){
			String[] type_header = reader.readNext();//header
			String[] line;
			while((line = reader.readNext())!=null){
				String id = line[0];
				String year = line[15];
				
				String[] locationData = data.get(id);
				if(locationData!=null){
					CSVWriter location_writer = locationWriters.get(year);
					if(location_writer==null){
						location_writer = new CSVWriter(new FileWriter("Store_locations_"+year+".csv"));
						location_writer.writeNext(locationHeader);
						locationWriters.put(year, location_writer);
					}
					location_writer.writeNext(locationData);
				}

				CSVWriter type_writer = typesWriters.get(year);
				if(type_writer==null){
					type_writer = new CSVWriter(new FileWriter("Store_types_"+year+".csv"));
					type_writer.writeNext(type_header);
					typesWriters.put(year, type_writer);
				}
				type_writer.writeNext(line);
			}
		}
		for(CSVWriter writer:locationWriters.values())
			writer.close();
		for(CSVWriter writer:typesWriters.values())
			writer.close();
	}

}
