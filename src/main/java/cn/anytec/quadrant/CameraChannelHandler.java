package cn.anytec.quadrant;

import cn.anytec.quadrant.CameraControl.CameraLiveViewer;
import cn.anytec.quadrant.CameraControl.CameraViewManager;
import cn.anytec.quadrant.CameraData.FDCameraData;
import cn.anytec.quadrant.findface.Constant;
import cn.anytec.quadrant.findface.FindFaceHandler;
import cn.anytec.quadrant.util.CameraViewCount;
import cn.anytec.quadrant.ws.DataPushRunable;
import cn.anytec.quadrant.ws.DataPushScheduler;
import cn.anytec.quadrant.ws.SystemWebSocketHandler;
import cn.anytec.quadrant.ws.WsMessStore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;


@Component
@Sharable
public class CameraChannelHandler extends ChannelInboundHandlerAdapter {
	private static final ByteBuf HEARTBEAT_SEQUENCE = 	Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("1", CharsetUtil.ISO_8859_1));
	private static final Logger logger = LoggerFactory.getLogger(CameraChannelHandler.class);

	@Autowired
	FindFaceHandler findFaceHandler;

	//@Autowired
	//private cn.anytec.welcome.quadrant.CameraUtil.FDCameraDataHandler FDCameraDataHandler;
	
	public CameraChannelHandler() {}



	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.debug(ctx.channel().remoteAddress() + " connected");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String camIp = getCameraIp(ctx);
		if(CameraViewManager.getCameraLiveViewer(camIp)!=null)
			CameraViewManager.getCameraLiveViewer(camIp).setOnline(false);
		logger.debug("Camera:{} offline",camIp);
		//FDCameraDataHandler.setCameraSessionOffline(clientIp);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg != null && msg instanceof FDCameraData) {
			String camIp = getCameraIp(ctx);

			FDCameraData data = (FDCameraData)msg;
			String camMac = data.mStrMac;
			//logger.info("cameraIp :{}",camIp);
			//logger.info("cameraMac :{}",camMac);
			CameraViewCount cameraViewCount = CameraViewCount.getCameraViewCount(camMac);
			if(data.mFaceNum > 0){
				if(cameraViewCount.times+1 == Constant.STRATEGY_CAMERA_SCALE){
					cameraViewCount.times = 0;
					FindFaceHandler.getInstance().notifyFindFace(data);
				}else {
					cameraViewCount.count();
				}
			}
			//logger.info("Mac:"+camMac);
			if(!CameraViewManager.isKnownCamera(camIp)){
				String oldIp = CameraViewManager.isoldMac(camMac);
				if(oldIp!=null){
					CameraViewManager.removeCameraLiveViewer(oldIp);
				}else {
					DataPushScheduler.createCondition(camMac);
					DataPushRunable dataPushRunable = new DataPushRunable(camMac);
					CameraDataBootstrap.setPushRunable(camMac,dataPushRunable);
					Thread thread = new Thread(dataPushRunable);
					thread.setDaemon(true);
					thread.start();
				}
				CameraViewManager.setCameraLiveViewer(camIp,new CameraLiveViewer(camMac,true));
			}
			CameraViewManager.getCameraLiveViewer(camIp).setOnline(true);

			if(WsMessStore.getWssIds(camMac) == null)
				return;
			WsMessStore.getInstance().addMessage(data);

		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			logger.debug("");
			String cameraIp = getCameraIp(ctx);
			ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) {
					if (!future.isSuccess()) {
						future.channel().close();
						//FDCameraDataHandler.setCameraSessionOffline(clientIp);
					}
				}
			});

		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String clientIp = getCameraIp(ctx);
		logger.error(cause.getMessage() , cause);
		logger.debug("ExceptionCaught from {}", clientIp);// let camera reconnect
		ctx.channel().close();
	}

	
	private String getCameraIp(ChannelHandlerContext ctx) {
		return ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
	}

	
	
}
