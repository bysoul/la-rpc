package com.bys.larpc.consumer.conutil;

import com.bys.larpc.rpcutil.Helper;
import com.bys.larpc.rpcutil.RpcProto;
import com.bys.larpc.rpcutil.ProtostuffUtil;
import com.google.protobuf.ByteString;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MessageProducer {
    public static byte[] encode(String requestMessage, int reqId , String className, Method method, Object[] args) {
        if (requestMessage.equals("RequestMessage")) {
            RpcProto.RequestMessage.Builder rBuilder = RpcProto.RequestMessage.newBuilder()
                    .setRequestId(reqId)
                    .setClassName(className)
                    .setMethodName(method.getName());
            int argc = args.length;
            Class[] typeClass=method.getParameterTypes();
            if (argc != 0) {
                ArrayList<String> types = new ArrayList<>();
                for (int i = 0; i < argc; i++) {
                    types.add(typeClass[i].toString());
                }
                rBuilder.addAllArgTypes(types);

                ArrayList<Byte> arguments = new ArrayList<>();
                //stuff序列化args
                Helper h = new Helper(args);
                byte[] a = ProtostuffUtil.serialize(h);
                rBuilder.setArgs(ByteString.copyFrom(a));
            }
            return rBuilder.build().toByteArray();
        }
        else
            return null;
    }

}
