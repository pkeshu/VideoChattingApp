package com.pickndrop.keshartestappforvediocalling.main.ui;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.models.Contacts;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactAdapter {

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.image_contact)
        protected ImageView userImage;

        @BindView(R.id.name_contact)
        protected TextView nameTxtV;

        @BindView(R.id.call_btn)
        protected Button callBtn;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
