package es.upv.comm.webm.dash.container.segment.cueing;

import java.util.ArrayList;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.ParseException;

public class Cues implements Debug {
	
	private int mSegmentOffset;
	
	private ArrayList<CuePoint> mCuePoints;

	public Cues() {
		mCuePoints = new ArrayList<CuePoint>();
	}
	
	public int getSegmentOffset() {
		return mSegmentOffset;
	}
	
	public void setSegmentOffset(int segmentOffset) {
		mSegmentOffset = segmentOffset;
		for (CuePoint cuePoint : mCuePoints) {
			cuePoint.setSegmentOffset(mSegmentOffset);
		}
	}

	public ArrayList<CuePoint> getCuePoints() {
		return mCuePoints;
	}

	private void addCuePoint(CuePoint cuePoint) {
		mCuePoints.add(cuePoint);
	}

	public static Cues create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Cues create(EBMLReader ebmlReader, DataSource dataSource) {
		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.CueingData_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}
	}

	public static Cues create(Element cuesElement, EBMLReader ebmlReader, DataSource dataSource) { 
		Cues cues = new Cues();
		
		Element auxElement = ((MasterElement) cuesElement).readNextChild(ebmlReader);
		
		while (auxElement != null) {
			if (auxElement.equals(MatroskaDocType.CuePoint_Id)) {
				if (D)
					Log.d(LOG_TAG, Cues.class.getSimpleName() + ": " + "    Parsing CuePoint...");
				CuePoint cuePoint = CuePoint.create(auxElement, ebmlReader, dataSource);
				cues.addCuePoint(cuePoint);
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) cuesElement).readNextChild(ebmlReader);
		}

		return cues;
	}

}
