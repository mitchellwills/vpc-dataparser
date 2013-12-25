package datamerger.canals;

import static datamerger.data.AlternateIdDataObject.Queries.*;
import static datamerger.data.DataObject.Queries.*;
import static datamerger.data.DataParseUtil.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.joda.time.*;

import parserutil.datatable.*;
import au.com.bytecode.opencsv.*;

import com.google.common.base.*;

import datamerger.canals.JobItem.JobItemState;
import datamerger.canals.pointdepth.*;
import datamerger.data.*;

public class CanalsMerger {

	public static void parse(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<JobItem> jobItems,
			final DatabaseTable<IsmarLink> ismarLinks,
			final DatabaseTable<IsmarNode> ismarNodes) throws IOException {

		DataParseUtil.parseAlternateIdsInput(new FileReader("canaldata/canals.csv"), canals, Canal.class);
		DataParseUtil.parseAlternateIdsInput(new FileReader("canaldata/segments.csv"), segments, CanalSegment.class);
		
		parseCanalUTPData(canals, segments, jobItems);
		parseCanalSMUData(canals, segments, jobItems);
		parseSegmentiGIS(canals, segments, jobItems);
		parseSegmentiTotaleGIS(canals, segments, jobItems);
		parseRiiGIS(canals, segments, jobItems);
		parseSegmentCentroids(canals, segments);
		
		parseDescriptions(canals);
		
		//parsePuntiBatimetrici(canals, segments);

		parseIsmarSegmentMapping(canals, segments, ismarLinks);
		parseIsmarLinks(ismarLinks);
		parseIsmarNodes(ismarNodes);

		parseVE12Data(canals, segments, ismarLinks);
	}
	
	
	private static final Pattern SEGMENTID_AND_PATTERN = Pattern.compile("(\\w+) and (\\w+)");
	private static void parseIsmarSegmentMapping(final DatabaseTable<Canal> canals, final DatabaseTable<CanalSegment> segments, final DatabaseTable<IsmarLink> links) throws FileNotFoundException, IOException{
		try(CSVReader reader = new CSVReader(new FileReader("canaldata/ismartosegments.csv"))){
			List<String[]> allRows = reader.readAll();
			for(int i = 1; i<allRows.size(); ++i){//skip first row of headers
				String[] row = allRows.get(i);
				if(row.length==0)
					continue;
				String ismarLinkId = row[0].trim();
				if(ismarLinkId.isEmpty())
					continue;
				String segmentIdField = row[1].trim();
				String canalName = DataParseUtil.notEmpty(row[2].trim());
				
				Matcher andPatternMatcher = SEGMENTID_AND_PATTERN.matcher(segmentIdField);
				
				Set<String> segmentIds = new HashSet<String>();
				
				if(andPatternMatcher.find()){
					segmentIds.add(andPatternMatcher.group(1));
					segmentIds.add(andPatternMatcher.group(2));
				}
				else if(!segmentIdField.equals("Rii tera")){
					segmentIds.add(segmentIdField);
				}

				IsmarLink link = links.createIfNotExist(IsmarLink.class, ismarLinkId);
				for(String segmentId:segmentIds){
					CanalSegment segment = segments.queryOne(byIdOrAlternate(segmentId));
					segment.ismarLinks().get().add(link);
					link.segmentIds().get().add(segment.id().get());

					Canal canal = canals.queryOne(byIdOrAlternate(segment.canalId().get()));
					canal.name().set(canalName, "ismartosegments.csv");
				}
			}
		}
	}
	
