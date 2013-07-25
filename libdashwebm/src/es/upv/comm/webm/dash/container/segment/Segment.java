package es.upv.comm.webm.dash.container.segment;


import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.ParseException;
import es.upv.comm.webm.dash.container.segment.cueing.CuePoint;
import es.upv.comm.webm.dash.container.segment.cueing.CueTrackPosition;
import es.upv.comm.webm.dash.container.segment.cueing.Cues;
import es.upv.comm.webm.dash.container.segment.info.Info;
import es.upv.comm.webm.dash.container.segment.seek.SeekHead;
import es.upv.comm.webm.dash.container.segment.track.Track;
import es.upv.comm.webm.dash.http.ByteRange;
import es.upv.comm.webm.dash.mpd.Representation;
import es.upv.comm.webm.dash.util.HexByteArray;

public class Segment implements Debug {

	private Representation mRepresentation;
	private int mSegmentOffset;
	private SeekHead mSeekHead;
	private Info mInfo;
	private Track mTrack;
	private Cues mCues;
	
	public void setRepresentation(Representation representation) {
		this.mRepresentation = representation;
	}
	
	public int getSegmentOffset() {
		return mSegmentOffset;
	}

	public void setSegmentOffset(int segmentOffset) {
		mSegmentOffset = segmentOffset;
	}

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
	
	public ByteRange getCueByteRange(int index){
		ByteRange br = null;
		
		CuePoint curretCuePoint = null;
		if (mCues.getCuePoints().size() > index) {
			curretCuePoint = mCues.getCuePoints().get(index);
		}

		CuePoint nextCuePoint = null;
		if (mCues.getCuePoints().size() > index + 1) {
			nextCuePoint = mCues.getCuePoints().get(index + 1);
		} else {
			// last cue point
		}
		
		
		if (curretCuePoint != null) {
			if (nextCuePoint != null) {
				br = new ByteRange(curretCuePoint.getClusterOffset(), nextCuePoint.getClusterOffset());
			} else {
				br = new ByteRange(curretCuePoint.getClusterOffset(), mRepresentation.getIndexRange().getInitByte());
			}
		}
		
		return br;
	}

	public void setCues(Cues cues) {
		mCues = cues;
		mCues.setSegmentOffset(mSegmentOffset);
	}

	public static Segment create(Representation representation,DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(representation, reader, dataSource);
	}

	private static Segment create(Representation representation,EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Segment_Id)) {
			return create(representation, rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}
	

	public static Segment create( Representation representation, Element segmentElement, EBMLReader ebmlReader, DataSource dataSource) {
		
		int exitCount = 0;
		boolean finished = false;

		Segment segment = new Segment();
		segment.setRepresentation(representation);
		segment.setSegmentOffset((int) dataSource.getFilePointer());

		if (D)
			Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Segment Offset: " + segment.getSegmentOffset());

		Element auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);
		while (auxElement != null && !finished) {
			if (auxElement.equals(MatroskaDocType.SeekHead_Id)) {
				if (D)
					Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Parsing SeekHead...");
				SeekHead seekHead = SeekHead.create(auxElement, ebmlReader, dataSource);
				segment.setSeekHead(seekHead);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.SegmentInfo_Id)) {
				if (D)
					Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Parsing SegmentInfo...");
				Info info = Info.create(auxElement, ebmlReader, dataSource);
				segment.setInfo(info);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Cluster_Id)) {
				if (D)
					Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Parsing Cluster...");

				// only skip
				
				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Tracks_Id)) {
				if (D)
					Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Parsing Track...");
				Track track = Track.create(auxElement, ebmlReader, dataSource);
				segment.setTrack(track);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.CueingData_Id)) {
				if (D)
					Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Parsing Cueing...");
				Cues cues = Cues.create(auxElement, ebmlReader, dataSource);
				cues.setSegmentOffset(segment.getSegmentOffset());
				segment.setCues(cues);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Attachments_Id)) {
				if (D)
					Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Parsing Attachment...");

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else {
				if (D)
					Log.d(LOG_TAG, Segment.class.getSimpleName() + ": " + "  Unknown element: " + HexByteArray.bytesToHex(auxElement.getType()) + " Offset: "+dataSource.getFilePointer());

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

				exitCount++;
				if (exitCount > 5) {
					finished = true;
				}
			}

		}

		return segment;
	}

}
