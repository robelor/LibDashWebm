package es.iteam.comm.webm.container.segment.cueing;

import java.util.ArrayList;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.iteam.comm.webm.Debug;
import es.iteam.comm.webm.container.WebmContainer;
import es.iteam.comm.webm.container.WebmParseException;
import es.iteam.comm.webm.util.HexByteArray;

public class CuePoint implements Debug {

	private long mCueTime;
	private ArrayList<CueTrackPosition> mCueTrackPositions;
	
	public CuePoint() {
		mCueTrackPositions = new ArrayList<CueTrackPosition>();
	}

	public long getTrackNumber() {
		return mCueTime;
	}

	private void setCueTime(long cueTime) {
		mCueTime = cueTime;
	}
	
	public ArrayList<CueTrackPosition> getCueTrackPositions() {
		return mCueTrackPositions;
	}
	
	private void addCueTrackPosition(CueTrackPosition cueTrackPosition){
		mCueTrackPositions.add(cueTrackPosition);
	}

	public static CuePoint create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static CuePoint create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.CuePoint_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static CuePoint create(Element cuePointElement, EBMLReader ebmlReader, DataSource dataSource) {
		CuePoint cuePoint = new CuePoint();

		Element auxElement = ((MasterElement) cuePointElement).readNextChild(ebmlReader);
		while (auxElement != null) {

			if (auxElement.equals(MatroskaDocType.CueTime_Id)) {
				auxElement.readData(dataSource);
				long cueTime = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "      CueTime: " + cueTime);
				cuePoint.setCueTime(cueTime);

			} else if (auxElement.equals(MatroskaDocType.CueTrackPositions_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "      Parsing CueTracksPosition...");
				CueTrackPosition cueTrackPosition = CueTrackPosition.create(auxElement, ebmlReader, dataSource);
				cuePoint.addCueTrackPosition(cueTrackPosition);

			}else{
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "      Unhandled element: "+HexByteArray.bytesToHex(auxElement.getType()));
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) cuePointElement).readNextChild(ebmlReader);
		}

		return cuePoint;

	}

}