	private static void parseCanalSMUData(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<JobItem> jobItems) throws FileNotFoundException, IOException{
		SimpleTable smuData = CSVTableReader.read(new FileReader("canaldata/smu-segmenti.csv"));

		tryForEach(smuData.getEntriesValues(), "smu-segmenti.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> smuEntry) {
				String segmentId = smuEntry.get("ID");
				String canalId = smuEntry.get("ID del rio");
				
				Canal canal = canals.queryOne(byIdOrAlternate(canalId));
				CanalSegment segment = segments.queryOne(byIdOrAlternate(segmentId));
				canal.segments().get().add(segment);

				segment.canalId().set(canal.id().get(), "smu-segmenti.csv");
				
				segment.cemented().set(DataParseUtil.parseYesNo(smuEntry.get("fondo cementificato")), "smu-segmenti.csv");
	
				segment.averageWidth().set(DataParseUtil.parsePositiveMeasurement(smuEntry.get("larghezza media (m)"), "m", Unit.M), "smu-segmenti.csv");

				segment.minWidth().set(DataParseUtil.parsePositiveMeasurement(smuEntry.get("larghezza minima (m)"), "m", Unit.M), "smu-segmenti.csv");
	
				segment.area().set(DataParseUtil.parsePositiveMeasurement(smuEntry.get("superficie (m2)"), "m2", Unit.M2), "smu-segmenti.csv");

				segment.length().set(DataParseUtil.parsePositiveMeasurement(smuEntry.get("lunghezza (m)"), "m", Unit.M), "smu-segmenti.csv");
				
				String[] itemIds = DataParseUtil.fromStringArray(smuEntry.get("jobItems"));
				for(String itemId:itemIds){
					JobItem item = jobItems.getById(itemId);
					if(item!=null)
						item.segmentIds().get().add(segment.id().get());
					segment.jobItems().get().add(jobItems.queryOne(byId(itemId)));
				}
				return null;
			}
		});
	}

	private static void parseCanalUTPData(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<JobItem> jobItems) throws FileNotFoundException, IOException{
		SimpleTable utpData = CSVTableReader.read(new FileReader("canaldata/utp-segmenti e intersezioni.csv"));

		tryForEach(utpData.getEntriesValues(), "utp-segmenti e intersezioni.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> utpEntry) {
				String id = DataParseUtil.notEmpty(utpEntry.get("ID UTP"), utpEntry.get("codice UTP"));
				if(id==null)
					throw new RuntimeException("Unknown Id");
				JobItem jobItem = jobItems.create(JobItem.class, id);
				
				switch(utpEntry.get("stato avanzamento lavori")){
				case "terminati":
					jobItem.state().set(JobItemState.Finished, "utp-segmenti e intersezioni.csv");
					break;
				case "in progettazione":
					jobItem.state().set(JobItemState.InProgress, "utp-segmenti e intersezioni.csv");
					break;
				default:
					throw new RuntimeException("Unknown job item state: "+utpEntry.get("stato avanzamento lavori"));
				}
	
				jobItem.averageDepth().set(DataParseUtil.parsePositiveMeasurement(utpEntry.get("batimetria media"), "cm", Unit.CM), "utp-segmenti e intersezioni.csv");

				jobItem.landfillCost().set(DataParseUtil.parseMeasurement(utpEntry.get("costo discarica"), "?", Unit.Euro), "utp-segmenti e intersezioni.csv");
				jobItem.dryDredgeCost().set(DataParseUtil.parseMeasurement(utpEntry.get("costo scavo secco"), "?", Unit.Euro), "utp-segmenti e intersezioni.csv");
				jobItem.wetDredgeCost().set(DataParseUtil.parseMeasurement(utpEntry.get("costo scavo umido"), "?", Unit.Euro), "utp-segmenti e intersezioni.csv");
				
				jobItem.dryDredged().set(DataParseUtil.parseMeasurement(utpEntry.get("mc scavo secco"), null, Unit.M3), "utp-segmenti e intersezioni.csv");
				jobItem.wetDredged().set(DataParseUtil.parseMeasurement(utpEntry.get("mc scavo umido"), null, Unit.M3), "utp-segmenti e intersezioni.csv");

				jobItem.mudVolume().set(DataParseUtil.parseMeasurement(utpEntry.get("volume fango stimato"), "m3", Unit.M3), "utp-segmenti e intersezioni.csv");
	
				jobItem.note().set(DataParseUtil.notEmpty(utpEntry.get("note")), "utp-segmenti e intersezioni.csv");
				
				jobItem.targetDepth().set(DataParseUtil.parsePositiveMeasurement(utpEntry.get("fondo di progetto"), "cm", Unit.CM), "utp-segmenti e intersezioni.csv");
				
				jobItem.startDate().set(DataParseUtil.parseLocalDate(utpEntry.get("data inizo lavori")), "utp-segmenti e intersezioni.csv");
				jobItem.endDate().set(DataParseUtil.parseLocalDate(DataParseUtil.notEmpty(utpEntry.get("data di compilazione"), utpEntry.get("data fine lavori"))), "utp-segmenti e intersezioni.csv");
				
				return null;
			}
		});
	}

	private static void parseRiiGIS(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<JobItem> jobItems) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/gis-rii.csv"));

		tryForEach(data.getEntriesValues(), "gis-rii.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("Codice");
				Canal canal = canals.queryOne(byIdOrAlternate(id));

				canal.gisId().set(id, "gis-rii.csv");
				
				canal.name().set(entry.get("Nome_Rio"), "gis-rii.csv");
				canal.insulaNumber().set(DataParseUtil.parseInt(entry.get("Insula_Numero")), "gis-rii.csv");
				canal.length().set(DataParseUtil.parsePositiveMeasurement(entry.get("Lunghezza"), null, Unit.M), "gis-rii.csv");
				canal.area().set(DataParseUtil.parsePositiveMeasurement(entry.get("Superficie"), null, Unit.M2), "gis-rii.csv");
				canal.targetDepth().set(DataParseUtil.parsePositiveMeasurement(entry.get("Fondo_di_Progetto"), null, Unit.CM), "gis-rii.csv");
				
				return null;
			}
		});
	}
	private static void parseDescriptions(final DatabaseTable<Canal> canals) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/descriptions.csv"));

		tryForEach(data.getEntriesValues(), "descriptions.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("Code");
				Canal canal = canals.queryOne(byIdOrAlternate(id));

				canal.englishIntroduction().set(entry.get("Introduction (English)"), "descriptions.csv");
				canal.italianIntroduction().set(entry.get("Introduzione (Italiano)"), "descriptions.csv");
				
				return null;
			}
		});
	}
	
	private static void parseSegmentiGIS(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<JobItem> jobItems) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/gis-segmenti.csv"));
		
		tryForEach(data.getEntriesValues(), "gis-segmenti.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("Codice");
				String canalId = entry.get("Codice_Rio");

				Canal canal = canals.queryOne(byIdOrAlternate(canalId));
				CanalSegment segment = segments.queryOne(byIdOrAlternate(id));

				segment.gisId().set(id, "gis-segmenti.csv");
				
				segment.canalId().set(canal.id().get(), "gis-segmenti.csv");
				segment.length().set(DataParseUtil.parsePositiveMeasurement(entry.get("Lunghezza"), null, Unit.M), "gis-segmenti.csv");
				segment.area().set(DataParseUtil.parsePositiveMeasurement(entry.get("Superficie"), null, Unit.M2), "gis-segmenti.csv");
				segment.averageWidth().set(DataParseUtil.parsePositiveMeasurement(entry.get("Larghezza_Media"), null, Unit.M), "gis-segmenti.csv");
				
				return null;
			}
		
		});
	}
	
	private static void parseSegmentiTotaleGIS(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<JobItem> jobItems) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/gis-segmenti-totale.csv"));
		
		tryForEach(data.getEntriesValues(), "gis-segmenti-totale.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("id");
				String canalId = entry.get("id_rio");

				Canal canal = canals.queryOne(byIdOrAlternate(canalId));
				CanalSegment segment = segments.queryOne(byIdOrAlternate(id));

				segment.gisTotalId().set(id, "gis-segmenti-totale.csv");
				
				segment.canalId().set(canal.id().get(), "gis-segmenti-totale.csv");
				segment.length().set(DataParseUtil.parsePositiveMeasurement(entry.get("lung"), null, Unit.M), "gis-segmenti-totale.csv");
				segment.area().set(DataParseUtil.parsePositiveMeasurement(entry.get("sup"), null, Unit.M2), "gis-segmenti-totale.csv");
				segment.averageWidth().set(DataParseUtil.parsePositiveMeasurement(entry.get("larg_md"), null, Unit.M), "gis-segmenti-totale.csv");
				segment.minWidth().set(DataParseUtil.parsePositiveMeasurement(entry.get("larg_min"), null, Unit.M), "gis-segmenti-totale.csv");
				
				return null;
			}
		
		});
	}


	private static void parseIsmarLinks(final DatabaseTable<IsmarLink> links) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/ismar-links.csv"));
		
		tryForEach(data.getEntriesValues(), "ismar-links.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String linkId = entry.get("id");

				IsmarLink link = links.createIfNotExist(IsmarLink.class, linkId);

				link.nodeIds().get().add(entry.get("id_nodo1"));
				link.nodeIds().get().add(entry.get("id_nodo2"));

				link.cemented().set(DataParseUtil.parseTF(entry.get("cementificato")), "ismar-links.csv");
				
				return null;
			}
		
		});
	}
	private static void parseIsmarNodes(final DatabaseTable<IsmarNode> nodes) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/ismar-nodes.csv"));
		
		tryForEach(data.getEntriesValues(), "ismar-nodes.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String nodeId = entry.get("id");

				IsmarNode node = nodes.createIfNotExist(IsmarNode.class, nodeId);
				
				for(int i = 1; i<=4; ++i){
					String linkId = entry.get("id_link"+i);
					if(!linkId.equals("0"))
						node.linkIds().get().add(linkId);
				}

				double x = Double.parseDouble(entry.get("x"));
				double y = Double.parseDouble(entry.get("y"));
				node.location().set(new Location(x, y), "ismar-nodes.csv");
				
				return null;
			}
		
		});
	}

	private static void parseSegmentCentroids(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/segment-centroids.csv"));
		
		tryForEach(data.getEntriesValues(), "segment-centroids.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("id").trim();
				CanalSegment segment = segments.queryOne(byIdOrAlternate(id));
				
				if(!entry.get("X").isEmpty() && !entry.get("Y").isEmpty()){
					double lon = Double.parseDouble(entry.get("X"));
					double lat = Double.parseDouble(entry.get("Y"));
					segment.location().set(new Location(lon, lat), "segment-centroids.csv");
				}
				
				return null;
			}
		});
	}

	private static void parseVE12Data(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments,
			final DatabaseTable<IsmarLink> ismarLinks) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/VE12-Hydro_Individual Canal pages.csv"));
		
		tryForEach(data.getEntriesValues(), "VE12-Hydro_Individual Canal pages.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				String id = entry.get("Code").trim();
				Canal canal = canals.queryOne(byIdOrAlternate(id));

				canal.length().set(DataParseUtil.parsePositiveMeasurement(entry.get("Length (m)"), null, Unit.M), "VE12-Hydro_Individual Canal pages.csv");
				canal.averageDepth().set(DataParseUtil.parseNegativeMeasurement(entry.get("Average Depth (m)"), null, Unit.M), "VE12-Hydro_Individual Canal pages.csv");
	
				if(!entry.get("Latitude ").isEmpty() && !entry.get("Longitude").isEmpty()){
					double lat = Double.parseDouble(entry.get("Latitude "));
					double lon = Double.parseDouble(entry.get("Longitude"));
					canal.location().set(new Location(lon, lat), "VE12-Hydro_Individual Canal pages.csv");
				}
				
				return null;
			}
		});
	}
	

	private static void parsePuntiBatimetrici(final DatabaseTable<Canal> canals,
			final DatabaseTable<CanalSegment> segments) throws FileNotFoundException, IOException{
		SimpleTable data = CSVTableReader.read(new FileReader("canaldata/punti-batrimetrici-segmenti.csv"));
		
		int max = 0;
		for(Map<String, String> entry:data.getEntriesValues()){
			int id = Integer.parseInt(entry.get("id"));
			if(id>max)
				max = id;
		}
		final List<CanalPointDepthMeasurement> measurements = new ArrayList<CanalPointDepthMeasurement>(max);
		tryForEach(data.getEntriesValues(), "punti-batrimetrici-segmenti.csv", new Function<Map<String, String>, Void>(){
			@Override
			public Void apply(Map<String, String> entry) {
				int id = Integer.parseInt(entry.get("id"));

				double x = Double.parseDouble(entry.get("x"));
				double y = Double.parseDouble(entry.get("y"));
				Location location = new Location(x, y);
				
				String segmentId = entry.get("Codice");
				segmentId = segmentId.isEmpty()?null:segments.queryOne(byIdOrAlternate(segmentId)).id().get();
				
				LocalDate date = DataParseUtil.parseLocalDate(entry.get("dt_ril"));
				
				Measurement depth = DataParseUtil.parseNegativeMeasurement(entry.get("z"), null, Unit.M);

				measurements.add(new CanalPointDepthMeasurement(id, location, date, segmentId, depth));
				
				return null;
			}
		});
		PointDepthProcessor processor = new PointDepthProcessor(measurements);
		processor.process();
	}
}
