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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.Contact;
import cn.ucai.fulicenter.bean.Group;
import cn.ucai.fulicenter.bean.Message;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.data.OkHttpUtils;
import cn.ucai.fulicenter.listener.OnSetAvatarListener;
import cn.ucai.fulicenter.superWeChatApplication;
import cn.ucai.fulicenter.utils.ImageUtils;

public class NewGroupActivity extends BaseActivity {
	private EditText groupNameEditText;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox checkBox;
	private CheckBox memberCheckbox;
	private LinearLayout openInviteContainer;
	ImageView groupPhoto;

	OnSetAvatarListener mOnSetAvatarListener;
	NewGroupActivity mContext;
	String name;
	private String avatarName;
	ProgressDialog dialog;
	private final int CREATE_NEW_GROUP = 10;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(cn.ucai.fulicenter.R.layout.activity_new_group);
		groupNameEditText = (EditText) findViewById(cn.ucai.fulicenter.R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(cn.ucai.fulicenter.R.id.edit_group_introduction);
		checkBox = (CheckBox) findViewById(cn.ucai.fulicenter.R.id.cb_public);
		memberCheckbox = (CheckBox) findViewById(cn.ucai.fulicenter.R.id.cb_member_inviter);
		openInviteContainer = (LinearLayout) findViewById(cn.ucai.fulicenter.R.id.ll_open_invite);

		groupPhoto = (ImageView) findViewById(R.id.tv_group_avatar);
		mContext = this;

		setListenner();
	}

	private void setListenner() {
		setGroupAvatarListenner();
		setOnCheckedChangeListener();
		setSaveGroupListenner();
	}

	private void setSaveGroupListenner() {
		findViewById(R.id.butnSave).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str6 = getResources().getString(cn.ucai.fulicenter.R.string.Group_name_cannot_be_empty);
				String name = groupNameEditText.getText().toString();
				if (TextUtils.isEmpty(name)) {
					Intent intent = new Intent(mContext, AlertDialog.class);
					intent.putExtra("msg", str6);
					startActivity(intent);
				} else {
					// 进通讯录选人
					Log.i("main", "进通讯录选人");
					startActivityForResult(new Intent(mContext, GroupPickContactsActivity.class).putExtra("groupName", name), CREATE_NEW_GROUP);
				}
			}
		});
	}

	private void setOnCheckedChangeListener() {
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					openInviteContainer.setVisibility(View.INVISIBLE);
				} else {
					openInviteContainer.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void setGroupAvatarListenner() {
		groupPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOnSetAvatarListener = new OnSetAvatarListener(mContext, R.id.new_group,
						getAvatarName(), I.AVATAR_TYPE_GROUP_PATH);
			}
		});
	}



	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("main", "OOOOOOOOOOOOKKKKKKKKKKKKKKKK");
		if(resultCode != RESULT_OK) {
			Log.i("main", "NNNNNNNNNNNNNNNNNOOOOOOOOOOOOOOOOO");
			return;
		}
		if (requestCode == CREATE_NEW_GROUP) {
			//新建群组
			Log.i("main", "NNNNNNNNNNNNNNNNNNNNNNNNNNNN");
			setProgressDialog();
			createNewGroup(data);
		} else {
			mOnSetAvatarListener.setAvatar(requestCode, data, groupPhoto);
		}
	}

	private void createNewGroup(final Intent data) {
		final String st2 = getResources().getString(cn.ucai.fulicenter.R.string.Failed_to_create_groups);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 调用sdk创建群组方法
				String groupName = groupNameEditText.getText().toString().trim();
				String desc = introductionEditText.getText().toString();
				Contact[] contacts = (Contact[]) data.getSerializableExtra("newmembers");
				String[] members = null;
				String[] membersId = null;
				if (contacts != null) {
					members = new String[contacts.length];
					membersId = new String[contacts.length];
					for (int i = 0; i < contacts.length; i++) {
						members[i] = contacts[i].getMContactCname() + ",";
						membersId[i] = contacts[i].getMContactId() + ",";
					}
				}
				final EMGroup emGroup;
				try {
					if (checkBox.isChecked()) {
						//创建公开群，此种方式创建的群，可以自由加入
						//创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
						emGroup = EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true, 200);
					} else {
						//创建不公开群
						emGroup = EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(), 200);
					}
					String hxid = emGroup.getGroupId();
					createNewGroupAppserver(groupName, hxid, desc, contacts);
