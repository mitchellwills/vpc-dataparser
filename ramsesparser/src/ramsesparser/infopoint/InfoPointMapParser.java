package ramsesparser.infopoint;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import parserutil.datatable.SimpleTable;

public class InfoPointMapParser {
	private final InfoPointPanelParser panelParser;
	private final String[] layers;

	public InfoPointMapParser(InfoPointPanelParser panelParser, String... layers){
		this.panelParser = panelParser;
		this.layers = layers;
		
	}

	public SimpleTable parse(double minX, double minY, double maxX, double maxY, double dx, double dy, int zoomLevel) throws IOException{
		int numX = (int)((maxX-minX)/dx)+1;
		int numY = (int)((maxY-minY)/dy)+1;
		System.out.println("Processing "+numX+"x"+numY+" - "+(numX*numY)+" queries total");
		Map<String, Entry> entries = new HashMap<>();
		for(int iY = 0; iY<numY; ++iY){
			for(int iX = 0; iX<numX; ++iX){
				double x = minX+dx*iX;
				double y = minY+dy*iY;
				Collection<Entry> data = null;
				for(int i = 0; i<10; ++i){
					try{
						data = panelParser.parse(x, y, zoomLevel, layers);
					} catch(SocketTimeoutException e){
						System.err.println("Timed out...");
					}
				}
				if(data==null)
					throw new RuntimeException("Timed out 10 times...");
				for(Entry e:data)
					entries.put(e.getTitle().getValue(), e);
				//System.out.println("Processed "+entries.size()+" ("+x+", "+y+") - "+(numX*iY+iX)+"/"+(numX*numY)+" queries");
			}
		}
		return new InfoPointDataTable(entries.values());
	}
}
