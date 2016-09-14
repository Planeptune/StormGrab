package com.neptune;

import com.google.gson.Gson;
import com.neptune.config.facerig.PictureKey;
import com.neptune.config.grab.GrabCommand;
import com.neptune.constant.LogPath;
import com.neptune.kafka.KafkaNewProducer;
import com.neptune.tool.IVideoNotifier;
import com.neptune.tool.VideoNotifierImpl;
import com.neptune.util.HDFSHelper;
import com.neptune.util.ImageBase64;
import com.neptune.util.ImageHelper;
import com.neptune.util.LogWriter;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.InvalidTimestampException;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.ManagementFactory;

/**
 * Created by neptune on 16-9-12.
 * 用于抓帧的子进程，可用性尚未测试，建议暂不使用，留到最后检查
 */
public class GrabThread extends Thread {
    private final static String TAG = "Grab";
    private static String LOG_PATH = "/grab-thread.log";
    private final static int FAIL_LIMIT = 20;

    private HDFSHelper mHelper;
    private String mUrl;
    private String mDir;
    private FFmpegFrameGrabber mGrabber;
    private OpenCVFrameConverter.ToIplImage mIlplImageConverter;
    private Java2DFrameConverter mImageConverter;

    private String mFormat = "picture-%05d-%d.png";
    private int mWidth = 227;
    private int mHeight = 227;
    //grab rate
    private double mGrabRate = 1.0;

    private boolean mIsRunning;
    private boolean mIsActive;

    private int mCount = 0;
    private int mIndex = 0;

    private IVideoNotifier mNotifier;

    private KafkaNewProducer mProducer;
    private Callback mCallback;

    private String mTopic;

    private Gson mGson;

    //private FileLogger mLogger;

