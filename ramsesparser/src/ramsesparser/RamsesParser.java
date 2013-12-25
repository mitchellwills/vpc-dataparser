package ramsesparser;

import java.io.FileWriter;
import java.io.IOException;

import parserutil.datatable.CSVTableWriter;
import parserutil.datatable.SimpleTable;
import ramsesparser.infopoint.InfoPointMapParser;
import ramsesparser.infopoint.InfoPointPanelParser;

public class RamsesParser {

	public static void main(String[] args) throws IOException {
		InfoPointPanelParser panelParser = new InfoPointPanelParser("maps.insula.it", "/lavori/infopoint/portal");
		InfoPointMapParser mapParser = new InfoPointMapParser(panelParser, "toponimi", "mimuv-eventi");
		SimpleTable table = mapParser.parse(2308820.8063237, 5033429.9004349, 2314205.785848, 5038143.0307463, 120, 120, 0);
		//CSVTableWriter.write(new PrintWriter(System.out), table);
		CSVTableWriter.write(new FileWriter("temp.csv"), table);
	}

}