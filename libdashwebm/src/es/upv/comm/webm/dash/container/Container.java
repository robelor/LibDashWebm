package es.upv.comm.webm.dash.container;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.header.Header;
import es.upv.comm.webm.dash.container.segment.Segment;

public class Container implements Debug {

	private Header mHeader;
	private Segment mSegment;

	public Header getHeader() {
		return mHeader;
	}

	public void setHeader(Header header) {
		mHeader = header;
	}

	public Segment getSegment() {
		return mSegment;
	}

	public void setSegment(Segment segment) {
		mSegment = segment;
	}

	public static Container parse(DataSource dataSource) {
		Container container = new Container();

		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		Element rootElement = reader.readNextElement();

		if (rootElement == null) {
			throw new java.lang.RuntimeException("Error: Unable to scan for EBML elements");
		} else {

			if (rootElement.equals(MatroskaDocType.EBMLHeader_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "Parsing Header...");
				container.setHeader(Header.create(rootElement, reader, dataSource));
			}
			
			rootElement = reader.readNextElement();

			if (rootElement.equals(MatroskaDocType.Segment_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "Parsing Segment...");
				container.setSegment(Segment.create(rootElement, reader, dataSource));
			}
			
			rootElement = reader.readNextElement();
		}

		return container;
	}

}