//					runOnUiThread(new Runnable() {
//						public void run() {
//							progressDialog.dismiss();
//							setResult(RESULT_OK);
//							finish();
//						}
//					});
				} catch (final EaseMobException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}

			}
		});
	}

	public final String boundary = "apiclient-" + System.currentTimeMillis();
	public final String mimeType = "multipart/form-data;boundary=" + boundary;

	private void createNewGroupAppserver(String groupName,final String hxid, String desc,final Contact[] contacts) {
		Log.i("main", "createNewGroupAppserver");
		final String st2 = getResources().getString(cn.ucai.fulicenter.R.string.Failed_to_create_groups);

		File file = new File(ImageUtils.getAvatarPath(mContext, I.AVATAR_TYPE_GROUP_PATH),
				name + I.AVATAR_SUFFIX_JPG);
		boolean isPublic = checkBox.isChecked();
		boolean isInvites = memberCheckbox.isChecked();
		User user = superWeChatApplication.getInstance().getUser();


		OkHttpUtils<Group> utils = new OkHttpUtils<>();

		utils.url(superWeChatApplication.SERVER_ROOT)
				.addParam(I.KEY_REQUEST, I.REQUEST_CREATE_GROUP)
				.addParam(I.Group.HX_ID, hxid)
				.addParam(I.Group.NAME, groupName)
				.addParam(I.Group.DESCRIPTION, desc)
				.addParam(I.Group.OWNER, user.getMUserName())
				.addParam(I.Group.IS_PUBLIC, isPublic + "")
				.addParam(I.Group.ALLOW_INVITES, isInvites + "")
				.addParam(I.User.USER_ID, user.getMUserId() + "")
				.targetClass(Group.class)
				.addFile(file)
				.execute(new OkHttpUtils.OnCompleteListener<Group>() {
					@Override
					public void onSuccess(final Group result) {
						if (result.isResult()) {
							if (contacts != null) {
								addGroupMembers(result, contacts, hxid);
								mContext.sendStickyBroadcast(new Intent("update_contact_list"));
								progressDialog.dismiss();
								setResult(RESULT_OK);
								finish();
							} else {
								progressDialog.dismiss();
								superWeChatApplication.getInstance().getGroupList().add(result);
								setResult(RESULT_OK);
								Toast.makeText(NewGroupActivity.this, R.string.Create_groups_Success, Toast.LENGTH_LONG).show();
								finish();
							}
						} else {
							runOnUiThread(new Runnable() {
								public void run() {
									progressDialog.dismiss();
									Toast.makeText(NewGroupActivity.this, R.string.Create_groups_Failed, Toast.LENGTH_LONG).show();
								}
							});
						}

					}

					@Override
					public void onError(String error) {
						progressDialog.dismiss();
						Toast.makeText(NewGroupActivity.this, st2 + error, Toast.LENGTH_LONG).show();
					}
				});
	}

	private void addGroupMembers(final Group result,Contact[] contacts, String hxid) {
		String userIds = "";
		String userNames = "";


		for (int i = 0; i < contacts.length; i++) {
			userNames = contacts[i].getMContactCname() + ",";
			userIds = contacts[i].getMContactId() + ",";
		}


		try {
			String path = new ApiParams()
                    .with(I.Member.USER_ID, userIds)
                    .with(I.Member.USER_NAME, userNames)
                    .with(I.Member.GROUP_HX_ID, hxid)
                    .getRequestUrl(I.REQUEST_ADD_GROUP_MEMBER);
			executeRequest(new GsonRequest<Message>(path,Message.class,addGroupMembersListener(result),errorListener()));
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private Response.Listener<Message> addGroupMembersListener(final Group result) {
		return new Response.Listener<Message>() {
			@Override
			public void onResponse(Message message) {
				if (message.isResult()) {
					progressDialog.dismiss();
					superWeChatApplication.getInstance().getGroupList().add(result);
					setResult(RESULT_OK);
					Toast.makeText(NewGroupActivity.this, R.string.Create_groups_Success, Toast.LENGTH_LONG).show();
				} else {
					progressDialog.dismiss();
					Toast.makeText(NewGroupActivity.this, R.string.Create_groups_Failed, Toast.LENGTH_LONG).show();
				}
				finish();
			}
		};
	}




	private void setProgressDialog() {
		String st1 = getResources().getString(cn.ucai.fulicenter.R.string.Is_to_create_a_group_chat);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(st1);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}


	public void back(View view) {
		finish();
	}

	public String getAvatarName() {
		name = System.currentTimeMillis() + "";
		return name;
	}

}
