package cn.anytec.quadrant;
/**
 * 继承ReplayingDecoder（extends FrameDecoder）--编解码器
 * 判断数据是否接受完全
 * 实现近乎无阻塞读取而仅是数据
 */

import cn.anytec.quadrant.CameraData.FDCameraData;
import cn.anytec.quadrant.CameraData.FaceDefine;
import cn.anytec.quadrant.findface.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

//import io.netty.handler.codec.ByteToMessageDecoder;

@Component
public class CameraByteToMessageDecoder extends ReplayingDecoder<Void> {
//	private static final Logger logger = LoggerFactory.getLogger(CameraByteToMessageDecoder.class);
	private static final int FD_HEADER_LENGTH = 176;
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		FDCameraData fdData = new FDCameraData();
		byte[] magicStr = new byte[4];
		byte[] totalLen = new byte[4];
		byte[] mac = new byte[18];
		byte[] reserved = new byte[6];
		byte[] faceNum = new byte[4];
		byte[] timeStamp = new byte[8];
		byte[] newFdFlag = new byte[4];
		byte[] dummy = new byte[4];
		byte[] data = new byte[2];

		in.readBytes(magicStr);
		in.readBytes(totalLen);
		fdData.mJpgSize = byte2int(totalLen) - (FD_HEADER_LENGTH - 4);
		
		in.readBytes(mac);
		fdData.mStrMac = bytes2String(mac).substring(0, 17).toUpperCase();
		in.readBytes(reserved);
		in.readBytes(faceNum);
		fdData.mFaceNum = byte2int(faceNum);
		in.readBytes(dummy);
		in.readBytes(timeStamp);
		in.readBytes(newFdFlag);
		if (fdData.mFaceNum <= 0)
			fdData.mHasNewFd = false;
		else
			fdData.mHasNewFd = true;

		for (int i = 0; i < FDCameraData.MAX_FACE_ITEM_PER_FRAME; i++) {
			in.readBytes(data);
			//int confidence = byte2int(data);

			in.readBytes(data);
			//int ID = byte2int(data);

			in.readBytes(data);
			//double left = byte2int(data) / 640 * Constant.WIDTH;

			in.readBytes(data);
			//double right = byte2int(data) / 640 * Constant.WIDTH;

			in.readBytes(data);
			//double top = byte2int(data) / 360 * Constant.HEIGHT;

			in.readBytes(data);
			//double bottom = byte2int(data) / 360 * Constant.HEIGHT;

			/*fdData.mFaceItem[i].left = left;
			fdData.mFaceItem[i].right = right;
			fdData.mFaceItem[i].top = top;
			fdData.mFaceItem[i].bottom = bottom;
			fdData.mFaceItem[i].confidence = confidence;
			fdData.mFaceItem[i].ID = ID;*/

				/*FaceDefine faceDefine = new FaceDefine();
				faceDefine.confidence = confidence;
				faceDefine.ID = ID;
				faceDefine.left = left;
				faceDefine.right = right;
				faceDefine.top = top;
				faceDefine.bottom = bottom;
				fdData.mFaceItem.add(faceDefine);*/

		}
		in.readBytes(dummy);
		fdData.mJpgData = new byte[fdData.mJpgSize];
		if(fdData.mJpgSize < 0) throw new Exception("Negative fdData.mJpgSize");
		in.readBytes(fdData.mJpgData);
		out.add(fdData);

	}

	private int byte2int(byte[] b) {
		int[] aTemplate = new int[4];
		aTemplate[0] = 0x000000FF;
		aTemplate[1] = 0x0000FF00;
		aTemplate[2] = 0x00FF0000;
		aTemplate[3] = 0xFF000000;

		int a = 0;
		if (b != null && b.length <= 4) {
			for (int i = 0; i < b.length; i++) {
				a = a + ((b[i] << i * 8) & aTemplate[i]);
			}
		}

		return a;
	}

	public String byte2hex(byte bytex) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[2];
		int v;

		v = bytex & 0xFF;
		hexChars[0] = hexArray[v >>> 4];
		hexChars[1] = hexArray[v & 0x0F];

		return new String(hexChars);
	}

	public String bytes2String(byte[] bytes) {
		String s = new String();
		boolean showingHex = false;
		for (int j = 0; j < bytes.length; j++) {
			if (bytes[j] > 126 || bytes[j] < 32 || (bytes[j] == 0x77 && showingHex)
					|| (j - 1 > 0 && bytes[j - 1] == 0x77 && showingHex)) {
				if (!showingHex) {
					s += "[";
					showingHex = true;
				}
				s += byte2hex(bytes[j]);
			} else {
				if (showingHex) {
					s += "]";
					showingHex = false;
				}
				s += (char) bytes[j];
			}
		}
		return s;
	}
}
