package es.upv.comm.webm.dash.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.SystemClock;
import android.util.Log;
import es.upv.comm.webm.dash.Debug;

public class AsyncInputStream extends InputStream implements Debug, Runnable {

	private Thread thisThread;

	private InputStream is;
	private byte[] data;

	private boolean eof = false;
	
	private int mStreamIndex;
	private NetworkSpeedListener mNetworkSpeedListener;
	

	private int isIndex;
	private int index;

	final Lock lock = new ReentrantLock();
	final Condition notEmpty = lock.newCondition();

	public AsyncInputStream(InputStream inputStream, int size, int streamIndex, NetworkSpeedListener networkSpeedListener) {
		is = new BufferedInputStream( inputStream);
		data = new byte[size + 1];
		
		mStreamIndex = streamIndex;
		mNetworkSpeedListener = networkSpeedListener;
	}

	public void start() {
		if (thisThread == null) {
			thisThread = new Thread(this);
			thisThread.setName("AsyncInputStream");
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
	public int read() throws IOException {
		byte[] buff = new byte[1];
		int count = read(buff);
		if (count == 1) {
			return buff[0];
		} else {
			return -1;
		}
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int count = -1;

		lock.lock();
		try {

			if (index == data.length - 1) {
				return -1;
			}

			if (isIndex == data.length - 1 && length > isIndex - index) {
				return -1;
			}

			while (index == isIndex) {
//				Log.d(LOG_TAG, HttpUtils.class.getSimpleName() + ": " + "await................................");

				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Log.d(LOG_TAG, HttpUtils.class.getSimpleName() + ": " + "Read................................." + offset + " " + length);

			count = isIndex - index;
			if (length < count) {
				count = length;
			}

			System.arraycopy(data, index, buffer, offset, count);
			index += count;

		} finally {
			lock.unlock();
		}

		return count;
	}

	@Override
	public void run() {

		try {

			int readed = 0;
			boolean endReached = false;
			
			float speed = -1;
			long t1 = SystemClock.elapsedRealtime();
			while (readed < data.length && !endReached) {
				lock.lock();
				try {
					int thisRead = is.read(data, readed, data.length - readed);
					if (thisRead > 0) {
						readed = readed + thisRead;
						isIndex = readed - 1;

					} else {
						endReached = true;
					}

					notEmpty.signal();
				} finally {
					lock.unlock();
				}

			}
			long t2 = SystemClock.elapsedRealtime();

			long ms = t2 - t1;

			 speed = (float) readed / ((float) ms / (float) 1000l);
			
			mNetworkSpeedListener.networkSpeed(0, mStreamIndex, speed);

			eof = true;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public long skip(long byteCount) throws IOException {
		Log.d(LOG_TAG, HttpUtils.class.getSimpleName() + ": " + "Skip.................................");

		byte[] buff = new byte[(int) byteCount];
		int count = read(buff);

		return count;
	}

}
