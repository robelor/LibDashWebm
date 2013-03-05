package es.upv.comm.webm.dash.container.segment;

import java.util.ArrayList;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.ParseException;
import es.upv.comm.webm.dash.container.segment.cluster.Cluster;
import es.upv.comm.webm.dash.container.segment.cueing.CuePoint;
import es.upv.comm.webm.dash.container.segment.cueing.CueTrackPosition;
import es.upv.comm.webm.dash.container.segment.cueing.Cues;
import es.upv.comm.webm.dash.container.segment.info.Info;
import es.upv.comm.webm.dash.container.segment.seek.SeekHead;
import es.upv.comm.webm.dash.container.segment.track.Track;
import es.upv.comm.webm.dash.util.HexByteArray;

public class Segment implements Debug {

	private int mSegmentOffset;
	private SeekHead mSeekHead;
	private Info mInfo;
	private Track mTrack;
	private ArrayList<Cluster> mClusters;
	private Cues mCues;

	public Segment() {
		mClusters = new ArrayList<Cluster>();
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

	public ArrayList<Cluster> getmClusters() {
		return mClusters;
	}

	private void addCluster(Cluster cluster) {
		mClusters.add(cluster);
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
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Segment_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Segment create(Element segmentElement, EBMLReader ebmlReader, DataSource dataSource) {
		int exitCount = 0;
		boolean finished = false;

		Segment segment = new Segment();
		segment.setSegmentOffset((int) dataSource.getFilePointer());

		if (D)
			Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Segment Offset: " + segment.getSegmentOffset());

		Element auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);
		while (auxElement != null && !finished) {
			if (auxElement.equals(MatroskaDocType.SeekHead_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Parsing SeekHead...");
				SeekHead seekHead = SeekHead.create(auxElement, ebmlReader, dataSource);
				segment.setSeekHead(seekHead);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.SegmentInfo_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Parsing SegmentInfo...");
				Info info = Info.create(auxElement, ebmlReader, dataSource);
				segment.setInfo(info);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Cluster_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Parsing Cluster...");
				Cluster cluster = Cluster.create(auxElement, ebmlReader, dataSource);
				segment.addCluster(cluster);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Tracks_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Parsing Track...");
				Track track = Track.create(auxElement, ebmlReader, dataSource);
				segment.setTrack(track);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.CueingData_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Parsing Cueing...");
				Cues cues = Cues.create(auxElement, ebmlReader, dataSource, segment.getSegmentOffset());
				segment.setCues(cues);

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else if (auxElement.equals(MatroskaDocType.Attachments_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Parsing Attachment...");

				auxElement.skipData(dataSource);
				auxElement = ((MasterElement) segmentElement).readNextChild(ebmlReader);

			} else {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "  Unknown element: " + HexByteArray.bytesToHex(auxElement.getType()) + " Offset: "+dataSource.getFilePointer());

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

	public void getNextBlock() {
		
		if(mCues!=null ){
			CuePoint cp = mCues.getCuePoints().get(0);
			
			CueTrackPosition ctp= cp.getCueTrackPositions().get(0);
			
			if (D)
				Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "*************>"+ctp.getCueTrack());
			if (D)
				Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "*************> Start: "+(ctp.getmCueClusterPosition()+ctp.getSegmentOffset()));
			
			CuePoint cp2 = mCues.getCuePoints().get(1);
			CueTrackPosition ctp2= cp2.getCueTrackPositions().get(0);
			if (D)
				Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "*************> End: "+ (ctp2.getmCueClusterPosition()+ctp2.getSegmentOffset()));
			
			
		}
		
	}

}
