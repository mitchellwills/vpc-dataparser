package geojson;

import geojson.GeoJson.GeoJsonLineGeometry;
import geojson.GeoJson.GeoJsonPoint;

public class GeoJsonUtil {
	public static double lineAngle(GeoJsonLineGeometry line){
		if(line.getPointCount()<=1)
			throw new RuntimeException("There must be at least two points");
		
		double x1 = line.getPoint(0).getX();
		double x2 = line.getPoint(line.getPointCount()-1).getX();
		double y1 = line.getPoint(0).getY();
		double y2 = line.getPoint(line.getPointCount()-1).getY();
		
		return Math.atan2(y2-y1, x2-x1);
	}
	public static GeoJsonPoint lineMidpoint(GeoJsonLineGeometry line){
		if(line.getPointCount()<=1)
			throw new RuntimeException("There must be at least two points");
		
		int i1 = (line.getPointCount()-1)/2;
		int i2 = line.getPointCount()/2;
		double x1 = line.getPoint(i1).getX();
		double x2 = line.getPoint(i2).getX();
		double y1 = line.getPoint(i1).getY();
		double y2 = line.getPoint(i2).getY();

		return new GeoJsonPoint((x2-x1)/2+x1, (y2-y1)/2+y1);
	}
}
