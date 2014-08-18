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
 * @author zhf 矿级消耗系统 公共方法组件放置类
 *
 */

public class ZmPubTool  {
	private static nc.bs.pub.formulaparse.FormulaParse fp = new nc.bs.pub.formulaparse.FormulaParse();

	public static final Object execFomular(String fomular, String[] names,
			String[] values) throws BusinessException {
		fp.setExpress(fomular);
		if (names.length != values.length) {
			throw new BusinessException("传入参数异常");
		}
		int index = 0;
		for (String name : names) {
			fp.addVariable(name, values[index]);
			index++;
		}
		return fp.getValue();
	}
	// 暂时使用以下方式定义 步长
	public static final int STEP_VALUE = 10;
	public static final int START_VALUE = 10;
	/**
	 * 
	 * @author zhf
	 * @说明：（鹤岗矿业）对vo进行行号设置 2011-1-26下午03:34:51
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
	 * @说明：（鸡西矿业）对新生成的库存单据进行处理
	 * 2011-9-9下午03:25:45
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
	 * @说明：（鹤岗矿业）将为null的字符串处理为“”。 2010-11-22下午02:51:02
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
				//				设置是否批次管理
				fou_valus[0] = item.getCinventoryid();
				if(isLotMgtForInv(item.getCinventoryid(), inv_fomu, fou_names, fou_valus))
					item.setAttributeValue("isLotMgt", 1);//批次管理
				//				设置是否辅计量管理
			}
//		}		
	}
	
	/**
	 * 
	 * @author zhf
	 * @说明：（鸡西矿业）存货是否批次管理
	 * 2011-9-9下午05:21:14
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
