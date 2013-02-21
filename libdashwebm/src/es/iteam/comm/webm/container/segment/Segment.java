package es.iteam.comm.webm.container.segment;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.ElementType;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;

import android.util.Log;

import es.iteam.comm.webm.Debug;
import org.ebml.matroska.MatroskaDocType;
import es.iteam.comm.webm.container.WebmContainer;
import es.iteam.comm.webm.container.WebmParseException;
import es.iteam.comm.webm.container.segment.cueing.Cues;
import es.iteam.comm.webm.container.segment.info.Info;
import es.iteam.comm.webm.container.segment.seek.SeekHead;
import es.iteam.comm.webm.container.segment.track.Track;
import es.iteam.comm.webm.util.HexByteArray;

public class Segment implements Debug {

	private SeekHead mSeekHead;
	private Info mInfo;
	private Track mTrack;
	private Cues mCues;

	public SeekHead getSeekHead() {
		return mSeekHead;
	}

	private void setSeekHead(SeekHead seekHead) {
		mSeekHead = seekHead;
	}

	public Info getInfo() {
		return mInfo;
	}

	private void setInfo(Info info) {
		mInfo = info;
	}

	public Track getTrack() {
		return mTrack;
	}

	private void setTrack(Track track) {
		mTrack = track;
	}
	
	public Cues getCues() {
		return mCues;
	}
	
	public void setCues(Cues cues) {
		mCues = cues;
	}

	public static Segment create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Segment create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Segment_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Segment create(Element segmentElement, EBMLReader ebmlReader, DataSource dataSource) {
		Segment segment = new Segment();

		Element auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			if (auxElement.equals(MatroskaDocType.SeekHead_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "  Parsing SeekHead...");
				SeekHead seekHead = SeekHead.create(auxElement, ebmlReader, dataSource);
				segment.setSeekHead(seekHead);
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);
				
			} else if (auxElement.equals(MatroskaDocType.SegmentInfo_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "  Parsing SegmentInfo...");
				Info info = Info.create(auxElement, ebmlReader, dataSource);
				segment.setInfo(info);
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Cluster_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "  Parsing Cluster...");
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Tracks_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "  Parsing Track...");
				 Track track = Track.create(auxElement, ebmlReader, dataSource); 
				 segment.setTrack(track);
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.CueingData_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "  Parsing Cueing...");
				
				
				Cues cues = Cues.create(auxElement, ebmlReader, dataSource);
				segment.setCues(cues);
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Attachments_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "  Parsing Attachment...");
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);
				
			} else {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "  Unknown element: " + HexByteArray.bytesToHex(auxElement.getType()));
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);
			}


		}

		return segment;
	}

}
