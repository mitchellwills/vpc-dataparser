package ismarsegmentmodelparser.parser;

import java.util.*;

public class Fort11Timestep {

	public final TimestepHeader header;
	private final List<SegmentEntry> entries;

	public Fort11Timestep(TimestepHeader header, List<SegmentEntry> entries) {
		this.header = header;
		this.entries = entries;
	}
	
	public SegmentEntry getEntry(int id){
		for(SegmentEntry entry:entries){
			if(entry.id==id)
				return entry;
		}
		return null;
	}
	public int minId(){
		int max = Integer.MAX_VALUE;
		for(SegmentEntry entry:entries){
			if(entry.id<max)
				max = entry.id;
		}
		return max;
	}
	public int maxId(){
		int max = Integer.MIN_VALUE;
		for(SegmentEntry entry:entries){
			if(entry.id>max)
				max = entry.id;
		}
		return max;
	}

	public static class TimestepHeader {

		public final double time;
		public final int numEntries;

		public TimestepHeader(double time, int numEntries) {
			this.time = time;
			this.numEntries = numEntries;
		}

	}

	public static class SegmentEntry{

		public final int id;
		public final double velocity;
		public final double waterElevation;

		public SegmentEntry(int id, double velocity, double waterElevation) {
			this.id = id;
			this.velocity = velocity;
			this.waterElevation = waterElevation;
		}
		
	}
}
