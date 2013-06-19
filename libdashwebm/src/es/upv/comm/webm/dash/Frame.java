package es.upv.comm.webm.dash;

import org.ebml.matroska.MatroskaBlock;

import es.upv.comm.webm.dash.buffer.MeasurableBufferElement;

public class Frame implements MeasurableBufferElement {

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

	@Override
	public int measure() {
		return getFrameTime();
	}

}
