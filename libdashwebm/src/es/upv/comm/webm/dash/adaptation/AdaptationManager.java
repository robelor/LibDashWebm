package es.upv.comm.webm.dash.adaptation;

public class AdaptationManager {
	
	public int getFirstSegmentTrack(){
		return 1;
	}
	
	int i;
	
	public int getNextSegmentTrack(){
		return i++ % 2;
//		return 1;
	}

}
