package es.upv.comm.webm.dash.container;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.io.DataSource;
import org.ebml.io.InputStreamDataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.header.Header;
import es.upv.comm.webm.dash.container.segment.Segment;
import es.upv.comm.webm.dash.container.segment.cueing.Cues;
import es.upv.comm.webm.dash.http.HttpUtils;
import es.upv.comm.webm.dash.mpd.Representation;

public class Container implements Debug {

	private URL mContainerUrl;
	private Representation mRepresentation;
	
	private Header mHeader;
	private Segment mSegment;

	public Container(URL baseUrl, Representation representation) throws MalformedURLException {
		mRepresentation = representation;
		mContainerUrl = new URL(baseUrl + mRepresentation.getBaseUrl());
	}

	public void init() {
		InputStreamDataSource isds = null;

		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Init, URL:" + mContainerUrl+" "+mRepresentation.getInitRange());
		isds = new InputStreamDataSource(new ByteArrayInputStream(HttpUtils.readUrlRange(mContainerUrl, mRepresentation.getInitRange())));
		parseInitialization(isds);
		
		
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Index, URL:" + mContainerUrl+" "+mRepresentation.getIndexRange());
		isds = new InputStreamDataSource(new ByteArrayInputStream(HttpUtils.readUrlRange(mContainerUrl, mRepresentation.getIndexRange())));
		parseIndex(isds);
		
	}
	
	public URL getContainerUrl() {
		return mContainerUrl;
	}

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

	public void parseInitialization(DataSource dataSource) {

		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		Element rootElement = reader.readNextElement();

		if (rootElement == null) {
			throw new java.lang.RuntimeException("Error: Unable to scan for EBML elements");
		} else {

			if (rootElement.equals(MatroskaDocType.EBMLHeader_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "Parsing Header...");
				setHeader(Header.create(rootElement, reader, dataSource));
			}

			rootElement = reader.readNextElement();

			if (rootElement.equals(MatroskaDocType.Segment_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "Parsing Segment...");
				setSegment(Segment.create(rootElement, reader, dataSource));
			}

			rootElement = reader.readNextElement();
		}

	}
	
	public void parseIndex(DataSource dataSource){
		if (getSegment() != null) {
			getSegment().setCues(Cues.create(dataSource));
		}
	}

}
