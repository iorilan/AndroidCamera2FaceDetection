  public static void rotateCamera(Activity activity, android.hardware.Camera camera) {
	  int degree = getCameraRotationDegree(activity, camera);
         camera.setDisplayOrientation(degree);
		 camera.getParameters().setRotation(degree);
    }


    public static int getCameraRotationDegree(Activity activity, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch(rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result ;
        if(info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        }else{
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;

    }
