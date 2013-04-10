package es.upv.comm.webm.dash.buffer;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ebml.matroska.MatroskaBlock;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.Frame;
import es.upv.comm.webm.dash.Stream;
import es.upv.comm.webm.dash.adaptation.AdaptationManager;

public class Buffer implements Debug {

	private Stream[] mVideoStreams;

	private int mSize;
	private int mMinSize;

	private int mHeadTimestamp;
	private int mTailTimestamp;

	private LinkedBlockingQueue<Frame> mFrames;
	private BufferFedder mBufferFedder;
	private final Lock mLock = new ReentrantLock();
	private final Condition mFull = mLock.newCondition();

	private int mCurrentFedderStream = 0;

	private int mCurrentSegment = -1;;
	private Frame mCurrentFrame;

	private HashSet<BufferReportListener> mBufferReportListeners = new HashSet<BufferReportListener>();

	private AdaptationManager mAdaptationManager;

	private Timer mReportTimer;

	public Buffer(Stream[] videoStreams, int size, int minSize, AdaptationManager adaptationManager) {

		mVideoStreams = videoStreams;

		mSize = size;
		mMinSize = minSize;

		mAdaptationManager = adaptationManager;

		mFrames = new LinkedBlockingQueue<Frame>();

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
	}

	public int getSize() {
		return mSize;
	}

	public int getMinSize() {
		return mMinSize;
	}

	public long getHeadTimestamp() {
		return mHeadTimestamp;
	}

	public long getTailTimestamp() {
		return mTailTimestamp;
	}

	public boolean addBufferReportListener(BufferReportListener listener) {
		return mBufferReportListeners.add(listener);
	}

	public boolean removeBufferReportListener(BufferReportListener listener) {
		return mBufferReportListeners.remove(listener);
	}

	public void fireBufferReport(BufferReport bufferReport) {
		for (BufferReportListener listener : mBufferReportListeners) {
			listener.bufferReport(bufferReport);
		}
	}

	public boolean advance() {

		try {

			try {
				mCurrentFrame = mFrames.take();
				Frame head = mFrames.peek();
				if (head != null) {
					mHeadTimestamp = head.getFrameTime();
				} else {
					mHeadTimestamp = 0;
					mTailTimestamp = 0;
				}
				mLock.lock();
				mFull.signal();
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}

		} finally {
			mLock.unlock();
		}

	}

	public void put(Frame frame) {
//		Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Put, frame time: " + frame.getFrameTime());

		mLock.lock();
		try {
			try {

				mFrames.put(frame);

				while (mSize <= (mTailTimestamp - mHeadTimestamp)) {
//					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Fedder Stopped");
					mFull.await();
				}

				mTailTimestamp = frame.getFrameTime();
				if (mFrames.size() == 0) {
					mHeadTimestamp = frame.getFrameTime();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} finally {
			mLock.unlock();
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
					s.seekTo(++mCurrentSegment);

					continue;
				}

				MatroskaBlock mb = s.getCurrentBlock();

				Frame tail = new Frame(i, mb);
				put(tail);

			}
		}

	}

	private class ReportTimerTask extends TimerTask {

		@Override
		public void run() {
			BufferReport br = new BufferReport(mSize, mHeadTimestamp, mTailTimestamp);
			fireBufferReport(br);

		}
	}

}
