package es.upv.comm.webm.dash.adaptation;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.mpd.AdaptationSet;

public class BasicBufferBasedAdaptationManager implements Debug, AdaptationManager {


	public BasicBufferBasedAdaptationManager(AdaptationSet adaptationSet) {
	}

	@Override
	public int getFirstSegmentTrack() {
		
		return 0;
	}

	@Override
	public int getNextSegmentTrack() {

		return 0;

	}

	

}
