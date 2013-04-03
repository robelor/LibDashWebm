package es.upv.comm.webm.dash.container.segment.cueing;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.ParseException;
import es.upv.comm.webm.dash.util.HexByteArray;

public class CueTrackPosition implements Debug {

	private int mCueTrack;
	private int mSegmentOffset;
	private int mCueClusterPosition;
	private int mCueBlockNumber;

	public int getCueTrack() {
		return mCueTrack;
	}

	private void setCueTrack(int cueTrack) {
		mCueTrack = cueTrack;
	}

	public int getSegmentOffset() {
		return mSegmentOffset;
	}

	public void setSegmentOffset(int segmentOffset) {
		mSegmentOffset = segmentOffset;
	}

	public int getmCueClusterPosition() {
		return mCueClusterPosition;
	}

	public void setmCueClusterPosition(int mCueClusterPosition) {
		this.mCueClusterPosition = mCueClusterPosition;
	}

	public int getmCueBlockNumber() {
		return mCueBlockNumber;
	}

	public void setmCueBlockNumber(int mCueBlockNumber) {
		this.mCueBlockNumber = mCueBlockNumber;
	}

	public static CueTrackPosition create(DataSource dataSource, int segmentOffset) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource, segmentOffset);
	}

	private static CueTrackPosition create(EBMLReader ebmlReader, DataSource dataSource, int segmentOffset) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.CueTrack_Id)) {
			return create(rootElement, ebmlReader, dataSource, segmentOffset);
		} else {
			return null;
		}

	}

	public static CueTrackPosition create(Element cueTrackPositionElement, EBMLReader ebmlReader, DataSource dataSource, int segmentOffset) {
		CueTrackPosition cueTrackPosition = new CueTrackPosition();
		cueTrackPosition.setSegmentOffset(segmentOffset);

		Element auxElement = ((MasterElement) cueTrackPositionElement).readNextChild(ebmlReader);
		while (auxElement != null) {

			if (auxElement.equals(MatroskaDocType.CueTrack_Id)) {
				auxElement.readData(dataSource);
				int cueTrack = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, CueTrackPosition.class.getSimpleName() + ": " + "        CueTrack: " + cueTrack);
				cueTrackPosition.setCueTrack(cueTrack);

			} else if (auxElement.equals(MatroskaDocType.CueClusterPosition_Id)) {
				auxElement.readData(dataSource);
				int cueClusterPosition = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, CueTrackPosition.class.getSimpleName() + ": " + "        CueClusterPosition: " + (cueClusterPosition + cueTrackPosition.getSegmentOffset() ));
				cueTrackPosition.setmCueClusterPosition(cueClusterPosition);

			} else if (auxElement.equals(MatroskaDocType.CueBlockNumber_Id)) {
				auxElement.readData(dataSource);
				int cueBlockNumber = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, CueTrackPosition.class.getSimpleName() + ": " + "        CueBlockNumber: " + cueBlockNumber);
				cueTrackPosition.setmCueBlockNumber(cueBlockNumber);

			} else {
				if (D)
					Log.d(LOG_TAG, CueTrackPosition.class.getSimpleName() + ": " + "        Unhandled element: " + HexByteArray.bytesToHex(auxElement.getType()));
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) cueTrackPositionElement).readNextChild(ebmlReader);
		}

		return cueTrackPosition;

	}

}
