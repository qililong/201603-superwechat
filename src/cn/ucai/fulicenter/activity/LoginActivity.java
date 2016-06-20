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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;


import java.io.IOException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ucai.fulicenter.Constant;
import cn.ucai.fulicenter.DemoHXSDKHelper;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.applib.controller.HXSDKHelper;
import cn.ucai.fulicenter.bean.Message;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.data.OkHttpUtils;
import cn.ucai.fulicenter.db.EMUserDao;
import cn.ucai.fulicenter.db.UserDao;
import cn.ucai.fulicenter.domain.EMUser;
import cn.ucai.fulicenter.listener.OnSetAvatarListener;
import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.task.DownloadContactListTask;
import cn.ucai.fulicenter.utils.CommonUtils;
import cn.ucai.fulicenter.utils.MD5;
import cn.ucai.fulicenter.utils.Utils;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	Context mContext = this;
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private boolean progressShow;
	private boolean autoLogin = false;

	private String currentUsername;
	private String currentPassword;

	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUserNameChangedListener();
		setRegisterListener();
		setLoginListener();
	}

	private void setUserNameChangedListener() {
		// 如果用户名密码都有，直接进入主页面
		if (DemoHXSDKHelper.getInstance().isLogined()) {
			autoLogin = true;
			startActivity(new Intent(LoginActivity.this, FulicenterActivity.class));
			return;
		}
		setContentView(cn.ucai.fulicenter.R.layout.activity_login);

		usernameEditText = (EditText) findViewById(cn.ucai.fulicenter.R.id.username);
		passwordEditText = (EditText) findViewById(cn.ucai.fulicenter.R.id.password);

		// 如果用户名改变，清空密码
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordEditText.setText(null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		if (FuliCenterApplication.getInstance().getUserName() != null) {
			usernameEditText.setText(FuliCenterApplication.getInstance().getUserName());
		}
	}

	/**
	 * 登录
	 * 
	 * @param
	 */
	private void setLoginListener() {
		findViewById(R.id.btn_login1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!CommonUtils.isNetWorkConnected(mContext)) {
					Toast.makeText(mContext, cn.ucai.fulicenter.R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
					return;
				}
				currentUsername = usernameEditText.getText().toString().trim();
				currentPassword = passwordEditText.getText().toString().trim();

				if (TextUtils.isEmpty(currentUsername)) {
					Toast.makeText(mContext, cn.ucai.fulicenter.R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(currentPassword)) {
					Toast.makeText(mContext, cn.ucai.fulicenter.R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
					return;
				}

				showProgressShow();

				final long start = System.currentTimeMillis();
				// 调用sdk登陆方法登陆聊天服务器
				EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

					@Override
					public void onSuccess() {
						if (!progressShow) {
							return;
						}
						loginAppServer();
					}

					@Override
					public void onProgress(int progress, String status) {
					}

					@Override
					public void onError(final int code, final String message) {
						if (!progressShow) {
							return;
						}
						runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								Toast.makeText(getApplicationContext(), getString(cn.ucai.fulicenter.R.string.Login_failed) + message,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
			}
		});

	}

	private void loginAppServer() {
		UserDao dao = new UserDao(mContext);
		User user = dao.findUserByUserName(currentUsername);
		if (user != null) {
			if (user.getMUserPassword().equals(MD5.getData(currentPassword))) {
				saveUser(user);
				Log.e("main", "User:" + user.toString());
				loginSuccess();
			} else {
				pd.dismiss();
				Toast.makeText(getApplicationContext(), cn.ucai.fulicenter.R.string.login_failure_failed, Toast.LENGTH_LONG).show();
			}
		} else {
			try {
				String path = new ApiParams()
						.with(I.User.USER_NAME, currentUsername)
                        .with(I.User.PASSWORD, currentPassword)
						.getRequestUrl(I.REQUEST_LOGIN);
				executeRequest(new GsonRequest<User>(path, User.class, responseListener(), errorListener()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Response.Listener<User> responseListener() {
		return new Response.Listener<User>() {
			@Override
			public void onResponse(User user) {
				if (user.isResult()) {
					saveUser(user);
					loginSuccess();
				} else {
					pd.dismiss();
					Utils.showToast(mContext,Utils.getResourceString(mContext,user.getMsg()),Toast.LENGTH_SHORT);
				}
			}
		};
	}

	private void saveUser(User user) {
		FuliCenterApplication instance = FuliCenterApplication.getInstance();
		instance.setUser(user);
		// 登陆成功，保存用户名密码
		instance.setUserName(currentUsername);
		instance.setPassword(currentPassword);
		FuliCenterApplication.currentUserNick = user.getMUserNick();
	}

	private void loginSuccess() {
		try {
			// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
			// ** manually load all local groups and
			EMGroupManager.getInstance().loadAllGroups();
			EMChatManager.getInstance().loadAllConversations();
			//下载用户头像到SD卡
			final OkHttpUtils<Message> utils = new OkHttpUtils<Message>();
			utils.url(FuliCenterApplication.SERVER_ROOT)
					.addParam(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_AVATAR)
					.addParam(I.AVATAR_TYPE,currentUsername)
					.doInBackground(new Callback() {
						@Override
						public void onFailure(Request request, IOException e) {
							Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onResponse(com.squareup.okhttp.Response response) throws IOException {
							String avatarPath = I.AVATAR_TYPE_USER_PATH + I.BACKSLASH
									+ currentUsername + I.AVATAR_SUFFIX_JPG;
							File file = OnSetAvatarListener.getAvatarFile(LoginActivity.this, avatarPath);
							FileOutputStream out = null;
							out = new FileOutputStream(file);
							utils.downloadFile(response,file,false);
						}
					}).execute(null);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new DownloadContactListTask(mContext, currentUsername).execute();
				}
			});
			// 处理好友和群组
			initializeContacts();
		} catch (Exception e) {
			e.printStackTrace();
			// 取好友或者群聊失败，不让进入主页面
			runOnUiThread(new Runnable() {
				public void run() {
					pd.dismiss();
					DemoHXSDKHelper.getInstance().logout(true, null);
					Toast.makeText(getApplicationContext(), cn.ucai.fulicenter.R.string.login_failure_failed, Toast.LENGTH_LONG).show();
				}
			});
			return;
		}
		// 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
		boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
				FuliCenterApplication.currentUserNick.trim());
		if (!updatenick) {
			Log.e("LoginActivity", "update current user nick fail");
		}
		if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
			pd.dismiss();
		}
		// 进入主页面
		String action = getIntent().getStringExtra("action");
		Intent intent = new Intent(LoginActivity.this,
				FulicenterActivity.class)
				.putExtra("action",action);
		startActivity(intent);

		finish();
	}

	private void showProgressShow() {
		progressShow = true;
		pd = new ProgressDialog(LoginActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				progressShow = false;
			}
		});
		pd.setMessage(getString(cn.ucai.fulicenter.R.string.Is_landing));
		pd.show();
	}

	private void initializeContacts() {
		Map<String, EMUser> userlist = new HashMap<String, EMUser>();
		// 添加user"申请与通知"
		EMUser newFriends = new EMUser();
		newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getResources().getString(
				cn.ucai.fulicenter.R.string.Application_and_notify);
		newFriends.setNick(strChat);

		userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
		// 添加"群聊"
		EMUser groupUser = new EMUser();
		String strGroup = getResources().getString(cn.ucai.fulicenter.R.string.group_chat);
		groupUser.setUsername(Constant.GROUP_USERNAME);
		groupUser.setNick(strGroup);
		groupUser.setHeader("");
		userlist.put(Constant.GROUP_USERNAME, groupUser);
		
		// 添加"Robot"
//		EMUser robotUser = new EMUser();
//		String strRobot = getResources().getString(cn.ucai.superwechat.R.string.robot_chat);
//		robotUser.setUsername(Constant.CHAT_ROBOT);
//		robotUser.setNick(strRobot);
//		robotUser.setHeader("");
//		userlist.put(Constant.CHAT_ROBOT, robotUser);
		
		// 存入内存
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).setContactList(userlist);
		// 存入db
		EMUserDao dao = new EMUserDao(LoginActivity.this);
		List<EMUser> users = new ArrayList<EMUser>(userlist.values());
		dao.saveContactList(users);
	}
	
	/**
	 * 注册
	 * 
	 * @param
	 */
	private void setRegisterListener() {
		findViewById(R.id.btn_register1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(mContext, RegisterActivity.class), 0);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}

	public void back(View view) {
		finish();
	}
}
