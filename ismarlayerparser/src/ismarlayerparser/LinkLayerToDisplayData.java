package ismarlayerparser;

import geojson.*;
import geojson.GeoJson.GeoJsonFeature;
import geojson.GeoJson.GeoJsonLineGeometry;
import geojson.GeoJson.GeoJsonPoint;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.json.*;

import parserutil.datatable.*;
import au.com.bytecode.opencsv.*;

import com.google.common.collect.*;


public class LinkLayerToDisplayData {
	public static void main(String[] args) throws IOException{
		String linkSrc = new String(Files.readAllBytes(Paths.get("link.geojson")));
		String nodiSrc = new String(Files.readAllBytes(Paths.get("nodi.geojson")));
		
		GeoJson linkGeojson = new GeoJson(new JSONObject(linkSrc));
		GeoJson nodiGeojson = new GeoJson(new JSONObject(nodiSrc));
		
		SimpleTable linkNodesTable = CSVTableReader.read(new FileReader("links.csv"));
		class LinkNodes{
			public final int node1;
			public final int node2;
			public LinkNodes(int node1, int node2){
				this.node1 = node1;
				this.node2 = node2;
			}
		}
		Map<Integer, LinkNodes> linkNodes = Maps.newHashMap();
		for(Map<String, String> line:linkNodesTable.getEntriesValues()){
			int id = Integer.parseInt(line.get("link"));
			int node1 = Integer.parseInt(line.get("node1"));
			int node2 = Integer.parseInt(line.get("node2"));
			linkNodes.put(id, new LinkNodes(node1, node2));
		}

		/*try(CSVWriter writer = new CSVWriter(new FileWriter("out.csv"))){
			writer.writeNext(new String[]{"id", "polygon"});
			
			Collection<GeoJsonFeature> links = linkGeojson.getFeatures();
			for(GeoJsonFeature link:links){
				GeoJsonLineGeometry line = (GeoJsonLineGeometry)link.getGeometry();
	
				GeoJsonPoint midpoint = GeoJsonUtil.lineMidpoint(line);
				double angle = GeoJsonUtil.lineAngle(line);
				GeoJsonPoint p1 = new GeoJsonPoint(midpoint.getX()-Math.cos(angle)*0.00015, midpoint.getY()-Math.sin(angle)*0.00015);
				GeoJsonPoint p2 = new GeoJsonPoint(midpoint.getX()+Math.cos(angle)*0.00015, midpoint.getY()+Math.sin(angle)*0.00015);
				String wkt = "LINESTRING ("+p1.getX()+" "+p1.getY()+", "+p2.getX()+" "+p2.getY()+")";
				writer.writeNext(new String[]{link.getProperties().get("id").toString(), wkt});
			}
		}*/
		
		CSVReader reader = new CSVReader(new FileReader("modeltogisid.csv"));
		List<String[]> modeltogisidFile = reader.readAll();
		Map<Integer, String> modeltogisid = Maps.newHashMap();
		for(int i = 1; i<modeltogisidFile.size(); ++i){
			String[] line = modeltogisidFile.get(i);
			modeltogisid.put(Integer.parseInt(line[1]), line[2]);
		}
		
		try(Writer fileWriter = new FileWriter("modelSegments.json")){
			Collection<GeoJsonFeature> links = linkGeojson.getFeatures();
			JSONObject dataRoot = new JSONObject();
			for(GeoJsonFeature link:links){
				GeoJsonLineGeometry line = (GeoJsonLineGeometry)link.getGeometry();
				System.out.println(link.getProperties());
				
				Integer linkId = (Integer)link.getProperties().get("id");

				GeoJsonPoint midpoint = GeoJsonUtil.lineMidpoint(line);
				
				LinkNodes nodes = linkNodes.get(linkId);
				if(nodes==null)
					continue;
				GeoJsonPoint node1 = ((GeoJsonPoint)getFeatureById(nodiGeojson, Integer.toString(nodes.node1)).getGeometry());
				GeoJsonPoint node2 = ((GeoJsonPoint)getFeatureById(nodiGeojson, Integer.toString(nodes.node2)).getGeometry());
				double angle = GeoJsonUtil.lineAngle(new GeoJsonLineGeometry(node1, node2));

				JSONObject linkData = new JSONObject();
				
				JSONObject midpointJson = new JSONObject();
				midpointJson.put("x", midpoint.getX());
				midpointJson.put("y", midpoint.getY());

				linkData.put("properties", jsonObjectFromMap(link.getProperties()));
				linkData.put("segmentId", modeltogisid.get(linkId));
					
				linkData.put("midpoint", midpointJson);
				linkData.put("angle", angle);
				
				dataRoot.put(((Integer)link.getProperties().get("id")).toString(), linkData);
			}
			
			dataRoot.write(fileWriter);
		}
	}
	private static final GeoJsonFeature getFeatureById(GeoJson geojson, String id){
		for(GeoJsonFeature feature:geojson.getFeatures()){
			if(id.equals(feature.getProperties().get("id").toString()))
				return feature;
		}
		return null;
	}
	private static final JSONObject jsonObjectFromMap(Map<String, Object> data){
		JSONObject object = new JSONObject();
		for(Map.Entry<String, Object> entry:data.entrySet()){
			object.put(entry.getKey(), entry.getValue());
		}
		return object;
	}
}
