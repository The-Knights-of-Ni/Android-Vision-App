package ftcteam5206.org.android_vision;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import java.util.ArrayList;

/**
 * Created by tarunsingh on 12/18/16.
 */

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OpenCV Test";

    //OpenCV camera preview object
    private CameraBridgeViewBase openCvCameraView;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status){
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d(TAG, "OpenCV loaded successfully");
                    openCvCameraView.enableView();
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //Is true for one frame when beacon detection is called
    private boolean frameRequested = false;

    //System time when beacon detection is called (for performance analysis)
    private long lastFrameRequestedTime = 0;

    //0 is rear, 1 is front
    private int cameraId = 0;

    //True if right side of beacon is red and left side is blue
    private boolean redIsRight;
    private String result;

    //TextView to display result of beacon detection
    private TextView beaconTextView;
    private Button detectBeaconButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.openCvView);
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);

        beaconTextView = (TextView) findViewById(R.id.beaconTextView);

        detectBeaconButton = (Button) findViewById(R.id.detectBeaconButton);
        detectBeaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectBeacon();
            }
        });

        //Have to use either 3.1.0 or 2.4.13 since Imgproc.moments() isn't in 3.0.0
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);

        ArrayList<View> views = new ArrayList<>();
        views.add(detectBeaconButton);
        openCvCameraView.addTouchables(views);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Have to use either 3.1.0 or 2.4.13 since Imgproc.moments() isn't in 3.0.0
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    //Image comes in rotated 90 degrees
    //TODO: Change vision algorithm to avoid unnecessary matrix transformations
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgb = inputFrame.rgba();

        //Rotate image to be right side up
        Core.transpose(rgb, rgb);
        Core.flip(rgb, rgb, 1);

        //Beacon detection was called
        if(frameRequested){
            //Only run detection on one frame
            frameRequested = false;
            redIsRight = VisionHelper.detectBeacon(rgb);

            //Update UI with results of beacon detection
            if(redIsRight)
                result = "Blue is left, red is right";
            else
                result = "Red is left, blue is right";
            Log.d(TAG, result);
            //TODO: Probably want to switch to AsyncTask instead
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateBeaconTextView(result);
                }
            });

            //Log how long beacon detection took
            Log.d(TAG, "Returned processed frame: " + (System.currentTimeMillis() - lastFrameRequestedTime));
        }
        return rgb;
    }

    /** Runs beacon detection on next frame */
    private void detectBeacon() {
        frameRequested = true;
        lastFrameRequestedTime = System.currentTimeMillis();
    }

    /** Changes camera being used from rear to front, or front to rear */
    private void swapCamera(){
        //lol kyler would be so proud
        cameraId = cameraId^1;
        openCvCameraView.disableView();
        openCvCameraView.setCameraIndex(cameraId);
        openCvCameraView.enableView();
    }

    /** Wrapper method to update UI */
    private void updateBeaconTextView(String result) {
        beaconTextView.setText(result);
    }
}
