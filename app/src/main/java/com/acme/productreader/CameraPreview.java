package com.acme.productreader;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/** A Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";
    private final SurfaceHolder mHolder;
    private final Camera mCamera;
    private final Camera.PreviewCallback mPreviewCallback;
    private final Camera.AutoFocusCallback mAutoFocusCallback;

    public CameraPreview(Context context, Camera camera, Camera.PreviewCallback previewCallback, Camera.AutoFocusCallback autoFocusCb) {
        super(context);
        mCamera = camera;
        mPreviewCallback = previewCallback;
        mAutoFocusCallback = autoFocusCb;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "[surfaceCreated]");
        try {
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.autoFocus(mAutoFocusCallback);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        } catch (Exception e){
            Log.d(TAG, "Error with camera: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "[surfaceDestroyed]");
        if(mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "[surfaceChanged]");
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size previewSize = getBestPreviewSize(parameters);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setPictureSize(previewSize.width, previewSize.height);
        parameters.setJpegQuality(100);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters){
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        Camera.Size bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        return bestSize;
    }

}