package es.iteam.comm.webm.container.segment.cueing_old;

import java.util.ArrayList;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import es.iteam.comm.webm.util.HexByteArray;


public class Cues {

	private ArrayList<CuePoint> mCuePoints;

	public Cues() {
	}

	public static void create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		create(reader, dataSource);
	}

	private static void create(EBMLReader ebmlReader, DataSource dataSource) {
		EBMLReader reader = ebmlReader;
		DataSource data = dataSource;

		Element level1 = null;
		Element level0 = null;

		level0 = reader.readNextElement();
		if (level0.equals(MatroskaDocType.CueingData_Id)) {
			System.out.println("kkkkkkkkkkkkk");
			level1 = ((MasterElement) level0).readNextChild(reader);
		}
		
		
		
		
		
		// Element rootElement = reader.readNextElement();
		// if (rootElement == null) {
		// throw new java.lang.RuntimeException("Error: Unable to scan for EBML elements");
		// }
		//
		// if (rootElement.equals(MatroskaDocType.CueingData_Id)) {
		// rootElement.readData(data);
		// BinaryElement be = ((BinaryElement)rootElement).readNextChild(reader));
		//
		// Element auxElement = ((MasterElement) rootElement).readNextChild(reader);
		//
		//
		// // create(auxElement, reader, data);
		// }

	}

	public static void create(Element rootElement, EBMLReader ebmlReader, DataSource dataSource) {
		Element root = rootElement;
		EBMLReader reader = ebmlReader;
		DataSource data = dataSource;

		Element auxElement = ((MasterElement) root).readNextChild(reader);
		while (auxElement != null) {

			System.out.println("--->>>>>>>>>>>>>>>>>>>>>" + HexByteArray.bytesToHex(auxElement.getType()));

			if (auxElement.equals(MatroskaDocType.CuePoint_Id)) {
				System.out.println("bbbbbbbbbbbbbbbbbbbb");
			}

			auxElement.skipData(data);
			auxElement = ((MasterElement) root).readNextChild(reader);
		}
	}

}
