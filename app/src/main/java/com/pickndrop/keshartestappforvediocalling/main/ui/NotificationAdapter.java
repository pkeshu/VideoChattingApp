package com.pickndrop.keshartestappforvediocalling.main.ui;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.models.Contacts;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotificationAdapter {

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_notification)
        protected ImageView userImage;

        @BindView(R.id.name_notification)
        protected TextView nameTxtV;

        @BindView(R.id.request_accept_btn)
        protected Button accept_btn;
        @BindView(R.id.declined_btn)
        protected Button declinedBtn;
        private Contacts contacts;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            Picasso.get().load(contacts.getImage()).into(userImage);
//            nameTxtV.setText(contacts.getName());
        }

        @OnClick(R.id.declined_btn)
        protected void declinedBtnPressed(View view) {

        }

        @OnClick(R.id.request_accept_btn)
        protected void acceptBtnPressed(View view) {

        }

        public void setData(Contacts contacts) {
            this.contacts = contacts;
        }
    }
}
