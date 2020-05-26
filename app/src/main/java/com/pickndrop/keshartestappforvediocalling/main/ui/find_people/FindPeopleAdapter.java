package com.pickndrop.keshartestappforvediocalling.main.ui.find_people;

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

public class FindPeopleAdapter {
    public static class FindPeopleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //        private OnAcceptBtnClick onAcceptBtnClick;
        private OnItemClick onItemClick;
        @BindView(R.id.image_contact)
        protected ImageView userImage;

        @BindView(R.id.name_contact)
        protected TextView nameTxtV;

//        @BindView(R.id.request_accept_btn)
//        protected Button acceptBtn;

        @BindView(R.id.call_btn)
        protected Button callBtn;

        private Contacts contacts;

        public FindPeopleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void setData(Contacts contacts) {
            this.contacts = contacts;
            nameTxtV.setText(contacts.getName());
            Picasso.get()
                    .load(contacts.getImage()).into(userImage);
            callBtn.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            if (onItemClick != null)
                onItemClick.ItemClickListerner(view, contacts, getAdapterPosition());
        }

        public interface OnItemClick {
            void ItemClickListerner(View view, Contacts contacts, int position);
        }

        public void setItemClickListerner(OnItemClick onItemClick) {
            this.onItemClick = onItemClick;
        }

//        public interface OnAcceptBtnClick {
//            void AcceptBtnClickListerner(View view, Contacts contacts, int position);
//        }
//
//        public void setOnAcceptBtnClickListerner(OnAcceptBtnClick onClickListerner) {
//            this.onAcceptBtnClick = onClickListerner;
//        }

//        public interface OnRejectBtnClick {
//            void RejectBtnClickListerner(View view, Contacts contacts, int position);
//        }
//
//        public void setOnRejectBtnClickListerner(OnRejectBtnClick onClickListerner) {
//            this.onRejectBtnClick = onClickListerner;
//        }
    }


}
