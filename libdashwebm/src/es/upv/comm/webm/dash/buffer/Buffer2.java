package es.upv.comm.webm.dash.buffer;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.ebml.matroska.MatroskaBlock;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.Frame;
import es.upv.comm.webm.dash.Stream;
import es.upv.comm.webm.dash.adaptation.AdaptationManager;

public class Buffer2 implements Debug {

	private Stream[] mVideoStreams;

	private int mBufferType;
	private int mSize;
	private int mMinSize;

	private int mHeadTimestamp;
	private int mTailTimestamp;

	private LinkedBlockingBufferQueue<Frame> mFrames;
	private BufferFedder mBufferFedder;

	private int mCurrentFedderStream = 0;

	private int mCurrentSegment = -1;
	private Frame mCurrentFrame;

	private HashSet<BufferReportListener> mBufferReportListeners = new HashSet<BufferReportListener>();

	private AdaptationManager mAdaptationManager;

	private Timer mReportTimer;

	public Buffer2(Stream[] videoStreams,int bufferType, int size, int minSize, AdaptationManager adaptationManager) {

		mVideoStreams = videoStreams;

		mBufferType = bufferType;
		mSize = size;
		mMinSize = minSize;

		mAdaptationManager = adaptationManager;

		mFrames = new LinkedBlockingBufferQueue<Frame>(mSize,minSize);

		mCurrentFedderStream = mAdaptationManager.getFirstSegmentTrack();

	}

	public void start() {
		if (mBufferFedder == null) {
			mBufferFedder = new BufferFedder();
			mBufferFedder.start();
		}

		if (mReportTimer == null) {
			mReportTimer = new Timer("Report Timer", true);
			mReportTimer.scheduleAtFixedRate(new ReportTimerTask(), 100, 100);
		}
	}

	public void stop() {
		if (mBufferFedder != null) {
			mBufferFedder.stop();
		}
		if (mReportTimer != null) {
			mReportTimer.cancel();
			mReportTimer = null;
		}
	}





	public boolean addBufferReportListener(BufferReportListener listener) {
		return mBufferReportListeners.add(listener);
	}

	public boolean removeBufferReportListener(BufferReportListener listener) {
		return mBufferReportListeners.remove(listener);
	}

	public void fireBufferReport(BufferReport bufferReport) {
		// Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Buffer Report: "+bufferReport.toString());
		for (BufferReportListener listener : mBufferReportListeners) {
			listener.bufferReport(bufferReport);
		}
	}

	public boolean advance() {

		try {
			mCurrentFrame = mFrames.take();
			Frame head = mFrames.peek();
			if (head != null) {
				mHeadTimestamp = head.getFrameTime();
			} else {
				mHeadTimestamp = 0;
				mTailTimestamp = 0;
			}
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}

	}

	public void put(Frame frame) {
		// Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Put, frame time: " + frame.getFrameTime());


			try {

				mFrames.put(frame);

				mTailTimestamp = frame.getFrameTime();
				if (mFrames.size() == 0) {
					mHeadTimestamp = frame.getFrameTime();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}



	}

	public int getStreamIndex() {
		return mCurrentFrame.getStreamIndex();
	}

	public long getSampleTime() {
		return mCurrentFrame.getFrameTime();
	};

	public int readSampleData(ByteBuffer byteBuffer, int offset) {

		int dataLength = -1;

		Frame frame = mCurrentFrame;
		MatroskaBlock mb = frame.getBlock();

		if (mb != null) {
			byte[] data = mb.getData();
			dataLength = (int) mb.getSize();

			if (byteBuffer != null) {
				byteBuffer.clear();
				byteBuffer.put(data, 4, dataLength - 4);
			}
		}

		return (int) dataLength;

	}

	private class BufferFedder implements Runnable {

		private Thread thisThread;

		public void start() {
			if (thisThread == null) {
				thisThread = new Thread(this, "Buffer Fedder");
				thisThread.setDaemon(true);
				thisThread.start();
			}
		}

		public void stop() {
			if (thisThread != null) {
				thisThread = null;
			}
		}

		@Override
		public void run() {

			while (Thread.currentThread() == thisThread) {

				int i = mCurrentFedderStream;

				Stream s = mVideoStreams[i];

				boolean segmentFinished = !s.advance();
				if (segmentFinished) {

					int next = mAdaptationManager.getNextSegmentTrack();
					mCurrentFedderStream = next;
					s = mVideoStreams[next];

					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Seek to stream: " + next);
					
//					if(mCurrentSegment == 19)
//						mCurrentSegment = 23;

					boolean finished = !s.seekTo(++mCurrentSegment, mBufferType);

					if (finished) {
						break;
					}

					continue;
				}

				MatroskaBlock mb = s.getCurrentBlock();

				Frame tail = new Frame(i, mb);
				put(tail);

			}

			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Buffer feeder finised");
		}

	}

	private class ReportTimerTask extends TimerTask {

		@Override
		public void run() {
			BufferReport br = new BufferReport(mSize, mFrames.getMeasuredSize());
			fireBufferReport(br);

		}
	}

}
