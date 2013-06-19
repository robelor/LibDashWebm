package es.upv.comm.webm.dash.adaptation;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.http.NetworkSpeedListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;

public class LowerStreamAdaptationManager implements Debug, AdaptationManager, NetworkSpeedListener {

	private int mCurrentRepresentation;
	private int mBufferFill = 100;

	public LowerStreamAdaptationManager(AdaptationSet adaptationSet) {
	}


	@Override
	public int getFirstSegmentTrack() {
		
		return 0;
	}

	@Override
	public int getNextSegmentTrack() {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "getNextSegmentTrack()"); 
		return 0;

	}


	@Override
	public void networkSpeed(int streamIndex, int index, float speed) {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "networkSpeed()");
	}


	

}
