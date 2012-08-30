package com.mandar.opencv.base;

//
//import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_ANY;
//
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//
//import com.googlecode.javacv.CanvasFrame;
//import com.googlecode.javacv.OpenCVFrameGrabber;
//import com.googlecode.javacv.cpp.opencv_core.IplImage;
//import com.mandar.opencv.FrameDrawer;
//
//public class CameraCapture {
//   
//
//   public static void main(String[] args) {
//      System.out.println("Starting OpenCV...");
//      
//      FrameDrawer framedrawer = new FrameDrawer();
//      framedrawer.drawFrames();
//      
//   } // end of main()
//} // end of CameraCapture class

//http://www.cs.iit.edu/~agam/cs512/lect-notes/opencv-intro/opencv-intro.html#SECTION00072000000000000000
//http://www.cs.iit.edu/~agam/cs512/lect-notes/opencv-intro/opencv-intro.html

import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.CV_TERMCRIT_EPS;
import static com.googlecode.javacv.cpp.opencv_core.CV_TERMCRIT_ITER;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_SIMPLEX;
import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_ITALIC;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvInitFont;
import static com.googlecode.javacv.cpp.opencv_core.cvFont;
import static com.googlecode.javacv.cpp.opencv_core.cvLine;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_core.cvTermCriteria;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_FOURCC;
import static com.googlecode.javacv.cpp.opencv_highgui.cvConvertImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindCornerSubPix;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGoodFeaturesToTrack;
import static com.googlecode.javacv.cpp.opencv_video.cvCalcOpticalFlowPyrLK;
import static com.googlecode.javacv.cpp.opencv_video.cvCamShift;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.OpenCVFrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.CvBox2D;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvConnectedComp;
import com.googlecode.javacv.cpp.opencv_legacy.CvFace;

public class CameraCapture {
   private static final String APP_NAME = "LucasKanadeJavaCV";
   private static final String OUTPUT_FILE_NAME = "rectoutput.avi";
   private static final int FOURCC = CV_FOURCC('X', 'V', 'I', 'D');
   private static final int MAX_CORNERS = 500;
   private static final int MAX_FEATURES = 550;
   private static final int WINDOW_SIZE = 10;
   private static int fastInt = 0;

   private static OpenCVFrameGrabber grabber;
   private static IplImage currentFrame;
   private static IplImage[] frameBuffer;

   private static CanvasFrame canvas = new CanvasFrame("Mouse Tracker");
   private static CvPoint mouse1 = null;
   private static CvPoint mouse2 = null;
   private static CameraCapture cc = new CameraCapture();

   private final static double[] roiPts = new double[8];
   private static double avgX;
   private static double avgY;
   
   private static boolean queryFrame() {

      IplImage frame = null;
      try {
         frame = grabber.grab();
      } catch (Exception e) {
         System.out.println("End of Video");
         e.printStackTrace();
         return false;
      }

      if (frame != null) {
         cvConvertImage(frame, currentFrame, 0);

         IplImage temp = frameBuffer[0];
         frameBuffer[0] = frameBuffer[1];
         frameBuffer[1] = temp;
         cvConvertImage(frame, frameBuffer[0], 0);

         return true;
      }
      else {
         return false;
      }
   }

