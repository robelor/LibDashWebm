package es.upv.comm.webm.dash.container.segment.cluster;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaBlock;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.ParseException;
import es.upv.comm.webm.dash.util.HexByteArray;

public class Cluster implements Debug {
	

	private Element mElement;
	private EBMLReader mEbmlReader;
	private DataSource mDataSource;

	private int mTimeCode;

	public int getTimeCode() {
		return mTimeCode;
	}

	public void setTimeCode(int timeCode) {
		mTimeCode = timeCode;
	}

	public Cluster(DataSource dataSource) {
		this(new EBMLReader(dataSource, MatroskaDocType.obj), dataSource);
	}

	private Cluster(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Cluster_Id)) {
			init(rootElement, ebmlReader, dataSource);
		} else {
			throw new ParseException("Error: This is not a Cluster");  
		}

	}

	public Cluster(Element clusterElement, EBMLReader ebmlReader, DataSource dataSource) {
		init(clusterElement, ebmlReader, dataSource);
	}

	private void init(Element clusterElement, EBMLReader ebmlReader, DataSource dataSource) {

		mElement = clusterElement;
		mEbmlReader = ebmlReader;
		mDataSource = dataSource;

		Element auxElement = ((MasterElement) mElement).readNextChild(mEbmlReader);

		if (auxElement != null) {
			if ( auxElement.equals(MatroskaDocType.ClusterTimecode_Id)) {
				auxElement.readData(mDataSource);
				int timeCode = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Cluster.class.getSimpleName() + ": " + "    TimeCode: " + timeCode);
				setTimeCode(timeCode);
			}

			auxElement.skipData(mDataSource);
		}else{
			// problemas
		}
	}

	public MatroskaBlock getNextBlock() {
		MatroskaBlock mb = null;

		Element auxElement = ((MasterElement) mElement).readNextChild(mEbmlReader);

		if (auxElement != null) {
			if (auxElement.equals(MatroskaDocType.ClusterSimpleBlock_Id)) {
				auxElement.readData(mDataSource);
				mb = ((MatroskaBlock) auxElement);
				mb.parseBlock();
				mb.setClusterTimeCode(mTimeCode);

//				if (D)
//					Log.d(LOG_TAG, Cluster.class.getSimpleName() + ": " + "    SimpleBlock Track: " + mb.getTrackNo());
//				if (D)
//					Log.d(LOG_TAG, Cluster.class.getSimpleName() + ": " + "    SimpleBlock Timecode: " + mb.getBlockTimecode());
//				if (D)
//					Log.d(LOG_TAG, Cluster.class.getSimpleName() + ": " + "    SimpleBlock Size: " + mb.getSize());
//				if (D & mb.isKeyFrame())
//					Log.d(LOG_TAG, Cluster.class.getSimpleName() + ": " + "    SimpleBlock is key frame ");
			} else {
				if (D)
					Log.d(LOG_TAG, Cluster.class.getSimpleName() + ": " + "    Unhandled element: " + HexByteArray.bytesToHex(auxElement.getType()));
			}

			auxElement.skipData(mDataSource);
		}

		return mb;

	}

}
