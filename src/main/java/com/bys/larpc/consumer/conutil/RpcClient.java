package com.bys.larpc.consumer.conutil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class RpcClient {
    public static RpcClient client;
    private String hostName="localhost";
    private int port=8080;
    private   Channel channel;
    private HashMap<Integer,byte[]> hs;

    public RpcClient() {
        hs=new HashMap<>();
        connect();
    }
    /*public void run() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(hostName,port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            ch.pipeline().addLast(
                                    new ProxyHandler(proxy,requestarray));

                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }*/
    public void connect() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(hostName,port))
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            ch.pipeline().addLast(
                                    new ProxyHandler(client));

                        }
                });
        b.connect();
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    public void send(byte[] bytes){
        ByteBuf firstMessage=Unpooled.directBuffer();
        firstMessage.writeInt(bytes.length);
        firstMessage.writeBytes(bytes);
        channel.writeAndFlush(firstMessage);
    }
    public byte[] get(int reqId){
        return hs.get(reqId);
    }

    public void setResult(int reqId,byte[] result) {
        hs.put(reqId,result);
    }
}
