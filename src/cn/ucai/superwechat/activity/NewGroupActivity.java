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
package cn.ucai.superwechat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.listener.OnSetAvatarListener;

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
		setContentView(cn.ucai.superwechat.R.layout.activity_new_group);
		groupNameEditText = (EditText) findViewById(cn.ucai.superwechat.R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(cn.ucai.superwechat.R.id.edit_group_introduction);
		checkBox = (CheckBox) findViewById(cn.ucai.superwechat.R.id.cb_public);
		memberCheckbox = (CheckBox) findViewById(cn.ucai.superwechat.R.id.cb_member_inviter);
		openInviteContainer = (LinearLayout) findViewById(cn.ucai.superwechat.R.id.ll_open_invite);

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
				String str6 = getResources().getString(cn.ucai.superwechat.R.string.Group_name_cannot_be_empty);
				String name = groupNameEditText.getText().toString();
				if (TextUtils.isEmpty(name)) {
					Intent intent = new Intent(mContext, AlertDialog.class);
					intent.putExtra("msg", str6);
					startActivity(intent);
				} else {
					// 进通讯录选人
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
		if(resultCode != RESULT_OK){
			return;
		}
		if (resultCode == CREATE_NEW_GROUP) {
			//新建群组
			setProgressDialog();
			createNewGroup(data);
		} else {
			mOnSetAvatarListener.setAvatar(requestCode, data, groupPhoto);
		}
	}

	private void createNewGroup(final Intent data) {
		final String st2 = getResources().getString(cn.ucai.superwechat.R.string.Failed_to_create_groups);
		new Thread(new Runnable() {
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
				EMGroup emGroup;
				try {
					if(checkBox.isChecked()){
						//创建公开群，此种方式创建的群，可以自由加入
						//创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
						emGroup = EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true,200);
					}else{
						//创建不公开群
						emGroup = EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(),200);
					}
					String hxid = emGroup.getGroupId();
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
						}
					});
				} catch (final EaseMobException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}

			}
		}).start();
	}

	private void setProgressDialog() {
		String st1 = getResources().getString(cn.ucai.superwechat.R.string.Is_to_create_a_group_chat);
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
