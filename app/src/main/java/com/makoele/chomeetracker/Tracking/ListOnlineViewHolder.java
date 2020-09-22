package com.makoele.chomeetracker.Tracking;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makoele.chomeetracker.Interfaces.ItemClickListener;
import com.makoele.chomeetracker.R;

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtEmail;
    ItemClickListener itemClickListener;

    public ListOnlineViewHolder(View itemView) {
        super(itemView);

        txtEmail = (TextView)itemView.findViewById(R.id.txt_Email);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition() );
    }
}
