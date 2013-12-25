package ismarsegmentmodelparser.parser;

import ismarsegmentmodelparser.parser.Fort11Timestep.SegmentEntry;
import ismarsegmentmodelparser.parser.Fort11Timestep.TimestepHeader;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.joda.time.*;
import org.joda.time.format.*;
import org.json.*;

import parserutil.datatable.*;
import parserutil.datatable.AbstractTable;

import com.google.common.collect.*;

public class Fort11Parser {

	static DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM dd, yyyy HH:mm");
	public static void parse(Path folder) throws IOException {
		Path fort11File = folder.resolve("fort.11");
		List<String> allLines = Files.readAllLines(fort11File, Charset.defaultCharset());

		List<Fort11Timestep> timesteps = Lists.newArrayList();
		
		Iterator<String> lineIterator = allLines.iterator();
		while(lineIterator.hasNext()){
			timesteps.add(parseTimestep(lineIterator));
		}
		
		LocalDateTime initial = new LocalDateTime(2013, 11, 1, 0, 0);
		double max = 0;
		JSONArray data = new JSONArray();
		for(Fort11Timestep timestep:timesteps){
			JSONArray entryVelocityData = new JSONArray();
			for(int id = timestep.minId(); id<=timestep.maxId(); ++id){
				SegmentEntry entry = timestep.getEntry(id);
				if(entry!=null){
					entryVelocityData.put(entry.id-1, String.format("%.3f", entry.velocity));
					if(max < Math.abs(entry.velocity))
						max = Math.abs(entry.velocity);
				}
			}
			JSONObject timestepData = new JSONObject();
			int timestepIndex = (int)Math.round(timestep.header.time/3600);
			LocalDateTime date = initial.plusHours(timestepIndex);
			timestepData.put("l", fmt.print(date));
			timestepData.put("v", entryVelocityData);
			data.put(timestepIndex, timestepData);
		}
		try(FileWriter writer = new FileWriter("out.json")){
			data.write(writer);
		}
		System.out.println("wrote file");
		

		SimpleTable reader = CSVTableReader.read(new FileReader("modeltogisid.csv"));
		final Map<Integer, String> idToGis = Maps.newHashMap();
		for(Map<String, String> entry:reader.getEntriesValues()){
			idToGis.put(Integer.parseInt(entry.get("Ismar Link Ids")), entry.get("id"));
		}
		
		Fort11Timestep firstTimestep = timesteps.get(0);
		Fort11Timestep lastTimestep = timesteps.get(timesteps.size()-1);
		Map<Integer, Double> changes = Maps.newHashMap();
		for(int id = firstTimestep.minId(); id<=firstTimestep.maxId(); ++id){
			SegmentEntry firstEntry = firstTimestep.getEntry(id);
			SegmentEntry lastEntry = lastTimestep.getEntry(id);
			changes.put(id, lastEntry.waterElevation);
		}
		CSVTableWriter.write(new FileWriter("elevationChange.csv"), new AbstractTable<Map.Entry<Integer, Double>>(changes.entrySet()) {
			@Override
			protected void processEntry(
					Map.Entry<Integer, Double> entry,
					AbstractTable.EntryDataCollection entryValues) {
				entryValues.put("id", idToGis.get(entry.getKey()+2000));
				entryValues.put("change", Double.toString(entry.getValue()));
			}
		});
	}
	
	
	private static Fort11Timestep parseTimestep(Iterator<String> lineIterator){
		TimestepHeader header = parseTimestepHeader(lineIterator);
		
		List<Fort11Timestep.SegmentEntry> entries = Lists.newArrayList();
		
		for(int i = 0; i<header.numEntries; ++i){
			entries.add(parseSegmentEntry(lineIterator));
		}
		
		return new Fort11Timestep(header, entries);
	}

	
	private static final Pattern HEADER_PATTERN = Pattern.compile("\\s+(?<time>\\d+.?\\d*)\\s+(?<numEntries>\\d+)\\s+(\\d+)");
	private static TimestepHeader parseTimestepHeader(Iterator<String> lineIterator){
		String line = lineIterator.next();
		Matcher matcher = HEADER_PATTERN.matcher(line);
		matcher.find();
		double time = Double.parseDouble(matcher.group("time"));
		int numEntries = Integer.parseInt(matcher.group("numEntries"));
		return new TimestepHeader(time, numEntries);
	}
	private static final Pattern ENTRY_PATTERN = Pattern.compile("\\s+(?<id>\\d+)\\s+(?<velocity>-?\\d+.?\\d*)\\s+(?<waterElevation>-?\\d+.?\\d*)\\s+(?<salinity>\\d+.?\\d*)\\s+(?<sedimentChange>-?\\d+.?\\d*)\\s+(?<suspendedConc>\\d+.?\\d*)\\s+(?<suspendedMass>\\d+.?\\d*)");
	private static SegmentEntry parseSegmentEntry(Iterator<String> lineIterator){
		String line = lineIterator.next();
		Matcher matcher = ENTRY_PATTERN.matcher(line);
		matcher.find();
		int id = Integer.parseInt(matcher.group("id"));
		double vel = Double.parseDouble(matcher.group("velocity"));
		double waterElevation = Double.parseDouble(matcher.group("waterElevation"));
		return new SegmentEntry(id, vel, waterElevation);
	}

}
