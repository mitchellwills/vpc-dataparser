package downloader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Downloader {
	
	private static final CloseableHttpClient httpclient = HttpClients.createDefault();
	
	public enum LayerType{
		GML("gml"), SHAPEFILE("shp"), MAPINFO("tab"), DXF("dxf");
		public final String pathElement;
		LayerType(String pathElement){
			this.pathElement = pathElement;
		}
	}
	
	public static void main(String[] args) throws IOException {
		List<String> layerNames = Files.readAllLines(Paths.get("layerlist.txt"), Charset.defaultCharset());
		Iterator<String> nameIterator = layerNames.iterator();
		while(nameIterator.hasNext()){
			String name = nameIterator.next();
			if(name.startsWith("#") || name.isEmpty())
				nameIterator.remove();
		}
		downloadLayers(layerNames, LayerType.MAPINFO);
	}
	
	private static void downloadLayers(List<String> layerNames, LayerType type) throws IOException {
		Path directory = Files.createDirectories(Paths.get(type.pathElement));
		for(String layerName:layerNames){
			Path layerFile = directory.resolve(layerName+".zip");
			if(!Files.exists(layerFile)){
				try{
					byte[] data = download(layerName, type);
					Files.createFile(layerFile);
					Files.write(layerFile, data);
					System.out.println("Downloaded "+layerName+" in "+type+" format");
				} catch(IOException e){
					System.err.println(e.getMessage());
				}
			}
			else
				System.out.println("Found "+layerName+" in "+type+" format");
		}
		System.out.println("Done");
	}

	private static byte[] download(String layerName, LayerType type) throws IOException{
		String url = "http://maps.insula.it/common/download/"+type.pathElement+"/"+layerName;
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(httpGet);

		byte[] contents = null;
		try {
			if(response.getStatusLine().getStatusCode()!=200)
				throw new IOException("Error fetching "+layerName+" in "+type+" ("+response.getStatusLine().getStatusCode()+")");
		    HttpEntity entity = response.getEntity();
		    contents = EntityUtils.toByteArray(entity);
		    EntityUtils.consume(entity);
		} finally {
			response.close();
		}
		return contents;
	}
	

}
