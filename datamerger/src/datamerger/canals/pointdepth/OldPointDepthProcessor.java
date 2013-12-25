package datamerger.canals.pointdepth;

import java.util.*;
import java.util.regex.*;

import com.google.common.collect.*;

public class OldPointDepthProcessor {
	public static final int INITIAL_SMALL_COLLECTION_SIZE = 7;
	public static final int MINIMUM_COLLECTION_SIZE = 5;
	
	private final List<CanalPointDepthMeasurement> measurements;
	private final Map<Integer, CanalPointDepthMeasurement> measurementsFromId = Maps.newHashMap();

	public OldPointDepthProcessor(List<CanalPointDepthMeasurement> measurements){
		this.measurements = measurements;
		for(CanalPointDepthMeasurement m:measurements){
			measurementsFromId.put(m.id, m);
		}
	}

	private final Collection<CanalPointDepthMeasurement> outliers = Lists.newArrayList();
	private final List<OldPointDepthCollector> smallCollections = Lists.newArrayList();
	private final Collection<OldPointDepthCollector> largeCollections = Lists.newArrayList();

	public  void process(){
		firstPass();
		
		mergeOutliers();
		mergeSmallCollections();
		mergeOutliers();
		mergeSmallCollections();
		disolveSmallCollectionsToOutliers();
		mergeOutliers();

		Multiset<Integer> sizes = TreeMultiset.create();
		for(OldPointDepthCollector c:largeCollections){
			sizes.add(c.points().size());
		}

		Multiset<Integer> smallSizes = TreeMultiset.create();
		for(OldPointDepthCollector c:smallCollections){
			smallSizes.add(c.points().size());
		}

		System.out.print("\n\n$ ");
		Scanner s = new Scanner(System.in);
		while(s.hasNext()){
			try{
				String command = s.nextLine();
				if(command.isEmpty()){
				}
				else if(command.equals("exit")){
					return;
				}
				else if(command.equals("results")){
					System.out.println("Size distro:");
					for(Multiset.Entry<Integer> e:sizes.entrySet())
						System.out.println("\t"+e.getElement()+": "+e.getCount());
					
					System.out.println("Small collections: "+smallCollections.size());
					for(Multiset.Entry<Integer> e:smallSizes.entrySet())
						System.out.println("\t"+e.getElement()+": "+e.getCount());
					for(OldPointDepthCollector collector:smallCollections)
						System.out.println("\t"+collector+" - "+collector.getLocationRSquare());
					System.out.println("Remaining outlier count: "+outliers.size());
				}
				else if(command.equals("outliers")){
					System.out.println("Remaining outliers: "+outliers.size());
					for(CanalPointDepthMeasurement m:outliers)
						System.out.println("\t"+m.id);
				}
				else if(command.startsWith("size:")){
					int size = Integer.parseInt(command.substring(5));
					for(OldPointDepthCollector c:largeCollections){
						if(c.points().size()==size){
							System.out.println("\t"+c);
						}
					}
				}
				else if(command.startsWith("collect/w:")){
					int id = Integer.parseInt(command.substring(10));
					OldPointDepthCollector result = null;
					for(OldPointDepthCollector c:largeCollections){
						boolean contains = false;
						for(CanalPointDepthMeasurement m:c.points()){
							if(m.id == id){
								contains = true;
								break;
							}
						}
						if(contains){
							result = c;
							break;
						}
					}
					if(result!=null){
						System.out.println(result);
						System.out.println("\tr^2: "+result.getLocationRSquare());
					}
					else
						System.out.println("Did not find result");
				}
				else if(command.startsWith("testin:")){
					Pattern p = Pattern.compile("test:(\\d+) in/w (\\d+)");
					Matcher m = p.matcher(command);
					int id = Integer.parseInt(m.group(1));
					int targetId = Integer.parseInt(m.group(2));
					OldPointDepthCollector targetCollection = null;
					for(OldPointDepthCollector c:largeCollections){
						boolean contains = false;
						for(CanalPointDepthMeasurement measurement:c.points()){
							if(measurement.id == targetId){
								contains = true;
								break;
							}
						}
						if(contains){
							targetCollection = c;
							break;
						}
					}
					if(targetCollection!=null){
						CanalPointDepthMeasurement measurement = measurementsFromId.get(id);
						if(measurement!=null){
							System.out.println("Inserting "+measurement+" into "+targetCollection);
							System.out.println("\tMinDist: "+targetCollection.minimumDistance(measurement));
							System.out.println("\tResid: "+targetCollection.locationResidual(measurement));
						}
						else
							System.out.println("Did not find measurement: "+id);
					}
					else
						System.out.println("Did not find target");
				}
				else{
					System.out.println("Unknown command...");
				}
			} catch(Exception e){
				e.printStackTrace();
			}
			System.out.print("\n\n$ ");
		}
	}

