package kingkong.facerecognize.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

import kingkong.facerecognize.app.R;

/**
 * Created by KingKong-HE on 2018/1/3.
 *
 * @author KingKong-HE
 * @Time 2018/1/3
 * @Email 709872217@QQ.COM
 */
public class MainActivity extends AppCompatActivity {

    private Button btuLoginID;

    private Button btuCleanID;

    private Button btuRegFaceID;

    private Button btuVeID;

    private Button btuNextID;

    private EditText loginNameID;

    //采用身份识别接口进行在线人脸识别
    private IdentityVerifier mIdVerifier;

    // 数字密码类型为3，其他类型暂未开放
    private static final int PWD_TYPE_NUM = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginNameID = findViewById(R.id.loginNameID);
        btuNextID = findViewById(R.id.btuNextID);
        btuCleanID = findViewById(R.id.btuCleanID);
        btuLoginID = findViewById(R.id.btuLoginID);
        btuRegFaceID = findViewById(R.id.btuRegFaceID);
        btuVeID = findViewById(R.id.btuVeID);

        mIdVerifier = IdentityVerifier.createVerifier(MainActivity.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    Toast.makeText(MainActivity.this, "引擎初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "引擎初始化失败，错误码：" + errorCode, Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginNameID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btuCleanID.setVisibility(View.GONE);
                btuLoginID.setVisibility(View.GONE);
                btuRegFaceID.setVisibility(View.GONE);
                btuVeID.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //查询按钮
        btuNextID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFaceModelCommand(false);
            }
        });

        //去登录按钮
        btuLoginID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //清除模型数据按钮
        btuCleanID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFaceModelCommand(true);
//                getVPModelCommand(true);
            }
        });

        //注册脸部模型按钮
        btuRegFaceID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceRegisteredActivity.class);
                intent.putExtra("login_name",loginNameID.getText().toString());
                startActivityForResult(intent,20);
            }
        });

        //注册声纹模型按钮
        btuVeID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login_name = loginNameID.getText().toString();
                Intent intent = new Intent(MainActivity.this, VoiceprintRegisteredActivity.class);
                intent.putExtra("login_name",loginNameID.getText().toString());
                startActivityForResult(intent,20);
            }
        });

    }

    //查询&删除人脸模型 flag =true 删除 否则 查询
    private void getFaceModelCommand(final boolean flag) {

        String mAuthid = loginNameID.getText().toString();

        if (TextUtils.isEmpty(mAuthid)) {
            Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设置人脸模型操作参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthid);

        // 设置模型参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();

        String paramStr = "query";
        if(flag){
            paramStr= "delete";
        }

        // 执行模型操作
        mIdVerifier.execute("ifr", paramStr, params.toString(), new IdentityListener() {
            @Override
            public void onResult(IdentityResult result, boolean islast) {
                Log.d("king--", result.getResultString());

                JSONObject jsonResult = null;
                int ret = ErrorCode.SUCCESS;
                try {
                    jsonResult = new JSONObject(result.getResultString());
                    ret = jsonResult.getInt("ret");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (ErrorCode.SUCCESS == ret) {
                    if(flag){
                       loginNameID.setText("");
                        Toast.makeText(MainActivity.this, "人脸模型删除成功", Toast.LENGTH_SHORT).show();
                    }else{
                        getVPModelCommand(false);
                    }
                } else { }
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            }

            @Override
            public void onError(SpeechError error) {
                // 弹出错误信息
                Toast.makeText(MainActivity.this, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
                btuRegFaceID.setVisibility(View.VISIBLE);
            }
        });
    }

    //查询&删除声纹模型 flag =true 删除 否则 查询
    private void getVPModelCommand(final boolean flag) {

        String mAuthid = loginNameID.getText().toString();

        if (TextUtils.isEmpty(mAuthid)) {
            Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设置人脸模型操作参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthid);

        // 设置模型参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();

        String paramStr = "query";
        if(flag){
            paramStr= "delete";
        }

        // 设置模型操作的密码类型
        params.append("pwdt=" + PWD_TYPE_NUM + ",");

        // 执行模型操作
        mIdVerifier.execute("ivp", paramStr, params.toString(), new IdentityListener() {
            @Override
            public void onResult(IdentityResult result, boolean islast) {
                Log.d("king--", result.getResultString());

                JSONObject jsonResult = null;
                int ret = ErrorCode.SUCCESS;
                try {
                    jsonResult = new JSONObject(result.getResultString());
                    ret = jsonResult.getInt("ret");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (ErrorCode.SUCCESS == ret) {
                    if(flag){
                        loginNameID.setText("");
                        Toast.makeText(MainActivity.this, "声纹模型删除成功", Toast.LENGTH_SHORT).show();
                    }else{
                        btuCleanID.setVisibility(View.VISIBLE);
                        btuLoginID.setVisibility(View.VISIBLE);
                    }
                } else { }
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            }

            @Override
            public void onError(SpeechError error) {
                // 弹出错误信息
                Toast.makeText(MainActivity.this, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
                btuVeID.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != 20){
            return;
        }

        if(resultCode == 21){

            btuCleanID.setVisibility(View.GONE);
            btuLoginID.setVisibility(View.GONE);
            btuRegFaceID.setVisibility(View.GONE);
            btuVeID.setVisibility(View.GONE);

            getFaceModelCommand(false);
        }
    }
}
