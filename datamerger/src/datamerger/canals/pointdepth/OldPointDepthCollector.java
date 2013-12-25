package datamerger.canals.pointdepth;

import java.util.*;

import org.apache.commons.math3.stat.regression.*;
import org.joda.time.*;

import com.google.common.collect.*;

public class OldPointDepthCollector {
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
	
	public double getLocationRSquare(){
		return locationRegression.getRSquare();
	}

	public void add(CanalPointDepthMeasurement measurement){
		if(date == null)
			date = measurement.date;
		measurements.add(measurement);
		locationRegression.addData(measurement.location.lon, measurement.location.lat);
	}
	public void add(OldPointDepthCollector collector){
		for(CanalPointDepthMeasurement measurement:collector.points())
			add(measurement);
	}

	
	public double fitsCollection(CanalPointDepthMeasurement measurement){
		if(measurements.size() == 0)
			return 1.0;
		if(date != null && !date.equals(measurement.date)){
			return 0;
		}
		
		double minDist = minimumDistance(measurement);
		double resid = locationResidual(measurement);
		double newRSquare = RSquareWith(measurement);
		
		if(newRSquare<0.97)
			return 0;
		
		if(resid<0.1 && minDist<20)
			return newRSquare;
		if(minDist > 10){
			return newRSquare;
		}
		if(resid > 1){
			return 0;
		}
		return newRSquare;
	}
	public double fitsCollection(OldPointDepthCollector collector){
		if(collector.points().isEmpty() || points().isEmpty())
			throw new RuntimeException("Neither collection must be empty");
		
		if(date != null && !date.equals(collector.date)){
			return 0;
		}
		
		double minDist = minimumDistance(collector);
		if(minDist > 10){
			return 0;
		}

		double newRSquare = RSquareWith(collector);
		
		if(newRSquare > 0.9999)
			return newRSquare;
		if(newRSquare > getLocationRSquare())//new regression is better
			return newRSquare;
		
		return 0;
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
	
	public double RSquareWith(OldPointDepthCollector collector){
		SimpleRegression newLocationRegression = cloneLocationRegression();
		for(CanalPointDepthMeasurement measurement:collector.points())
			newLocationRegression.addData(measurement.location.lon, measurement.location.lat);
		
		return newLocationRegression.getRSquare();
	}

	public double minimumDistance(OldPointDepthCollector collector){
		double minDistance = Double.MAX_VALUE;
		for(CanalPointDepthMeasurement otherMeasurement:collector.points()){
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
		builder.append(points().size());
		builder.append(" [");
		for (CanalPointDepthMeasurement measurement : points()) {
			builder.append(measurement.id);
			builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}
}
