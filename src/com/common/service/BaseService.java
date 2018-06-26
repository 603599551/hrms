package com.common.service;

public abstract class BaseService {

    protected int nextSort(int sort){
        int i=sort;
        while(true){
            i++;
            if(i%10==0){
                break;
            }
        }
        return i;
    }

}