   public static void main(String[] args) throws Exception {

      // Check parameters
      //    if (args.length < 1) {
      //      System.err.printf("%s: %s\n", APP_NAME, "No video name given");
      //      System.err.printf("Usage: java %s <video file name> [output file name]\n", APP_NAME);
      //
      //      System.exit(1);
      //    }

      String outputFileName;
      if (args.length == 2) {
         outputFileName = args[1];
      }
      else
      {
         outputFileName = OUTPUT_FILE_NAME;
      }

      // Load video
      String fileName = "C:\\Users\\mandarm\\workspace\\MouseTracker\\assets\\1.1.1 1530.mpg";//args[0];
      grabber = new OpenCVFrameGrabber("C:\\Users\\mandarm\\workspace\\MouseTracker\\assets\\1.1.1 1530.mpg");
      grabber.start();

      int width = grabber.grab().width();
      int height = grabber.grab().height();
      double framerate = grabber.getFrameRate();
      if(framerate<=0)
         framerate=23;
     else
        framerate=1000/framerate;
      
      // Extract video parameters
      CvSize frameSize = cvSize(width, height);

      // Initialize video writer
      OpenCVFrameRecorder recorder = new OpenCVFrameRecorder(outputFileName, frameSize.width(), frameSize.height());
      recorder.setCodecID(FOURCC);
      recorder.setFrameRate(framerate);
      recorder.start();

      // Initialize variables for optical flow calculation
      currentFrame = cvCreateImage(frameSize, IPL_DEPTH_8U, 3);
      IplImage eigenImage = cvCreateImage(frameSize, IPL_DEPTH_32F, 1);
      IplImage tempImage = cvCreateImage(frameSize, IPL_DEPTH_32F, 1);

      int[] cornerCount = { MAX_CORNERS };
      byte[] featuresFound = new byte[MAX_CORNERS];
      float[] featureErrors = new float[MAX_CORNERS];

      IplImage[] pyramidImages = new IplImage[2];
      frameBuffer = new IplImage[2];

      for (int i = 0; i < 2; i++) {
         frameBuffer[i] = cvCreateImage(frameSize, IPL_DEPTH_8U, 1);
         pyramidImages[i] = cvCreateImage(frameSize, IPL_DEPTH_32F, 1);
      }

      //Get region of interest
      //double[] localROIPts = getROI();
      
      // Process video
      while (queryFrame()) {
         
         double xTot = 0;
         double yTot = 0;
         int totalXPoints = 0;
         int totalYPoints = 0;

         CvPoint2D32f corners1 = new CvPoint2D32f(MAX_CORNERS);
         CvPoint2D32f corners2 = new CvPoint2D32f(MAX_CORNERS);

         // Corner finding with Shi and Thomasi
         cvGoodFeaturesToTrack(
               frameBuffer[0],
               eigenImage,
               tempImage,
               corners1,
               cornerCount,
               0.01,
               5.0,
               null,
               3,
               0,
               0.4);

         cvFindCornerSubPix(
               frameBuffer[0],
               corners1,
               cornerCount[0],
               cvSize(WINDOW_SIZE, WINDOW_SIZE),
               cvSize(-1, -1),
               cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.3));

         // Pyramid Lucas-Kanade
         cvCalcOpticalFlowPyrLK(
               frameBuffer[0],
               frameBuffer[1],
               pyramidImages[0],
               pyramidImages[1],
               corners1,
               corners2,
               cornerCount[0],
               cvSize(WINDOW_SIZE, WINDOW_SIZE),
               5,
               featuresFound,
               featureErrors,
               cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.3),
               0);

         //:O
         //calculateCamShift(localROIPts);

         // Draw optical flow vectors
         for (int i = 0; i < cornerCount[0]; i++) {

            corners1.position(i);
            corners2.position(i);
            
            if( Math.abs(Math.round(corners2.x()) - Math.round(corners1.x())) < 2 ){
               featuresFound[i] = 0;
            }
              
            if (featuresFound[i] == 0 || featureErrors[i] > MAX_FEATURES)
            {
               continue;
            }

            xTot += corners2.x(); totalXPoints++;
            yTot += corners2.y(); totalYPoints++;
            
            CvPoint point1 = cvPoint(Math.round(corners1.x()), Math.round(corners1.y()));
            CvPoint point2 = cvPoint(Math.round(corners2.x()), Math.round(corners2.y()));
            cvRectangle(currentFrame, point1, point2, CV_RGB(0, 255, 0), 1, 8, 0);
            cvLine(currentFrame, point1, point2, CV_RGB(255, 0, 0), 1, 8, 0);

         }

         //Draw partition
         CvPoint point1 = cvPoint(240, 0);
         CvPoint point2 = cvPoint(242, 480);
         cvRectangle(currentFrame, point1, point2, CV_RGB(255, 255, 0), 4, 8, 0);
         
