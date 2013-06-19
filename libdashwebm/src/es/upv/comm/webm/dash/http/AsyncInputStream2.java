package es.upv.comm.webm.dash.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;

public class AsyncInputStream2 extends InputStream implements Debug, Runnable {

	private Thread thisThread;

	private InputStream is;
	private byte[] data;

	private boolean eof = false;

	private int isIndex;
	private int index;

	final Lock lock = new ReentrantLock();
	final Condition notEmpty = lock.newCondition();

	public AsyncInputStream2(InputStream inputStream, int size) {
		is = inputStream;
		data = new byte[size];
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
		lock.lock();
		try {
			while (index == isIndex) {
				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			lock.unlock();
		}
		if (index < data.length) {
			return data[index++];
		} else {
			return -1;
		}

	}

	@Override
	public void run() {
		while (thisThread == Thread.currentThread() && !eof) {

			try {

				int readed = 0;
				int partial = 0;

				// recorded sample
				while (readed != data.length && Thread.currentThread() == thisThread) {
					partial = is.read(data, readed, data.length - readed);

					readed += partial;

					isIndex = readed;

					if (partial != -1) {
						lock.lock();
						try {
							notEmpty.signal();
						} finally {
							lock.unlock();
						}
					}

				}
				eof = true;

			} catch (IOException e) {
				e.printStackTrace();
				eof = true;
			}

		}
	}

}
