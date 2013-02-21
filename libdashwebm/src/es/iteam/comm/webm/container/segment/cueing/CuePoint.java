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

public class CuePoint implements Debug {

	private long mTrackNumber;
	
	

	public long getTrackNumber() {
		return mTrackNumber;
	}

	private void setTrackNumber(long trackNumber) {
		mTrackNumber = trackNumber;
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

		if (rootElement.equals(MatroskaDocType.TrackEntry_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static CuePoint create(Element cuePointElement, EBMLReader ebmlReader, DataSource dataSource) {
		CuePoint trackEntry = new CuePoint();

		Element auxElement = ((MasterElement) cuePointElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			

			System.out.println(">>>>>" + HexByteArray.bytesToHex(auxElement.getType()));

			if (auxElement.equals(MatroskaDocType.CueTime_Id)) {
				auxElement.readData(dataSource);
				long cueTime = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "      CueTime: " + cueTime);
				trackEntry.setTrackNumber(cueTime);

			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) cuePointElement).readNextChild(ebmlReader);
		}

		return trackEntry;

	}

}
