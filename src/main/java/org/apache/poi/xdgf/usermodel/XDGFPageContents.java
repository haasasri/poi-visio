package org.apache.poi.xdgf.usermodel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.xmlbeans.XmlException;

import com.microsoft.schemas.office.visio.x2012.main.PageContentsDocument;

public class XDGFPageContents extends XDGFBaseContents {

	Map<Long, XDGFMaster> _masters = new HashMap<>();
	XDGFPage _page;
	
	public XDGFPageContents(PackagePart part, PackageRelationship rel, XDGFDocument document) {
		super(part, rel, document);
	}

	@Override
	protected void onDocumentRead() {
		try {
			try {
				_pageContents = PageContentsDocument.Factory.parse(getPackagePart().getInputStream()).getPageContents();
			} catch (XmlException | IOException e) {
				throw new POIXMLException(e);
			}
			
			for (POIXMLDocumentPart part: getRelations()) {
				if (!(part instanceof XDGFMasterContents))
					throw new POIXMLException("Unexpected page relation: " + part);
				
				XDGFMaster master = ((XDGFMasterContents)part).getMaster();
				_masters.put(master.getID(), master);
			}
			
			super.onDocumentRead();
			
			for (XDGFShape shape: _shapes.values()) {
				if (shape.isTopmost())
					shape.setupMaster(this, null);
			}
		
		} catch (POIXMLException e) {
			throw XDGFException.wrap(this, e);
		}
	}

	public XDGFPage getPage() {
		return _page;
	}
	
	protected void setPage(XDGFPage page) {
		_page = page;
	}
	
	public XDGFMaster getMasterById(long id) {
		return _masters.get(id);
	}
}