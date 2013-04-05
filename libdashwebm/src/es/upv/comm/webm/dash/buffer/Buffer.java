package es.upv.comm.webm.dash.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.ebml.matroska.MatroskaBlock;

import es.upv.comm.webm.dash.Frame;
import es.upv.comm.webm.dash.Stream;

public class Buffer {

	private ArrayList<Stream> mVideoStreams;

	private int mSize;
	private int mMinSize;

	private long mHeadTimestamp;
	private long mTailTimestamp;

	private LinkedBlockingQueue<Frame> mFrames;
	private BufferFedder mBufferFedder;

	private int mCurrentFedderStream = 0;

	private Frame mCurrentFrame;

	private HashSet<BufferReportListener> mBufferReportListeners = new HashSet<BufferReportListener>();

	public Buffer(ArrayList<Stream> videoStreams, int size, int minSize) {
		mVideoStreams = videoStreams;

		mSize = size;
		mMinSize = minSize;

		mFrames = new LinkedBlockingQueue<Frame>();
		mBufferFedder = new BufferFedder();
		mBufferFedder.start();
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
			mCurrentFrame = mFrames.take();
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public int getTrack() {
		return mCurrentFrame.getTrack();
	}

	public int readSampleData(ByteBuffer byteBuffer, int offset) {

		int dataLength = -1;

		try {
			Frame frame = mFrames.take();
			MatroskaBlock mb = frame.getBlock();

			if (mb != null) {
				byte[] data = mb.getData();
				dataLength = (int) mb.getSize();

				if (byteBuffer != null) {
					byteBuffer.clear();
					byteBuffer.put(data, 4, dataLength - 4);
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
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

				Stream s = mVideoStreams.get(mCurrentFedderStream);
				if (s != null) {
					// s.readSampleData(byteBuffer, offset)
					s.advance();
				}

			}

		}
	}

}
