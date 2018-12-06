package neotalk.neolabs.com.neotalk.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import neotalk.neolabs.com.neotalk.R;
import neotalk.neolabs.com.neotalk.chat.MessageActivity;
import neotalk.neolabs.com.neotalk.model.ChatModel;
import neotalk.neolabs.com.neotalk.model.UserModel;

public class ChatFragment extends Fragment {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private String uid;
        private List<ChatModel>chatModels = new ArrayList<>();
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter() {
             uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

             FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {

                 @Override
                 public void onDataChange(DataSnapshot dataSnapshot) {
                     chatModels.clear();
                     for(DataSnapshot item : dataSnapshot.getChildren()) {
                          chatModels.add(item.getValue(ChatModel.class));
                     }
                     notifyDataSetChanged();
                 }

                 @Override
                 public void onCancelled(DatabaseError databaseError) {

                 }
             });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat, viewGroup, false);


            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {

            final CustomViewHolder customViewHolder = (CustomViewHolder)viewHolder;
            String destinationUid = null;

            for(String user: chatModels.get(i).users.keySet()) {

                if(!user.equals(uid)) {
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }

                FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        Glide.with(customViewHolder.itemView.getContext())
                                .load(userModel.profileImageUrl)
                                .apply(new RequestOptions().circleCrop())
                                .into(customViewHolder.imageView);

                        customViewHolder.textview_title.setText(userModel.userName);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.<String>reverseOrder());
                commentMap.putAll(chatModels.get(i).comments);
                String lastMessageKey = (String) commentMap.keySet().toArray()[0];
                customViewHolder.textView_last_messages.setText(chatModels.get(i).comments.get(lastMessageKey).message);

                customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MessageActivity.class);
                        intent.putExtra("destinationUid", destinationUsers.get(i));
                        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }
                });

                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                long unixTime = (long) chatModels.get(i).comments.get(lastMessageKey).timestamp;
                Date date = new Date(unixTime);

                customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));

            }

        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textview_title;
            public TextView textView_last_messages;
            public TextView textView_timestamp;

            public CustomViewHolder(View view) {
                super(view);

                imageView = (ImageView) view.findViewById(R.id.chatitem_imageview);
                textview_title = (TextView) view.findViewById(R.id.chatitem_textview_title);
                textView_last_messages = (TextView) view.findViewById(R.id.chatitem_textview_lastMessage);
                textView_timestamp.findViewById(R.id.chatitem_textView_timestamp);
            }
    }

    }
}
