package com.store.order.services;

import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoodsAndGoodsTypeTreeService {
    static GoodsAndGoodsTypeTreeService me=new GoodsAndGoodsTypeTreeService();

    public static GoodsAndGoodsTypeTreeService getMe() {
        return me;
    }


    private List<Record> list;
    /**
     * 对菜单进行排序，将顶级菜单放入一个list中
     * 将子菜单放入list中，并将list放入父菜单中
     * @param list
     * @return
     */
    public List sort(List<Record> list){
        this.list=new ArrayList<>(list);
        List<Map> reList=new ArrayList();

        //查找根节点
        List<Record> rootList=query("0");
        if(rootList!=null && !rootList.isEmpty()){
            for(Record r:rootList){
                Map map=r.getColumns();
                reList.add(map);
                this.list.remove(r);
                _sort(map);
            }
        }

        return reList;
    }
    private void _sort(Map map){
        String id=(String)map.get("id");
        String linkTop=(String)map.get("link");
        List<Map> listInMap=(List<Map>)map.get("children");
        if(listInMap==null){
            listInMap=new ArrayList<Map>();
            map.put("children",listInMap);
        }

        List<Record> list=query(id);
        if(list!=null && !list.isEmpty()){
            for(Record r:list){
                Map mapTemp=r.getColumns();

                String link=(String)mapTemp.get("link");
                if("/".equals(link)){
                    mapTemp.put("link",linkTop);
                }else {
                    mapTemp.put("link", linkTop + link);
                }

                listInMap.add(mapTemp);
                this.list.remove(r);
                _sort(mapTemp);
            }
        }
    }
    private List<Record> query(String parentId){
        List<Record> reList=new ArrayList();
        for(Record r:list){
            String parentIdDb=r.get("parent_id");
            if(parentId.equals(parentIdDb)){
                reList.add(r);
            }
        }
        return reList;
    }

    public List addGoods2GoodsType(List<Map> goodsTypeList,List<Record> goodsList){
        List reList=new ArrayList(goodsTypeList);
        for(Map goodsTypeMap:goodsTypeList){
            goodsTypeMap.put("isShow",false);
            List<Map> listInMap=(List<Map>)goodsTypeMap.get("children");
            for(Map subGoodsTypeMap:listInMap){
//                subGoodsTypeMap.put("isEdit",false);
                goodsTypeMap.put("isShow",false);
                String typeId=(String)subGoodsTypeMap.get("id");
                String parentId=(String)subGoodsTypeMap.get("parent_id");

                List<Map> goodsListInMap=(List<Map>)subGoodsTypeMap.get("children");
                if(goodsListInMap==null){
                    goodsListInMap=new ArrayList<Map>();
                    subGoodsTypeMap.put("children",goodsListInMap);
                }

                for(Record goods:goodsList){
                    String type2IdOfGoods=goods.getStr("type_2");
                    if(typeId.equals(type2IdOfGoods)){
                        Map goodsMap=goods.getColumns();
                        goodsMap.put("isEdit",true);
                        goodsListInMap.add(goodsMap);

                    }
                }
            }

        }
        return reList;
    }
}