    public GrabThread(String url, String dir, IVideoNotifier notifier, String topic, String brokerList)//, FileLogger logger)
    {
        mUrl = url;
        mDir = dir;
        String id = String.valueOf(Math.abs(url.hashCode()));
        mFormat = id + "-%05d-%d.png";
        //mLogger = logger;
        mHelper = new HDFSHelper(dir);
        mGrabber = new FFmpegFrameGrabber(url);
        mIlplImageConverter = new OpenCVFrameConverter.ToIplImage();
        mImageConverter = new Java2DFrameConverter();
        mNotifier = notifier;
        mTopic = topic;
        //brokerList: ip:port,ip:port...
        if (brokerList != null && brokerList.length() > 2) {
            mProducer = new KafkaNewProducer(brokerList);
            mCallback = new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e != null) {
                        LogWriter.writeLog(LOG_PATH, "Kafka Exception :" + e.getMessage());
                        LogWriter.writeLog(LOG_PATH, mUrl + " The offset of the record is: " + recordMetadata.offset());
                    } else {
                        LogWriter.writeLog("grab-thread,log", mUrl + " The offset of the record is: " + recordMetadata.offset());
                    }
                }
            };
        }
        mGson = new Gson();

        LOG_PATH = LogPath.PATH + "/grab-thread.log";
    }

    public FFmpegFrameGrabber getGrabber() {
        return mGrabber;
    }

    /**
     * start grab in a child thread
     */
    @Override
    public void run() {
        mNotifier.prepare();
//        startGrab();
        grab();
    }

    /**
     * execute grab
     */
    private void grab() {
        mIsRunning = true;
        mIsActive = true;

        Frame frame = null;
        opencv_core.IplImage image = null;
        int oldW;
        int oldH;
        BufferedImage bi = null;
        ByteArrayOutputStream baos = null;
//        OutputStream baos = null;
        String fileName = null;
        boolean res = false;
        long time;
        int expectNumber = 0;
        int grabStep = 1;
        int lastNumber = -1;
        int frameNumber = 0;
        boolean setOK = true;
        int realStep;
        int errorTimes = 0;
        boolean isFirst = true;

        int num = 0;

        PictureKey pictureKey = new PictureKey();
        try {
            mNotifier.notify("prepare to start grabbing video from" + mUrl);
            LogWriter.writeLog(LOG_PATH, mUrl + " prepared for grabbing");
            mGrabber.setTimeout(3000);
            mGrabber.start();
            double frameRate = mGrabber.getFrameRate();
            grabStep = (int) (frameRate / mGrabRate);
            mNotifier.notify("finish starting, " +
                    "frameRate=" + frameRate
                    + ", frameLength=" + mGrabber.getLengthInFrames()
                    + ", grabStep=" + grabStep);
            LogWriter.writeLog(LOG_PATH, mUrl + "started,frameRate=" + mGrabber.getLengthInFrames() + ",grabStep=" + grabStep);
            while (mIsRunning) {

                if (!mIsActive) {
                    LogWriter.writeLog(LOG_PATH, mUrl + " grab paused");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LogWriter.writeLog(LOG_PATH, e.getMessage());
                    }
                    continue;
                }
                mNotifier.notify("prepare to grab frame " + mCount + "/" + mIndex);
                LogWriter.writeLog(LOG_PATH, mUrl + " prepared for grabbing frame " + mCount + "/" + mIndex);

                //抓取图片
                try {
                    frame = mGrabber.grabImage();
                    if (frame != null)
                        num++;
                    //隔一段时间抓一帧
                    if (num < grabStep)
                        continue;
                    else
                        num = 0;
                    frameNumber = mGrabber.getFrameNumber();
                    LogWriter.writeLog(LOG_PATH, "frame number= " + frameNumber);
                    //抓到同一帧，继续（？）
                    if (frameNumber == lastNumber) {
                        LogWriter.writeLog(LOG_PATH, "grab same frame: " + frame);
                        continue;
                    }
                    //抓到不同帧，记录帧
                    else {
                        lastNumber = frameNumber;
                    }
                } catch (FrameGrabber.Exception e) {
                    LogWriter.writeLog(LOG_PATH, e.getMessage());
                    continue;
                }

                mNotifier.notify("grab frame " + mCount + "/" + mIndex);
                LogWriter.writeLog(LOG_PATH, mUrl + "grab frame " + mCount + "/" + "mIndex");
                image = mIlplImageConverter.convertToIplImage(frame);
                if (image != null) {
                    //resize image
                    oldW = image.width();
                    oldH = image.height();
                    bi = new BufferedImage(oldW, oldH, BufferedImage.TYPE_3BYTE_BGR);
                    bi.getGraphics().drawImage(mImageConverter.getBufferedImage(frame), 0, 0, oldW, oldH, null);
                    bi = ImageHelper.resize(bi, mWidth, mHeight);
                    LogWriter.writeLog(LOG_PATH, mUrl + " size: " + bi.getWidth() + "*" + bi.getHeight());
                    //write image to byte array output stream
                    baos = new ByteArrayOutputStream();
                    try {
//                        baos = new FileOutputStream(mDir+File.separator+String.format(mFormat, mCount, System.currentTimeMillis()));
                        ImageIO.write(bi, "png", baos);
                    } catch (IOException e) {
                        LogWriter.writeLog(LOG_PATH, e.getMessage());
                    }
                    //write data in byte array output stream to hdfs
                    InputStream is = new ByteArrayInputStream(baos.toByteArray());
                    time = System.currentTimeMillis();
                    fileName = String.format(mFormat, mCount, time);
                    //TODO 尝试在这里加了编码，如果下一个topology消息出错，首先检查此处
                    //pictureKey.setCodex(ImageBase64.encodingImg(is));

                    res = mHelper.upload(is, fileName);
                    try {
                        baos.close();
                        baos = null;
                    } catch (IOException e) {
                        LogWriter.writeLog(LOG_PATH, e.getMessage());
                    }
                    mNotifier.notify("write frame " + mCount + " to " + fileName + ", " + res);
                    LogWriter.writeLog(LOG_PATH, mUrl + "write frame " + mCount + " to " + fileName + ", " + res);
                    //send message to kafka
                    pictureKey.url = mDir + File.separator + fileName;
                    pictureKey.video_id = mUrl;
                    pictureKey.time_stamp = String.valueOf(time);
                    pictureKey.dir = mDir;
                    if (mProducer != null) {
                        String msg = mGson.toJson(pictureKey);
                        //Note:
                        //the process may be blocked even dead when kafka Error happened such as:
                        //org.apache.kafka.common.errors.InvalidTimestampException:
                        //The timestamp of the message is out of acceptable range.
                        //(The Exception will be displayed in mCallback, I know neither the reason nor how to fix it!!!)

                        try {
                            mProducer.send(mTopic, msg);
                            LogWriter.writeLog(LOG_PATH, mTopic + "send kafka message: " + msg);
                        } catch (InvalidTimestampException e) {
                            LogWriter.writeLog(LOG_PATH, mTopic + " send message fail: " + e.getMessage());
                        } catch (Exception e) {
                            LogWriter.writeLog(LOG_PATH, "kafka exception: " + e.getMessage());
                        }
                    }
                    mCount++;
                } else {
                    LogWriter.writeLog(LOG_PATH, mUrl + " sorry, the image of frame " + mIndex + " is null");
                }
                mIndex++;
            }
            mNotifier.notify("grabbing total: " + mCount + "/" + mIndex + " in " + mUrl);
            LogWriter.writeLog(LOG_PATH, mUrl + "grabbing total: " + mCount + "/" + mIndex);

            mGrabber.stop();
            mGrabber.release();

        } catch (FrameGrabber.Exception e) {
            LogWriter.writeLog(LOG_PATH, e.getMessage());
        } finally {
            clear();
        }
    }

    /**
     * 释放资源
     */
    private void clear() {
        if (mNotifier != null) {
            mNotifier.stop();
            mNotifier = null;
        }

        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }

        if (mProducer != null) {
            mProducer.close();
            mProducer = null;
        }

    }

    /**
     * 开始抓取
     */
    private boolean startGrab() {
        mIsRunning = true;
        mIsActive = true;
        try {
            mGrabber.start();
            mNotifier.notify("start grabbing");
            LogWriter.writeLog(LOG_PATH, mUrl + " start grabbing");
            return true;
        } catch (FrameGrabber.Exception e) {
            LogWriter.writeLog(LOG_PATH, e.getMessage());
            return false;
        }

    }

    /**
     * 重启抓取
     *
     * @see #grab() has contains this, so no need to invoke it
     */
    private boolean restartGrab() {
        mIsRunning = true;
        mIsActive = true;
        try {
            mGrabber.restart();
            return true;
        } catch (FrameGrabber.Exception e) {
            LogWriter.writeLog(LOG_PATH, e.getMessage());
            return false;
        }
    }

    /**
     * 停止抓取
     */
    public void stopGrab() {
        mIsRunning = false;
        mIsActive = false;
        mNotifier.notify("stop grabbing");
        LogWriter.writeLog(LOG_PATH, mUrl + " stop grabbing");
    }

    /**
     * 暂停抓取
     */
    public void pauseGrab() {
        mIsActive = false;
        mNotifier.notify("pause grabbing");
        LogWriter.writeLog(LOG_PATH, mUrl + "pause grabbing");
    }

    /**
     * 继续抓取
     */
    public void continueGrab() {
        mIsActive = true;
        mNotifier.notify("continue grabbing");
        LogWriter.writeLog(LOG_PATH, mUrl + "continue grabbing");
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public int getCount() {
        return mCount;
    }

    public int getIndex() {
        return mIndex;
    }

    public double getGrabRate() {
        return mGrabRate;
    }

    /**
     * 设置帧率，每秒抓几帧
     * default value: 1f
     */
    public void setGrabRate(double rate) {
        if (rate > 0)
            mGrabRate = rate;
    }

    /**
     * set output format like mp4
     * It seems that the method needn't be invoked
     * ？格式化？未知
     */
    public void setOutputFormat(String outputFormat) {
        mGrabber.setFormat(outputFormat);
    }

    /**
     * 设置输出的文件名格式
     */
    public void setNameFormat(String format) {
        this.mFormat = format;
    }


    /**
     * 设置图片大小
     */
    public void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }


    /**
     * listen message
     * the thread should run only if the process is a child process,
     * otherwise there will be some IOExceptions.
     */
    static class ListenThread extends Thread {
        private BufferedReader reader;
        private String STOP;

        private boolean run;

        private MessageListener listener;

        public interface MessageListener {
            void handleMessage(String msg);
        }

        public ListenThread(BufferedReader reader, String stop) {
            this.reader = reader;
            this.STOP = stop;
            this.run = true;
        }

        public void setListener(MessageListener l) {
            this.listener = l;
        }

        @Override
        public void run() {

            String msg;
            while (run) {
                try {
                    msg = reader.readLine();
                    if (listener != null)
                        listener.handleMessage(msg);
                    if (msg == null || STOP.equals(msg))
                        break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopListen() {
            this.run = false;
        }
    }


    /**
     * Note:
     * this method contains some test values which should be modify and rebuilt
     * <p>
     * the main method need at lease 7 arguments
     * args[0] redis host
     * args[1] redis port
     * args[2] redis password
     * args[3] video rtmp url
     * args[4] hdfs absolute directory path (including ip or hostname)
     * args[5] kafka topic to send message
     * args[6] kafka brokerList to send message
     * args[7] file name format [optional]
     */
    public static void main(String[] args) {

        if (args.length < 7)
            throw new RuntimeException("the main method of GrabThread need at lease 7 arguments");
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String password = args[2];
        String url = args[3];
        String dir = args[4];
        String topic = args[5];
        String brokerList = args[6];

        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];

        //FileLogger logger = new FileLogger("GrabThread@" + pid);

        //this notifier is not needed
        IVideoNotifier notifier = new VideoNotifierImpl(
                host, port,
                password, new String[]{url});

        final GrabThread grabThread = new GrabThread(url, dir, notifier, topic, brokerList);

        if (args.length >= 8) {
            try {
                double rate = Double.valueOf(args[7]);
                grabThread.setGrabRate(rate);
            } catch (NumberFormatException e) {
                LogWriter.writeLog(LOG_PATH, e.getMessage());
            }
        }
        grabThread.start();

        //start a ListenThread
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ListenThread listenThread = new ListenThread(reader, GrabCommand.DEL);
        listenThread.setListener(new ListenThread.MessageListener() {
            public void handleMessage(String msg) {
                if (msg.equals(GrabCommand.DEL)) {
                    grabThread.stopGrab();
                } else if (msg.equals(GrabCommand.PAUSE)) {
                    grabThread.pauseGrab();
                } else if (msg.equals(GrabCommand.CONTINUE)) {
                    grabThread.continueGrab();
                }
            }
        });
        if (brokerList != null && brokerList.length() > 2)
            listenThread.start();

        LogWriter.writeLog(LOG_PATH, url + "Child process: GrabThread is running in " + pid);

        try {
            grabThread.join();
        } catch (InterruptedException e) {
            LogWriter.writeLog(LOG_PATH, e.getMessage());
        } finally {
            listenThread.stopListen();
            LogWriter.writeLog(LOG_PATH, url + "Child process: GrabThread  " + pid + " has exit");
            System.exit(0);
        }

    }
}
