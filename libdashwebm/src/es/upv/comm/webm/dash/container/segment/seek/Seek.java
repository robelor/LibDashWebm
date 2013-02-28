package es.upv.comm.webm.dash.container.segment.seek;

import org.ebml.BinaryElement;
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

public class Seek implements Debug {

	private byte[] mSeekId;
	private long mSeekPosition;

	public byte[] getSeekId() {
		return mSeekId;
	}

	private void setSeekId(byte[] seekId) {
		mSeekId = seekId;
	}

	public long getSeekPosition() {
		return mSeekPosition;
	}

	private void setSeekPosition(long seekPosition) {
		mSeekPosition = seekPosition;
	}

	public static Seek create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Seek create(EBMLReader ebmlReader, DataSource dataSource) {

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

	public static Seek create(Element seekElement, EBMLReader ebmlReader, DataSource dataSource) {
		Seek seek = new Seek();

		Element auxElement = ((MasterElement) seekElement).readNextChild(ebmlReader);
		while (auxElement != null) {

			if (auxElement.equals(MatroskaDocType.SeekID_Id)) {
				auxElement.readData(dataSource);
				byte[] seedId = ((BinaryElement) auxElement).getData();
				seek.setSeekId(seedId);
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      SeekId: " + HexByteArray.bytesToHex(seedId));

			} else if (auxElement.equals(MatroskaDocType.SeekPosition_Id)) {
				auxElement.readData(dataSource);
				long seekPosition = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      SeekPosition: " + seekPosition);
				seek.setSeekPosition(seekPosition);
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) seekElement).readNextChild(ebmlReader);
		}

		return seek;
	}

}
