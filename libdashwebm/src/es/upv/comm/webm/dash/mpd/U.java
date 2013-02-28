package es.upv.comm.webm.dash.mpd;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class U {
	
	public static String getValue(Element item, String str) {
		NodeList n = item.getElementsByTagName(str);
		return U.getElementValue(n.item(0));
	}
	
	public static final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	public static final String getAttribute(NamedNodeMap namedNodeMap, String attr) {
		Node n = namedNodeMap.getNamedItem(attr);
		if (n != null) {
			return n.getNodeValue();
		}

		return null;
	}

}
