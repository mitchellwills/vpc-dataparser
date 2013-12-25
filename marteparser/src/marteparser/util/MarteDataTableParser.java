package marteparser.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import parserutil.ParserUtil;

public class MarteDataTableParser {
	
	public static List<Map<String, String>> parseTable(Element table){
		return null;
	}
	
	public static Map<String, String> parseKeyValueTables(Elements tables){
		Map<String, String> data = new HashMap<String, String>();
		for(Element dataTable:tables){
			Elements rows = dataTable.select("tr");
			for(Element row:rows){
				Elements columns = row.select("td");
				for(int i = 0; i<columns.size()-1; i+=2){
					Element keyElement = columns.get(i);
					if(!keyElement.attr("align").equals("right"))//all keys are right aligned
						continue;
					String key = ParserUtil.trimKey(keyElement.text());
					if(key.isEmpty())
						continue;
					Element valueElement = columns.get(i+1);
					String value = parseValueElement(valueElement);
					data.put(key, value);
				}
			}
		}
		return data;
	}
	private static String parseValueElement(Element valueElement){
		Elements textInputs = valueElement.select("input[type=text]");
		if(!textInputs.isEmpty()){
			String beforeText = ((TextNode)valueElement.childNode(0)).text();
			Element textInput = textInputs.first();//childNode(1)
			String afterText = ((TextNode)valueElement.childNode(2)).text();
			return ParserUtil.trimValue(beforeText+textInput.val()+afterText);
		}
		Elements checkboxInputs = valueElement.select("input[type=checkbox]");
		if(!checkboxInputs.isEmpty()){
			Element checkboxInput = checkboxInputs.first();
			boolean checked = checkboxInput.hasAttr("checked");
			return ParserUtil.trimValue(Boolean.toString(checked));
		}

		Elements selects = valueElement.select("select");
		if(!selects.isEmpty()){
			Element select = selects.first();
			Elements options = select.select("option");
			for(Element option:options){
				if(option.hasAttr("selected"))
					return ParserUtil.trimValue(option.text());
			}
			return null;
		}
		return ParserUtil.trimValue(valueElement.text());
	}
}
