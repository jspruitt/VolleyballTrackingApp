package edu.tjhsst.researchapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;
public class MainActivity extends AppCompatActivity {
    private Size previewsize;
    private Size jpegSizes[]=null;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private static final int  MY_PERMISSIONS_REQUEST_CAMERA = 103;
    Button getpicture;
    private static final SparseIntArray ORIENTATIONS=new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView=(TextureView)findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
    public void getPermissions(){
        //get permission to use camera
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA))
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }
    public  void openCamera()
    {
        CameraManager manager=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try
        {
            String camerId=manager.getCameraIdList()[0];
            CameraCharacteristics characteristics=manager.getCameraCharacteristics(camerId);
            StreamConfigurationMap map=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewsize=map.getOutputSizes(SurfaceTexture.class)[0];
            getPermissions();
            manager.openCamera(camerId,stateCallback,null);
        }catch (Exception e)
        {
            Log.d("hhhh", e.getMessage());
        }
    }
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        Log.v("jjjj", e.getX() + " " + e.getY());

        return true;
    }
    private TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.v("hhh", "onSurfaceTextureSizeChanged");
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.v("hhh", "onSurfaceTextureDestroyed");
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.v("hhh", "onSurfaceTextureUpdated");
        }
    };
    private CameraDevice.StateCallback stateCallback=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice=camera;
            startCamera();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.v("hhh", "onDisconnected");

        }
        @Override
        public void onError(CameraDevice camera, int error) {
            Log.v("hhh", "onError");
        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        if(cameraDevice!=null)
        {
            cameraDevice.close();
        }
    }
    void  startCamera()
    {
        if(cameraDevice==null||!textureView.isAvailable()|| previewsize==null)
        {
            return;
        }
        SurfaceTexture texture=textureView.getSurfaceTexture();
        if(texture==null)
        {
            return;
        }
        texture.setDefaultBufferSize(previewsize.getWidth(),previewsize.getHeight());
        Surface surface=new Surface(texture);
        try
        {
            previewBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        }catch (Exception e)
        {
            Log.v("hhh", e.getMessage());
        }
        previewBuilder.addTarget(surface);
        try
        {
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    previewSession=session;
                    getChangedPreview();
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.v("hhh"," onConfigureFailed" );
                }
            },null);
        }catch (Exception e)
        {
            Log.v("hhh", e.getMessage());
        }
    }
    void getChangedPreview()
    {
        if(cameraDevice==null)
        {
            return;
        }
        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread=new HandlerThread("changed Preview");
        thread.start();
        Handler handler=new Handler(thread.getLooper());
        try
        {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, handler);
        }catch (Exception e){}
    }
}