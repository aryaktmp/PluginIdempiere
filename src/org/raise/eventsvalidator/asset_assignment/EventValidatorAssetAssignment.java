package org.raise.eventsvalidator.asset_assignment;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAsset;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;
import org.raise.model.asset_assignment.MAssetAssignment;
import org.raise.model.asset_assignment.MAssetAssignmentLine;

public class EventValidatorAssetAssignment extends AbstractEventHandler{

	CLogger log = CLogger.getCLogger(EventValidatorAssetAssignment.class);
	private int A_Asset_ID;
	private int AD_User_ID;
	@Override
	protected void doHandleEvent(Event event) {
		// TODO Auto-generated method stub
		if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			PO po = getPO(event);
			A_Asset_ID = po.get_ValueAsInt("A_Asset_ID");
			AD_User_ID = po.get_ValueAsInt("AD_User_ID");
			log.warning("~~~Topic : " + event.getTopic() + "~~~PO : " + po);
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)) {
			PO po = getPO(event);
			A_Asset_ID = po.get_ValueAsInt("A_Asset_ID");
			AD_User_ID = po.get_ValueAsInt("AD_User_ID");
			log.warning("~~~Topic : " + event.getTopic() + "~~~PO : " + po);
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			PO po = getPO(event);
			MAsset asset = new MAsset(null, A_Asset_ID, null);
			if(asset.get_ValueAsBoolean("isAssigned") == true){
				po.set_ValueOfColumn("DocStatus", MAssetAssignment.DOCSTATUS_Drafted);
				po.set_ValueOfColumn("DocAction", MAssetAssignment.DOCACTION_Complete);
				po.set_ValueOfColumn("Processed", false);
				po.saveEx();
				throw new AdempiereException("Asset already assigned");
			}
			log.warning("~~~Event : " + event.getTopic() + "~~~PO : " + po);
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			PO po = getPO(event);
			MAsset asset = new MAsset(null, A_Asset_ID, null);
			po.set_ValueOfColumn("isAssigned", true);
			po.saveEx();
			asset.set_ValueOfColumn("isAssigned", true);
			asset.setAD_User_ID(AD_User_ID);
			asset.saveEx();
		}

	}

	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MAssetAssignment.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_COMPLETE, MAssetAssignment.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_NEW, MAssetAssignmentLine.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MAssetAssignmentLine.Table_Name);
	}

}
