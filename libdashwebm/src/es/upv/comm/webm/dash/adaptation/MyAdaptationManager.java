package es.upv.comm.webm.dash.adaptation;

import es.upv.comm.webm.dash.Stream;
import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;

public class MyAdaptationManager implements AdaptationManager, BufferReportListener {
	private AdaptationSet mAdaptationSet;
	private Stream[] mStream;
	private int mBufferStatus;
	private int mCurrentRepresentation = 0;

	MyAdaptationManager(AdaptationSet adaptationSet, Stream[] videoStreams) {
		mAdaptationSet = adaptationSet;
		mStream = videoStreams;
	}

	@Override
	public int getFirstSegmentTrack() {
		return 0;
	}

	@Override
	public int getNextSegmentTrack() {
		return (mBufferStatus > 66) ?
				(mCurrentRepresentation == mStream.length - 1) ? mCurrentRepresentation : ++mCurrentRepresentation
				:
				(mCurrentRepresentation == 0) ? mCurrentRepresentation : --mCurrentRepresentation;
	}

	@Override
	public void bufferReport(BufferReport bufferReport) {
		mBufferStatus = bufferReport.getBufferUsage();
	}

}
