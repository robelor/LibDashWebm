package es.upv.comm.webm.dash.adaptation;

import java.util.ArrayList;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;
import es.upv.comm.webm.dash.mpd.Representation;

public class BufferBasedAdaptationManager2 implements Debug, AdaptationManager, BufferReportListener {

	private int mCurrentRepresentation;
	private int mMinBufferFill = 100;
	private int mReportCount = 0;

	private AdaptationSet mAdaptationSet;
	private ArrayList<Representation> mRepresentations;

	public BufferBasedAdaptationManager2(AdaptationSet adaptationSet) {
		mAdaptationSet = adaptationSet;
		mRepresentations = mAdaptationSet.getRepresentations();
	}

	@Override
	public int getFirstSegmentTrack() {
		mCurrentRepresentation = 0;
		return mCurrentRepresentation;
	}

	@Override
	public int getNextSegmentTrack() {
		if (mReportCount > 0) {
			if (mMinBufferFill < 66) {
				goWorse();
			} else if (mMinBufferFill > 80) {
				goBetter();
			}

			mMinBufferFill = 100;
		}
		mReportCount = 0;
		return mCurrentRepresentation;
	}

	@Override
	public void bufferReport(BufferReport bufferReport) {
		mReportCount++;
		if (mMinBufferFill > bufferReport.getBufferUsage()) {
			mMinBufferFill = bufferReport.getBufferUsage();
		}

	}

	private void goWorse() {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "goWorse()");
		if (mCurrentRepresentation > 0)
			mCurrentRepresentation--;

		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Current representation: " + mCurrentRepresentation);
	}

	private void goBetter() {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "goBetter()");
		if (mCurrentRepresentation <= (mRepresentations.size() - 2))
			mCurrentRepresentation++;

		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Current representation: " + mCurrentRepresentation);

	}

}
