package kingkong.facerecognize.app.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.cloud.util.VerifierUtil;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import kingkong.facerecognize.app.HomeActivity;
import kingkong.facerecognize.app.R;

/**
 * Created by KingKong-HE on 2018/1/2.
 *
 * @author KingKong-HE
 * @Time 2018/1/2
 * @Email 709872217@QQ.COM
 */
public class LoginActivity extends AppCompatActivity{

    private CameraView cameraViewID;

    private TextView btuCommit,resultTextID,volumeTextID;

    private RelativeLayout waterRippleID;

    private Toolbar toolbarID;

    //采用身份识别接口进行在线人脸识别
    private IdentityVerifier mIdVerifier;

    // 数字密码类型为3，其他类型暂未开放
    private static final int PWD_TYPE_NUM = 3;

    //生成的密码
    private  String verifyNumPwd;

    // 是否可以录音
    private boolean isStartWork = false;
    // 是否可以录音
    private boolean mCanStartRecord = false;

    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;

    // 进度对话框
    private ProgressDialog mProDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        btuCommit = findViewById(R.id.btuCommit);

        resultTextID = findViewById(R.id.resultTextID);

        volumeTextID = findViewById(R.id.volumeTextID);

        cameraViewID = (CameraView)findViewById(R.id.cameraViewID);

        waterRippleID = findViewById(R.id.waterRippleID);

        toolbarID = findViewById(R.id.toolbar);

        verifyNumPwd = VerifierUtil.generateNumberPassword(8);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍后");

