package nc.vo.zmpub.ic.tool;

import nc.vo.ic.pub.bill.GeneralBillItemVO;
import nc.vo.ic.pub.bill.GeneralBillVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.constant.ScmConst;
import nc.vo.scm.pu.PuPubVO;

/**
 * 
 * @author zhf ������ϵͳ �����������������
 *
 */

public class ZmPubTool  {
	private static nc.bs.pub.formulaparse.FormulaParse fp = new nc.bs.pub.formulaparse.FormulaParse();

	public static final Object execFomular(String fomular, String[] names,
			String[] values) throws BusinessException {
		fp.setExpress(fomular);
		if (names.length != values.length) {
			throw new BusinessException("��������쳣");
		}
		int index = 0;
		for (String name : names) {
			fp.addVariable(name, values[index]);
			index++;
		}
		return fp.getValue();
	}
	// ��ʱʹ�����·�ʽ���� ����
	public static final int STEP_VALUE = 10;
	public static final int START_VALUE = 10;
	/**
	 * 
	 * @author zhf
	 * @˵�������׸ڿ�ҵ����vo�����к����� 2011-1-26����03:34:51
	 * @param voaCA
	 * @param sBillType
	 * @param sRowNOKey
	 */
	public static void setVOsRowNoByRule(
			CircularlyAccessibleValueObject[] voaCA, String sRowNOKey) {

		if (voaCA == null)
			return;
		int index = START_VALUE;
		for (CircularlyAccessibleValueObject vo : voaCA) {
			vo.setAttributeValue(sRowNOKey, String.valueOf(index));
			index = index + STEP_VALUE;
		}

	}
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ���������ɵĿ�浥�ݽ��д���
	 * 2011-9-9����03:25:45
	 * @param bill
	 */
	public static void dealIcGenBillVO(GeneralBillVO[] bills) throws BusinessException{
		for(GeneralBillVO bill:bills){
			dealIcGenBillVO(bill);
		}
	}
	/**
	 * 
	 * @author zhf
	 * @˵�������׸ڿ�ҵ����Ϊnull���ַ�������Ϊ������ 2010-11-22����02:51:02
	 * @param value
	 * @return
	 */
	public static String getString_NullAsTrimZeroLen(Object value) {
		if (value == null) {
			return "";
		}
		return value.toString().trim();
	}

	public static void dealIcGenBillVO(GeneralBillVO bill) throws BusinessException{
		if(bill == null)
			return;
		GeneralBillItemVO[] items = null;
	
			bill.getParentVO().setStatus(VOStatus.NEW);
			items = bill.getItemVOs();
			setVOsRowNoByRule(items, "crowno");
			if(items == null || items.length == 0)
				return;
			String inv_fomu = "wholemanaflag->getColValue(bd_invmandoc,wholemanaflag,pk_invmandoc,invman)";
			fou_names[0] = "invman";
//			String[] values = new String[1];
			for(GeneralBillItemVO item:items){
				item.setStatus(VOStatus.NEW);
				//				�����Ƿ����ι���
				fou_valus[0] = item.getCinventoryid();
				if(isLotMgtForInv(item.getCinventoryid(), inv_fomu, fou_names, fou_valus))
					item.setAttributeValue("isLotMgt", 1);//���ι���
				//				�����Ƿ񸨼�������
			}
//		}		
	}
	
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ������Ƿ����ι���
	 * 2011-9-9����05:21:14
	 * @param invmanid
	 * @param fomular
	 * @param names
	 * @param values
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isLotMgtForInv(String invmanid,String fomular,String[] names,String[] values)
	throws BusinessException{
		return PuPubVO.getUFBoolean_NullAs(execFomular(fomular, names, values), UFBoolean.FALSE).booleanValue();
	}
	
	public static String getIcBillSaveActionName(String icbilltype){
		if(icbilltype.equalsIgnoreCase(ScmConst.m_otherIn)
				||icbilltype.equalsIgnoreCase(ScmConst.m_otherOut)
				||icbilltype.equalsIgnoreCase(ScmConst.m_materialOut))
			return "WRITE";
		else
			return "PUSHSAVE";
	}


	
	private static String[] fou_names = new String[]{"no"};
	private static String[] fou_valus = new String[]{"no"};

	

}
