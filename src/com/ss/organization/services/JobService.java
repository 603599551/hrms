package com.ss.organization.services;

import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobService {
    static JobService me=new JobService();

    public static JobService getMe() {
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
        //将二级菜单放入到一级菜单中，key值为：list
        for(int i=0;i<mylist.size();i++){
            Record r=mylist.get(i);
            String parentId=r.get("parent_id");
            for(Map top:reList){
                String idTop=(String)top.get("id");
                String linkTop=(String)top.get("link");
                if(parentId.equals(idTop)){
                    List topList=(List)top.get("children");
                    if(topList==null){
                        topList=new ArrayList();
                        top.put("children",topList);
                    }
                    Map map=r.getColumns();
                    String link=(String)map.get("link");
                    if("/".equals(link)){
                        map.put("link",linkTop);
                    }else {
                        map.put("link", linkTop + link);
                    }
                    topList.add(map);
                    mylist.remove(r);
                    i--;
                }else{
                    List topList=(List)top.get("children");
                    if(topList!=null && !topList.isEmpty()){
                        sort(mylist,topList);
                    }
                }
            }
        }
    }
}
