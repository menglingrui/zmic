package nc.bs.zmpub.autoicbill;
import java.sql.SQLException;
import java.util.ArrayList;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.ic.pub.IGeneralBill;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ui.scm.service.LocalCallService;
import nc.vo.ic.pub.bill.GeneralBillItemVO;
import nc.vo.ic.pub.bill.GeneralBillVO;
import nc.vo.ic.pub.bill.QryConditionVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.ValidationException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.constant.ScmConst;
import nc.vo.scm.constant.ic.InOutFlag;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.service.ServcallVO;
import nc.vo.trade.voutils.IFilter;
import nc.vo.trade.voutils.VOUtil;
import nc.vo.zmpub.ic.consts.ZmPubConst;
import nc.vo.zmpub.ic.tool.ZmPubTool;
/**
 *  �Զ���ʽ������浥��
 */
public class AutoIcBillBO  {

	private BaseDAO dao = null;
	private BaseDAO getDao(){
		if(dao == null){
			dao = new BaseDAO();
		}
		return dao;
	}
	
   

	public AutoIcBillBO(){
		super();
	}
	public AutoIcBillBO(BaseDAO dao){
		super();
		this.dao = dao;
	}

	
	
    /**
     * bs��ģ�����
     * @param paramVO
     * @return
     * @throws BusinessException 
     */
	public GeneralBillVO pick(GeneralBillVO bill,UFDate date) throws BusinessException {		
		GeneralBillItemVO[] items = bill.getItemVOs();		
			Class[] ParameterTypes = new Class[] { GeneralBillVO.class,UFDate.class};
		   Object[] ParameterValues = new Object[] {bill,date};
			Object o = null;
			try {
				bill = (GeneralBillVO) callRemoteService("ic", "nc.bs.ic.ic2a1.PickBillBO", "pickAutoForPushSave",
						ParameterTypes, ParameterValues,1);
			} catch (Exception e) {			
		}
			return bill;
		
	}
	public  Object callRemoteService(String modulename,
			String classname, String methodname, Class[] ParameterTypes,
			Object[] ParameterValues, int iCallPubServerType) throws Exception {
		ServcallVO[] scd = new ServcallVO[1];
		Object oret = null;
		scd[0] = new ServcallVO();
		scd[0].setBeanName(classname);
		scd[0].setMethodName(methodname);
		scd[0].setParameterTypes(ParameterTypes);
		scd[0].setParameter(ParameterValues);
		Object[] otemps = null;
		if (iCallPubServerType == 1) {
			otemps = LocalCallService.callEJBService(modulename, scd);
		} else if (iCallPubServerType == 2) {
			otemps = LocalCallService.callService(modulename, scd);
		}
		if (otemps != null && otemps.length > 0)
			oret = otemps[0];

		return oret;
	}

	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ�����µ���ִ��ָ������
	 * 2011-9-8����02:24:52
	 * @param date ��¼����
	 * @param billvos ����
	 * @param actionname ��ִ�еĶ���
	 * @throws BusinessException
	 */
	public void pushBillDoActions(String date, AggregatedValueObject[] billvos,String actionname) throws BusinessException {

		if(billvos == null || billvos.length == 0)
			return;
		String s_billtype = PuPubVO.getString_TrimZeroLenAsNull(billvos[0].getParentVO().getAttributeValue("cbilltypecode"));

		IPFBusiAction bsBusiAction = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
	    
		for(AggregatedValueObject billvo:billvos){
			bsBusiAction.processAction(actionname, s_billtype,date,null,billvo, null,null);
		}
	}
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ�����µ���ִ��ָ������
	 * 2011-9-8����02:24:52
	 * @param date ��¼����
	 * @param billvos ����
	 * @param actionname ��ִ�еĶ���
	 * @throws BusinessException
	 */
	public void pushBillDoAction(String date, AggregatedValueObject billvo,String actionname) throws BusinessException {

		if(billvo == null)
			return;
		String s_billtype = PuPubVO.getString_TrimZeroLenAsNull(billvo.getParentVO().getAttributeValue("cbilltypecode"));

		IPFBusiAction bsBusiAction = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		//		for(AggregatedValueObject billvo:billvos){
		bsBusiAction.processAction(actionname, s_billtype,date,null,billvo, null,null);
		//		}
	}

	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ��У���Ƿ�������ο�浥��
	 * 2011-9-9����08:40:30
	 * @param ID ��ǰҵ�񵥾�ͷID
	 * @param isFirst �Ƿ���Դͷ��Ϣ����
	 * @throws ValidationException
	 */
	public void checkExitNextBills(String ID,String icbilltype,boolean isFirst) throws BusinessException{
		if(PuPubVO.getString_TrimZeroLenAsNull(ID)==null)
			return;
		String sql = " select count(0) from ic_general_b b inner join ic_general_h h on h.cgeneralhid = b.cgeneralhid " +
		" where isnull(h.dr,0)=0 and isnull(b.dr,0)=0 and h.cbilltypecode = '"+icbilltype+"' " +
		" and b.csourcebillhid = '"+ID+"' and h.fbillflag = "+ZmPubConst.ic_sign_billstatus;
		int flag = PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor() ), 0);
		if(flag>0)
			throw new ValidationException("������ǩ�ֵ����γ��ⵥ��");
		if(!isFirst)
			return;
		sql = " select count(0) from ic_general_b b inner join ic_general_h h on h.cgeneralhid = b.cgeneralhid " +
		" where isnull(h.dr,0)=0 and isnull(b.dr,0)=0 and h.cbilltypecode = '"+icbilltype+"' " +
		" and b.cfirstbillhid = '"+ID+"' and h.fbillflag = "+ZmPubConst.ic_sign_billstatus;
		flag = PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), 0);
		if(flag>0)
			throw new ValidationException("������ǩ�ֵ�������ⵥ��");
	}

	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ���Ʋ�������ʱɾ�����ο�浥��
	 * 2011-9-9����09:35:59
	 * @param ID ҵ�񵥾�ID
	 *  @param date ��ǰ����
	 * @throws BusinessException
	 */
	public void deleteNextBillsForMove(String ID,String date,String cuser) throws BusinessException{
		if(PuPubVO.getString_TrimZeroLenAsNull(ID)==null)
			return;
		deleteNextBillByFirstID(ID, ScmConst.m_otherIn, date,cuser,false);
		deleteNextBillBySourceID(ID, ScmConst.m_otherOut, date,cuser,false);
	}
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ������Դͷ������Ϣɾ����浥��
	 * 2011-9-9����09:36:42
	 * @param ID
	 * @throws BusinessException
	 */
	public void deleteNextBillByFirstID(String ID,String icbilltype,String date,String cuser,boolean isCancelSign) throws BusinessException{

		//		�Ȳ�ѯ ���ýű�ɾ��
		GeneralBillVO[] bills = queryNextBillsByFirstID(ID, icbilltype);
		if(bills == null || bills.length == 0)
			return;
		for(GeneralBillVO bill:bills){
			bill.getHeaderVO().setCoperatoridnow(cuser);
		}
		if(isCancelSign){
			pushBillDoActions(date, bills, "CANCELSIGN");
			
			bills = queryNextBillsByFirstID(ID, icbilltype);
			if(bills == null || bills.length == 0)
				return;
			for(GeneralBillVO bill:bills){
				bill.getHeaderVO().setCoperatoridnow(cuser);
			}
		}
		checkExitNextBills(ID, icbilltype, true);
		pushBillDoActions(date, bills, "DELETE");		
	}

	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ���������ε�����Ϣɾ����浥��
	 * 2011-9-9����09:37:08
	 * @param ID
	 * @throws BusinessException
	 */
	public void deleteNextBillBySourceID(String ID,String icbilltype,String date,String cuser,boolean isCancelSign) throws BusinessException{
		//     �Ȳ�ѯ���ýű�ɾ��
		GeneralBillVO[] bills = queryNextBillsBySourceID(ID, icbilltype);
		if(bills == null || bills.length == 0)
			return;
		for(GeneralBillVO bill:bills){
			bill.getHeaderVO().setCoperatoridnow(cuser);
		}
		
		if(isCancelSign){
			pushBillDoActions(date, bills, "CANCELSIGN");
			
			bills = queryNextBillsBySourceID(ID, icbilltype);
			if(bills == null || bills.length == 0)
				return;
			for(GeneralBillVO bill:bills){
				bill.getHeaderVO().setCoperatoridnow(cuser);
			}
		}
		checkExitNextBills(ID, icbilltype, false);
		pushBillDoActions(date, bills, "DELETE");
	}
	
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ������ԴͷID��ѯ ��浥��
	 * 2011-9-9����09:50:40
	 * @param ID
	 * @return
	 * @throws BusinessException
	 */
	public GeneralBillVO[] queryNextBillsByFirstID(String ID,String icbilltype) throws BusinessException{
		GeneralBillVO[] bills = null;
		String where  = " cfirstbillhid = '"+ID+"' and cbilltypecode = '"+icbilltype+"'";
		QryConditionVO voCond = new QryConditionVO(where);
	    ArrayList alListData = (ArrayList)queryBills(icbilltype, voCond);
	    if(alListData == null || alListData.size() == 0)
	    	return null;
	    bills = new GeneralBillVO[alListData.size()];
	    alListData.toArray(bills);
		return bills;
	}
	
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ��������ԴID ��ѯ��浥��
	 * 2011-9-9����09:50:45
	 * @param ID
	 * @return
	 * @throws BusinessException
	 */
	public GeneralBillVO[] queryNextBillsBySourceID(String ID,String icbilltype) throws BusinessException{
		GeneralBillVO[] bills = null;
		String where  = " csourcebillhid = '"+ID+"' and cbilltypecode = '"+icbilltype+"'";
		QryConditionVO voCond = new QryConditionVO(where);
	    ArrayList alListData = (ArrayList)queryBills(icbilltype, voCond);
	    if(alListData == null || alListData.size() == 0)
	    	return null;
	    bills = new GeneralBillVO[alListData.size()];
	    alListData.toArray(bills);
		return bills;
	}

	private  String beanName = IGeneralBill.class.getName(); 

	public  ArrayList queryBills(String arg0 ,QryConditionVO arg1 ) throws BusinessException{
		IGeneralBill bo = (IGeneralBill)NCLocator.getInstance().lookup(beanName);    
		ArrayList o =  bo.queryBills(arg0 ,arg1 );					
		return o;
	}
	
	/**
	 * 
	 * @author zhf
	 * @˵�����Զ����ɳ���ⵥ
	 * 2011-9-10����08:51:28
	 * @param bill �����ɿ�浥�ݵ�ҵ������
	 * @param paraVo ƽ̨����  ���ڲ�������Դ�������͡����ڣ�
	 * @param icbilltype  ���ɵĿ�浥������
	 * @param classes	��ת������vo��������
	 * @param isAutoPick  ���ⵥ��ʱ�Ƿ��Զ��������
	 * @param isAutoSign  �Ƿ��Զ�ǩ��
	 * @param isRet �Ƿ񷵻������ɵĿ�浥����Ϣ
	 * @return
	 * @throws BusinessException
	 */
	public GeneralBillVO autoGenIcBill(
			AggregatedValueObject bill,
			PfParameterVO paraVo,
			String icbilltype,
			boolean isAutoPick,
			boolean isAutoSign,
			boolean isRet)
			throws BusinessException {
		return autoGenIcBill(bill, paraVo, icbilltype, null, null, null,
				isAutoPick, isAutoSign, isRet);
	}
	
	public GeneralBillVO autoGenIcBill(
			AggregatedValueObject bill,
			PfParameterVO paraVo,
			String icbilltype,
			Class[] classes,
			String[] saHeadKey,//�ֵ���ͷ����
			String[] saBodyKey,//�ֵ���������
			boolean isAutoPick,
			boolean isAutoSign,
			boolean isRet) throws BusinessException{
		
		AggregatedValueObject[] genvos = null;
		if(classes == null || classes.length == 0){
			genvos = ChangeToICBill.transAndGenBillVO(bill, null,
					null, 
					null, null, null, 
					icbilltype, paraVo);
		}else{
			genvos = ChangeToICBill.transAndGenBillVO(bill, classes[0],
					classes[1], 
					classes[2], saHeadKey, saBodyKey, 
					icbilltype, paraVo);
		}
		
		if(genvos == null || genvos.length == 0 || genvos.length>1)
			throw new BusinessException("����ת���쳣");

//		�Զ����
		GeneralBillVO newout = (GeneralBillVO)genvos[0];
		
		newout = doSaveAndSignICBill(newout, paraVo.m_currentDate, isAutoSign,isAutoPick,isRet);
		if(isRet)
			return newout;
		return null;
	}
	
	class nullNumFilter implements IFilter{

		private boolean isin = false;
		public nullNumFilter(boolean isin){
			this.isin = isin;
		}
		public boolean accept(Object o) {
			// TODO Auto-generated method stub
			GeneralBillItemVO item = (GeneralBillItemVO)o;
			if(item == null)
				return false;
			if(PuPubVO.getUFDouble_NullAsZero(isin?item.getNshouldinnum():item.getNshouldoutnum()).equals(UFDouble.ZERO_DBL)){
				return false;
			}
			return true;
		}
		
	}
	
	
	
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ���Զ������浥��  ��ϵͳ��ʽ����
	 * 2011-9-9����05:48:28
	 * @param bill ��浥��
	 * @param date ��ǰ����
	 * @param isAutoSign �Ƿ��Զ�ǩ��
	 * @param isRet �Ƿ񷵻ر����ĵ���
	 * @return
	 * @throws BusinessException
	 */
	public GeneralBillVO doSaveAndSignICBill(
			GeneralBillVO bill,
			String date,
			boolean isAutoSign,
			boolean isAutoPick,
			boolean isRet) 
	throws BusinessException{

		ZmPubTool.dealIcGenBillVO(bill);

		String ccuruser = bill.getHeaderVO().getCoperatoridnow();

		boolean isin = bill.getBillInOutFlag() == InOutFlag.IN;

		GeneralBillItemVO[] items = bill.getItemVOs();
		items = (GeneralBillItemVO[])VOUtil.filter(items, new nullNumFilter(isin));
		bill.setChildrenVO(items);
		
		if(PuPubVO.getString_TrimZeroLenAsNull(items[0].getVbatchcode())!=null){
			isAutoPick = false;
			for(GeneralBillItemVO item:items){
				item.setNoutnum(item.getNshouldoutnum());
				item.setNoutassistnum(item.getNshouldoutassistnum());
			}
		}

		if(isAutoPick){
			bill = pick(bill, new UFDate(date));
			items = bill.getItemVOs();
			String error = null;
			for(GeneralBillItemVO item:items){
				error = PuPubVO.getString_TrimZeroLenAsNull(item.getVnotebody());
				boolean isnullnum = PuPubVO.getUFDouble_NullAsZero(item.getNshouldoutnum()).doubleValue() == 0?true:false;
				if(error!=null && !isnullnum){
					throw new BusinessException("�к�"+ZmPubTool.getString_NullAsTrimZeroLen(item.getCrowno())+"���"+
							ZmPubTool.getString_NullAsTrimZeroLen(item.getInv().getCinventorycode())+error);
				}
			}
		}

		String s_billtype = PuPubVO.getString_TrimZeroLenAsNull(
				bill.getParentVO().getAttributeValue("cbilltypecode"));

		pushBillDoAction(date, bill, ZmPubTool.getIcBillSaveActionName(s_billtype));
		//		String sss=bill.getCurUserID();
		//		String ssss=sss;

		GeneralBillVO[] bills = null;

		String clastbillid = ZmPubTool.getString_NullAsTrimZeroLen(
				bill.getChildrenVO()[0].getAttributeValue("csourcebillhid"));

		if(isRet){
			bills = queryNextBillsBySourceID(clastbillid, s_billtype);
		}
		if(isAutoSign){
			//			���²�ѯ�� �������ⵥ	
			if(!isRet){
				bills = queryNextBillsBySourceID(clastbillid, s_billtype);
			}
			if(bills == null ||bills.length == 0 || bills.length>1){
				throw new BusinessException("���ݴ����쳣");
			}

			for(GeneralBillVO tbill:bills){
				tbill.getHeaderVO().setCoperatoridnow(ccuruser);
			}

			//			String s=bills[0].getCurUserID();
			//			String ss=s;

			pushBillDoActions(date, bills, "SIGN");
		}
		if(isRet)
			return bills[0];
		return null;
	}
	
	public String transIDs(String tarcorp, String sourceValue,
			String tablename, String basFiledName, String manFiledName,
			String basdocName) throws Exception {
		StringBuffer sqlb = new StringBuffer();
		sqlb.append("select ");
		sqlb.append(basFiledName);
		sqlb.append(" from ");
		sqlb.append(tablename);
		sqlb.append(" where ");
		sqlb.append(manFiledName);
		sqlb.append(" = '");
		sqlb.append(sourceValue);
		sqlb.append("'");
		Object o = getDao().executeQuery(sqlb.toString(), new ColumnProcessor());
		if (o == null) {
			throw new SQLException("��ѯ" + basdocName + "�����쳣");
		}
		sqlb = new StringBuffer();
		sqlb.append("select ");
		sqlb.append(manFiledName);
		sqlb.append(" from ");
		sqlb.append(tablename);
		sqlb.append(" where ");
		sqlb.append(basFiledName);
		sqlb.append(" = '");
		sqlb.append(PuPubVO.getString_TrimZeroLenAsNull(o));
		sqlb.append("' and pk_corp = '" + tarcorp + "'");
		o = getDao().executeQuery(sqlb.toString(), new ColumnProcessor());
		if (o == null) {
			throw new SQLException(basdocName + "�繫˾ת���쳣��δ���䵽Ŀ�깫˾"
					+ sqlb.toString());
		}

		return PuPubVO.getString_TrimZeroLenAsNull(o);
	}
}