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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class RpcClient {
    public static volatile RpcClient client;
    //check connection
    public static volatile boolean flag=true;
    public AtomicInteger timeout;
    private String hostName;
    private int port;
    private  volatile Channel channel;
    private HashMap<Integer,byte[]> responseCache;
    public ConcurrentHashMap<Integer,byte[]> requestCache;
    public Integer times;
    private EventLoopGroup group ;


    public RpcClient() throws Exception {
        ClassPathXmlApplicationContext context=
                new ClassPathXmlApplicationContext("applicationContext.xml");
        RemoteAddress remoteAddress=context.getBean("remoteAddress",RemoteAddress.class);
        hostName=remoteAddress.getHostName();
        port=remoteAddress.getPort();
        context.close();

        responseCache =new HashMap<>();
        requestCache=new ConcurrentHashMap<>();
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
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(120, 200, 200, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            ch.pipeline().addLast(
                                    new ProxyHandler(client));

                        }
                });
        ChannelFuture cf= null;
        b.connect().addListener(new ConnectionListener());
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    public void send(int requestId,byte[] bytes) throws Exception {
        while(channel==null){
            if(!flag){
                throw new Exception("Can't connect to server");
            }
        }
        requestCache.put(requestId,bytes);
        ByteBuf firstMessage=Unpooled.directBuffer();
        firstMessage.writeInt(bytes.length);
        firstMessage.writeBytes(bytes);
        channel.writeAndFlush(firstMessage);
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
    public byte[] get(int reqId) throws Exception{
        if(!flag){
            throw new Exception("Can't connect to server");
        }
        byte[] response= responseCache.get(reqId);
        responseCache.remove(reqId);
        return response;
    }

    public void setResult(int reqId,byte[] result) {
        responseCache.put(reqId,result);
    }

    public void removeRequest(int requestId) {
        requestCache.remove(requestId);
    }

    public void close(){
        group.shutdownGracefully();
    }
}
