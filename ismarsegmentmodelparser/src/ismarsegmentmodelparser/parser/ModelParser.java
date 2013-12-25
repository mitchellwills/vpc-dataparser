package ismarsegmentmodelparser.parser;

import java.io.*;
import java.nio.file.*;

public class ModelParser {
	public static void parse(Path folder) throws IOException{
		GridParser.parse(folder);
		//BoundryParser.parse(folder);
		Fort11Parser.parse(folder);
	}
}
