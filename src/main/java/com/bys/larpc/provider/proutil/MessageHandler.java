package com.bys.larpc.provider.proutil;
import com.bys.larpc.consumer.conutil.RpcClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    private byte[] heartbeat;

    public MessageHandler() {
        ByteBuf hb=Unpooled.directBuffer();
        byte ping=0;
        hb.writeInt(1);
        hb.writeByte(ping);
        this.heartbeat=new byte[hb.readableBytes()];
        hb.readBytes(heartbeat);
    }

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
        if(request.length==1){
            ByteBuf heartbeat=Unpooled.directBuffer();
            heartbeat.writeBytes(this.heartbeat);
            ctx.channel().writeAndFlush(heartbeat);
            System.out.println("ping");
        }
        else {
            System.out.println(request.toString());
            RpcServer.server.callMethod(ctx.channel(), request);
        }
    }

    public void channelReadComplete(ChannelHandlerContext ctx)throws Exception{
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                ctx.channel().close();
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
