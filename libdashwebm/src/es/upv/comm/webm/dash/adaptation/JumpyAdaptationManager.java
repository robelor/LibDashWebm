package es.upv.comm.webm.dash.adaptation;

public class JumpyAdaptationManager implements AdaptationManager {
	
	@Override
	public int getFirstSegmentTrack(){
		return 0;
	}
	
	int i;
	
	@Override
	public int getNextSegmentTrack(){
		return i++ % 3;
//		return 0;
	}

}
