package com.makoele.chomeetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makoele.chomeetracker.Model.User;

import java.util.ArrayList;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder>
{
    ArrayList<User>nameList;
    Context c;
    public MembersAdapter(ArrayList<User> nameList, Context c)
    {
        this.nameList = nameList;
        this.c = c;
    }

    @Override
    public int getItemCount(){
        return nameList.size();
    }

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,parent,false);
        MembersViewHolder membersViewHolder = new MembersViewHolder(v ,c, nameList);

        return membersViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {

        User currentUserObj = nameList.get(position);
        holder.name_txt.setText(currentUserObj.getEmail());

    }

    public static class MembersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView name_txt;
        Context c;
        ArrayList<User>nameArrayList;
        FirebaseAuth auth;
        FirebaseUser user;

        public MembersViewHolder(View itemView, Context c, ArrayList<User>nameArrayList){
            super(itemView);
            this.c=c;
            this.nameArrayList = nameArrayList;

            itemView.setOnClickListener(this);
            auth = FirebaseAuth.getInstance();
            user= auth.getCurrentUser();

            name_txt = itemView.findViewById(R.id.item_title);
        }


        @Override
        public void onClick(View view) {
            Toast.makeText(c,"You ahve clicked this user",Toast.LENGTH_LONG).show();
        }
    }
}
