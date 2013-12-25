package ismarsegmentmodelparser.parser;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import com.google.common.collect.*;

public class BoundryParser {
	public static void parse(Path folder) throws IOException {
		Path boundryFile = folder.resolve("zetanov7.txt");
		List<String> allLines = Files.readAllLines(boundryFile, Charset.defaultCharset());

		List<BoundryTimestep> timesteps = Lists.newArrayList();
		
		for(String line:allLines){
			timesteps.add(parseTimestep(line));
		}
		
		for(BoundryTimestep timestep:timesteps){
			System.out.println(timestep.getDepth(29));
		}
	}

	private static BoundryTimestep parseTimestep(String line){
		String[] entries = line.split("\\s+");
		
		double timestep = Double.parseDouble(entries[1]);
		
		List<Double> depths = Lists.newArrayList();
		for(int i = 2; i<entries.length; ++i)
			depths.add(Double.parseDouble(entries[i]));

		return new BoundryTimestep(timestep, depths);
	}
}
