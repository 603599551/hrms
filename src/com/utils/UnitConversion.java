package com.utils;

import com.jfinal.plugin.activerecord.Record;
import easy.util.NumberUtils;
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
    public static int smallUnit2outUnit(int num,String unit,String unitBig,int unitNum,String boxAttr,int boxAttrNum,String outUnit){
        if(StringUtils.isBlank(unit)){
            throw new NullPointerException("unit不能为空！");
        }
        if(StringUtils.isBlank(outUnit)){
            throw new NullPointerException("outUnit不能为空！");
        }
        int reNum=0;
        if(StringUtils.isNotBlank(boxAttr) && outUnit.equals(boxAttr)){//如果“提货单位”与“装箱规格单位”相同
            reNum=(int)Math.ceil((double)num/(double)(unitNum*boxAttrNum));
        }else if(StringUtils.isNotBlank(unitBig) && outUnit.equals(unitBig)){
            reNum=(int)Math.ceil((double)num/(double)unitNum);
        }else if(StringUtils.isNotBlank(unit) && outUnit.equals(unit)){
            reNum=num;
        }else{
            throw new RuntimeException("没有与提货单位相匹配的转换单位！");
        }
        return reNum;
    }
    /**
     将提货单位的数量，换算成最小单位的数量
     如果提货单位是装箱单位，那么提货单位换算成最小单位的公式是：num*unitNum*boxAttrNum
     如果提货单位是大单位，那么提货单位换算成最小单位的公式是：num*unitNum
     * @param num                要换算的数量（提货单位）
     * @param unit               小单位
     * @param unitBig           大单位
     * @param unitNum           小单位换算成大单位的数值
     * @param boxAttr           装箱单位（有可能为空）
     * @param boxAttrNum        大单位换算成装箱单位的数值
     * @param outUnit           提货单位
     * @return
     */
    public static int outUnit2SmallUnit(int num,String unit,String unitBig,int unitNum,String boxAttr,int boxAttrNum,String outUnit){
        if(StringUtils.isBlank(unit)){
            throw new NullPointerException("unit不能为空！");
        }
        if(StringUtils.isBlank(outUnit)){
            throw new NullPointerException("outUnit不能为空！");
        }
        int reNum=0;
        if(StringUtils.isNotBlank(boxAttr) && outUnit.equals(boxAttr)){//如果“提货单位”与“装箱规格单位”相同
            reNum=num*unitNum*boxAttrNum;
        }else if(outUnit.equals(unitBig)){
            reNum=num*unitNum;
        }else if(outUnit.equals(unit)){
            reNum=num;
        }else{
            throw new RuntimeException("没有与提货单位相匹配的转换单位！");
        }
        return reNum;
    }


    /**
     * 根据出库单位返回规格（该规格的最大单位是出库单位）<br/>
     * 从record中取出如下数据：out_unit,box_attr,box_attr_num,unit_big,unit,unit_num
     * 如：提货单位是箱，返回规格是：100袋/箱
     * @return
     */
    public static String getAttrByOutUnit(Record r){
        String outUnit=r.getStr("out_unit");//出库单位
        String boxAttr=r.getStr("box_attr");//装箱单位
        Object boxAttrNumObj=r.get("box_attr_num");//大单位换算成箱的数值
        String unitBig=r.getStr("unit_big");//大单位
        String unit=r.getStr("unit");//最小单位
        Object unitNumObj=r.getStr("unit_num");//小单位换算成大单位的数值

        int boxAttrNum= NumberUtils.parseInt(boxAttrNumObj,0);
        int unitNum= NumberUtils.parseInt(unitNumObj,0);

        return getAttrByOutUnit(unit,unitNum,unitBig,boxAttrNum,boxAttr,outUnit);
    }
    /**
     * 根据提货单位返回规格（该规格的最大单位是出库单位）<br/>
     * 如：提货单位是箱，返回规格是：100袋/箱
     *
     * @return
     */
    public static String getAttrByOutUnit(String unit,int unitNum,String unitBig,int boxAttrNum,String boxAttr,String outUnit){
        if(StringUtils.isBlank(unit)){
            throw new NullPointerException("unit不能为空！");
        }
        if(StringUtils.isBlank(outUnit)){
            throw new NullPointerException("outUnit不能为空！");
        }

        String attribute2="";
        if(outUnit.equals(boxAttr)){
            attribute2=boxAttrNum+unitBig+"/"+boxAttr;
        }else if(outUnit.equals(unitBig)){
            attribute2=unitNum+unit+"/"+unitBig;
        }else if(outUnit.equals(unit)){
            attribute2=unit;
        }else{
            throw new RuntimeException("没有与提货单位相匹配的转换单位！");
        }
        return attribute2;
    }
}
