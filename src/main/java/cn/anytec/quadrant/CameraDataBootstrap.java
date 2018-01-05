package cn.anytec.quadrant;

import cn.anytec.quadrant.findface.Constant;
import cn.anytec.quadrant.ws.DataPushRunable;
import cn.anytec.quadrant.ws.WsMessStore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CameraDataBootstrap {

    private static Logger logger = Logger.getLogger(CameraDataBootstrap.class);
    private static Map<String,DataPushRunable> pushRunableMap;

    @Autowired
    private CameraChannelHandler cameraChannelHandler;
    private int port = 8100;
    private static final int CHANNEL_IDLE_TIMEOUT_SECOND = 120;
    private ServerBootstrap bootstrap = null;
    private EventLoopGroup group = null;
    public CameraDataBootstrap() {}

    public void init() {

        pushRunableMap = new HashMap<>(Constant.CAMERA_AMOUNT);

        logger.debug("初始化CameraDataBootstrap,建立Channel");
        logger.debug("启动推送线程");
        WsMessStore.getInstance().startPushMessThread();

        group = new NioEventLoopGroup();
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 0, CHANNEL_IDLE_TIMEOUT_SECOND, TimeUnit.SECONDS))
                                    .addLast(new CameraByteToMessageDecoder())
                                    .addLast(cameraChannelHandler);
                        }
                    });

            bootstrap.bind().sync();
            System.out.println("[[ Camera Server Started ]]");
//			ChannelFuture f = bootstrap.bind().sync();
//			f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @PreDestroy
    public void stop() {
        try {
            System.out.println("[[ Camera Server shutdowing ]]");
            group.shutdownGracefully().sync();
            System.out.println("[[ Camera Server Stoped ]]");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setPushRunable(String camMac,DataPushRunable dataPushRunable){
        pushRunableMap.put(camMac,dataPushRunable);
    }
    public static DataPushRunable getDataPushRunable(String camMac){
        return pushRunableMap.get(camMac);
    }
}
