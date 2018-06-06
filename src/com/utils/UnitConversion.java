package com.utils;

import org.apache.commons.lang.StringUtils;

/**
 * 单位换算
 * @author mym
 */
public class UnitConversion {


    /**
     将最小单位的数量，换算成提货单位
     如果提货单位是装箱单位，那么门店想要的数量（最小单位）换算成提货单位的公式是：wantNumOutUnit=门店想要数量/(大单位数量*装箱规格数量)，不能整除的+1
     如果提货单位是大单位，那么门店想要的数量（最小单位）换算成提货单位的公式是：wantNumOutUnit=门店想要数量/大单位数量，不能整除的+1
     * @param num                要换算的数量（最小单位）
     * @param unitBig           大单位
     * @param unitNum           小单位换算成大单位的数值
     * @param boxAttr           装箱单位
     * @param boxAttrNum        大单位换算成装箱单位的数值
     * @param outUnit           提货单位
     * @return
     */
    public static int smallUnit2outUnit(int num,String unitBig,int unitNum,String boxAttr,int boxAttrNum,String outUnit){

        int reNum=0;
        if(StringUtils.isNotBlank(boxAttr) && outUnit.equals(boxAttr)){//如果“提货单位”与“装箱规格单位”相同
            reNum=(int)Math.ceil((double)num/(double)(unitNum*boxAttrNum));
        }else if(outUnit.equals(unitBig)){
            reNum=(int)Math.ceil((double)num/(double)unitNum);
        }
        return reNum;
    }
    /**
     将提货单位的数量，换算成最小单位的数量
     如果提货单位是装箱单位，那么提货单位换算成最小单位的公式是：num*unitNum*boxAttrNum
     如果提货单位是大单位，那么提货单位换算成最小单位的公式是：num*unitNum
     * @param num                要换算的数量（提货单位）
     * @param unitBig           大单位
     * @param unitNum           小单位换算成大单位的数值
     * @param boxAttr           装箱单位（有可能为空）
     * @param boxAttrNum        大单位换算成装箱单位的数值
     * @param outUnit           提货单位
     * @return
     */
    public static int outUnit2SmallUnit(int num,String unitBig,int unitNum,String boxAttr,int boxAttrNum,String outUnit){

        int reNum=0;
        if(StringUtils.isNotBlank(boxAttr) && outUnit.equals(boxAttr)){//如果“提货单位”与“装箱规格单位”相同
            reNum=num*unitNum*boxAttrNum;
        }else if(outUnit.equals(unitBig)){
            reNum=num*unitNum;
        }
        return reNum;
    }
}
