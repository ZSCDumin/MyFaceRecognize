package kingkong.facerecognize.app.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.record.PcmRecorder;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import kingkong.facerecognize.app.R;
import kingkong.facerecognize.app.entity.VoiceprintPasswordInfo;

/**
 * Created by KingKong-HE on 2017/12/26.
 * 人脸验证界面
 * @author KingKong-HE
 * @Time 2017/12/26
 * @Email 709872217@QQ.COM
 */
public class VoiceprintRecognizeActivity extends AppCompatActivity{

    private Button btuCommit;
    private TextView toolbar_subtitle,textPasswordID;

    // 进度对话框
    private ProgressDialog mProDialog;

    //采用身份识别接口进行在线人脸识别
    private IdentityVerifier mIdVerifier;

    // 数字声纹密码段，默认有5段
    private String[] mNumPwdSegs;
    // 数字声纹密码
    private String mNumPwd = "";

    // 数字密码类型为3，其他类型暂未开放
    private static final int PWD_TYPE_NUM = 3;

    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voiceper_recognize);

        toolbar_subtitle = (TextView) findViewById(R.id.toolbar_subtitle);
        textPasswordID = (TextView) findViewById(R.id.textPasswordID);
        btuCommit = findViewById(R.id.btuCommit);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍后");

        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // cancel进度框时,取消正在进行的操作
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        mIdVerifier = IdentityVerifier.createVerifier(VoiceprintRecognizeActivity.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    Toast.makeText(VoiceprintRecognizeActivity.this,"引擎初始化成功",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VoiceprintRecognizeActivity.this,"引擎初始化失败，错误码：" + errorCode,Toast.LENGTH_SHORT).show();
                }
            }
        });

        toolbar_subtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mNumPwdSegs) {
                    // 首次注册密码为空时，调用下载密码
                    getDownPassword();
                } else {
                    Toast.makeText(VoiceprintRecognizeActivity.this,"数字密码已存在",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btuCommit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        // 取消之前操作
                        if (mIdVerifier.isWorking()) {
                            mIdVerifier.cancel();
                        }

                        setVocalEnroll();

//                        try {
//                            mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
//                            PcmRecorder.PcmRecordListener pcmRecordListener = new PcmRecorder.PcmRecordListener() {
//                                @Override
//                                public void onRecordBuffer(byte[] bytes, int offset, int length) {
//                                    StringBuffer params = new StringBuffer();
//                                    params.append("rgn=5,");
//                                    params.append("ptxt=" + mNumPwd + ",");
//                                    params.append("pwdt=" + PWD_TYPE_NUM + ",");
//                                    mIdVerifier.writeData("ivp", params.toString(), bytes, 0, length);
//                                }
//
//                                @Override
//                                public void onError(SpeechError speechError) {
//
//                                }
//
//                                @Override
//                                public void onRecordStarted(boolean b) {
//
//                                }
//
//                                @Override
//                                public void onRecordReleased() {
//
//                                }
//                            };
//                            mPcmRecorder.startRecording(pcmRecordListener);
//                        } catch (SpeechError e) {
//                            e.printStackTrace();
//                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();

                        mIdVerifier.stopWrite("ivp");
                        if (null != mPcmRecorder) {
                            mPcmRecorder.stopRecord(true);
                        }
                        break;
                }
                return false;
            }
        });

    }


    //注册声纹
    private void setVocalEnroll() {

        if(mNumPwdSegs == null || mNumPwdSegs.length <= 0){
            return;
        }

        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("请长按“按住说话”按钮！\n");
        strBuffer.append("请读出：" + mNumPwdSegs[0] + "\n");
        strBuffer.append("训练 第" + 1 + "遍，剩余4遍\n");
        textPasswordID.setText(strBuffer.toString());

        // 设置声纹注册参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "enroll");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, "king1234566");
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mVerifyListener);
    }

    //获取数字密码
    public void getDownPassword(){
        // 获取密码之前先终止之前的操作
        mIdVerifier.cancel();
        mNumPwd = null;
        // 下载密码时，按住说话触摸无效
        btuCommit.setClickable(false);

        mProDialog.setMessage("下载中...");
        mProDialog.show();

        // 设置下载密码参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");

        // 子业务执行参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();
        // 设置模型操作的密码类型
        params.append("pwdt=" + PWD_TYPE_NUM + ",");
        // 执行密码下载操作
        mIdVerifier.execute("ivp", "download", params.toString(), new IdentityListener() {
            @Override
            public void onResult(IdentityResult result, boolean b) {
                mProDialog.dismiss();
                btuCommit.setClickable(true);

                String json = result.getResultString();
                if(!TextUtils.isEmpty(json)){
                    VoiceprintPasswordInfo bean = new Gson().fromJson(json, VoiceprintPasswordInfo.class);

                    if(bean != null){

                        mNumPwdSegs = new String[bean.getNum_pwd().size()];

                        for (int i = 0 ; i < bean.getNum_pwd().size(); i ++){
                           String numPasswrod = bean.getNum_pwd().get(i);
                            mNumPwdSegs[i] = numPasswrod;
                            if(i == 0){
                                mNumPwd = numPasswrod;
                            }
                        }

                        textPasswordID.setText(TextUtils.isEmpty(mNumPwd) ? "" : mNumPwd);
                    }
                }
            }

            @Override
            public void onError(SpeechError error) {
                mProDialog.dismiss();
                textPasswordID.setText("密码下载失败！" + error.getPlainDescription(true));
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }

    //验证监听器
    private IdentityListener mVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d("king---", result.getResultString());

            JSONObject jsonResult = null;
            try {
                jsonResult = new JSONObject(result.getResultString());
                int ret = jsonResult.getInt("ret");

                if (ErrorCode.SUCCESS == ret) {

                    final int suc = Integer.parseInt(jsonResult.optString("suc"));
                    final int rgn = Integer.parseInt(jsonResult.optString("rgn"));

                    if (suc == rgn) {
                        textPasswordID.setText("注册成功");

                        if (mPcmRecorder != null) {
                            mPcmRecorder.stopRecord(true);
                        }
                    } else {
                        int nowTimes = suc + 1;
                        int leftTimes = 5 - nowTimes;

                        StringBuffer strBuffer = new StringBuffer();
                        strBuffer.append("请长按“按住说话”按钮！\n");
                        strBuffer.append("请读出：" + mNumPwdSegs[nowTimes - 1] + "\n");
                        strBuffer.append("训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
                        textPasswordID.setText(strBuffer.toString());
                    }

                } else {
                    Toast.makeText(VoiceprintRecognizeActivity.this,new SpeechError(ret).getPlainDescription(true) ,Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_VOLUME == eventType) {
                Toast.makeText(VoiceprintRecognizeActivity.this,"音量：" + arg1 ,Toast.LENGTH_SHORT).show();
            } else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
                Toast.makeText(VoiceprintRecognizeActivity.this,"录音结束" ,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SpeechError error) {
            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            Toast.makeText(VoiceprintRecognizeActivity.this,error.getPlainDescription(true) ,Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public void finish() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
        super.finish();
    }
}
