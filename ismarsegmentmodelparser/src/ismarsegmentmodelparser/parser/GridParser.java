package ismarsegmentmodelparser.parser;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class GridParser {

	public static void parse(Path folder) throws IOException {
		Path boundryFile = folder.resolve("mygrid05NEW.txt");
		List<String> allLines = Files.readAllLines(boundryFile, Charset.defaultCharset());

		Iterator<String> lineIterator = allLines.iterator();
		parseNodes(lineIterator);
		parseSegments(lineIterator);
	}

	private static final Pattern SEGMENT_PATTERN = Pattern.compile("(?<id>\\d+)\\s+(-?\\d+.?\\d*)\\s+(-?\\d+.?\\d*)\\s+(-?\\d+.?\\d*)\\s+([01])\\s+(-?\\d+.?\\d*)\\s+([01])\\s+(?<node1>\\d+)\\s+(?<node2>\\d+)");
	private static void parseSegments(Iterator<String> lineIterator) throws IOException {
		try(Writer writer = new FileWriter("links.csv")){
			writer.write("link,node1,node2\n");
			while(lineIterator.hasNext()){
				String line = lineIterator.next();
	
				Matcher matcher = SEGMENT_PATTERN.matcher(line);
				matcher.find();
	
				int id = Integer.parseInt(matcher.group("id"));
				int node1 = Integer.parseInt(matcher.group("node1"));
				int node2 = Integer.parseInt(matcher.group("node2"));
				writer.write(id+","+node1+","+node2+"\n");
			}
		}
	}

	
	private static final Pattern NODE_PATTERN = Pattern.compile("(?<id>\\d+)\\s+\\d+\\s+\\d+\\s+\\d+\\s+(?<boundry>[01])\\s+(?<numSeg>\\d+)\\s+(?<seg1>\\d+)\\s+(?<seg2>\\d+)\\s+(?<seg3>\\d+)\\s+(?<seg4>\\d+)\\s+(?<seg5>\\d+)\\s+(?<seg6>\\d+)");
	private static void parseNodes(Iterator<String> lineIterator) {
		lineIterator.next();//top border
		int boundryIdCount = 1;
		while(true){
			String line = lineIterator.next();
			if(line.equals("*****************************************************************************************"))
				return;

			Matcher matcher = NODE_PATTERN.matcher(line);
			matcher.find();
			int id = Integer.parseInt(matcher.group("id"));
			boolean boundry = matcher.group("boundry").equals("1");
			int boundryId;
			if(boundry)
				boundryId = boundryIdCount++;
			else
				boundryId = 0;
			
			if(boundry)
			System.out.println(id+" - "+boundryId);
		}
	}
}
