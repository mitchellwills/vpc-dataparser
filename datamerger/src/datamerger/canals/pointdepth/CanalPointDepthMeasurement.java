package datamerger.canals.pointdepth;

import org.joda.time.*;

import datamerger.data.*;

public class CanalPointDepthMeasurement {
	public final int id;
	public final Location location;
	public final LocalDate date;
	public final String segmentId;
	public final Measurement depth;

	public CanalPointDepthMeasurement(int id, Location location, LocalDate date, String segmentId, Measurement depth){
		this.id = id;
		this.location = location;
		this.date = date;
		this.segmentId = segmentId;
		this.depth = depth;
	}
}
