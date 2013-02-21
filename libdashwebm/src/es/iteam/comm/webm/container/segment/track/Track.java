package es.iteam.comm.webm.container.segment.track;

import java.util.ArrayList;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.iteam.comm.webm.Debug;
import es.iteam.comm.webm.container.WebmContainer;
import es.iteam.comm.webm.container.WebmParseException;

public class Track implements Debug {

	private ArrayList<TrackEntry> mTrackEntries;

	public Track() {
		mTrackEntries = new ArrayList<TrackEntry>();
	}

	public ArrayList<TrackEntry> getTrackEntries() {
		return mTrackEntries;
	}

	private void addTrackEntry(TrackEntry trackEntry) {
		mTrackEntries.add(trackEntry);
	}

	public static Track create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Track create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Tracks_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Track create(Element trackElement, EBMLReader ebmlReader, DataSource dataSource) {
		Track track = new Track();

		Element auxElement = ((MasterElement) trackElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			if (auxElement.equals(MatroskaDocType.TrackEntry_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "    Parsing TrackEntry...");
				TrackEntry trackEntry = TrackEntry.create(auxElement, ebmlReader, dataSource);
				track.addTrackEntry(trackEntry);
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) trackElement).readNextChild(ebmlReader);
		}

		return track;
	}

}
