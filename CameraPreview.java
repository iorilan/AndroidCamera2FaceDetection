
/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    interface previewCallback{
        void onPreviewCreated();
    }
    private static final String TAG = "TestCamera";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private previewCallback mPreviewCallback;
    private Activity _parentActivity;
    public void setmPreviewCallback(previewCallback pvcb, Activity activity){
        mPreviewCallback = pvcb;
        _parentActivity = activity;
    }


    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            UtilApp.rotateCamera(_parentActivity,mCamera);
            mCamera.setPreviewDisplay(holder);


            if(mPreviewCallback != null){
                mPreviewCallback.onPreviewCreated();
            }


        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    private  Camera.FaceDetectionListener mOnFaceDetected;
    public void setOnFaceDetected( Camera.FaceDetectionListener onFaceDetected){
        mOnFaceDetected = onFaceDetected;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }
   //     mCamera.startPreview();
        // set preview size and make any resize, rotate or
        // reformatting changes here

        UtilApp.rotateCamera(_parentActivity,mCamera);
        mCamera.startPreview();


        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);

            if(mOnFaceDetected != null){
                mCamera.setFaceDetectionListener(mOnFaceDetected);
            }

            Camera.Parameters params = mCamera.getParameters();
            // start face detection only *after* preview has started

            if(mOnFaceDetected != null){
                if (params.getMaxNumDetectedFaces() > 0){
                    // camera supports face detection, so can start it:
                    mCamera.startFaceDetection();
                    Log.d("DEBUG", "Face detection started");
                }
            }


        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }



}