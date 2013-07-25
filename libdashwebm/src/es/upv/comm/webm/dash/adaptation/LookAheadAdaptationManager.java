package es.upv.comm.webm.dash.adaptation;

import java.util.ArrayList;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.Stream;
import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.container.segment.cueing.CuePoint;
import es.upv.comm.webm.dash.http.ByteRange;
import es.upv.comm.webm.dash.http.NetworkSpeedListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;
import es.upv.comm.webm.dash.mpd.Representation;

public class LookAheadAdaptationManager implements Debug, AdaptationManager, BufferReportListener, NetworkSpeedListener {

	private int mCurrentIndex = 0;
	private int mCurrentRepresentation;
	private int mMinBufferFill = 100;
	private float mNetworkSpeed = 0;
	private float mFutureNetworkSpeed = 0;
	private int mReportCount = 0;

	private AdaptationSet mAdaptationSet;
	private Stream[] mVideoStreams;

	private ArrayList<Representation> mRepresentations;

	public LookAheadAdaptationManager(AdaptationSet adaptationSet, Stream[] videoStreams) {
		mAdaptationSet = adaptationSet;
		mVideoStreams = videoStreams;

		mRepresentations = mAdaptationSet.getRepresentations();
	}

	@Override
	public synchronized int getFirstSegmentTrack() {
		mCurrentRepresentation = 0;
		mCurrentIndex++;

		return mCurrentRepresentation;
	}

	@Override
	public synchronized int getNextSegmentTrack() {
		if (mReportCount > 0) {
			
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "\tAvailable Bandwidth: " + mNetworkSpeed);

			int requiredBandwidth;
			
			int next1 = mVideoStreams.length - 1;
			int next2 = mVideoStreams.length - 1;

			
			requiredBandwidth = (int) (getMeanBandwidth(next1, next2) / 10);
			
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "\tRequired Bandwidth: "+next1+" : "+next2+": " + requiredBandwidth);
			
			
			
			
			boolean mySwitch = true;
			while(requiredBandwidth > mNetworkSpeed && (next1>= 0 && next2 > 0)){
				if(mySwitch){
					next1--;
					mySwitch = false;
				}else{
					next2--;
					mySwitch = true;
				}
				requiredBandwidth = (int) (getMeanBandwidth(next1, next2) / 9);
				if (D)
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "\tRequired Bandwidth: "+next1+" : "+next2+": " + requiredBandwidth);
			}
				
			
			
			if(mMinBufferFill < 66){
				if(next1 > 0){
					next1--;
				}
			}
			
			if(mCurrentIndex == 1){
				return 1;
			}
			
			

			mCurrentRepresentation = next1;

			mMinBufferFill = 100;
		}

		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "\t--->" + mCurrentRepresentation);

		mReportCount = 0;
		mCurrentIndex++;
		

		

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

	}
	
	private int getMeanBandwidth(int currentQuality, int nextQuality){
		int aux = 0;
		
		int next1 = currentQuality;
		int next2 = nextQuality;

		ByteRange br1 = mVideoStreams[next1].getContainer().getSegment().getCueByteRange(mCurrentIndex);
		ByteRange br2 = mVideoStreams[next2].getContainer().getSegment().getCueByteRange(mCurrentIndex + 1);

		if (br1 != null && br2 != null) {

			aux = br1.getRangeSize() + br2.getRangeSize();
			
		}
		
		
		return aux;
	}

}
