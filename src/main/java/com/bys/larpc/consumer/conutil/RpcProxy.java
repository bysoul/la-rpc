package com.bys.larpc.consumer.conutil;


import com.bys.larpc.rpcutil.Helper;
import com.bys.larpc.rpcutil.RpcProto;
import com.bys.larpc.rpcutil.ProtostuffUtil;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import static java.lang.Thread.sleep;
import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

public class RpcProxy implements InvocationHandler {
    private Object result;
    private String name;
    public Object get(String name,Class nClass){
        this.name=name;
        return Proxy.newProxyInstance(nClass.getClassLoader(),new Class[]{nClass},this);
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //System.out.println("RpcProxy:invoke");
        //生成请求ID
        int requestId=(int)Thread.currentThread().getId();
        //生成请求byte[]
        byte[] requestArray = MessageProducer.encode("RequestMessage",requestId,name,method,args);
        //singleton
        if(RpcClient.client==null){
            synchronized (RpcClient.class){
                if(RpcClient.client==null)
                    RpcClient.client=new RpcClient();
            }
        }
        RpcClient.client.send(requestId,requestArray);
        //get from responseCache
        byte[] responseArray=RpcClient.client.get(requestId);
        
        RpcProto.ResponseMessage responseMessage=RpcProto.ResponseMessage.parseFrom(responseArray);
        //System.out.println(responseMessage);
        if(responseMessage.getStatus()){
            Helper result=ProtostuffUtil.deserialize(responseMessage.getResult().toByteArray(),Helper.class);
            return result.getResult();
        }
        else{
            System.out.println("Rpc failed.");
            return null;
        }
    }
}

