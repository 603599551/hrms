package com.ss.services;

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
            String parentId=r.get("parent_id");
            if("0".equals(parentId)){
                reList.add(r.getColumns());
                mylist.remove(r);
                i--;
            }
        }
        sort(mylist,reList);
        return reList;
    }

    private void sort(List<Record> mylist,List<Map> reList){
        for(int i=0;i<mylist.size();i++){
            Record r=mylist.get(i);
            Map temp=r.getColumns();
            String parentId=r.get("parent_id");

            for(Map map :reList){
//                if(pa)
            }
        }
    }
}
