package es.upv.comm.webm.dash.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.transform.stream.StreamSource;

import android.os.SystemClock;
import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.Stream;

public class AsyncInputStream5 extends InputStream implements Debug, Runnable {

	private Thread thisThread;

	private BufferedInputStream bis;
	private final byte[] buffer;
	private final int size;
	private boolean eof = false;

	private int wIndex;
	private int rIndex;

	final Lock lock = new ReentrantLock();
	final Condition notEmpty = lock.newCondition();

	public AsyncInputStream5(InputStream inputStream, int size) {
		Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Downloading size: " + size);

		this.size = size;
		this.buffer = new byte[this.size];
		bis = new BufferedInputStream(inputStream);

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
	public void run() {
		lock.lock();
		try {

			long l1;
			long l2;

			l1 = SystemClock.elapsedRealtime();

			int readed = 0;
			while (readed != size && !eof) {
				try {

					readed += bis.read(buffer, readed, size - readed);
					wIndex = readed;
					notEmpty.signal();

				} catch (IOException e) {
					e.printStackTrace();
					eof = true;
				}

			}

			l2 = SystemClock.elapsedRealtime();

			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Downloaded: " + readed);
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Download Time: " + (l2 - l1));

		} finally {
			lock.unlock();
		}

	}

	@Override
	public int read() throws IOException {

		lock.lock();
		try {

			if (rIndex == size - 1) {
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "*************++++++++++++++++++++++++++++++");
				return -1;
			}

			while (rIndex == wIndex) {

				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			int r = buffer[rIndex++] & 0xff;
			return r;

		} finally {
			lock.unlock();
		}

	}
}
