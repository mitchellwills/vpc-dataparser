package geojson;

import java.util.*;

import org.json.*;

import com.google.common.collect.*;

public class GeoJson {

	private JSONObject root;

	public GeoJson(JSONObject root){
		this.root = root;
	}
	
	public Collection<GeoJsonFeature> getFeatures(){
		List<GeoJsonFeature> features = Lists.newArrayList();
		JSONArray jsonObject = root.getJSONArray("features");
		for(int i = 0; i<jsonObject.length(); ++i){
			JSONObject featureRoot = jsonObject.getJSONObject(i);
			features.add(new GeoJsonFeature(featureRoot));
		}
		return features;
	}
	

	public class GeoJsonFeature {
		private JSONObject root;

		public GeoJsonFeature(JSONObject root){
			this.root = root;
		}
		
		public GeoJsonGeometry getGeometry(){
			return GeoJsonGeometry.parseGeometry(root.getJSONObject("geometry"));
		}

		@SuppressWarnings("unchecked")
		public Map<String, Object> getProperties() {
			Map<String, Object> properties = Maps.newHashMap();
			JSONObject propertyRoot = root.getJSONObject("properties");
			for(String key:(Set<String>)propertyRoot.keySet()){
				properties.put(key, propertyRoot.get(key));
			}
			return properties;
		}
	}

	public static class GeoJsonGeometry{
		public static GeoJsonGeometry parseGeometry(JSONObject o){
			String type = o.getString("type");
			switch(type){
			case "LineString":
				JSONArray points = o.getJSONArray("coordinates");
				return new GeoJsonLineGeometry(points);
			case "Point":
				JSONArray pointData = o.getJSONArray("coordinates");
				return new GeoJsonPoint(pointData);
			}
			throw new RuntimeException("Unknown geometry type: "+type);
		}
	}
	
	public static class GeoJsonPoint extends GeoJsonGeometry{
		private final double x;
		private final double y;

		public GeoJsonPoint(JSONArray coordArray){
			this(coordArray.getDouble(0), coordArray.getDouble(1));
		}
		public GeoJsonPoint(double x, double y){
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}
	}
	
	public static class GeoJsonLineGeometry extends GeoJsonGeometry{
		private final GeoJsonPoint[] points;

		public GeoJsonLineGeometry(JSONArray points){
			this(parsePointsFromArray(points));
		}
		private static GeoJsonPoint[] parsePointsFromArray(JSONArray pointArray) {
			GeoJsonPoint[] points = new GeoJsonPoint[pointArray.length()];
			for(int i = 0; i<pointArray.length(); ++i){
				JSONArray pointData = pointArray.getJSONArray(i);
				points[i] = new GeoJsonPoint(pointData);
			}
			return points;
		}
		public GeoJsonLineGeometry(GeoJsonPoint... points){
			this.points = points;
		}
		public GeoJsonPoint getPoint(int index){
			return points[index];
		}
		public int getPointCount(){
			return points.length;
		}
		@Override
		public String toString() {
			return "Line "+ Arrays.toString(points);
		}
	}
}
