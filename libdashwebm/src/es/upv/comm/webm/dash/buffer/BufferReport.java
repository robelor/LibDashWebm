package es.upv.comm.webm.dash.buffer;

public class BufferReport {

	private int mSize;

	private int mMeasuredSize;

	public BufferReport(int size, int measuredSize) {

		mSize = size;
		mMeasuredSize = measuredSize;

	}

	public int getBufferUsage() {
		int usage = (mMeasuredSize * 100) / mSize;
		return usage;
	}

	@Override
	public String toString() {
		return "Size: " + mSize + " Buffer%: " + getBufferUsage();
	}

}
