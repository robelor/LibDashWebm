package es.upv.comm.webm.dash;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.ebml.io.InputStreamDataSource;
import org.ebml.matroska.MatroskaBlock;

import android.media.MediaFormat;
import android.util.Log;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.segment.cluster.Cluster;
import es.upv.comm.webm.dash.container.segment.cueing.CuePoint;
import es.upv.comm.webm.dash.container.segment.cueing.Cues;
import es.upv.comm.webm.dash.container.segment.track.TrackEntry;
import es.upv.comm.webm.dash.http.ByteRange;
import es.upv.comm.webm.dash.http.HttpUtils;
import es.upv.comm.webm.dash.mpd.Representation;

public class Stream implements Debug {

	private static final String MIME_VIDEO = "video/x-vnd.on2.vp8";
	private static final String MIME_AUDIO = "audio/vorbis";

	public enum State {
		IDLE, INITIALIZED, PREPARED, STARTED
	}

	private State mState = State.IDLE;;

	private Representation mRepresentation;
	private URL mUrl;

	private Container mContainer;
	private ArrayList<CuePoint> mCuePoints;

	private int mCurrentCueIndex = -1;
	private Cluster mCurrentCluster;
	private ByteRange mCurrentByteRange;

	private MatroskaBlock mCurrentBlock;

	public Stream(Representation representation, URL baseUrl) throws IOException {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Creating Stream...");

		mRepresentation = representation;
		mUrl = new URL(baseUrl + representation.getBaseUrl());

		mContainer = new Container(mUrl, representation);
		mContainer.init();

		mCuePoints = mContainer.getSegment().getCues().getCuePoints();

		mState = State.INITIALIZED;
	}

	public URL getStreamUrl() {
		return mUrl;
	}

	public void prepare() {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Prepare...");

		seekTo(0);
		advance();

		mState = State.PREPARED;
	}

	public boolean seekToTime(int time) {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Seek to time: " + time);

		int index = -1;
		for (CuePoint cuePoint : mCuePoints) {
			index++;
			if (cuePoint.getTCueTime() >= time) {
				break;
			}
		}

		return seekTo(index);
	}
	
	public boolean seekToNextCluster() {
		return seekTo(++mCurrentCueIndex);
	}

	public boolean seekTo(int index) {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Seek to cue: " + index);

		if (index >= mCuePoints.size()) {
			return false;
		}

		mCurrentCueIndex = index;
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Cue point: " + mCurrentCueIndex);

		CuePoint curretCuePoint = null;
		if (mCuePoints.size() > index) {
			curretCuePoint = mCuePoints.get(index);
		}

		CuePoint nextCuePoint = null;
		if (mCuePoints.size() > index + 1) {
			nextCuePoint = mCuePoints.get(index + 1);
		} else {
			// last cue point
		}

		if (curretCuePoint != null) {
			if (nextCuePoint != null) {
				mCurrentByteRange = new ByteRange(curretCuePoint.getClusterOffset(), nextCuePoint.getClusterOffset());
			} else {
				mCurrentByteRange = new ByteRange(curretCuePoint.getClusterOffset(), mRepresentation.getIndexRange().getInitByte());
			}
		} else {
			return false;
		}

		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Cluster range: " + mCurrentByteRange + " Size: " + mCurrentByteRange.getRangeSize()
					+ " Bytes");

		try {
			mCurrentCluster = new Cluster(new InputStreamDataSource(new ByteArrayInputStream(HttpUtils.readUrlRange(mUrl, mCurrentByteRange))));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	public int readSampleData(ByteBuffer byteBuffer, int offset) {

		int dataLength = -1;

		if (mCurrentBlock != null) {
			byte[] data = mCurrentBlock.getData();
			dataLength = (int) mCurrentBlock.getSize();

			if (byteBuffer != null) {
				byteBuffer.clear();
				byteBuffer.put(data, 4, dataLength - 4);
			}
		}

		return (int) dataLength;

	}

	public boolean advance() {

		if (mCurrentCueIndex < 0) {
			prepare();
		}

		MatroskaBlock mb = mCurrentCluster.getNextBlock();

		if (mb != null) {
			mCurrentBlock = mb;
			return true;
		} else {
			if (seekTo(++mCurrentCueIndex)) {
				return advance();
			}
			mCurrentBlock = null;
			return false;

		}

	}

	public int getSampleTime() {
		return mCurrentBlock.getSampleTime();
	}

	public MediaFormat getStreamFormat() {
		MediaFormat mf = new MediaFormat();

		if (mContainer.getSegment().getTrack().getTrackEntries().size() > 0) {

			TrackEntry te = mContainer.getSegment().getTrack().getTrackEntries().get(0);

			if (te.getVideo() != null) {
				mf.setString(MediaFormat.KEY_MIME, MIME_VIDEO);
				mf.setLong(MediaFormat.KEY_DURATION, te.getTrackDefaultDuration());
				mf.setInteger(MediaFormat.KEY_WIDTH, te.getVideo().getmWidth());
				mf.setInteger(MediaFormat.KEY_HEIGHT, te.getVideo().getmHeight());

			} else if (te.getAudio() != null) {
				mf.setString(MediaFormat.KEY_MIME, MIME_AUDIO);

			}

		}

		return mf;
	}

}
