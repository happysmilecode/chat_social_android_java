package ar.codeslu.plax.global;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import androidx.multidex.MultiDex;
import android.util.DisplayMetrics;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.instacart.library.truetime.TrueTime;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ar.codeslu.plax.LockScreen;
import ar.codeslu.plax.db.TinyDB;
import in.myinnos.customfontlibrary.TypefaceUtil;


/**
 * Created by mostafa on 29/10/18.
 */

public class AppBack extends Application {
    //Vars
    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean wasInBackground;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
    //Firebase
    FirebaseAuth mAuth;
    DatabaseReference mData;
    //shared
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    //vars
    String choosenFont,choosenLang;
    TinyDB tinydb ;
    boolean lockState;
    @Override
    public void onCreate() {
        super.onCreate();
        //gettime
        new InitTrueTimeAsyncTask().execute();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//Emoji init
        EmojiManager.install(new IosEmojiProvider());
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference(Global.USERS);

        //notif counter
        settings = getSharedPreferences("Setting", MODE_PRIVATE);
        editor = settings.edit();

        //chat saver
        tinydb = new TinyDB(this);

        //change app lang
         choosenLang = shared().getString("lang", "en");
         choosenFont = shared().getString("font", "8");
         lockState =   shared().getBoolean("lock", false);
        changelang(choosenLang);
        changefont(Integer.parseInt(choosenFont));
        if(shared().getBoolean("lock", false) && mAuth.getCurrentUser() != null)
        {
            //lock screen
            lockscreen(shared().getBoolean("lock", false));
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);


    }

    public void startOnline() {
        if (mAuth.getCurrentUser() != null) {
            //init data
            Map<String, Object> map = new HashMap<>();
            map.put(Global.Online, true);
            mData.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
            Global.local_on = true;
            mData.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (Global.check_int(getApplicationContext()) && !wasInBackground && !Global.local_on) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(Global.Online, true);
                            mData.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
                            Global.local_on = true;
                        }
                        //check data
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put(Global.Online, false);
                        map2.put(Global.time, ServerValue.TIMESTAMP);
                        mData.child(mAuth.getCurrentUser().getUid()).onDisconnect().updateChildren(map2);
                        Global.local_on = false;

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }

    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                AppBack.this.wasInBackground = true;
                if (mAuth.getCurrentUser() != null) {
                    if (Global.check_int(getApplicationContext())) {
                        mData.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    //check data
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(Global.Online, false);
                                    map.put(Global.time, ServerValue.TIMESTAMP);
                                    mData.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
                                    Global.local_on = false;
                                    Global.currentactivity = null;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }

        this.wasInBackground = false;
    }

    public class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                TrueTime.build()
                        //.withSharedPreferences(SampleActivity.this)
                        .withNtpHost("time.google.com")
                        .withLoggingEnabled(false)
                        .withConnectionTimeout(3_1428)
                        .initialize();
            } catch (IOException e) {
            }
            return null;
        }
    }




    public SharedPreferences.Editor editSharePrefs() {
        return editor;
    }

    public SharedPreferences shared() {
        return settings;
    }

    public void changelang(String choosenLang) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(choosenLang.toLowerCase()));
        editSharePrefs().putString("lang", choosenLang);
        editSharePrefs().apply();
        res.updateConfiguration(conf, dm);
    }
    public void changefont(int i) {
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/"+i+".ttf");
        editSharePrefs().putString("font", String.valueOf(i));
        editSharePrefs().apply();
    }

    public void setchatsdb(String friendId)
    {
        tinydb.putListChats(mAuth.getCurrentUser().getUid() + "/" + friendId, Global.messG);
    }
    public void getchatsdb(String friendId)
    {
        Global.messG = tinydb.getListChats(mAuth.getCurrentUser().getUid() + "/" + friendId);
    }

    public void setdialogdb(String userId)
    {
        tinydb.putListDialog(userId, Global.diaG);
    }
    public void getdialogdb(String userId)
    {
        Global.diaG = tinydb.getListDialog(userId);
    }
    public  void lockscreenE()
    {
        Intent intent = new Intent(this,LockScreen.class);
        intent.putExtra("typeL",0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public  void lockscreen(boolean locked)
    {
        if(locked) {
            Intent intent = new Intent(this, LockScreen.class);
            intent.putExtra("typeL", 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


}

