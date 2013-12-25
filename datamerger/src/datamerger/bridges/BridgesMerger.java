package datamerger.bridges;

import static datamerger.data.AlternateIdDataObject.Queries.*;
import static datamerger.data.DataParseUtil.*;

import java.io.*;
import java.util.*;

import parserutil.datatable.*;

import com.google.common.base.*;

import datamerger.canals.*;
import datamerger.data.*;

public class BridgesMerger {

	public static void parse(final DatabaseTable<Bridge> bridges,
			final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments) throws IOException {
		DataParseUtil.parseAlternateIdsInput(new FileReader("bridgedata/bridges.csv"), bridges, Bridge.class);
		parseEditedPontiStandalone(bridges, canals, segments);
		parsePontiGIS(bridges, canals, segments);
		parseBridgeSMUData(canals, segments, bridges);
	}


	private static void parseEditedPontiStandalone(final DatabaseTable<Bridge> bridges,
			final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("bridgedata/Edited Ponti (Standalone).csv"));

		tryForEach(data.getEntriesValues(), "Edited Ponti (Standalone).csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("Bridge Code").trim();
				Bridge bridge = bridges.queryOne(byIdOrAlternate(id));

				String segmentId = DataParseUtil.notEmpty(entry.get("Segment Code").trim());
				CanalSegment segment = segmentId==null?null:segments.queryOne(byIdOrAlternate(segmentId));
				
				String canalName = DataParseUtil.notEmpty(entry.get("Crossing River").trim());
				
				if(segment!=null){
					bridge.segmentId().set(segment.id().get(), "Edited Ponti (Standalone).csv");
					if(segment.canalId()!=null){
						Canal canal = canals.queryOne(byIdOrAlternate(segment.canalId().get()));
						canal.name().set(canalName, "Edited Ponti (Standalone).csv");
					}
				}

				bridge.name().set(DataParseUtil.notEmpty(entry.get("Name of Bridge")), "Edited Ponti (Standalone).csv");
				
				bridge.yearBuilt().set(DataParseUtil.parsePositiveInt(entry.get("Year Constructed")), "Edited Ponti (Standalone).csv");
				
				bridge.numeroZucchetta().set(DataParseUtil.notEmpty(entry.get("Numero Zucchetta").trim()), "Edited Ponti (Standalone).csv");
				
				bridge.isPrivate().set(DataParseUtil.parseYesNo(entry.get("Private")), "Edited Ponti (Standalone).csv");
				
				bridge.arcStyle().set(BridgeArcStyle.fromId(DataParseUtil.parseInt(entry.get("Arc Style Code"))), "Edited Ponti (Standalone).csv");

				bridge.constructionMaterial().set(BridgeConstructionMaterial.fromId(DataParseUtil.parseInt(entry.get("Construction Material Code"))), "Edited Ponti (Standalone).csv");

				bridge.decorations().set(DataParseUtil.notEmpty(entry.get("Decorations").trim()), "Edited Ponti (Standalone).csv");

				bridge.handicappedAccessible().set(DataParseUtil.parseYesNo(entry.get("Handicapped Accessible")), "Edited Ponti (Standalone).csv");

				bridge.additionalHandrail().set(DataParseUtil.parseYesNo(entry.get("Additional Handrail")), "Edited Ponti (Standalone).csv");

				bridge.northSteps().set(DataParseUtil.parseInt(entry.get("Number of Steps North")), "Edited Ponti (Standalone).csv");
				bridge.southSteps().set(DataParseUtil.parseInt(entry.get("Number of Steps South")), "Edited Ponti (Standalone).csv");
				bridge.totalSteps().set(DataParseUtil.parseInt(entry.get("Total Number of Steps")), "Edited Ponti (Standalone).csv");
				
				bridge.height().set(DataParseUtil.parsePositiveMeasurement(entry.get("Height"), null, Unit.M), "Edited Ponti (Standalone).csv");
				
				bridge.minimumStepWidth().set(DataParseUtil.parsePositiveMeasurement(entry.get("Minimum Step Width"), null, Unit.M), "Edited Ponti (Standalone).csv");
				
				bridge.overallWidth().set(DataParseUtil.parsePositiveMeasurement(entry.get("Overall Width"), null, Unit.M), "Edited Ponti (Standalone).csv");
				
				bridge.note().set(DataParseUtil.notEmpty(entry.get("Note:").trim()), "Edited Ponti (Standalone).csv");
				
				return null;
			}
		});
	}

	private static void parsePontiGIS(final DatabaseTable<Bridge> bridges,
			final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("bridgedata/ponti-gis.csv"));

		tryForEach(data.getEntriesValues(), "ponti-gis.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("Codice_Ponte").trim();
				Bridge bridge = bridges.queryOne(byIdOrAlternate(id));

				String segmentId = DataParseUtil.notEmpty(entry.get("Segmento").trim());
				CanalSegment segment = segmentId==null?null:segments.queryOne(byIdOrAlternate(segmentId));
				
				String canalName = DataParseUtil.notEmpty(entry.get("Nome_Rio").trim());
				
				if(segment!=null){
					bridge.segmentId().set(segment.id().get(), "ponti-gis.csv");
					if(segment.canalId()!=null){
						Canal canal = canals.queryOne(byIdOrAlternate(segment.canalId().get()));
						canal.name().set(canalName, "ponti-gis.csv");
					}
				}

				bridge.name().set(DataParseUtil.notEmpty(entry.get("Nome_Ponte")), "ponti-gis.csv");
				
				bridge.numeroZucchetta().set(DataParseUtil.notEmpty(entry.get("Numero_Zucchetta").trim()), "ponti-gis.csv");
				
				return null;
			}
		});
	}
	
	private static void parseBridgeSMUData(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<Bridge> bridges) throws FileNotFoundException, IOException{
		SimpleTable smuData = CSVTableReader.read(new FileReader("bridgedata/smu-ponti.csv"));
		tryForEach(smuData.getEntriesValues(), "smu-ponti.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> smuEntry) {
				String bridgeId = smuEntry.get("!!id");
				
				Bridge bridge = bridges.queryOne(byIdOrAlternate(bridgeId));

				bridge.name().set(smuEntry.get("nome del ponte"), "smu-ponti.csv");
				
				bridge.decorations().set(DataParseUtil.notEmpty(smuEntry.get("decorazioni")), "smu-ponti.csv");

				String segmentId = smuEntry.get("ID del segmento");
				String canalName = smuEntry.get("nome del rio");
				if(!segmentId.isEmpty()){
					CanalSegment segment = segments.queryOne(byIdOrAlternate(segmentId));
					if(segment!=null){
						bridge.segmentId().set(segment.id().get(), "smu-ponti.csv");
						if(segment.canalId()!=null){
							Canal canal = canals.queryOne(byIdOrAlternate(segment.canalId().get()));
							canal.name().set(canalName, "smu-ponti.csv");
						}
					}
				}

				bridge.handicappedAccessible().set(DataParseUtil.parseYesNo(smuEntry.get("accessibile handicappati")), "smu-ponti.csv");

				String isPriveString = smuEntry.get("ragione giuridica");
				switch(isPriveString){
				case "pubblica":
					bridge.isPrivate().set(false, "smu-ponti.csv");
					break;
				case "privata":
					bridge.isPrivate().set(true, "smu-ponti.csv");
					break;
				default:
					throw new RuntimeException("Unknown public/private status: "+isPriveString);
				}

				bridge.yearBuilt().set(DataParseUtil.parsePositiveInt(smuEntry.get("anno di costruzione")), "smu-ponti.csv");
				
				return null;
			}
		});
	}
}
