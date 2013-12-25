package maintenanceprocessor;

import java.io.*;
import java.util.*;

import org.joda.time.*;
import org.json.*;

import parserutil.datatable.*;
import au.com.bytecode.opencsv.*;

import com.google.common.collect.*;

public class MaintenanceProcessor {

	static LocalDate firstDate = LocalDate.parse("3000-01-01");
	static LocalDate lastDate = LocalDate.parse("1000-01-01");
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		SegmentCollection segments = new SegmentCollection();
		Map<String, String> segmentCodeToGisId = Maps.newHashMap();
		try(CSVReader segmentsReader = new CSVReader(new FileReader("segments.csv"))){
			segmentsReader.readNext();//read column headers
			String[] nextLine;
			while((nextLine = segmentsReader.readNext()) != null){
				String segmentCode = nextLine[0];
				String gisId = nextLine[5];
				segmentCodeToGisId.put(segmentCode, gisId);
			}
		}
		try(Reader reader = new FileReader("jobItems.csv")){
			SimpleTable jobItems = CSVTableReader.read(reader);
			for(Map<String, String> jobData:jobItems.getEntriesValues()){
				String endDateString = jobData.get("End Date");
				String[] segmentIds = jobData.get("Segment IDs").split(",");
				try{
					LocalDate date = LocalDate.parse(endDateString);
					for(String segmentCode:segmentIds){
						if(segmentCode.isEmpty() || segmentCodeToGisId.get(segmentCode).isEmpty())
							continue;
						if(date.isBefore(firstDate))
							firstDate = date;
						if(date.isAfter(lastDate))
							lastDate = date;
						CanalSegment segment = segments.getSegment(segmentCodeToGisId.get(segmentCode));
						segment.maintenance.put(date, jobData);
					}
				} catch(IllegalArgumentException e){
					//e.printStackTrace();
				}
			}
		}

		JSONObject data = new JSONObject();
		
		data.put("firstDate", firstDate.toString());
		data.put("numDates", index(lastDate));
		
		JSONObject segmentsData = new JSONObject();
		data.put("segments", segmentsData);
		
		for(CanalSegment segment:segments){
			JSONObject segmentData = new JSONObject();
			segmentsData.put(segment.segmentCode, segmentData);
			
			if(!segment.maintenance.isEmpty()){
				LocalDate first = segment.maintenance.firstKey();
				segmentData.put("first", index(first));
				
				JSONArray maintenance = new JSONArray();
				for(Map.Entry<LocalDate, Map<String, String>> entry:segment.maintenance.entrySet()){
					JSONObject maintenanceData = new JSONObject();
					maintenanceData.put("timeIndex", index(entry.getKey()));
					for(Map.Entry<String, String> e:entry.getValue().entrySet()){
						maintenanceData.put(e.getKey(), e.getValue());
					}
					maintenance.put(maintenanceData);
				}
				segmentData.put("all", maintenance);
			}
		}
		
		try(FileWriter writer = new FileWriter("maintenance.json")){
			data.write(writer);
		}
	}
	
	private static final long index(LocalDate date){
		return new Duration(firstDate.toDateTimeAtStartOfDay(), date.toDateTimeAtStartOfDay()).getStandardDays();
	}
	

	private static class CanalSegment{
		public final String segmentCode;
		public final SortedMap<LocalDate, Map<String, String>> maintenance = Maps.newTreeMap(new LocalDateComparator());
		public CanalSegment(String segmentCode){
			this.segmentCode = segmentCode;
		}
	}
	

	private static class SegmentCollection implements Iterable<CanalSegment>{
		private final Map<String, CanalSegment> segments = Maps.newHashMap();
		public CanalSegment getSegment(String segmentCode){
			CanalSegment segment = segments.get(segmentCode);
			if(segment!=null)
				return segment;
			segment = new CanalSegment(segmentCode);
			segments.put(segmentCode, segment);
			return segment;
		}
		public Iterator<CanalSegment> iterator(){
			return segments.values().iterator();
		}
	}

	public static class LocalDateComparator implements Comparator<LocalDate> {
		@Override
		public int compare(LocalDate d1, LocalDate d2) {
			if(d1.equals(d2))
				return 0;
			if(d1.isBefore(d2))
				return -1;
			return 1;
		}
	}

}
