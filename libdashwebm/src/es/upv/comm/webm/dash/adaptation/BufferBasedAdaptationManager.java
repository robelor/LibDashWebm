package es.upv.comm.webm.dash.adaptation;

import java.util.ArrayList;

import android.os.SystemClock;

import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;
import es.upv.comm.webm.dash.mpd.Representation;

public class BufferBasedAdaptationManager implements AdaptationManager, BufferReportListener {

	private static final int MIN_TRANSITION_INTERVAL = 4000;

	private long mLastRepresentationChangeTime = 0;
	private int mCurrentRepresentation;

	private AdaptationSet mAdaptationSet;
	private ArrayList<Representation> mRepresentations;

	public BufferBasedAdaptationManager(AdaptationSet adaptationSet) {
		mAdaptationSet = adaptationSet;
		mRepresentations = mAdaptationSet.getRepresentations();
	}

	@Override
	public int getFirstSegmentTrack() {
		mLastRepresentationChangeTime = SystemClock.elapsedRealtime();
		mCurrentRepresentation = mRepresentations.size() - 1;
		return mCurrentRepresentation;
	}

	@Override
	public int getNextSegmentTrack() {
		return mCurrentRepresentation;
	}

	@Override
	public void bufferReport(BufferReport bufferReport) {
		long now = SystemClock.elapsedRealtime();
		if (bufferReport.getBufferUsage() < 66) {
			if (now - mLastRepresentationChangeTime > MIN_TRANSITION_INTERVAL) {
				goWorse();
			}
		}else{
			if (now - mLastRepresentationChangeTime > MIN_TRANSITION_INTERVAL) {
				goBetter();
			}
		}

	}

	private void goWorse() {
		if (mCurrentRepresentation > 0)
			mCurrentRepresentation--;
	}

	private void goBetter() {
		if (mCurrentRepresentation < mRepresentations.size() - 2)
			mCurrentRepresentation++;

	}

}
