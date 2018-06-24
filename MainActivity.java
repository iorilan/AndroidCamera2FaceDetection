public class MainActivity extends AppCompatActivity  implements CameraPreview.previewCallback, Camera.FaceDetectionListener ,Camera.ShutterCallback,Camera.PictureCallback {

    private static final String TAG = "CameraActivity";

    private CameraPreview mPreview;
    private Camera mCamera;
    public Bitmap mBitmap;
    private ImageView ivpic;

    private final Context mContext = this;
    private boolean mBound = false;

    private final double Thredshold = 0;

    private RelativeLayout m_pnlResult;
    private TextView m_lblName;
    private TextView m_lblWelcome;
    private TextView m_lblMessage;
    private ImageView m_imgPerson;
    private ImageView m_imgBorder;
    private ImageView m_imgBg;
    private ImageView m_successTick;
    private Activity _self;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _self = this;

        //init ui elements




        checkpermission();

        FrameLayout previewImage = (FrameLayout)findViewById(R.id.camera_preview);
        mPreview.setmPreviewCallback(this,this);
        mPreview.setOnFaceDetected(this);


    }

    private  long _timeDetected = 0;
    private long frequencyMiliseconds = 5000;
    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if(faces != null && faces.length > 0){
            long now = Calendar.getInstance().getTime().getTime();
            if(now > 0 && now - _timeDetected < frequencyMiliseconds){
                return;
            }
            _timeDetected = now;


           takepic();
        }
    }

    private void displayResult(SenseResult model){
        ...
		// after few seconds hide result
		m_pnlResult.postDelayed(new Runnable() {
                public void run() {
                    m_pnlResult.setVisibility(View.INVISIBLE);
                }
            }, frequencyMiliseconds);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    public void onPreviewCreated() {

    }

	// when face detected , can focus and snap to improve the score
    private void focusArea(int left, int top, int right, int bottom, int weight){
        if (mCamera != null) {
            Camera camera = mCamera;
            camera.cancelAutoFocus();

            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> mylist = new ArrayList<Camera.Area>();
                mylist.add(new Camera.Area(new Rect(left, top, right, bottom), weight));
                parameters.setFocusAreas(mylist);
            }

            camera.setParameters(parameters);
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    camera.cancelAutoFocus();
                    Camera.Parameters params = camera.getParameters();
                    if (params.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        camera.setParameters(params);
                    }
                }
            });
        }
    }

    private void checkpermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    checkpermission();
                }
                return;
            }
        }
    }

    private void init() {
        boolean hascamera = checkCameraHardware(this);
        if (hascamera) {
            Log.v(TAG, "HAS CAMERA");
        } else {
            Log.v(TAG, "NO CAMERA");
        }

        if (hascamera) {
            // Create an instance of Camera
            mCamera = getCameraInstance();

            if (mCamera != null) {
                // Create our Preview view and set it as the content of our activity.
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
            } else {

                Log.v(TAG, "mCamera is null");
            }
        }
    }

    @Override
    protected void onDestroy() {
        mCamera.release();
        super.onDestroy();
    }

    public void takepic() {
        if (mCamera != null ) {
            mCamera.takePicture(this, this, this);
        }
    }

    @Override
    public void onShutter() {

    }
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

            String output_file_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +  "SenseResult.jpeg";
            File pictureFile = new File(output_file_name);
            if (pictureFile.exists()) {
                pictureFile.delete();
            }


            try {
                pictureFile.createNewFile();

                FileOutputStream fos = new FileOutputStream(pictureFile);

                Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);

                ExifInterface exif=new ExifInterface(pictureFile.toString());

                Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                int degree = UtilApp.getCameraRotationDegree(_self,mCamera);
                realImage= rotate(realImage, degree);

                boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                m_imgPerson.setImageBitmap(realImage);

                fos.close();



                Log.d("Info", bo + "");

            } catch (FileNotFoundException e) {
                Log.d("Info", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }



        new searchFaceTask().execute(mBitmap);

        UtilApp.rotateCamera(_self,mCamera); // for portrait mode
        mCamera.startPreview();
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            if(Camera.getNumberOfCameras() > 1){
                c = Camera.open(1); // front camera
            }
            else{
                c = Camera.open(); // attempt to get a Camera instance
            }
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }



    private class searchFaceTask extends AsyncTask<Bitmap, String, SenseResult> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected SenseResult doInBackground(Bitmap... args) {
            Bitmap imagebitmap = null;
            try {
                imagebitmap = args[0];
                File f = BitmapToFile(imagebitmap);

                Gson gson = new GsonBuilder().create();
                String url = getString(R.string.url_searchFace);
                String result = UtilHttp.searchFace(f, url);
                SenseResult ret = gson.fromJson(result, SenseResult.class);
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(SenseResult result) {
            if(result == null){
                UtilApp.showPopupMsg(mContext,"unexpected result from server.");
                return;
            }

            displayResult(result);
        }
    }

    private File BitmapToFile(Bitmap bitmap){
        //create a file to write bitmap data
        File f = new File(mContext.getCacheDir(), "imageData.PNG");
        try {
            f.createNewFile();
            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }
	
	
	// load person image from server
    private class LoadPersonImgTask extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        protected Bitmap doInBackground(String... args) {
            Bitmap imagebitmap = null;
            try {
                imagebitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return imagebitmap;
        }

        protected void onPostExecute(Bitmap image) {
            if(image != null){
                m_imgPerson.setImageBitmap(image);
            }
        }
    }



}
