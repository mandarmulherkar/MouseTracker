package com.mandar.opencv;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_ANY;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FrameDrawer {
   /**
    * Sample code for starters, testing with a webcam
    * 
    * 
    * /
   private static boolean isRunning = true;

   public void drawFrames() {
      try {
         CanvasFrame canvas = new CanvasFrame("Camera Capture");
         canvas.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               isRunning = false;
            }
         });
         System.out.println("Starting frame grabber...");
         OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(CV_CAP_ANY);
         grabber.start();
         IplImage frame;
         while (isRunning) {
            if ((frame = grabber.grab()) == null)
               break;
            canvas.showImage(frame);
         }
         grabber.stop();
         canvas.dispose();
      } catch (Exception e) {
         System.out.println(e);
      }
   }
}
