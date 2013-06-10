package es.upv.comm.webm.dash;

import java.util.Comparator;

public class StreamComparator implements Comparator<Stream> {

	@Override
	public int compare(Stream lhs, Stream rhs) {
		int li = Integer.parseInt(lhs.getRepresentation().getBandwidth());
		int ri = Integer.parseInt(rhs.getRepresentation().getBandwidth());
		
		return li - ri;

	}

}
