package com.bys.larpc.provider.entry;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.bys.larpc.rpcutil.RemoteAddress;
import com.bys.larpc.provider.proutil.MessageHandler;
import com.bys.larpc.provider.proutil.ServiceRegistry;
import com.bys.larpc.service.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class RpcServer {
    private Calculator calculator = new CalculatorImpl();

    public static void main(String[] args) throws IOException {
        int port=5687;
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(240, 1000, 1000, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            ch.pipeline().addLast(new MessageHandler());

                        }
                    });
            ChannelFuture f = b.bind().sync();
            ClassPathXmlApplicationContext context=
                    new ClassPathXmlApplicationContext("applicationContext.xml");
            RemoteAddress remoteAddress=context.getBean("remoteAddress",RemoteAddress.class);
            String zkHost=remoteAddress.getHostName();
            int zkPort=remoteAddress.getPort();
            context.close();
            ServiceRegistry sr=new ServiceRegistry(zkHost+":"+zkPort);
            sr.register("127.0.0.1:"+port);
            System.out.println("Server Running");
            f.channel().closeFuture().sync();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
