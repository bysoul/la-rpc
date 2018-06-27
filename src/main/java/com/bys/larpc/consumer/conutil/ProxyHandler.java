package com.bys.larpc.consumer.conutil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProxyHandler extends
        SimpleChannelInboundHandler<ByteBuf> {
    private byte[] req=null;
    private RpcClient rpcClient;

    public ProxyHandler(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("ProxyHandler:new channel");
        rpcClient.setChannel(ctx.channel());
        /*ByteBuf firstMessage=Unpooled.directBuffer();
        firstMessage.writeInt(req.length);
        firstMessage.writeBytes(req);
        ctx.writeAndFlush(firstMessage);*/
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception{
        System.out.println("reading");
        ByteBuf buf=(ByteBuf) msg;
        int requestId=buf.readInt();
        byte[] resp=new byte[buf.readableBytes()];
        buf.readBytes(resp);
        rpcClient.setResult(requestId,resp);
    }
}