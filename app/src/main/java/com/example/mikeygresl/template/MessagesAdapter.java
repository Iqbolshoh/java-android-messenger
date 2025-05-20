package com.example.mikeygresl.template;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.TextMessageCell> {

    private final List<Message> messages = new ArrayList<>();
    private View.OnClickListener onMessageClickListener;
    private View.OnLongClickListener onMessageLongClickListener;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String currentUID = auth.getCurrentUser().getUid();

    public void addMessage(Message message) {

        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public Message getMessage(int position) {

        return messages.get(position);
    }

    public void setOnMessageClickListener(View.OnClickListener onMessageClickListener) {

        this.onMessageClickListener = onMessageClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {

        this.onMessageLongClickListener = onLongClickListener;
    }

    @Override
    public TextMessageCell onCreateViewHolder(ViewGroup parent, int viewType) {

        return new TextMessageCell(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_cell, parent, false));
    }

    @Override
    public void onViewAttachedToWindow(TextMessageCell holder) {

        super.onViewAttachedToWindow(holder);
        holder.itemView.setOnLongClickListener(onMessageLongClickListener);
    }

    @Override
    public void onViewDetachedFromWindow(TextMessageCell holder) {

        super.onViewDetachedFromWindow(holder);
        holder.itemView.setOnClickListener(null);
        holder.itemView.setOnLongClickListener(null);
    }

    @Override
    public void onBindViewHolder(TextMessageCell cell, int position) {
        final Message message = getMessage(position);
        cell.message_text.setText(message.getContent());
        cell.message_sender.setText(message.getSender_email());
        cell.message_timestamp.setText(DateUtils.formatDateTime(cell.container.getContext(),
                message.getTimestamp(), DateUtils.FORMAT_SHOW_TIME));

        if (TextUtils.isEmpty(message.getContent())) {
            cell.message_text.setVisibility(View.GONE);
        } else {
            cell.message_text.setVisibility(View.VISIBLE);
        }

        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) cell.container.getLayoutParams();
        if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(message.getSender_email())) {
            lp.gravity = Gravity.RIGHT;
        } else {
            lp.gravity = Gravity.LEFT;
            cell.container.setBackgroundResource(R.drawable.rounded_message_box_rec);
            cell.message_text.setTextColor(Color.WHITE);
            cell.message_timestamp.setTextColor(Color.WHITE);

        }

        if (!message.hasImage()) {
            cell.image.setImageDrawable(null);
            cell.image.setVisibility(View.GONE);
        } else {
            cell.image.setVisibility(View.VISIBLE);
            final int maxWidth = (int) (cell.itemView.getResources().getDisplayMetrics().density * 220);

            Picasso.get()
                    .load(message.getURL())
                    .resize(maxWidth, 0)
                    .placeholder(new ColorDrawable(Color.LTGRAY))
                    .centerCrop()
                    .config(Bitmap.Config.RGB_565)
                    .into(cell.image);
        }
    }

    @Override
    public int getItemCount() {

        return messages.size();
    }

    static class TextMessageCell extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final ViewGroup container;
        private final TextView message_text;
        private final TextView message_sender;
        private final TextView message_timestamp;

        public TextMessageCell(View itemView) {

            super(itemView);
            container = itemView.findViewById(R.id.message_cell);
            image = itemView.findViewById(R.id.message_image);
            message_text = itemView.findViewById(R.id.message_text);
            message_sender = itemView.findViewById(R.id.message_sender);
            message_timestamp = itemView.findViewById(R.id.message_timestamp);
        }
    }
}
