package es.upv.comm.webm.dash.buffer;

public class BufferReport {

	private int mSize;

	private long mHeadTime;
	private long mTailTime;

	public BufferReport(int size, long headTime, long tailTime) {

		mSize = size;
		mHeadTime = headTime;
		mTailTime = tailTime;

	}

	public int getBufferUsage() {
		int aux = (int) (mTailTime - mHeadTime);
		int usage = (aux * 100) / mSize;
		return usage;
	}

}
