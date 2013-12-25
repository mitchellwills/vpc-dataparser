package ismarsegmentmodelparser.parser;

import java.util.*;

public class BoundryTimestep {

	private final List<Double> depths;
	public final double time;

	public BoundryTimestep(double time, List<Double> depths) {
		this.time = time;
		this.depths = depths;
	}
	
	public double getDepth(int index){
		return depths.get(index-1);
	}

}
