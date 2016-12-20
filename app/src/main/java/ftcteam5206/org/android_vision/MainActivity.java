package ftcteam5206.org.android_vision;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

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

    private CameraBridgeViewBase openCvCameraView;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status){
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
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

    private boolean frameRequested = false;
    private long lastFrameRequestedTime = 0;

    private int cameraId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.openCvView);
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);

        Button takePictureButton = (Button) findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);

        ArrayList<View> views = new ArrayList<>();
        views.add(takePictureButton);
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

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgb = inputFrame.rgba();
        Core.transpose(rgb, rgb);
        Core.flip(rgb, rgb, 1);
        if(frameRequested){
            frameRequested = false;
            VisionHelper.detectBeacon(rgb);
            Log.d(TAG, "Returned processed frame: " + (System.currentTimeMillis() - lastFrameRequestedTime));
        }
        return rgb;
    }

    private void takePicture() {
        Log.d(TAG, "Took picture");
        frameRequested = true;
        lastFrameRequestedTime = System.currentTimeMillis();
    }

    private void swapCamera(){
        //lol kyler would be so proud
        cameraId = cameraId^1;
        openCvCameraView.disableView();
        openCvCameraView.setCameraIndex(cameraId);
        openCvCameraView.enableView();
    }
}