         if(xTot != 0 && yTot != 0){
            avgX = xTot / totalXPoints;
            avgY = yTot / totalYPoints;
         }
         CvPoint circleCenter = cvPoint((int)Math.round(avgX), (int)Math.round(avgY));
         cvCircle(currentFrame, circleCenter, 5, CV_RGB(255, 0, 0), 6, 8, 0);//new2dpoint.

         
         recorder.record(currentFrame);

         try {
            canvas.showImage(currentFrame);
         } catch (Exception e) {
            System.out.println(e);
         }
      }

      grabber.stop();
      recorder.stop();
      canvas.dispose();
   }

   private static void calculateCamShift(double[] localROIPts) {

      
      CvRect window = new CvRect();
      window.x((int) localROIPts[0]);
      window.y((int) localROIPts[1]);
      window.height((int) Math.abs(localROIPts[1] - localROIPts[3]));
      window.width((int) Math.abs(localROIPts[0] - localROIPts[2]));



      CvConnectedComp connComp = new CvConnectedComp();
      CvBox2D box2d = new CvBox2D();

      cvCamShift(frameBuffer[0],
            window,
            cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.3),
            connComp,
            box2d);

//      CvRect newRect = connComp.rect();
//      CvPoint2D32f new2dpoint = box2d.center();
//      CvPoint circleCenter = cvPoint(Math.round(new2dpoint.x()), Math.round(new2dpoint.y()));
//
//      cvCircle(currentFrame, circleCenter, 10, CV_RGB(0, 255, 255), 1, 8, 0);//new2dpoint.

      CvPoint pointTest1 = cvPoint(Math.round(connComp.rect().x()), Math.round(connComp.rect().y()));
      CvPoint pointTest2 = cvPoint(Math.round(connComp.rect().x()) + connComp.rect().width(), Math.round(connComp.rect().y()) + connComp.rect().height());
      cvRectangle(currentFrame, pointTest1, pointTest2, CV_RGB(0, 0, 255), 1, 8, 0);
      
      CvFont font = new CvFont();
      double hScale=1.0;
      double vScale=1.0;
      int    lineWidth=1;
      cvInitFont(font, CV_FONT_HERSHEY_SIMPLEX | CV_FONT_ITALIC, hScale, vScale, 1, lineWidth, 2);
      cvPutText(currentFrame, ""+connComp.rect().x()+", "+connComp.rect().y(), pointTest1, font, CV_RGB(0, 0, 255) );

      CvFont font1 = new CvFont();
      double hScale1 =1.0;
      double vScale1 =1.0;
      int    lineWidth1 =1;
      cvInitFont(font1, CV_FONT_HERSHEY_SIMPLEX | CV_FONT_ITALIC, hScale1, vScale1, 1, lineWidth1, 2);
      cvPutText(currentFrame, ""+connComp.rect().x() + connComp.rect().height()+", "+connComp.rect().y() + connComp.rect().width(), pointTest2, font1, CV_RGB(0, 0, 255) );


   }

   private static double[] getROI() {

      double[] roiPts = null;

      try {
         queryFrame();
         canvas.showImage(currentFrame);
         roiPts = acquireRoiFromUser(canvas, 1);
      } catch (Exception e) {
         e.printStackTrace();
      }

      return roiPts;
   }

   private static double[] acquireRoiFromUser(final CanvasFrame monitorWindow,
         final double monitorWindowScale) throws Exception {
      if (monitorWindow == null) {
         throw new Exception("Error: No monitor window. Could not acquire ROI from user.");
      }
      Toolkit t = Toolkit.getDefaultToolkit();
      Dimension d = t.getBestCursorSize(15, 15);
      BufferedImage cursorImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = cursorImage.createGraphics();
      int cx = d.width / 2, cy = d.height / 2;
      g.setColor(Color.WHITE);
      g.drawRect(cx - 7, cy - 7, 14, 14);
      g.setColor(Color.BLACK);
      g.drawRect(cx - 6, cy - 6, 12, 12);
      g.setColor(Color.WHITE);
      g.drawRect(cx - 2, cy - 2, 4, 4);
      g.setColor(Color.BLACK);
      g.drawRect(cx - 1, cy - 1, 2, 2);
      if (d.width % 2 == 0) {
         cx += 1;
      }
      if (d.height % 2 == 0) {
         cy += 1;
      }
      Cursor cursor = t.createCustomCursor(cursorImage, new Point(cx, cy), null);
      monitorWindow.setCursor(cursor);

      final int[] count =
      { 0 };

      monitorWindow.getCanvas().addMouseMotionListener(new MouseMotionListener() {

         @Override
         public void mouseDragged(MouseEvent arg0) {
            System.out.println("Dragged");

         }

         @Override
         public void mouseMoved(MouseEvent arg0) {
            // TODO Auto-generated method stub

         }
      });

      monitorWindow.getCanvas().addMouseListener(new MouseAdapter() {
         double x, y;
         int mouseRelTimes;

         @Override
         public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            System.out.println("Dragged");
         }

         @Override
         public void mouseMoved(MouseEvent arg0) {
            super.mouseMoved(arg0);
            System.out.println("Moved");
         }

         @Override
         public void mousePressed(MouseEvent e) {
            super.mousePressed(e);

            roiPts[count[0]++] = e.getX() / monitorWindowScale;
            roiPts[count[0]++] = e.getY() / monitorWindowScale;

            Graphics2D g = monitorWindow.createGraphics();
            g.setColor(Color.RED);
            g.drawLine(e.getX() - 7, e.getY(), e.getX() + 7, e.getY());
            g.drawLine(e.getX(), e.getY() - 7, e.getX(), e.getY() + 7);
            monitorWindow.releaseGraphics(g);

            System.out.println("Pressed x: " + x + " y: " + y + " x2: " + e.getX() + " y2: " + e.getY());

         }

         @Override
         public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);

            mouseRelTimes++;

            if (mouseRelTimes == 2) {

               roiPts[count[0]++] = e.getX() / monitorWindowScale;
               roiPts[count[0]++] = e.getY() / monitorWindowScale;

               Graphics2D g = monitorWindow.createGraphics();
               g.setColor(Color.RED);
               g.drawLine(e.getX() - 7, e.getY(), e.getX() + 7, e.getY());
               g.drawLine(e.getX(), e.getY() - 7, e.getX(), e.getY() + 7);

               System.out.println("Released x: " + x + " y: " + y + " x2: " + e.getX() + " y2: " + e.getY());

               g.drawRect((int) roiPts[0], (int) roiPts[1], (int) Math.abs(roiPts[0] - roiPts[2]), (int) Math.abs(roiPts[1] - roiPts[3]));
               monitorWindow.releaseGraphics(g);
            }

         }

         @Override
         public void mouseClicked(MouseEvent e) {
            if (count[0] < 8) {
               roiPts[count[0]++] = e.getX() / monitorWindowScale;
               roiPts[count[0]++] = e.getY() / monitorWindowScale;
               Graphics2D g = monitorWindow.createGraphics();
               g.setColor(Color.RED);
               g.drawLine(e.getX() - 7, e.getY(), e.getX() + 7, e.getY());
               g.drawLine(e.getX(), e.getY() - 7, e.getX(), e.getY() + 7);
               monitorWindow.releaseGraphics(g);
            }
            if (count[0] >= 8) {
               synchronized (roiPts) {
                  monitorWindow.getCanvas().removeMouseListener(this);
                  monitorWindow.setCursor(null);
                  roiPts.notify();
               }
            }
         }
      });

      synchronized (roiPts) {
         roiPts.wait();
      }

      //     if (monitorWindows != null) {
      //         for (int i = 0; i < monitorWindows.length; i++) {
      //             if (monitorWindows[i] != null) {
      //                 monitorWindows[i].dispose();
      //                 monitorWindows[i] = null;
      //             }
      //         }
      //         monitorWindows = null;
      //     }

      return roiPts;
   }
}
