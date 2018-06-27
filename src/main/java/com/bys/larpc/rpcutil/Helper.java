package com.bys.larpc.rpcutil;

import java.io.Serializable;

public class Helper implements Serializable {
    Object[] objects;
    Object result;
    public Helper(){

    }

    public Helper(Object result) {
        this.result = result;
    }
    public Helper(Object[] o){
        this.objects=o;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object[] getObjects() {
        return objects;
    }


}
