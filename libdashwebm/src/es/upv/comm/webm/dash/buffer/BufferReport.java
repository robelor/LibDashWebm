package es.upv.comm.webm.dash.buffer;

public class BufferReport {

	private int mSize;

	private int mHeadTime;
	private int mTailTime;

	public BufferReport(int size, int headTime, int tailTime) {

		mSize = size;
		mHeadTime = headTime;
		mTailTime = tailTime;

	}

	public int getBufferUsage() {
		int aux = (int) (mTailTime - mHeadTime);
		int usage = (aux * 100) / mSize;
		return usage;
	}

	@Override
	public String toString() {
		return "Size: " + mSize + "  Head time: " + mHeadTime + "  Tail time: " + mTailTime + " Buffer%: " + getBufferUsage();
	}

}
