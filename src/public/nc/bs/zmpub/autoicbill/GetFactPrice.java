package nc.bs.zmpub.autoicbill;

import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.pu.PuPubVO;

public class GetFactPrice {

    public BaseDAO dao = null;

    public BaseDAO getBaseDao() {
        if (dao == null) {
            dao = new BaseDAO();
        }
        return dao;
    }
    
    public GetFactPrice(BaseDAO dao2){
    	super();
    	this.dao = dao2;
    }

    /**
     * @author zhw
     * @说明：（鹤岗矿业）根据计划项目找出库方式 2012-2-21下午02:27:22
     * @param cinvbasid
     * @param cbatchid
     * @throws BusinessException
     */
    public String getIOutWay(String pk_planproject) throws BusinessException {
    	if (PuPubVO.getString_TrimZeroLenAsNull(pk_planproject) == null)
    		return null;
    	String sql = "select ioutway from hg_planproject where pk_planproject = '" + pk_planproject
    	+ "' and isnull(dr,0)=0";
    	Object o = getBaseDao().executeQuery(sql, new ColumnProcessor());

    	if (o == null)
    		return null;
    	String string = null;
    	String str = PuPubVO.getString_TrimZeroLenAsNull(o);

    	if ("1".equalsIgnoreCase(str))
    		string = "合同价";
    	return string;
    }

    /**
     * 
     * @author zhf
     * @说明：（鹤岗矿业）根据计划项目 询价
     * 2011-10-17上午09:31:44
     * @param pk_planproject 计划项目id
     * @param cinvbasid 存货id
     * @param vbatch 批次号
     * @return
     * @throws Exception
     */
    public UFDouble getFactPrice(String pk_planproject, String cinvbasid, String vbatch) throws BusinessException {

    	if(PuPubVO.getString_TrimZeroLenAsNull(pk_planproject)==null||
    			PuPubVO.getString_TrimZeroLenAsNull(cinvbasid)==null||
    			PuPubVO.getString_TrimZeroLenAsNull(vbatch)==null){
    		return null;
    	}
    		
        String way = getIOutWay(pk_planproject);
        UFDouble pricedouble = UFDouble.ZERO_DBL;
        if ("合同价".equalsIgnoreCase(way)) {

        	//            if (cinvbasid == null || vbatch == null)
        	//                return new UFDouble(-1);
        	pricedouble = PuPubVO.getUFDouble_NullAsZero(callPurchasePactPrice(cinvbasid, vbatch));
        	return pricedouble;
        }
        return null; 
        
    }

    /**
     * @author zhw
     * @说明：（鹤岗矿业）查询采购订单价格  无税单价 2012-2-21下午02:27:22
     * @param cinvbasid
     * @param cbatchid
     * @throws BusinessException
     */
    public UFDouble callPurchasePactPrice(String cinvbasid, String vbatch) throws BusinessException {
    	if (PuPubVO.getString_TrimZeroLenAsNull(cinvbasid) == null
    			|| PuPubVO.getString_TrimZeroLenAsNull(vbatch) == null)
    		return null;

    	String sql = "select b.noriginalcurprice from po_order_b b inner join ic_general_b pi on pi.csourcebillbid = b.corder_bid "
    		+ " inner join  bd_invbasdoc bas on bas.pk_invbasdoc = pi.cinvbasid join ic_general_h h on h.cgeneralhid  = pi.cgeneralhid "
    		+ " where isnull(b.dr, 0) = 0 and isnull(pi.dr, 0) = 0 and isnull(bas.dr,0)=0  and isnull(h.dr,0)=0 "
    		+ " and pi.cinvbasid='" + cinvbasid + "' and pi.vbatchcode = '" + vbatch + "' and h.cbilltypecode  in('45','46','48','49','4A','4T') and h.pk_corp ='1002'";
    	Object o = getBaseDao().executeQuery(sql, new ColumnProcessor());

    	if (o == null)
    		return null;
    	UFDouble prices = null;
    	List ldata = (List) o;
    	if (ldata.size() == 0)
    		return null;
    	Object os = (Object) ldata.get(0);
    	prices = PuPubVO.getUFDouble_NullAsZero(os);
    	return prices;
    }

}