        toolbarID.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // cancel进度框时,取消正在进行的操作
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        mIdVerifier = IdentityVerifier.createVerifier(LoginActivity.this, new InitListener() {

            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    Toast.makeText(LoginActivity.this,"引擎初始化成功",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this,"引擎初始化失败，错误码：" + errorCode,Toast.LENGTH_SHORT).show();
                }
            }
        });

        //相机监听方法
        cameraViewID.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap bitmap = cameraKitImage.getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //可根据流量及网络状况对图片进行压缩
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] mImageData = baos.toByteArray();
                getVerification(mImageData);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btuCommit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        waterRippleID.setVisibility(View.VISIBLE);

                        if (!isStartWork) {
                            // 根据业务类型调用服务
                            vocalVerify();

                            isStartWork = true;
                            mCanStartRecord = true;
                        }
                        if (mCanStartRecord) {
                            try {
                                mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
                                mPcmRecorder.startRecording(mPcmRecordListener);
                            } catch (SpeechError e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:

                        v.performClick();

                        waterRippleID.setVisibility(View.GONE);

                        mIdVerifier.stopWrite("ivp");

                        if (null != mPcmRecorder) {
                            mPcmRecorder.stopRecord(true);
                        }

                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }

    //验证声纹
    private void vocalVerify() {

        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("您的验证密码：" + verifyNumPwd + "\n");
        strBuffer.append("请长按“按住说话”按钮进行验证！\n");
        resultTextID.setText(strBuffer.toString());
        // 设置声纹验证参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
        // 验证模式，单一验证模式：sin
        mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
        // 用户的唯一标识，在声纹业务获取注册、验证、查询和删除模型时都要填写，不能为空
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, "king123");
        // 设置监听器，开始会话
        mIdVerifier.startWorking(verifyListener);
    }

    //录音机监听器
    private PcmRecorder.PcmRecordListener mPcmRecordListener = new PcmRecorder.PcmRecordListener() {
        @Override
        public void onRecordBuffer(byte[] bytes, int offset, int length) {

            StringBuffer params = new StringBuffer();
            params.append("ptxt=" + verifyNumPwd + ",");
            params.append("pwdt=" + PWD_TYPE_NUM + ",");
            mIdVerifier.writeData("ivp", params.toString(), bytes, 0, length);

        }

        @Override
        public void onError(SpeechError speechError) {

        }

        @Override
        public void onRecordStarted(boolean b) {

        }

        @Override
        public void onRecordReleased() {

        }
    };

    /**
     * 声纹验证监听器
     */
    private IdentityListener verifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d("king-->", "verify:" + result.getResultString());
            try {
                JSONObject object = new JSONObject(result.getResultString());
                String decision = object.getString("decision");

                if ("accepted".equalsIgnoreCase(decision)) {
                    cameraViewID.captureImage();
                    resultTextID.setText("声纹验证通过");
                } else {
                    StringBuffer errorResult = new StringBuffer();
                    errorResult.append("声纹验证未通过"+ "\n");
                    errorResult.append("请长按“按住说话”重新验证!");
                    resultTextID.setText(errorResult.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            resultTextID.setText("");
            isStartWork = false;
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if(SpeechEvent.EVENT_VOLUME == eventType){
                volumeTextID.setText("音量："+arg1);
            }
//            if (SpeechEvent.EVENT_VOLUME == eventType) {
//                Toast.makeText(LoginActivity.this,"音量：" + arg1,Toast.LENGTH_SHORT).show();
//            } else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
//                Toast.makeText(LoginActivity.this,"录音结束",Toast.LENGTH_SHORT).show();
//            }
        }

        @Override
        public void onError(SpeechError error) {
            isStartWork = false;
            mCanStartRecord = false;

            StringBuffer errorResult = new StringBuffer();
//            errorResult.append("验证失败！\n");
//            errorResult.append("错误信息：" + error.getPlainDescription(true) + "\n");
//            errorResult.append("请长按“按住说话”重新验证!");
            errorResult.append("验证失败：" + error.getPlainDescription(false)+ "\n");
            errorResult.append("请长按“按住说话”重新验证!");
            resultTextID.setText(errorResult.toString());
        }
    };

    /**
     * 人脸验证监听器
     */
    private IdentityListener faceVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d("king---", result.getResultString());

            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            try {
                JSONObject object = new JSONObject(result.getResultString());
                Log.d("king---","object is: "+object.toString());
                String decision = object.getString("decision");

                if ("accepted".equalsIgnoreCase(decision)) {
                    Toast.makeText(LoginActivity.this,"验证通过" ,Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                    finish();
                } else {
                    StringBuffer errorResult = new StringBuffer();
                    errorResult.append("人脸识别验证未通过"+ "\n");
                    errorResult.append("请长按“按住说话”重新验证!");
                    resultTextID.setText(errorResult.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            StringBuffer errorResult = new StringBuffer();
            errorResult.append("验证失败：" + error.getPlainDescription(false)+ "\n");
            errorResult.append("请长按“按住说话”重新验证!");
            resultTextID.setText(errorResult.toString());
        }

    };

    //获取人脸验证信息
    public void getVerification(byte[] mImageData){
        if (null != mImageData && mImageData.length > 0) {
            mProDialog.setMessage("验证中...");
            mProDialog.show();
            // 设置人脸验证参数
            // 清空参数
            mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
            // 设置会话场景
            mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
            // 设置会话类型
            mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
            // 设置验证模式，单一验证模式：sin
            mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
            // 用户id
            mIdVerifier.setParameter(SpeechConstant.AUTH_ID, "king123");
            // 设置监听器，开始会话
            mIdVerifier.startWorking(faceVerifyListener);

            // 子业务执行参数，若无可以传空字符传
            StringBuffer params = new StringBuffer();
            // 向子业务写入数据，人脸数据可以一次写入
            mIdVerifier.writeData("ifr", params.toString(), mImageData, 0, mImageData.length);
            // 停止写入
            mIdVerifier.stopWrite("ifr");
        } else {
            Toast.makeText(LoginActivity.this,"请选择图片后再验证" ,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resultTextID.setText("请长按“按住说话”按钮进行验证");
        cameraViewID.start();
    }

    @Override
    protected void onPause() {
        cameraViewID.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
        }
        super.onDestroy();
    }

}
