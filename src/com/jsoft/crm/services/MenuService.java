package com.jsoft.crm.services;

import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuService {
    static MenuService me=new MenuService();

    public static MenuService getMe() {
        return me;
    }

    /**
     * 对菜单进行排序，将顶级菜单放入一个list中
     * 将子菜单放入list中，并将list放入父菜单中
     * @param list
     * @return
     */
    public List sort(List<Record> list){
        List<Record> mylist=new ArrayList(list);
        List<Map> reList=new ArrayList();
        //将一级菜单放入到reList中
        for(int i=0;i<mylist.size();i++){
            Record r=mylist.get(i);
            Map temp=r.getColumns();
            String parentId=r.get("PARENT_ID");
            if("0".equals(parentId)){
                reList.add(r.getColumns());
                mylist.remove(r);
                i--;
            }
        }
        //将二级菜单放入到一级菜单中，key值为：list
        for(int i=0;i<mylist.size();i++){
            Record r=mylist.get(i);
            String parentId=r.get("PARENT_ID");
            for(Map top:reList){
                String idTop=(String)top.get("id");
                if(parentId.equals(idTop)){
                    List topList=(List)top.get("list");
                    if(topList==null){
                        topList=new ArrayList();
                        top.put("list",topList);
                    }
                    topList.add(r.getColumns());
                }
            }
        }
        //没有子菜单的顶级菜单，去掉
        for(int i=0;i<reList.size();i++){
            Map r=reList.get(i);
            String parentId=(String)r.get("PARENT_ID");
            List subList=(List)r.get("list");
            if(subList==null || subList.isEmpty()){//如果子菜单的数量是0，那么将此顶级菜单删除
                reList.remove(i);
                i--;
            }
        }
        return reList;
    }
}
