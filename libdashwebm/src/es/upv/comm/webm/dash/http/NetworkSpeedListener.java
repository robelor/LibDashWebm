package es.upv.comm.webm.dash.http;

public interface NetworkSpeedListener {
	
	/**
	 * 
	 * @param streamIndex
	 * @param index
	 * @param speed in bytes per second
	 */
	public void networkSpeed(int streamIndex, int index, float speed);

}
