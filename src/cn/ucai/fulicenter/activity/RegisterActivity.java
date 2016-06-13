/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.ucai.fulicenter.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.Message;
import cn.ucai.fulicenter.data.OkHttpUtils;
import cn.ucai.fulicenter.listener.OnSetAvatarListener;
import cn.ucai.fulicenter.superWeChatApplication;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;

/**
 * 注册页
 */
public class RegisterActivity extends BaseActivity {
	final static String TAG = RegisterActivity.class.getCanonicalName();
	Activity mContext = this;
	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;
	private EditText userNickEditText;
	ImageView mIVAvatar;
	OnSetAvatarListener mOnSetAvatarListener;
	String avatarName;
	ProgressDialog pd;

	String username;
	String pwd;
	String nick;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(cn.ucai.fulicenter.R.layout.activity_register);
		initView();
		setListener();
	}

	private void setListener() {
		setRegisterListener();
		setLoginListener();
		setAvatarListener();
	}

	private void setAvatarListener() {
		findViewById(R.id.rl).setOnClickListener(new View.OnClickListener() {


			@Override
			public void onClick(View v) {
				mOnSetAvatarListener = new OnSetAvatarListener(mContext, R.id.ll,
						getAvatarName(), I.AVATAR_TYPE_USER_PATH);
			}
		});
	}

	private String getAvatarName() {
		avatarName = System.currentTimeMillis() + "";
		return avatarName;
	}
	private void setLoginListener() {
		findViewById(R.id.btnlogin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void initView() {
		userNameEditText = (EditText) findViewById(cn.ucai.fulicenter.R.id.username);
		passwordEditText = (EditText) findViewById(cn.ucai.fulicenter.R.id.password);
		confirmPwdEditText = (EditText) findViewById(cn.ucai.fulicenter.R.id.confirm_password);
		userNickEditText = (EditText) findViewById(R.id.usernick);
		mIVAvatar = (ImageView) findViewById(R.id.iv_photo);
		userNickEditText = (EditText) findViewById(R.id.usernick);
	}

	/**
	 * 注册
	 * @param
	 */
	private void setRegisterListener() {
		findViewById(R.id.btnregister).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				username = userNameEditText.getText().toString().trim();
				pwd = passwordEditText.getText().toString().trim();
				nick = userNickEditText.getText().toString().trim();
				String confirm_pwd = confirmPwdEditText.getText().toString().trim();
				if (TextUtils.isEmpty(username)) {
					userNameEditText.requestFocus();
					userNameEditText.setError(getResources().getString(cn.ucai.fulicenter.R.string.User_name_cannot_be_empty));
					return;
				} else if (!username.matches("[a-zA-Z]+[a-zA-Z0-9_]+")) {
					userNameEditText.requestFocus();
					userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_wd));
					return;
				} else if (TextUtils.isEmpty(pwd)) {
					Toast.makeText(mContext, getResources().getString(cn.ucai.fulicenter.R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
					passwordEditText.requestFocus();
					return;
				} else if (TextUtils.isEmpty(confirm_pwd)) {
					Toast.makeText(mContext, getResources().getString(cn.ucai.fulicenter.R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
					confirmPwdEditText.requestFocus();
					return;
				} else if (!pwd.equals(confirm_pwd)) {
					Toast.makeText(mContext, getResources().getString(cn.ucai.fulicenter.R.string.Two_input_password), Toast.LENGTH_SHORT).show();
					return;
				}

				if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
					pd = new ProgressDialog(mContext);
					pd.setMessage(getResources().getString(cn.ucai.fulicenter.R.string.Is_the_registered));
					pd.show();

					registerAppSever();
				}
			}
		});

	}

	private void registerAppSever() {
		//首先注册远端服务器账号，并上传头像---okhttp
		//注册环信账号
		//如果环信注册失败，调用取消注册的方法删除远端账号和图片
		File file = new File(ImageUtils.getAvatarPath(mContext,I.AVATAR_TYPE_USER_PATH),
				avatarName + I.AVATAR_SUFFIX_JPG);
		OkHttpUtils<Message> utils = new OkHttpUtils<Message>();
		utils.url(superWeChatApplication.SERVER_ROOT)
				.addParam(I.KEY_REQUEST,I.REQUEST_REGISTER)
				.addParam(I.User.USER_NAME,username)
				.addParam(I.User.PASSWORD,pwd)
				.addParam(I.User.NICK,nick)
				.targetClass(Message.class)
				.addFile(file)
				.execute(new OkHttpUtils.OnCompleteListener<Message>() {
					@Override
					public void onSuccess(Message result) {
						if (result.isResult()) {
							registerEMServer();
						} else {
							pd.dismiss();
							Utils.showToast(mContext, Utils.getResourceString(mContext, result.getMsg()), Toast.LENGTH_SHORT);
							Log.e(TAG, "register fail,error:" + result.getMsg());
						}
					}

					@Override
					public void onError(String error) {
						pd.dismiss();
						Toast.makeText(mContext, "连接服务器超时", Toast.LENGTH_LONG);
					}
				});

	}

	/*注册环信账号*/
	private void registerEMServer() {
		new Thread(new Runnable() {
			public void run() {
				try {
					// 调用sdk注册方法
					EMChatManager.getInstance().createAccountOnServer(username, pwd);
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							// 保存用户名
							superWeChatApplication.getInstance().setUserName(username);
							Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.fulicenter.R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
							finish();
						}
					});
				} catch (final EaseMobException e) {
					unRegister();
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							int errorCode = e.getErrorCode();
							if (errorCode == EMError.NONETWORK_ERROR) {
								Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.fulicenter.R.string.network_anomalies), Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.USER_ALREADY_EXISTS) {
								Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.fulicenter.R.string.User_already_exists), Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.UNAUTHORIZED) {
								Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.fulicenter.R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.ILLEGAL_USER_NAME) {
								Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.fulicenter.R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(getApplicationContext(), getResources().getString(cn.ucai.fulicenter.R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}
		}).start();
	}

	private void unRegister() {
		//取消远端服务器的注册
		//url=http://10.0.2.2:8080/SuperWeChatServer/Server?request=unregister&m_user_name=
		OkHttpUtils<Message> utils = new OkHttpUtils<Message>();
		utils.url(superWeChatApplication.SERVER_ROOT)
				.addParam(I.KEY_REQUEST,I.REQUEST_UNREGISTER)
				.addParam(I.User.USER_NAME,username)
				.targetClass(Message.class)
				.execute(new OkHttpUtils.OnCompleteListener<Message>() {
					@Override
					public void onSuccess(Message result) {
						if (result.isResult()) {

						} else {
							pd.dismiss();
							Utils.showToast(mContext, Utils.getResourceString(mContext, result.getMsg()), Toast.LENGTH_SHORT);
							Log.e(TAG, "register fail,error:" + result.getMsg());
						}
					}

					@Override
					public void onError(String error) {

					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode==RESULT_OK){
			mOnSetAvatarListener.setAvatar(requestCode,data,mIVAvatar);
		}
	}


	public void back(View view) {
		finish();
	}

}
