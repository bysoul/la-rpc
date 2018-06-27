package com.bys.larpc.provider.proutil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in=(ByteBuf)msg;
        byte[] request=new byte[in.readableBytes()];
        in.readBytes(request);
        //singleton
        if(RpcServer.server==null){
            synchronized (RpcServer.class){
                if(RpcServer.server==null){
                    RpcServer.server=new RpcServer();
                }
            }
        }
        RpcServer.server.callMethod(ctx.channel(),request);
    }

    public void channelReadComplete(ChannelHandlerContext ctx)throws Exception{
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
