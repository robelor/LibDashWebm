package es.upv.comm.webm.dash.adaptation;

import java.util.ArrayList;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.Stream;
import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.http.NetworkSpeedListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;
import es.upv.comm.webm.dash.mpd.Representation;

public class BufferNetworkBasedAdaptationManager implements Debug, AdaptationManager, BufferReportListener, NetworkSpeedListener {

	private int mCurrentSegment = 0;
	private int mCurrentRepresentation;
	private int mMinBufferFill = 100;
	private float mNetworkSpeed = 0;
	private float mFutureNetworkSpeed = 0;
	private int mReportCount = 0;

	private AdaptationSet mAdaptationSet;
	private Stream[] mVideoStreams;

	private ArrayList<Representation> mRepresentations;

	public BufferNetworkBasedAdaptationManager(AdaptationSet adaptationSet, Stream[] videoStreams) {
		mAdaptationSet = adaptationSet;
		mVideoStreams = videoStreams;

		mRepresentations = mAdaptationSet.getRepresentations();
	}

	@Override
	public synchronized int getFirstSegmentTrack() {
		mCurrentRepresentation = 0;
		mCurrentSegment++;

		return mCurrentRepresentation;
	}

	@Override
	public synchronized int getNextSegmentTrack() {
		if (mReportCount > 0) {
			int aux = -1;
			
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Future speed: "+mFutureNetworkSpeed);
			
			for (Representation rep : mAdaptationSet.getRepresentations()) {
				int repBw = Integer.parseInt(rep.getBandwidth());
				if (D)
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "\tRepresentation"+(aux+1)+": "+repBw);
				
				aux++;
				if (mFutureNetworkSpeed <= repBw) {
					break;
				}
			}
			
			mCurrentRepresentation =  aux;
			mMinBufferFill = 100;
		}
		
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "\t--->"+mCurrentRepresentation);

		mReportCount = 0;
		mCurrentSegment++;

		return mCurrentRepresentation;
	}

	@Override
	public void bufferReport(BufferReport bufferReport) {
		mReportCount++;
		if (mMinBufferFill > bufferReport.getBufferUsage()) {
			mMinBufferFill = bufferReport.getBufferUsage();
		}

	}

	@Override
	public void networkSpeed(int streamIndex, int index, float speed) {
		mNetworkSpeed = speed;

		if (mMinBufferFill < 15) {
			mFutureNetworkSpeed = 0.3f * mNetworkSpeed;
			
		} else if (mMinBufferFill < 35) {
			mFutureNetworkSpeed = 0.5f * mNetworkSpeed;
			
		} else if (mMinBufferFill < 50) {
			mFutureNetworkSpeed = mNetworkSpeed;
			
		} else {
			mFutureNetworkSpeed = (1 + 0.5f * ((float) mMinBufferFill / 100)) * mNetworkSpeed;
		}
		
		mFutureNetworkSpeed = mFutureNetworkSpeed * 8;

		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Network speed: " + mNetworkSpeed + " Buffer: " + mMinBufferFill + " future speed: "
					+ mFutureNetworkSpeed);
	}

}
