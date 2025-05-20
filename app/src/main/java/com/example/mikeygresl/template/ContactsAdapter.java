package com.example.mikeygresl.template;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contact> contactItemList;
    private View.OnClickListener onContactClickListtener;

    public ContactsAdapter(List<Contact> contactItemList) {
        this.contactItemList = contactItemList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_item, viewGroup, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ContactViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.itemView.setOnClickListener(onContactClickListtener);

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ContactViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.setOnClickListener(null);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder contactViewHolder, int i) {
        final Contact contact = contactItemList.get(i);

        contactViewHolder.fullnameTextView.setText(contact.getFname() + " " + contact.getLname());
        contactViewHolder.emailTextView.setText(contact.getEmail());

        contactViewHolder.avatar().setInfo(contact.getUID(), contact.getFname(), contact.getLname());
        contactViewHolder.avatar().invalidateSelf();
    }

    @Override
    public int getItemCount() {
        return contactItemList.size();
    }

    public void setOnContactClickListener(View.OnClickListener listener) {
        onContactClickListtener = listener;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        private TextView fullnameTextView;
        private TextView emailTextView;
        private ImageView imageView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            final AvatarDrawable avatarDrawable = new AvatarDrawable(itemView.getContext());
            avatarDrawable.setInfo("Test", "Unknown", "User");

            imageView = itemView.findViewById(R.id.avatar);
            imageView.setImageDrawable(avatarDrawable);

            fullnameTextView = itemView.findViewById(R.id.fullnameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
        }

        public AvatarDrawable avatar(){
            return (AvatarDrawable) imageView.getDrawable();
        }
    }
}
