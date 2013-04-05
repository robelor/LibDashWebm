package es.upv.comm.webm.dash;

import org.ebml.matroska.MatroskaBlock;

public class Frame {

	private int mTrack;
	private MatroskaBlock mBlock;

	public Frame(int track, MatroskaBlock block) {
		mTrack = track;
		mBlock = block;
	}
	
	public int getTrack() {
		return mTrack;
	}
	
	public MatroskaBlock getBlock() {
		return mBlock;
	}

}
