package datamerger;

import java.io.*;
import java.util.*;

import com.google.common.collect.*;

import datamerger.bridges.*;
import datamerger.canals.*;
import datamerger.data.*;
import datamerger.data.writer.*;
import datamerger.data.writer.DatabaseTableTableWriter.MeasurementDataColumn;
import datamerger.data.writer.DatabaseTableTableWriter.SingleColumnDataColumn;
import datamerger.data.writer.DatabaseTableTableWriter.SingleColumnDataColumnFromProperty;
import datamerger.data.writer.DatabaseTableTableWriter.TableFilter;
import datamerger.data.writer.DatabaseTableTableWriter.ToStringDataColumn;

public class Merger {

	public static void main(String[] args) throws IOException {
		final DatabaseTable<JobItem> jobItems = new DatabaseTable<JobItem>("Job Items");
		final DatabaseTable<CanalSegment> segments = new DatabaseTable<CanalSegment>("Canal Segments");
		final DatabaseTable<IsmarLink> ismarLinks = new DatabaseTable<IsmarLink>("ISMAR Links");
		final DatabaseTable<IsmarNode> ismarNodes = new DatabaseTable<IsmarNode>("ISMAR Nodes");
		final DatabaseTable<Canal> canals = new DatabaseTable<Canal>("Canals");
		final DatabaseTable<Bridge> bridges = new DatabaseTable<Bridge>("Bridges");

		CanalsMerger.parse(canals, segments, jobItems, ismarLinks, ismarNodes);
		//BridgesMerger.parse(bridges, canals, segments);
		
		
		System.err.println("\n\nValidating Data:");
		DataValidator.validateTable(canals);
		DataValidator.validateTable(segments);
		DataValidator.validateTable(jobItems);
		//DataValidator.validateTable(ismarLinks);
		//DataValidator.validateTable(ismarNodes);
		//DataValidator.validateTable(bridges);

		//new PrintDatabaseTableWriter(System.out).write(canals);
		//DataPrinter.print(bridges);

		new DatabaseTableTableWriter(new CSVTableWriter(new FileWriter("canals.csv")))
		.withColumn(new ToStringDataColumn("id", "id"))
		.withColumn(new ToStringDataColumn("wiki_friendly_title", "name"))
		.withColumn(new SingleColumnDataColumnFromProperty("Segment Ids", "segments"){
			@Override
			public String createDataFromProperty(Object value) {
				@SuppressWarnings("unchecked")
				Set<CanalSegment> segments = (Set<CanalSegment>)value;
				StringBuilder builder = new StringBuilder();
				Iterator<CanalSegment> iterator = segments.iterator();
				while(iterator.hasNext()){
					builder.append(iterator.next().id().get());
					if(iterator.hasNext())
						builder.append(",");
				}
				return builder.toString();
			}
		})
		.withColumn(new SingleColumnDataColumnFromProperty("ISMAR Links", "segments"){
			@Override
			public String createDataFromProperty(Object value) {
				@SuppressWarnings("unchecked")
				Set<CanalSegment> segments = (Set<CanalSegment>)value;
				Set<String> linkIds = Sets.newTreeSet();
				for(CanalSegment segment:segments){
					for(IsmarLink link:segment.ismarLinks().get())
						linkIds.add(link.id().get());
				}
				StringBuilder builder = new StringBuilder();
				Iterator<String> iterator = linkIds.iterator();
				while(iterator.hasNext()){
					builder.append(iterator.next());
					if(iterator.hasNext())
						builder.append(",");
				}
				return builder.toString();
			}
		})
		.withAllFieldsExcept("segments")
		.write(canals);
		
		new DatabaseTableTableWriter(new CSVTableWriter(new FileWriter("segments.csv")))
		.withColumn(new ToStringDataColumn("id", "id"))
		.withColumn(new SingleColumnDataColumn("wiki_friendly_title"){

			@Override
			public String createData(DataObject item) {
				CanalSegment segment = (CanalSegment)item;
				return "Segment "+segment.id().get();
			}
		})
		.withColumn(new SingleColumnDataColumnFromProperty("Job Item Ids", "jobItems"){
			@Override
			public String createDataFromProperty(Object value) {
				@SuppressWarnings("unchecked")
				List<JobItem> jobs = (List<JobItem>)value;
				StringBuilder builder = new StringBuilder();
				Iterator<JobItem> iterator = jobs.iterator();
				while(iterator.hasNext()){
					builder.append(iterator.next().id().get());
					if(iterator.hasNext())
						builder.append(",");
				}
				return builder.toString();
			}
		})
		.withColumn(new SingleColumnDataColumnFromProperty("Ismar Link Ids", "ismarLinks"){
			@Override
			public String createDataFromProperty(Object value) {
				@SuppressWarnings("unchecked")
				List<IsmarLink> links = (List<IsmarLink>)value;
				StringBuilder builder = new StringBuilder();
				Iterator<IsmarLink> iterator = links.iterator();
				while(iterator.hasNext()){
					IsmarLink link = iterator.next();
					builder.append(link.id().get());
					if(iterator.hasNext())
						builder.append(",");
				}
				return builder.toString();
			}
		})
		.withAllFieldsExcept("jobItems", "ismarLinks")
		.write(segments);
		
		new DatabaseTableTableWriter(new CSVTableWriter(new FileWriter("jobItems.csv")))
		.withColumn(new ToStringDataColumn("id", "id"))
		.withColumn(new SingleColumnDataColumnFromProperty("Title", "id"){
			@Override
			public String createDataFromProperty(Object value) {
				return "Job Item "+value;
			}
		})
		.withColumn(new SingleColumnDataColumnFromProperty("wiki_friendly_title", "id"){
			@Override
			public String createDataFromProperty(Object value) {
				return "Job Item "+value;
			}
		})
		.withAllFieldsExcept()
		.write(jobItems);
		
		new DatabaseTableTableWriter(new CSVTableWriter(new FileWriter("bridges.csv")))
		.withColumn(new ToStringDataColumn("id", "id"))
		.withAllFieldsExcept()
		.write(bridges);
		
		

		try(WikimediaTableWriter writer = new WikimediaTableWriter(new FileWriter("canalwikitable.txt"))){
			new DatabaseTableTableWriter(writer)
			.withColumn(new SingleColumnDataColumnFromProperty("Canal Name", "name"){
				@Override
				public String createDataFromProperty(Object value) {
					String name = (String)value;
					return "[["+name+"]]";
				}
			})
			.withColumn(MeasurementDataColumn.forType(Canal.class, "length"))
			.withColumn(MeasurementDataColumn.forType(Canal.class, "area"))
			.withColumn(MeasurementDataColumn.forType(Canal.class, "targetDepth"))
			.withColumn(MeasurementDataColumn.forType(Canal.class, "averageDepth"))
			.filter(new TableFilter(){
				@Override
				public <T> Iterable<T> filter(Iterable<T> input) {
					List<Canal> items = Lists.newArrayList();
					for(T item:input){
						Canal c = (Canal)item;
						if(c.name().get()!=null && !c.name().get().isEmpty())
							items.add(c);
					}
					return (Iterable<T>)items;
				}
			})
			.write(canals);
		}
		try(WikimediaTableWriter writer = new WikimediaTableWriter(new FileWriter("canalsegmentwikitable.txt"))){
			new DatabaseTableTableWriter(writer)
			.withColumn(new SingleColumnDataColumnFromProperty("Segment Name", "id"){
				@Override
				public String createDataFromProperty(Object value) {
					String id = (String)value;
					return "[[Segment "+id+"]]";
				}
			})
			.withColumn(new SingleColumnDataColumn("Canal Name"){
				@Override
				public String createData(DataObject item) {
					CanalSegment segment = (CanalSegment)item;
					String canalId = segment.canalId().get();
					if(canals.hasItemWithId(canalId)){
						Canal canal = canals.getById(canalId);
						if(canal!=null && canal.name().get()!=null)
							return "[["+canal.name().get()+"]]";
					}
					return null;
				}
			})
			.withColumn(MeasurementDataColumn.forType(CanalSegment.class, "length"))
			.withColumn(MeasurementDataColumn.forType(CanalSegment.class, "area"))
			.withColumn(MeasurementDataColumn.forType(CanalSegment.class, "averageWidth"))
			.withColumn(MeasurementDataColumn.forType(CanalSegment.class, "minWidth"))
			.write(segments);
		}
		
		try(WikimediaTableWriter writer = new WikimediaTableWriter(new FileWriter("bridgewikitable.txt"))){
			new DatabaseTableTableWriter(writer)
			.withColumn(new SingleColumnDataColumnFromProperty("Bridge Name", "name"){
				@Override
				public String createDataFromProperty(Object value) {
					String name = (String)value;
					return "[["+name+"]]";
				}
			})
			.withColumn(MeasurementDataColumn.forType(Canal.class, "length"))
			.withColumn(MeasurementDataColumn.forType(Canal.class, "area"))
			.withColumn(MeasurementDataColumn.forType(Canal.class, "targetDepth"))
			.withColumn(MeasurementDataColumn.forType(Canal.class, "averageDepth"))
			.filter(new TableFilter(){
				@Override
				public <T> Iterable<T> filter(Iterable<T> input) {
					List<Canal> items = Lists.newArrayList();
					for(T item:input){
						Canal c = (Canal)item;
						if(c.name().get()!=null && !c.name().get().isEmpty())
							items.add(c);
					}
					return (Iterable<T>)items;
				}
			})
			.write(canals);
		}
		
	}

}
