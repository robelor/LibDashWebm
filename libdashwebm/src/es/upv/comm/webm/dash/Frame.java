package es.upv.comm.webm.dash;

import org.ebml.matroska.MatroskaBlock;

public class Frame {

	private int mStreamIndex;
	private MatroskaBlock mBlock;

	public Frame(int streamIndex, MatroskaBlock block) {
		mStreamIndex = streamIndex;
		mBlock = block;
	}
	
	public int getStreamIndex() {
		return mStreamIndex;
	}
	
	public MatroskaBlock getBlock() {
		return mBlock;
	}
	
	public int getFrameTime(){
		return mBlock.getSampleTime();
	}

}