	private void firstPass(){
		OldPointDepthCollector collector = new OldPointDepthCollector();
		for(int i = 0; i<measurements.size(); ++i){
			CanalPointDepthMeasurement measurement = measurements.get(i);
			if(collector.fitsCollection(measurement)==0){
				if(collector.points().size()<=2){
					--i;
					outliers.add(measurements.get(i));
				}
				else if(collector.points().size()<=INITIAL_SMALL_COLLECTION_SIZE){
					smallCollections.add(collector);
				}
				else{
					largeCollections.add(collector);
				}
				collector = new OldPointDepthCollector();
			}
			collector.add(measurement);
		}
	}
	
	
	private void mergeOutliers(){
		boolean matchesFound = false;
		do{
			matchesFound = false;
			matchesFound |= mergeOutliersInto(largeCollections);
			matchesFound |= mergeOutliersInto(smallCollections);
			matchesFound |= mergeSmallCollectionsIntoMain();
		} while(matchesFound);
	}

	private boolean mergeSmallCollectionsIntoMain() {
		boolean matchesFound = false;
		Iterator<OldPointDepthCollector> collectorIterator = smallCollections.iterator();
		while(collectorIterator.hasNext()){
			OldPointDepthCollector smallCollection = collectorIterator.next();
			SortedMap<Double, OldPointDepthCollector> matches = Maps.newTreeMap();
			for(OldPointDepthCollector c:largeCollections){
				double rsquare;
				if((rsquare = c.fitsCollection(smallCollection))>0){
					matches.put(rsquare, c);
				}
			}
			if(matches.size()>0){
				matches.get(matches.lastKey()).add(smallCollection);
				collectorIterator.remove();
				matchesFound = true;
			}
		}
		return matchesFound;
	}

	private boolean mergeOutliersInto(Collection<OldPointDepthCollector> collectors) {
		boolean matchesFound = false;
		Iterator<CanalPointDepthMeasurement> pointIterator = outliers.iterator();
		while(pointIterator.hasNext()){
			CanalPointDepthMeasurement outlier = pointIterator.next();
			SortedMap<Double, OldPointDepthCollector> matches = Maps.newTreeMap();
			for(OldPointDepthCollector c:collectors){
				double rsquare;
				if( (rsquare = c.fitsCollection(outlier))>0 ){
					matches.put(rsquare, c);
					matchesFound = true;
					break;
				}
			}
			if(matches.size()>0){
				matches.get(matches.lastKey()).add(outlier);
				pointIterator.remove();
				matchesFound = true;
			}
		}
		return matchesFound;
	}
	
	private void mergeSmallCollections(){
		mergeSmallCollectionsTogether();
		addLargeSmallCollectionsIntoMain();
	}

	private void mergeSmallCollectionsTogether(){
		for(int i = 0; i<smallCollections.size(); ++i){
			OldPointDepthCollector collecitonI = smallCollections.get(i);

			SortedMap<Double, Integer> matches = Maps.newTreeMap();
			for(int j = i+1; j<smallCollections.size();++j){
				OldPointDepthCollector collectionJ = smallCollections.get(j);
				double rsquare;
				if( (rsquare = collecitonI.fitsCollection(collectionJ)) > 0){
					matches.put(rsquare, j);
				}
			}
			if(matches.size()>0){
				int j = matches.get(matches.lastKey());
				OldPointDepthCollector collectionJ = smallCollections.get(j);
				collectionJ.add(collectionJ);
				smallCollections.remove(j);
			}
			
		}
	}
	
	private void addLargeSmallCollectionsIntoMain() {
		Iterator<OldPointDepthCollector> collectorIterator = smallCollections.iterator();
		while(collectorIterator.hasNext()){
			OldPointDepthCollector smallCollection = collectorIterator.next();
			if(smallCollection.points().size() >= MINIMUM_COLLECTION_SIZE){
				collectorIterator.remove();
				largeCollections.add(smallCollection);
			}
		}
	}

	
	private void disolveSmallCollectionsToOutliers() {
		Iterator<OldPointDepthCollector> collectorIterator = smallCollections.iterator();
		while(collectorIterator.hasNext()){
			OldPointDepthCollector smallCollection = collectorIterator.next();
			for(CanalPointDepthMeasurement p:smallCollection.points()){
				outliers.add(p);
			}
			collectorIterator.remove();
		}
	}
}
