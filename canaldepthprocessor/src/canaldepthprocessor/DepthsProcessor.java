package canaldepthprocessor;

import java.io.*;
import java.util.*;

import org.joda.time.*;

import au.com.bytecode.opencsv.*;

import com.google.common.collect.*;

public class DepthsProcessor {
	public static void main(String[] args) throws IOException{
		SegmentCollection segments = new SegmentCollection();
		Map<String, String> gisIds = Maps.newHashMap();
		SortedSet<LocalDate> dateSet = Sets.newTreeSet(new LocalDateComparator());

		try(CSVReader segmentsReader = new CSVReader(new FileReader("segments.csv"))){
			segmentsReader.readNext();//read column headers
			String[] nextLine;
			while((nextLine = segmentsReader.readNext()) != null){
				String segmentCode = nextLine[0];
				String gisId = nextLine[5];
				gisIds.put(gisId, segmentCode);
			}
		}
		try(CSVReader jobReader = new CSVReader(new FileReader("jobItems.csv"))){
			jobReader.readNext();//read column headers
			String[] nextLine;
			while((nextLine = jobReader.readNext()) != null){
				String endDateString = nextLine[12];
				String[] segmentIds = nextLine[8].split(",");
				try{
					double depth = Double.parseDouble(nextLine[15])/100;
					LocalDate date = LocalDate.parse(endDateString);
					for(String segmentCode:segmentIds){
						if(segmentCode.isEmpty())
							continue;
						CanalSegment segment = segments.getSegment(segmentCode);
						segment.minDepths.put(date, depth);
						dateSet.add(date);
					}
				} catch(IllegalArgumentException e){
					//e.printStackTrace();
				}
			}
		}
		
		try(CSVReader depthReader = new CSVReader(new FileReader("canaldepthpoints.csv"))){
			depthReader.readNext();//read column headers
			String[] nextLine;
			while((nextLine = depthReader.readNext()) != null){
				String gisId = nextLine[14];
				String segmentCode = gisIds.get(gisId);
				String depthString = nextLine[3];
				String dateString = nextLine[7];
				try{
					LocalDate date = LocalDate.parse(dateString);
					double depth = Double.parseDouble(depthString);
					CanalSegment segment = segments.getSegment(segmentCode);
					segment.minDepths.put(date, depth);
					dateSet.add(date);
				} catch(IllegalArgumentException e){
					//e.printStackTrace();
				}
			}
		}
		
		try(CSVWriter writer = new CSVWriter(new FileWriter("rawdepths.csv"))){
			List<LocalDate> dates = Lists.newArrayList(dateSet);
			
			List<String> headerList = Lists.newArrayList("segmentId");
			for(LocalDate date:dates)
				headerList.add(date.toString());
			String[] headers = headerList.toArray(new String[headerList.size()]);
			writer.writeNext(headers);
			
			for(CanalSegment segment:segments){
				String[] data = new String[headers.length];
				data[0] = segment.segmentCode;
				for (Map.Entry<LocalDate, Double> e : segment.minDepths) {
					data[dates.indexOf(e.getKey())+1] = e.getValue().toString();
				}
				writer.writeNext(data);
			}
		}
		
	}
	
	
	
	private static class CanalSegment{
		public final String segmentCode;
		public final MinMap minDepths = new MinMap();
		public CanalSegment(String segmentCode){
			this.segmentCode = segmentCode;
		}
	}
	
	private static class MinMap implements Iterable<Map.Entry<LocalDate, Double>>{
		private final Map<LocalDate, Double> values = Maps.newTreeMap(new LocalDateComparator());
		public void put(LocalDate key, double value){
			Double previousMin = values.get(key);
			if(previousMin==null || previousMin>value)
				values.put(key, value);
		}
		public double get(LocalDate key){
			return values.get(key);
		}
		public String toString(){
			return values.toString();
		}
		public Iterator<Map.Entry<LocalDate, Double>> iterator(){
			return values.entrySet().iterator();
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
