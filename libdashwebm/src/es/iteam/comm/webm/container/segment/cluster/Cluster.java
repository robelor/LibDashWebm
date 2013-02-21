package es.iteam.comm.webm.container.segment.cluster;

import java.util.ArrayList;

import org.ebml.BinaryElement;
import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaBlock;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.iteam.comm.webm.Debug;
import es.iteam.comm.webm.container.WebmContainer;
import es.iteam.comm.webm.container.WebmParseException;
import es.iteam.comm.webm.util.HexByteArray;

public class Cluster implements Debug {

	public static Cluster create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Cluster create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Cluster_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Cluster create(Element clusterElement, EBMLReader ebmlReader, DataSource dataSource) {
		Cluster track = new Cluster();

		Element auxElement = ((MasterElement) clusterElement).readNextChild(ebmlReader);
		while (auxElement != null) {

			if (auxElement.equals(MatroskaDocType.ClusterTimecode_Id)) {
				auxElement.readData(dataSource);
				long timeCode = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    TimeCode: " + timeCode);
				// TrackEntry trackEntry = TrackEntry.create(auxElement, ebmlReader, dataSource);
				// track.addTrackEntry(trackEntry);
			} else if (auxElement.equals(MatroskaDocType.ClusterSimpleBlock_Id)) {
				auxElement.readData(dataSource);
				MatroskaBlock mb = ((MatroskaBlock) auxElement);
				mb.parseBlock();

				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    SimpleBlock Track: " + mb.getTrackNo());
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    SimpleBlock Timecode: " + mb.getBlockTimecode());
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    SimpleBlock Size: " + mb.getSize());
				if (D & mb.isKeyFrame())
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    SimpleBlock is key frame ");
				
				
				// TrackEntry trackEntry = TrackEntry.create(auxElement, ebmlReader, dataSource);
				// track.addTrackEntry(trackEntry);
			} else {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    Unhandled element: " + HexByteArray.bytesToHex(auxElement.getType()));
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) clusterElement).readNextChild(ebmlReader);
		}

		return track;
	}

}
