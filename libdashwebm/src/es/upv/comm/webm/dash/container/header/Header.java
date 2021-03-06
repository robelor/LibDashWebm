package es.upv.comm.webm.dash.container.header;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.StringElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.ParseException;

public class Header implements Debug {

	private String mDocType;

	public Header() {
	}

	public String getDocType() {
		return mDocType;
	}

	public void setDocType(String docType) {
		mDocType = docType;
	}

	public static Header create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Header create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.EBMLHeader_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Header create(Element headerElement, EBMLReader ebmlReader, DataSource dataSource) {
		Header header = new Header();

		Element auxElement = ((MasterElement) headerElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			
			if (auxElement.equals(MatroskaDocType.DocType_Id)) {
				auxElement.readData(dataSource);
				String docType = ((StringElement) auxElement).getValue();
				if (docType.compareTo("matroska") != 0 && docType.compareTo("webm") != 0) {
					throw new ParseException("It is not a webm/matroska type");
				} else {
					if(D)
						Log.d(LOG_TAG, Container.class.getSimpleName()+": "+"  DocType: "+ docType);
					header.setDocType(docType);
				}
			}
			
			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) headerElement).readNextChild(ebmlReader);
		}

		return header;
	}
}
