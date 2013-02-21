package es.iteam.comm.webm.container.segment.cueing;

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

public class CueTrackPosition implements Debug {

	private long mCueTrack;
	private long mCueClusterPosition;
	private long mCueBlockNumber;

	public long getCueTrack() {
		return mCueTrack;
	}

	private void setCueTrack(long cueTrack) {
		mCueTrack = cueTrack;
	}

	public long getmCueClusterPosition() {
		return mCueClusterPosition;
	}

	public void setmCueClusterPosition(long mCueClusterPosition) {
		this.mCueClusterPosition = mCueClusterPosition;
	}

	public long getmCueBlockNumber() {
		return mCueBlockNumber;
	}

	public void setmCueBlockNumber(long mCueBlockNumber) {
		this.mCueBlockNumber = mCueBlockNumber;
	}

	public static CueTrackPosition create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static CueTrackPosition create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.CueTrack_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static CueTrackPosition create(Element cuePositionElement, EBMLReader ebmlReader, DataSource dataSource) {
		CueTrackPosition cueTrackPositions = new CueTrackPosition();

		Element auxElement = ((MasterElement) cuePositionElement).readNextChild(ebmlReader);
		while (auxElement != null) {

			if (auxElement.equals(MatroskaDocType.CueTrack_Id)) {
				auxElement.readData(dataSource);
				long cueTrack = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "        CueTrack: " + cueTrack);
				cueTrackPositions.setCueTrack(cueTrack);

			} else if (auxElement.equals(MatroskaDocType.CueClusterPosition_Id)) {
				auxElement.readData(dataSource);
				long cueClusterPosition = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "        CueClusterPosition: " + cueClusterPosition);
				cueTrackPositions.setmCueClusterPosition(cueClusterPosition);

			} else if (auxElement.equals(MatroskaDocType.CueBlockNumber_Id)) {
				auxElement.readData(dataSource);
				long cueBlockNumber = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "        CueBlockNumber: " + cueBlockNumber);
				cueTrackPositions.setmCueBlockNumber(cueBlockNumber);

			}else{
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "        Unhandled element: "+HexByteArray.bytesToHex(auxElement.getType()));
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) cuePositionElement).readNextChild(ebmlReader);
		}

		return cueTrackPositions;

	}

}
