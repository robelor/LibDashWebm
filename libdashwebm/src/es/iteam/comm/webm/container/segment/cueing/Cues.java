package es.iteam.comm.webm.container.segment.cueing;

import java.util.ArrayList;

import org.ebml.BinaryElement;
import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.iteam.comm.webm.Debug;
import es.iteam.comm.webm.container.WebmContainer;
import es.iteam.comm.webm.container.WebmParseException;
import es.iteam.comm.webm.util.HexByteArray;

public class Cues implements Debug {

	private ArrayList<CuePoint> mCuePoints;

	public Cues() {
		mCuePoints = new ArrayList<CuePoint>();
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
			throw new WebmParseException("Error: Unable to scan for EBML elements");
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
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    Parsing CuePoint...");
				CuePoint cuePoint = CuePoint.create(auxElement, ebmlReader, dataSource);
				cues.addCuePoint(cuePoint);
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) cuesElement).readNextChild(ebmlReader);
		}

		return cues;
	}

}
