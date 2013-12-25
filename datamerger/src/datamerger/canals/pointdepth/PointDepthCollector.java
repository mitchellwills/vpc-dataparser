package datamerger.canals.pointdepth;

import java.util.*;

import org.apache.commons.math3.stat.regression.*;
import org.joda.time.*;

import com.google.common.collect.*;

public class PointDepthCollector {
	private final Set<CanalPointDepthMeasurement> measurements = Sets.newTreeSet(new Comparator<CanalPointDepthMeasurement>(){
		@Override
		public int compare(CanalPointDepthMeasurement arg0,
				CanalPointDepthMeasurement arg1) {
			return Integer.compare(arg0.id, arg1.id);
		}
	});
	private final SimpleRegression locationRegression = new SimpleRegression();
	private LocalDate date = null;
	
	public Set<CanalPointDepthMeasurement> points(){
		return measurements;
	}
	public int size(){
		return measurements.size();
	}
	public LocalDate date(){
		return date;
	}
	
	public double getLocationRSquare(){
		return locationRegression.getRSquare();
	}

	public void add(CanalPointDepthMeasurement measurement){
		if(date == null)
			date = measurement.date;
		measurements.add(measurement);
		locationRegression.addData(measurement.location.lon, measurement.location.lat);
	}
	public void add(PointDepthCollector collector){
		for(CanalPointDepthMeasurement measurement:collector.points())
			add(measurement);
	}


	private SimpleRegression cloneLocationRegression(){
		SimpleRegression cloneLocationRegression = new SimpleRegression();
		for(CanalPointDepthMeasurement measurement:points())
			cloneLocationRegression.addData(measurement.location.lon, measurement.location.lat);
		return cloneLocationRegression;
	}

	
	public double RSquareWith(CanalPointDepthMeasurement measurement){
		SimpleRegression newLocationRegression = cloneLocationRegression();
		newLocationRegression.addData(measurement.location.lon, measurement.location.lat);
		return newLocationRegression.getRSquare();
	}
	
	public double RSquareWith(PointDepthCollector other){
		SimpleRegression newLocationRegression = cloneLocationRegression();
		for(CanalPointDepthMeasurement measurement:other.points())
			newLocationRegression.addData(measurement.location.lon, measurement.location.lat);
		return newLocationRegression.getRSquare();
	}

	public double minimumDistance(PointDepthCollector other){
		double minDistance = Double.MAX_VALUE;
		for(CanalPointDepthMeasurement otherMeasurement:other.points()){
			double distance = minimumDistance(otherMeasurement);
			if(distance<minDistance)
				minDistance = distance;
		}
		return minDistance;
	}
	
	public double minimumDistance(CanalPointDepthMeasurement measurement){
		double minDistance = Double.MAX_VALUE;
		for(CanalPointDepthMeasurement otherMeasurement:measurements){
			double distance = measurement.location.distanceTo(otherMeasurement.location);
			if(distance<minDistance)
				minDistance = distance;
		}
		return minDistance;
	}
	
	public double locationResidual(CanalPointDepthMeasurement measurement){
		double x = measurement.location.lon;
		double y = measurement.location.lat;
		double m = locationRegression.getSlope();
		double b = locationRegression.getIntercept();
		
		return (Math.abs(y - m * x - b))/Math.sqrt(m * m + 1);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("PointDepthCollector: ");
		builder.append(size());
		builder.append(" [");
		for (CanalPointDepthMeasurement measurement : points()) {
			builder.append(measurement.id);
			builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}
}
