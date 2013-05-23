package es.upv.comm.webm.dash.http;

public class UrlRangeDownload{
	private byte[] mBuffer;
	private float mSpeed;
	
	public UrlRangeDownload(byte[] buffer, float speed) {
		mBuffer = buffer;
		mSpeed = speed;
	}
	
	public byte[] getBuffer() {
		return mBuffer;
	}
	
	public float getSpeed() {
		return mSpeed;
	}
}
