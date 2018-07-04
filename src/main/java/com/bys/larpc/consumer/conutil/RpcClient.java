package com.bys.larpc.consumer.conutil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class RpcClient {
    public static volatile RpcClient client;
    //check connection
    public static volatile boolean flag=true;
    public AtomicInteger timeout;
    private String hostName="localhost";
    private int port=8080;
    private  volatile Channel channel;
    private HashMap<Integer,byte[]> responses;
    public Integer times;
    private EventLoopGroup group ;


    public RpcClient() throws Exception {
        responses =new HashMap<>();
        times=0;
        group = new NioEventLoopGroup();
        connect(group);
        //timeout=new AtomicInteger(0);
    }
    public void connect(EventLoopGroup eventLoopGroup) {
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(hostName,port))
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(5, 200, 200, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            ch.pipeline().addLast(
                                    new ProxyHandler(client));

                        }
                });
        System.out.println("aaaa");
        ChannelFuture cf= null;
        try {
            cf = b.connect().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cf.addListener(new ConnectionListener());
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    public void send(byte[] bytes) throws Exception {
        while(channel==null){
            if(!flag){
                throw new Exception("Can't connect to server");
            }
        }
        ByteBuf firstMessage=Unpooled.directBuffer();
        firstMessage.writeInt(bytes.length);
        firstMessage.writeBytes(bytes);
        channel.writeAndFlush(firstMessage);
    }
    public byte[] get(int reqId){
        byte[] response= responses.get(reqId);
        responses.remove(reqId);
        return response;
    }

    public void setResult(int reqId,byte[] result) {
        responses.put(reqId,result);
    }
}
