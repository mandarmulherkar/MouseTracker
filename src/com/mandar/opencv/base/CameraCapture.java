package com.mandar.opencv.base;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_ANY;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.mandar.opencv.FrameDrawer;

public class CameraCapture {
   

   public static void main(String[] args) {
      System.out.println("Starting OpenCV...");
      
      FrameDrawer framedrawer = new FrameDrawer();
      framedrawer.drawFrames();
      
   } // end of main()
} // end of CameraCapture class 