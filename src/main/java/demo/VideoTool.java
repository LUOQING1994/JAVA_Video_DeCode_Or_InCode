package demo;


import javax.swing.WindowConstants;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * 如何在Java中使用javacv和opencvAPI
 * 说明：OpenCV属于javaCv的一个小模块，既javaCv包含OpenCV的所有API。
 * 但实际运用时，我们还是会引用单独的OpenCV模块。因为，单独的OpenCV模块处理速度比javaCv快
 * 使用方法：
 * 1，OpenCV官网上下载某一版本的OpenCV安装文件，并安装到本地
 * 2，myeclisp中引入安装后的文件中的jar包，并在Java的jdk中引入x64的依赖
 * 3，在类中，以静态代码块的方式引入依赖
 * 参考：https://blog.csdn.net/df0128/article/details/81412572?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param
 * @author luo
 *
 */

public class VideoTool {
	
	static OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
	// 得保证先执行该语句，用于加载库，才能调用其他操作库的语句
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) throws Exception {
		// 加密
		inCodeVideo("D:\\FFOutput\\long_dump_data.mp4", "D:\\FFOutput\\enCodeVideo.avi");
		// 解密
//		deCodeVideo("D:\\FFOutput\\enCodeVideo.avi");
	    }
	/**
	 * 
	 * @param filePath  原视频地址
	 * @param targerFilePath 加密后的视频地址
	 */
    public static void inCodeVideo(String filePath, String targerFilePath)
            throws Exception {
        // 创建视频帧抓取工具
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filePath);
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtsp://admin:flm2019hb@192.168.101.108:554");
        grabber.setImageHeight(480);
        grabber.setImageWidth(640);
        grabber.start();
        CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
        
        // 创建视频编码工具 参考：http://www.yanzuoguang.com/article/709
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(targerFilePath, 640, 480, 0);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_HUFFYUV);
        recorder.setFormat("avi");
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setVideoQuality(10);
        recorder.start();
        // 读取加密文件
        Mat tmp_key_imageMat = Imgcodecs.imread("src/main/resources/img_key.jpg");
        while (true) {
        	if (!canvas.isDisplayable()) {//窗口是否关闭
                grabber.stop();//停止抓取
                grabber.release();
                recorder.stop();
                recorder.release();
                System.exit(-1);//退出
            }
        	// 获取数据
            Frame frame = grabber.grab();
            
            // 帧加密过程
            org.bytedeco.opencv.opencv_core.Mat tmpFrameMat = converter.convert(frame);
            // javacv.mat 转 opencv.mat  参考：https://github.com/bytedeco/javacpp/issues/38
            Mat tmp_opencv_mat = new Mat(tmpFrameMat.address());
            Mat tmp_xor_matMat = new Mat();
            Core.bitwise_xor(tmp_opencv_mat, tmp_key_imageMat, tmp_xor_matMat);
            frame = converter.convert(tmp_xor_matMat);
            recorder.record(frame);
            canvas.showImage(frame);//获取摄像头图像并放到窗口上显示
            Thread.sleep(30);//30毫秒刷新一次图像
        }
    }
    /**
     * 视频加密
     * @param inCodeVideoPath
     * @throws Exception
     */
    public static void deCodeVideo(String inCodeVideoPath)
            throws Exception {
        // 创建视频帧抓取工具
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inCodeVideoPath);
        grabber.setImageHeight(480);
        grabber.setImageWidth(640);
        grabber.start();
        CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
     // 读取加密文件
        Mat tmp_key_imageMat = Imgcodecs.imread("src/main/resources/img_key.jpg");
        while (true) {
        	if (!canvas.isDisplayable()) {//窗口是否关闭
                grabber.stop();//停止抓取
                grabber.release();
                System.exit(-1);//退出
            }
        	// 获取数据
            Frame frame = grabber.grab();
            
            // 帧加密过程
            org.bytedeco.opencv.opencv_core.Mat tmpFrameMat = converter.convert(frame);
            // 在javaCv中，拥有两种mat，这两种mat无法通用 需要转换
            // javacv.mat 转 opencv.mat
            // 参考：https://github.com/bytedeco/javacpp/issues/38
            Mat tmp_opencv_mat = new Mat(tmpFrameMat.address());
            Mat tmp_xor_matMat = new Mat();
            Core.bitwise_xor(tmp_opencv_mat, tmp_key_imageMat, tmp_xor_matMat);
            
            frame = converter.convert(tmp_xor_matMat);
            
            canvas.showImage(frame);//获取摄像头图像并放到窗口上显示
            Thread.sleep(30);//30毫秒刷新一次图像
            
        }
    }
}
