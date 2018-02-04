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

//        //将二级菜单放入到一级菜单中，key值为：list
//        for(int i=0;i<mylist.size();i++){
//            Record r=mylist.get(i);
//            String parentId=r.get("parent_id");
//            for(Map top:reList){
//                String idTop=(String)top.get("id");
//                String linkTop=(String)top.get("link");
//                if(parentId.equals(idTop)){
//                    List topList=(List)top.get("children");
//                    if(topList==null){
//                        topList=new ArrayList();
//                        top.put("children",topList);
//                    }
//                    Map map=r.getColumns();
//                    String link=(String)map.get("link");
//                    if("/".equals(link)){
//                        map.put("link",linkTop);
//                    }else {
//                        map.put("link", linkTop + link);
//                    }
//                    topList.add(map);
//                    mylist.remove(r);
//                    i--;
//                }else{
//                    List topList=(List)top.get("children");
//                    if(topList!=null && !topList.isEmpty()){
//                        sort(mylist,topList);
//                    }
//                }
//            }
//        }
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
}
