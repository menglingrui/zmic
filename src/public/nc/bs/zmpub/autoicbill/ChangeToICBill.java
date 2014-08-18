package nc.bs.zmpub.autoicbill;

import nc.bs.pub.pf.PfUtilTools;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.vosplit.SplitBillVOs;

/**
 * 转换类
 */

public class ChangeToICBill {
	
	
	
	/**
	 * 
	 * @author zhf
	 * @说明：（鸡西矿业）vo转换生成库存单据vo
	 * 2011-9-8下午01:59:40
	 * @param billVO  待转换的单据vo
	 * @param sVoName 待转换单据vo类型
	 * @param sHeadVoName 待转换单据vo表头类型
	 * @param sBodyVoName 待转换单据vo表体类型
	 * @param saHeadKey 待转换单据vo 分单表头依据字段
	 * @param saBodyKey 待转换单据vo 分单表体依据字段
	 * @param ctarbilltype 目标单据类型
	 * @param paraVo 参量vo
	 * @return
	 * @throws Exception
	 */
	public static AggregatedValueObject[] transAndGenBillVO(AggregatedValueObject billVO,
			Class sVoClass,
			Class sHeadVoClass, 
			Class sBodyVoClass,
			String[] saHeadKey,
			String[] saBodyKey,
			String ctarbilltype,
			PfParameterVO paraVo) throws BusinessException {
		
		if(billVO==null){
			return null;
		}
		
		if(PuPubVO.getString_TrimZeroLenAsNull(ctarbilltype)==null)
			throw new BusinessException("目标单据类型为空");
		
		String csoucebilltype = paraVo.m_billType;
		if(PuPubVO.getString_TrimZeroLenAsNull(csoucebilltype)==null)
			throw new BusinessException("待转换单据【单据类型】为空");

		//分单
		AggregatedValueObject[] vos = null;
		if(saBodyKey != null && saBodyKey != null)
		vos = SplitBillVOs.getSplitVO(
				sVoClass.getName(), sHeadVoClass.getName(),sBodyVoClass.getName(),				
				billVO, saHeadKey, saBodyKey);
		else{
			vos = new AggregatedValueObject[1];
			vos[0] = billVO;
		}
			
		
		//				
		AggregatedValueObject[] vo = PfUtilTools.runChangeDataAry(csoucebilltype,ctarbilltype,vos,paraVo); 
		return vo;
	}
}
