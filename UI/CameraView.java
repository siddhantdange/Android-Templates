package com.fwdus.selfies4reform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by siddange on 12/28/13.
 */
public class CameraView extends LinearLayout implements View.OnTouchListener, View.OnClickListener, SurfaceHolder.Callback{
    Context ctx;
    Button cameraFlipButton, flashToggleButton, imageCaptureButton;
    Camera camera;
    SurfaceView cameraView;
    Bitmap imageBitmap;
    boolean readyToStart;
    ImageHandlerDelegate imgHandlerDelegate = null;


    public CameraView(Context context) {
        super(context);
        this.ctx = context;
    }

    public CameraView(Context context, ImageHandlerDelegate delegate){
        this(context);
        this.imgHandlerDelegate = delegate;
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    //set view to self
    public void inflateSelf() throws CameraViewException, IOException{

        //inflate from layout
        LayoutInflater.from(this.ctx).inflate(R.layout.cameraview, this, true);

        //connect buttons
        this.cameraFlipButton = (Button)findViewById(R.id.camera_view_flipButton);
        this.flashToggleButton = (Button)findViewById(R.id.camera_view_flashToggleButton);
        this.imageCaptureButton = (Button)findViewById(R.id.camera_view_captureButton);

        //set listeners
        this.cameraFlipButton.setOnClickListener(this);
        this.flashToggleButton.setOnClickListener(this);
        this.imageCaptureButton.setOnClickListener(this);

        this.cameraFlipButton.setOnTouchListener(this);
        this.flashToggleButton.setOnTouchListener(this);
        this.imageCaptureButton.setOnTouchListener(this);

        //begin camera feed
        if(Camera.getNumberOfCameras() > 0){
            this.cameraView = (SurfaceView)findViewById(R.id.camera_view_camerafeedview);

            //set surfaceholder delegate
            this.cameraView.getHolder().addCallback(this);

            try{
                this.camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } catch (Exception e){
                this.camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }

            Camera.Parameters parameters = this.camera.getParameters();
            parameters.set("orientation", "landscape");
            this.camera.setParameters(parameters);

            if(this.readyToStart){
                this.camera.setPreviewDisplay(cameraView.getHolder());
                this.camera.startPreview();
            } else{
                this.readyToStart = true;
            }


        } else{
            CameraViewException exception = new CameraViewException();
            exception.setMessage(exception.NO_CAMERA_EXCEPTION);

            throw exception;
        }
    }

    @Override
    public void onClick(View view) {
        //change image based on touch
        if(view.equals(this.cameraFlipButton)){

        } else if(view.equals(this.flashToggleButton)){

        } else if(view.equals(this.imageCaptureButton)){

            //handle UI
            this.camera.stopPreview();
            this.imageCaptureButton.setBackgroundResource(R.drawable.btn_camera);

            this.camera.takePicture(null, null, new Camera.PictureCallback(){
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    try{
                    //save image
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inSampleSize = 5;

                    Bitmap m = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                    //handle through delegate method
                    if(imgHandlerDelegate != null){
                        imgHandlerDelegate.handleBitmapImage(m);
                    }

                    } catch(Exception e){
                        Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        //change image based on touch
        if(view.equals(this.cameraFlipButton)){

        } else if(view.equals(this.flashToggleButton)){

        } else if(view.equals(this.imageCaptureButton)){

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                view.setBackgroundResource(R.drawable.btn_camera_selected);
            } else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                view.setBackgroundResource(R.drawable.btn_camera);
                this.onClick(view);
            }
        }

        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(this.readyToStart){
            try{
                this.camera.setPreviewDisplay(this.cameraView.getHolder());
                this.camera.startPreview();
            } catch(Exception e){

            }
        } else{
            this.readyToStart = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    static class CameraViewException extends Exception{
        public String NO_CAMERA_EXCEPTION = "No found cameras on phone";
        private String message = null;

        public CameraViewException() {
            super();
        }

        public CameraViewException(String detailMessage) {
            super(detailMessage);
        }

        private void setMessage(String message){
            this.message = message;
        }

        @Override
        public String getMessage() {
            return (this.message != null) ? this.message : super.getMessage();
        }
    }

    interface ImageHandlerDelegate{
        public void handleBitmapImage(Bitmap image);
    }

}
