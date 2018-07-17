package com.bys.larpc.consumer.conutil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProxyHandler extends
        SimpleChannelInboundHandler<ByteBuf> {
    private RpcClient rpcClient;
    private byte[] heartbeat;

    public ProxyHandler(RpcClient rpcClient) throws Exception{
        this.rpcClient = rpcClient;
        //init heartbeat
        ByteBuf heartbeat=Unpooled.directBuffer();
        byte ping=0;
        heartbeat.writeInt(1);
        heartbeat.writeByte(ping);
        this.heartbeat=new byte[heartbeat.readableBytes()];
        heartbeat.readBytes(this.heartbeat);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //remove channel connection-times
        rpcClient.times=0;
        System.out.println("ProxyHandler:new channel");
        rpcClient.setChannel(ctx.channel());
        //if reconnection is successful, resend requests.
        int cacheSize;
        if((cacheSize=rpcClient.requestCache.size())>0){
            for(Map.Entry<Integer,byte[]> entry:rpcClient.requestCache.entrySet()){
                try {
                    rpcClient.send(entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        /*ByteBuf firstMessage=Unpooled.directBuffer();
        firstMessage.writeInt(req.length);
        firstMessage.writeBytes(req);
        ctx.writeAndFlush(firstMessage);*/
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception{
        System.out.println("reading...");
        //reset heartbeat
        //rpcClient.timeout.set(0);
        rpcClient.times=0;

        ByteBuf buf=(ByteBuf) msg;
        if(buf.readableBytes()==1){
            System.out.println("get Pong");
        }
        else{
            int requestId=buf.readInt();
            //remove request bytes from requestCache
            rpcClient.removeRequest(requestId);
            byte[] resp=new byte[buf.readableBytes()];
            buf.readBytes(resp);
            rpcClient.setResult(requestId,resp);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                System.out.println("No message from server for 5s");
                RpcClient.client.times++;
                if(RpcClient.client.times==3){
                    RpcClient.client.times=0;
                    //关闭channel
                    ctx.channel().close();
                    channelInactive(ctx);
                }
                ByteBuf heartbeat=Unpooled.directBuffer();
                heartbeat.writeBytes(this.heartbeat);
                ctx.channel().writeAndFlush(heartbeat);
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
            }
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                    RpcClient.client.connect(eventLoop);
            }
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}