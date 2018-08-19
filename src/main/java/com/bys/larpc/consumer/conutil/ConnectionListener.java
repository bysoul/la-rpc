package com.bys.larpc.consumer.conutil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

public class ConnectionListener implements ChannelFutureListener {

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        System.out.println("ConnectionListener:operationComplete");
        if (!channelFuture.isSuccess()) {
            System.out.println(Thread.currentThread());
            System.out.println("Reconnect");
            RpcClient.client.times++;
            if(RpcClient.client.times==5) {
                RpcClient.client.connected =false;
                return;
            }
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                        RpcClient.client.connect(loop);
                }
            }, 1L, TimeUnit.SECONDS);
        }
    }
}
