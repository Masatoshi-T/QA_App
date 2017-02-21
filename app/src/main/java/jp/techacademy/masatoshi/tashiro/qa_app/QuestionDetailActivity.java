package jp.techacademy.masatoshi.tashiro.qa_app;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.R.attr.breadCrumbShortTitle;
import static android.R.attr.data;
import static android.R.attr.factor;
import static android.R.attr.focusable;
import static android.R.attr.textIsSelectable;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private Boolean isChecked = false;
    private ShineButton mFavoriteButton;
    private String mQuestionUid;
    private DatabaseReference mAnswerRef;
    private int mGenre;

    DatabaseReference mDataBase = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) { return; }
            }
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

        @Override
        public void onCancelled(DatabaseError databaseError) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        mQuestionUid = mQuestion.getQuestionUid();
        mGenre = mQuestion.getGenre();
        setTitle(mQuestion.getTitle());

        Log.d("Log_detail",String.valueOf(mGenre));

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mFavoriteButton = (ShineButton) findViewById(R.id.favorite_Button);

        if ( mFavoriteButton != null) {
            mFavoriteButton.init(QuestionDetailActivity.this);
            mFavoriteButton.setChecked(isChecked);
        }
        if (user != null) {
            mFavoriteButton.setVisibility(View.VISIBLE);
            DatabaseReference userRef = mDataBase.child(Const.UsersPATH).child(user.getUid());
            DatabaseReference favoriteRef = userRef.child("favorite");
            favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                final Map data = (Map) dataSnapshot.getValue();
                if (data == null || data.get(mQuestionUid) == null) {
                    isChecked = false;
                } else {
                    isChecked = true;
                }
                    mFavoriteButton.setChecked(isChecked);
                    mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!isChecked) {
                                isChecked = true;
                                mFavoriteButton.setChecked(isChecked);
                                DatabaseReference userRef = mDataBase.child(Const.UsersPATH).child(user.getUid());
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map data = (Map) dataSnapshot.getValue();
                                        if (data.get(mQuestionUid) == null) {
                                            Map<String, Object> data1 = new HashMap<String, Object>();
                                            data1.put(mQuestionUid, mQuestionUid);
                                            DatabaseReference userRef = mDataBase.child(Const.UsersPATH).child(user.getUid());
                                            DatabaseReference favoriteRef = userRef.child("favorite");
                                            favoriteRef.updateChildren(data1);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) { }
                                });
                            } else {
                                isChecked = false;
                                mFavoriteButton.setChecked(isChecked);
                                Map<String, Object> data1 = new HashMap<String, Object>();
                                data1.put(mQuestionUid,null);
                                DatabaseReference userRef = mDataBase.child(Const.UsersPATH).child(user.getUid());
                                DatabaseReference favoriteRef = userRef.child("favorite");
                                favoriteRef.updateChildren(data1);
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }else {
            mFavoriteButton.setVisibility(View.GONE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを収録する

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    intent.putExtra("genre",mGenre);
                    startActivity(intent);
                }
            }
        });
    }
}