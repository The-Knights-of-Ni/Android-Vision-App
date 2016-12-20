package ftcteam5206.org.android_vision;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tarunsingh on 12/19/16.
 */

public class VisionHelper {
    private static final String TAG = "OpenCV Test";

    //matrices for images
    private static Mat HSV;
    private static Mat red;
    private static Mat blue;
    private static Mat redHierarchy;
    private static Mat blueHierarchy;

    //constants for thresholding
    private final static int redL = 200;
    private final static int redH = 255;
    private final static int blueL = 125;
    private final static int blueH = 200;
    private final static int saturation = 0;
    private final static int value = 100;

    public static void detectBeacon(Mat src) {
        HSV = new Mat();
        red = new Mat();
        blue = new Mat();
        redHierarchy = new Mat();
        blueHierarchy = new Mat();

        Log.d(TAG, "Starting beacon detection");
        Imgproc.cvtColor(src, HSV, Imgproc.COLOR_RGB2HSV_FULL);

        //create matrices with the red and blue areas thresholded
        Core.inRange(HSV, new Scalar(redL,saturation,value),new Scalar(redH, 255,255),red);
        Core.inRange(HSV, new Scalar(blueL,saturation,value),new Scalar(blueH,255,255),blue);

        //store contour hierarchies for thresholded stuff
        List<MatOfPoint> redContourList = new ArrayList<>();
        List<MatOfPoint> blueContourList = new ArrayList<>();

        //find the contours in each thresholded image
        Imgproc.findContours(red, redContourList, redHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        Imgproc.findContours(blue, blueContourList, blueHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

        //find the largest contours, then place them on top of the source image
        //also find the center of the blob of redness
        //if there are no red contours, skip this step
        Moments redMoments = Imgproc.moments(red);
        double redCenter = 0;
        int redSelector = 0;
        if(redContourList.size() != 0) {
            //find the largest red contour
            double maxArea = Imgproc.contourArea(redContourList.get(redSelector));
            for (int i = 1; i < redContourList.size(); i++) {
                MatOfPoint redContour = redContourList.get(i);
                if (redContour != null) {
                    if (Imgproc.contourArea(redContourList.get(i)) > maxArea) {
                        redSelector = i;
                        maxArea = Imgproc.contourArea(redContourList.get(redSelector));
                    }
                }
            }
            redCenter = redMoments.m10/redMoments.m00;
        }

        //repeat previous process, but for blue
        int blueSelector = 0;
        double blueCenter = 0;
        Moments blueMoments = Imgproc.moments(blue);
        if(blueContourList.size() != 0) {
            //find the largest blue contour
            double maxArea = Imgproc.contourArea(blueContourList.get(blueSelector));
            for (int i = 1; i < blueContourList.size(); i++) {
                MatOfPoint redContour = blueContourList.get(i);
                if (redContour != null) {
                    if (Imgproc.contourArea(blueContourList.get(i)) > maxArea) {
                        blueSelector = i;
                        maxArea = Imgproc.contourArea(blueContourList.get(blueSelector));
                    }
                }
            }
            blueCenter = blueMoments.m10/blueMoments.m00;
        }

        //draw the contours on the image
        Imgproc.drawContours(src,redContourList,redSelector,new Scalar(255,0,0),-1);
        Imgproc.drawContours(src,blueContourList,blueSelector,new Scalar(0,0,255),-1);

        Log.d(TAG, "Red: " + redCenter + ", Blue: " + blueCenter);
        if(redCenter < blueCenter)
            Log.d(TAG, "Red is Left, Blue is Right");
        else
            Log.d(TAG, "Blue is Left, Red is Right");
    }
}
