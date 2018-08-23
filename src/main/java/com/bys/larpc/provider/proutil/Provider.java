package com.bys.larpc.provider.proutil;

import com.bys.larpc.rpcutil.Helper;
import com.bys.larpc.rpcutil.ProtostuffUtil;
import com.bys.larpc.rpcutil.RpcProto;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Provider {
    public static volatile Provider provider;
    private ExecutorService executor;
    private AtomicInteger i;
    public Provider() {
        executor = Executors.newCachedThreadPool();
        i=new AtomicInteger(0);
    }
    public void callMethod(final Channel channel, final byte[] bytes){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                call(channel,bytes);
            }
        });
    }
    private void call(Channel channel, byte[] bytes){
        byte[] response=null;
        RpcProto.RequestMessage requestMessage=null;
        RpcProto.ResponseMessage.Builder respBuilder=RpcProto.ResponseMessage.newBuilder();
        try {
            requestMessage = RpcProto.RequestMessage.parseFrom(bytes);
            String className = requestMessage.getClassName();
            String methodName = requestMessage.getMethodName();
            List<String> argTypes = requestMessage.getArgTypesList();
            ByteString args = requestMessage.getArgs();
            byte[] actual = args.toByteArray();
            //stuff反序列化args
            Helper dehelper = ProtostuffUtil.deserialize(actual, Helper.class);
            //反射
            System.out.println("Provider: "+i.getAndIncrement()+" "+new Date(System.currentTimeMillis()) + ": " + className);

            Class clazz = null;
            try {
                clazz = Class.forName("com.bys.larpc.service." + className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            int n = argTypes.size();
            Class[] types = new Class[n];
            for (int i = 0; i < n; i++) {
                if (argTypes.get(i).equals("byte"))
                    types[i] = byte.class;
                else if (argTypes.get(i).equals("short"))
                    types[i] = short.class;
                else if (argTypes.get(i).equals("int"))
                    types[i] = int.class;
                else if (argTypes.get(i).equals("long"))
                    types[i] = long.class;
                else if (argTypes.get(i).equals("float"))
                    types[i] = float.class;
                else if (argTypes.get(i).equals("double"))
                    types[i] = double.class;
                else if (argTypes.get(i).equals("boolean"))
                    types[i] = boolean.class;
                else if (argTypes.get(i).equals("char"))
                    types[i] = char.class;
                else
                    types[i] = Class.forName(argTypes.get(i));
            }
            Object object = clazz.newInstance();
            Method m = clazz.getMethod(methodName, types);
            Object res = m.invoke(object, dehelper.getObjects());
            respBuilder
                    .setRequestId(requestMessage.getRequestId())
                    .setStatus(true);
            Helper enhelper = new Helper(res);
            byte[] result = ProtostuffUtil.serialize(enhelper);
            respBuilder.setResult(ByteString.copyFrom(result));
        }
        catch (Exception e){
            respBuilder
                    .setRequestId(requestMessage.getRequestId())
                    .setStatus(false);
            e.printStackTrace();
        }
        finally {
            response=respBuilder.build().toByteArray();
            ByteBuf responseMessage=Unpooled.directBuffer();
            responseMessage.writeInt(response.length+4);
            responseMessage.writeInt(requestMessage.getRequestId());
            responseMessage.writeBytes(response);
            channel.writeAndFlush(responseMessage);
        }
    }
}
