package com.jiangdg.demo;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.jiangdg.natives.R;

public class MainActivity extends AppCompatActivity implements H264EncodeThread.OnEncodeResultListener {
    private CameraManager mCamManager;
    private H264EncodeThread mEncodeThread;
    private H264DecodeThread mDecodeThread;
    private static final String text = "坐监是香港废青们唯一的出路";

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if(mCamManager != null) {
                mCamManager.openCamera(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if(mEncodeThread != null) {
                            mEncodeThread.setFrameData(data);
                        }
                    }
                });
                mCamManager.startPreview(holder);
            }
            // 启动编码线程
            mEncodeThread = new H264EncodeThread(CameraManager.previewWidth, CameraManager.previewHeight);
            mEncodeThread.setOnEncodeResultListener(MainActivity.this);
            mEncodeThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 关闭编码线程
            if(mEncodeThread != null) {
                mEncodeThread.exit();
                mEncodeThread.setOnEncodeResultListener(null);
                mEncodeThread = null;
                Thread t = mEncodeThread;
                if(t != null) {
                    mEncodeThread = null;
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(mCamManager != null) {
                mCamManager.stopPreview();
                mCamManager.closeCamera();
            }
        }
    };
    private SurfaceHolder.Callback callback2 = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 启动解码线程
            mDecodeThread = new H264DecodeThread(CameraManager.previewWidth, CameraManager.previewHeight);
            mDecodeThread.setRenderSurface(holder.getSurface());
            mDecodeThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 关闭解码线程
            if(mDecodeThread != null) {
                mDecodeThread.exit();
                mDecodeThread.setRenderSurface(null);
                mDecodeThread = null;
                Thread dt = mDecodeThread;
                if(dt != null) {
                    mDecodeThread = null;
                    try {
                        dt.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCamManager = CameraManager.getInstance(this);

        initView();
    }

    private void initView() {
        final TextView mTvResult = findViewById(R.id.tv_result);
        SurfaceView mSurfaceEncrypt = findViewById(R.id.surface_encrypt);
        mSurfaceEncrypt.getHolder().addCallback(callback);
        SurfaceView mSurfaceDecrypt = findViewById(R.id.surface_decrypt);
        mSurfaceDecrypt.getHolder().addCallback(callback2);
        mTvResult.setText(text);
    }

    @Override
    public void onEncodeResult(byte[] h264) {
        if(mDecodeThread != null) {
            mDecodeThread.setDecodeData(h264);
        }
    }
}
