package com.andrew.link.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.andrew.link.R;
import com.andrew.link.global.Global;
import com.andrew.link.global.encryption;
import com.andrew.link.lists.Tokens;
import com.andrew.link.notify.FCM;
import com.andrew.link.notify.FCMresp;
import com.andrew.link.notify.Sender;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by CodeSlu on 01/11/18.
 */


public class ReactCustom extends Dialog {

    private Activity c;
    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    private String friendid, Mid;
    private ImageView like, funny, love, sad, angry, noR;
    private String reactMessage, messageReact;
    private FCM fcm;

    private LinearLayout reactMenu;

    public ReactCustom(Activity a, String friendid, String Mid, String messageReact) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.friendid = friendid;
        this.Mid = Mid;
        //message that you react on
        this.messageReact = messageReact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //view init
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.react_menu);
        like = findViewById(R.id.like);
        funny = findViewById(R.id.funny);
        love = findViewById(R.id.love);
        sad = findViewById(R.id.sad);
        angry = findViewById(R.id.angry);
        noR = findViewById(R.id.noR);
        reactMenu = findViewById(R.id.react_menu);

        if (Global.DARKSTATE)
            reactMenu.setBackground(ContextCompat.getDrawable(Global.conA, R.drawable.react_bg_d));
        else
            reactMenu.setBackground(ContextCompat.getDrawable(Global.conA, R.drawable.react_bg));

        //firebase init
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference(Global.CHATS);
        fcm = Global.getFCMservies();

        //encrypt

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload("like");
                reactMessage = "react//like//" + mAuth + friendid;
                dismiss();
            }
        });
        funny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload("funny");
                reactMessage = "react//funny//" + mAuth + friendid;
                dismiss();
            }
        });
        love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload("love");
                reactMessage = "react//love//" + mAuth + friendid;
                dismiss();
            }
        });
        sad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload("sad");
                reactMessage = "react//sad//" + mAuth + friendid;
                dismiss();
            }
        });
        angry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload("angry");
                reactMessage = "react//angry//" + mAuth + friendid;
                dismiss();
            }
        });
        noR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload("no");
                dismiss();
            }
        });
    }

    private void upload(final String reactF) {
        if (Global.check_int(Global.conA)) {
            final Map<String, Object> map = new HashMap<>();
            map.put("react", reactF);
            mData.child(mAuth.getCurrentUser().getUid()).child(friendid).child(Global.Messages).child(Mid)
                    .updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mData.child(friendid).child(mAuth.getCurrentUser().getUid()).child(Global.Messages).child(Mid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mData.child(friendid).child(mAuth.getCurrentUser().getUid()).child(Global.Messages).child(Mid)
                                        .updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (!reactF.equals("no"))
                                            sendMessNotify(Mid);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });





                }
            });

        } else
            Toast.makeText(Global.conA, Global.conA.getResources().getString(R.string.check_int), Toast.LENGTH_SHORT).show();

    }


    private void sendMessNotify(final String Mid) {

        final int[] i = {0};

        DatabaseReference mTokenget = FirebaseDatabase.getInstance().getReference(Global.tokens);
        mTokenget.child(friendid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Tokens tokens = dataSnapshot.getValue(Tokens.class);
                i[0]++;
                //encrypt
                reactMessage = encryption.encryptOrNull(reactMessage);
                messageReact = encryption.encryptOrNull(messageReact);

                Map<String, String> map = new HashMap<>();
                map.put("nType","message");
                map.put("tokens",tokens.toString());
                map.put("senderId",mAuth.getCurrentUser().getUid());
                map.put("senderName",Global.nameLocal);
                map.put("senderAva",Global.avaLocal);
                map.put("Mid",Mid);
                map.put("to", friendid);
                map.put("react",messageReact);
                map.put("message", reactMessage);
                Sender sender = new Sender(tokens.getTokens(), map);
                fcm.send(sender)
                        .enqueue(new Callback<FCMresp>() {
                            @Override
                            public void onResponse(Call<FCMresp> call, Response<FCMresp> response) {
                            }

                            @Override
                            public void onFailure(Call<FCMresp> call, Throwable t) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}