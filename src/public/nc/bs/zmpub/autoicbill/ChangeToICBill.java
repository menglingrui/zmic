package nc.bs.zmpub.autoicbill;

import nc.bs.pub.pf.PfUtilTools;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.vosplit.SplitBillVOs;

/**
 * ת����
 */

public class ChangeToICBill {
	
	
	
	/**
	 * 
	 * @author zhf
	 * @˵������������ҵ��voת�����ɿ�浥��vo
	 * 2011-9-8����01:59:40
	 * @param billVO  ��ת���ĵ���vo
	 * @param sVoName ��ת������vo����
	 * @param sHeadVoName ��ת������vo��ͷ����
	 * @param sBodyVoName ��ת������vo��������
	 * @param saHeadKey ��ת������vo �ֵ���ͷ�����ֶ�
	 * @param saBodyKey ��ת������vo �ֵ����������ֶ�
	 * @param ctarbilltype Ŀ�굥������
	 * @param paraVo ����vo
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
			throw new BusinessException("Ŀ�굥������Ϊ��");
		
		String csoucebilltype = paraVo.m_billType;
		if(PuPubVO.getString_TrimZeroLenAsNull(csoucebilltype)==null)
			throw new BusinessException("��ת�����ݡ��������͡�Ϊ��");

		//�ֵ�
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
