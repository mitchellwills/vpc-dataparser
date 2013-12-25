package ismarsegmentmodelparser;

import ismarsegmentmodelparser.parser.*;

import java.io.*;
import java.nio.file.*;

public class ModelProcessor {
	public static void main(String[] args) throws IOException{
		ModelParser.parse(Paths.get("model_no"));
	}
}
