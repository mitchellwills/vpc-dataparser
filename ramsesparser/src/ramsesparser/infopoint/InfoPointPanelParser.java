package ramsesparser.infopoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import parserutil.ParserUtil;
import parserutil.cache.LocalHttpCache;
import parserutil.datatable.KeyValuePair;

public class InfoPointPanelParser {
	private static final LocalHttpCache httpCache = new LocalHttpCache(0);
	
	private String host;
	private String layerBase;

	public InfoPointPanelParser(String host, String layerBase){
		this.host = host;
		this.layerBase = layerBase;
	}
	
	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param zoomLevel 0 is largest
	 * @param layers
	 * @return
	 * @throws IOException
	 */
	public Collection<Entry> parse(double lat, double lon, int zoomLevel, String... layers) throws IOException{
		String url = "http://"+host+layerBase+"/"+lat+"/"+lon+"/"+zoomLevel+"/?layers=";
		for(String layer:layers)
			url+=","+layer;

		String contents = httpCache.get(url);
		Document doc = Jsoup.parseBodyFragment(contents);
		Elements entryElements = doc.getElementsByClass("sidebarPanel");
		List<Entry> entries = new ArrayList<Entry>();
		for(Element element:entryElements){
			Entry entry = parseEntry(element);
			if(entry!=null)
				entries.add(entry);
		}
		
		return entries;
	}
	
	private Entry parseEntry(Element element){
		Elements spans = element.children().tagName("span");
		if(spans.size()<2)
			return null;
		Element titleElement = spans.get(0);
		Element contentElement = spans.get(1);
		String imageUrl = host+contentElement.getElementsByTag("img").get(0).attr("src");
		Map<String, List<KeyValuePair>> values = new HashMap<String, List<KeyValuePair>>();
		List<Element> content = new ArrayList<Element>();
		for(Element e:contentElement.children()){
			if(e.tagName().equals("strong") || e.tagName().equals("ul"))
				content.add(e);
		}
		for(int i = 0; i<content.size(); ++i){
			Element e = content.get(i);
			String name = "";
			if(e.tagName().equals("strong")){
				name = e.text();
				++i;
				e = content.get(i);
			}
			if(e.tagName().equals("ul"))
				values.put(name, parseValueList(e));
		}
		
		KeyValuePair title = new KeyValuePair(
				ParserUtil.trimKey(getNodeText(titleElement.childNode(0)).trim()),
				getNodeText(titleElement.childNode(1)).trim());
		return new Entry(title, imageUrl, values);
	}
	
	public List<KeyValuePair> parseValueList(Element listElement){
		List<KeyValuePair> values = new ArrayList<KeyValuePair>();
		for(Element e:listElement.children()){
			KeyValuePair title = new KeyValuePair(
					ParserUtil.trimKey(getNodeText(e.childNode(1)).trim()),
					getNodeText(e.childNode(2)).trim());
			values.add(title);
		}
		return values;
	}
	
	private String getNodeText(Node n){
		if(n instanceof TextNode)
			return ((TextNode)n).text();
		if(n instanceof Element)
			return ((Element)n).text();
		throw new RuntimeException("Unknown Type");
	}
}
