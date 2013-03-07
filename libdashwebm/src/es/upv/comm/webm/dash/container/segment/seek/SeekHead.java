package es.upv.comm.webm.dash.container.segment.seek;

import java.util.ArrayList;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.ParseException;

public class SeekHead implements Debug{
	
	private ArrayList<Seek> mSeeks;
	
	public SeekHead() {
		mSeeks = new ArrayList<Seek>();
	}
	
	public ArrayList<Seek> getSeeks() {
		return mSeeks;
	}
	
	private void addSeek(Seek seek){
		mSeeks.add(seek);
	}
	
	public static SeekHead create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static SeekHead create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Segment_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static SeekHead create(Element metaSeekElement, EBMLReader ebmlReader, DataSource dataSource) {
		SeekHead seekHead = new SeekHead();

		Element auxElement = ((MasterElement) metaSeekElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			if (auxElement.equals(MatroskaDocType.SeekEntry_Id)) {
				if(D)
					Log.d(LOG_TAG, SeekHead.class.getSimpleName()+": "+"    Parsing SeekEntry...");
				Seek seek = Seek.create(auxElement, ebmlReader, dataSource);
				seekHead.addSeek(seek);
			}
			
			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) metaSeekElement).readNextChild(ebmlReader);
		}

		return seekHead;
	}

}
