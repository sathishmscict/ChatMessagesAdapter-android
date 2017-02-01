package com.quickblox.sample.chatadapter.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.request.RequestListener;
import com.google.gson.Gson;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chatadapter.R;
import com.quickblox.sample.chatadapter.utils.UserData;
import com.quickblox.ui.kit.chatmessage.adapter.QBMessagesAdapter;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;


public class CustomMessageAdapter extends QBMessagesAdapter<QBChatMessage> {
    private static final String TAG = CustomMessageAdapter.class.getSimpleName();
    protected static final int TYPE_OWN_VIDEO_ATTACH = 5;
    protected static final int TYPE_OPPONENT_VIDEO_ATTACH = 6;

    private QBUser currentUser;
    private QBUser opponentUser;
    private UserData currentUserData;
    private UserData opponentUserData;

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages, ArrayList<QBUser> qbUsers) {
        super(context, chatMessages);
        setUsers(qbUsers);
    }

    private void setUsers(ArrayList<QBUser> qbUsers) {
        int currentUserID = QBChatService.getInstance().getUser().getId();

        for (QBUser user : qbUsers) {
            if (user.getId().equals(currentUserID)) {
                currentUser = user;
            } else {
                opponentUser = user;
            }
        }
        setUsersData();
    }

    private void setUsersData() {
        currentUserData = new Gson().fromJson(currentUser.getCustomData(), UserData.class);
        opponentUserData = new Gson().fromJson(opponentUser.getCustomData(), UserData.class);
    }

    @Override
    protected int customViewType(int position) {
        QBChatMessage chatMessage = getItem(position);

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = chatMessage.getAttachments().iterator().next();
            if (QBAttachment.VIDEO_TYPE.equals(attachment.getType())) {
                return isIncoming(chatMessage) ? TYPE_OPPONENT_VIDEO_ATTACH : TYPE_OWN_VIDEO_ATTACH;
            }
        }
        return -1;
    }

    @Override
    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateCustomViewHolder viewType= " + viewType);
        return viewType == TYPE_OWN_VIDEO_ATTACH ? new ImageAttachHolder(inflater.inflate(R.layout.list_item_attach_right, parent, false),
                R.id.msg_image_attach, R.id.msg_progressbar_attach, R.id.msg_text_time_attach) : null;
    }


    @Override
    protected void onBindViewCustomHolder(QBMessageViewHolder holder, QBChatMessage chatMessage, int position) {
        displayAttachment(holder, position);
        holder.avatar.setVisibility(View.GONE);
    }

    @Override
    protected void onBindViewMsgRightHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        TextView view = (TextView) holder.itemView.findViewById(R.id.custom_text_view);
        view.setText(currentUser.getFullName());
        super.onBindViewMsgRightHolder(holder, chatMessage, position);
    }

    @Override
    protected String getImageUrl(int position) {
        QBAttachment attachment = getQBAttach(position);
        return attachment.getUrl();
    }

    @Override
    public String obtainAvatarUrl(int valueType, QBChatMessage chatMessage) {
        return currentUser.getId().equals(chatMessage.getSenderId()) ?
                currentUserData.getUserAvatar() : opponentUserData.getUserAvatar();
    }
}
