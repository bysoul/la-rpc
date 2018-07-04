package com.bys.larpc.consumer.conutil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoop;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;

public class ConnectionListener implements ChannelFutureListener {

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            System.out.println(Thread.currentThread());
            System.out.println("Reconnect");
            RpcClient.client.times++;
            if(RpcClient.client.times==5) {
                RpcClient.client.flag=false;
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
