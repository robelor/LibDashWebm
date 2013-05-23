package es.upv.comm.webm.dash.adaptation;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;

public class BasicBufferBasedAdaptationManager implements Debug, AdaptationManager, BufferReportListener {

	private int mCurrentRepresentation;
	private int mBufferFill = 100;

	public BasicBufferBasedAdaptationManager(AdaptationSet adaptationSet) {
	}

	@Override
	public int getFirstSegmentTrack() {
		mCurrentRepresentation = 0;
		return mCurrentRepresentation;
	}

	@Override
	public int getNextSegmentTrack() {

		if (mBufferFill < 33) {
			return 0;
		} else if (mBufferFill < 66){
			return 1;
		}else{
			return 2;
		}

	}

	@Override
	public void bufferReport(BufferReport bufferReport) {
			mBufferFill = bufferReport.getBufferUsage();

	}

	

}
