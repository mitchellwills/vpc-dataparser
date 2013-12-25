package datamerger.canals.pointdepth;

import java.util.*;

import com.google.common.collect.*;

public class PointDepthProcessor {
	
	private final List<CanalPointDepthMeasurement> measurements;
	private final Map<Integer, CanalPointDepthMeasurement> measurementsFromId = Maps.newHashMap();

	public PointDepthProcessor(final List<CanalPointDepthMeasurement> measurements){
		this.measurements = measurements;
		for(CanalPointDepthMeasurement m:measurements){
			measurementsFromId.put(m.id, m);
		}
		//this.measurements = Lists.newArrayList();
		//for(int i = 1; i<=10000; ++i){
		//	this.measurements.add(measurementsFromId.get(i));
		//}
	}

	private List<CanalPointDepthMeasurement> remainingPoints = Lists.newLinkedList();
	private final List<PointDepthCollector> collections = Lists.newArrayList();
	private final Set<PointDepthCollector> badCollections = Sets.newHashSet();
	public void process(){
		remainingPoints.addAll(measurements);
		groupRemainingPoints(1);
		filterBadCollectionsByRSquare(0.999);
		mergePointSets(2, 0.999);
		for(double i = 2; i<10; i+=0.5)
			mergePointSets(i, 0.9999);
		filterBadCollectionsLessThanSize(2);
		
		
		
		System.out.println("Results:");
		System.out.println(collections.size()+" collections");
		Multiset<Integer> sizes = HashMultiset.create();
		for(PointDepthCollector c:collections){
			sizes.add(c.size());
			//System.out.println(c+" - "+c.getLocationRSquare());
		}
		for(Multiset.Entry<Integer> e:sizes.entrySet())
			System.out.println("\t"+e.getElement()+": "+e.getCount());
		
		System.out.println(badCollections.size()+" bad collections");
		System.out.println(remainingPoints.size()+" points remaining");
		
	}
	
	
	
	private boolean groupRemainingPoints(double maxDistance){
		List<CanalPointDepthMeasurement> newRemainingPoints = Lists.newLinkedList();
		boolean pointsGrouped = false;
		while(!remainingPoints.isEmpty()){
			pointsGrouped |= groupOneRemainingPointSet(maxDistance, newRemainingPoints);
		}
		remainingPoints = newRemainingPoints;
		return pointsGrouped;
	}
	int i = 0;
	private boolean groupOneRemainingPointSet(double maxDistance, List<CanalPointDepthMeasurement> newRemainingPoints){
		if(i%1000==0)
			System.out.println("Group remaining points iteration "+i+", remaining points: "+(newRemainingPoints.size()+remainingPoints.size()));
		++i;
		if(remainingPoints.isEmpty())
			return false;
		
		PointDepthCollector points = new PointDepthCollector();
		CanalPointDepthMeasurement firstPoint = remainingPoints.remove(0);
		points.add(firstPoint);

		boolean pointAdded = false;
		boolean pointAddedThisIteration = false;
		do{
			pointAddedThisIteration = false;
			
			Iterator<CanalPointDepthMeasurement> iterator = remainingPoints.iterator();
			while(iterator.hasNext()){
				CanalPointDepthMeasurement m = iterator.next();
				double minDist = points.minimumDistance(m);
				if(minDist < maxDistance && Objects.equals(m.date, points.date())){
					iterator.remove();
					points.add(m);
					pointAddedThisIteration = true;
					pointAdded = true;
				}
			}
		} while(pointAddedThisIteration);
		
		if(!pointAdded)
			newRemainingPoints.add(firstPoint);
		else
			collections.add(points);
		return pointAdded;
	}
	
	private void filterBadCollectionsByRSquare(double rSquareThreshold){
		int count = 0;
		Iterator<PointDepthCollector> iterator = collections.iterator();
		while(iterator.hasNext()){
			PointDepthCollector c = iterator.next();
			if(c.getLocationRSquare() < rSquareThreshold){
				iterator.remove();
				badCollections.add(c);
				++count;
			}
		}
		System.out.println("Filtered "+count+" points by RSquare: "+rSquareThreshold);
	}
	
	private void filterBadCollectionsLessThanSize(int size){
		int count = 0;
		Iterator<PointDepthCollector> iterator = collections.iterator();
		while(iterator.hasNext()){
			PointDepthCollector c = iterator.next();
			if(c.size() <= size){
				iterator.remove();
				badCollections.add(c);
				++count;
			}
		}
		System.out.println("Filtered "+count+" points by size <= "+size);
	}
	
	private void mergePointSets(final double maxDistance, final double rSquareThreshold){
		int count = 0;
		for(int i = 0; i<collections.size(); ++i){
			final PointDepthCollector collectorI = collections.get(i);
			
			PointDepthCollector bestMatch = chooseBestIteration(collections.subList(i+1, collections.size()), new IterationAction<PointDepthCollector>() {
				@Override
				public Double evaluate(PointDepthCollector otherCollector) {
					if(!Objects.equals(collectorI.date(), otherCollector.date()))
						return null;
					double rSquare = collectorI.RSquareWith(otherCollector);
					if(collectorI.minimumDistance(otherCollector) < maxDistance && rSquare > rSquareThreshold)
						return rSquare;
					return null;
				}
			});

			if(bestMatch!=null){
				collections.remove(bestMatch);
				collectorI.add(bestMatch);
				++count;
			}
			
		}
		System.out.println("Merged "+count+" collections, maxDist: "+maxDistance+", rSquare: "+rSquareThreshold);
	}
	

	
	private <T, R> T chooseBestIteration(Iterable<T> iterator, IterationAction<T> action){
		SortedMap<Comparable<?>, T> results = Maps.newTreeMap();
		for(T item:iterator){
			Comparable<?> rank = action.evaluate(item);
			if(rank!=null)
				results.put(rank, item);
		}
		if(results.isEmpty())
			return null;
		Comparable<?> firstKey = results.lastKey();
		return results.get(firstKey);
	}
	private interface IterationAction<T>{
		Comparable<?> evaluate(T input);
	}
}
