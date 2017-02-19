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
import com.google.firebase.database.ValueEventListener;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;
import static android.R.attr.factor;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private ShineButton mShineButton;
    private Boolean boo;
    private Boolean isChecked;
    private String str;
    private ShineButton mFavoriteButton;
    private int mGenre;
    private String mQuestionUid;

    private DatabaseReference mAnswerRef;

    Map<String, Object> data = new HashMap<String, Object>();
    DatabaseReference mDataBase = FirebaseDatabase.getInstance().getReference();

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }



            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("Log","変更したよ2");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        mGenre = mQuestion.getGenre();
        mQuestionUid = mQuestion.getQuestionUid();
        setTitle(mQuestion.getTitle());

        isChecked = Boolean.valueOf(mQuestion.getFavorite());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mFavoriteButton = (ShineButton) findViewById(R.id.favorite_Button);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            mFavoriteButton.setVisibility(View.VISIBLE);
            if (mGenre == 0) {
                mFavoriteButton.setVisibility(View.GONE);
            } else {
                mFavoriteButton.setVisibility(View.VISIBLE);
            }
            mFavoriteButton.setEnabled(true);
            if ( mFavoriteButton != null) {
                Log.d("Log", "起動");
                Log.d("Log",isChecked.toString());
                mFavoriteButton.init(this);
                mFavoriteButton.setChecked(isChecked);
            }
            mFavoriteButton.setChecked(isChecked);

            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isChecked){
                        isChecked = true;
                        mFavoriteButton.setChecked(isChecked);
                        data.put("favorite", "true");
                        Log.d("Log","true");
                    } else {
                        isChecked = false;
                        mFavoriteButton.setChecked(isChecked);
                        data.put("favorite", "false");
                        Log.d("Log","false");
                    }
                mDataBase.child(Const.ContentsPATH).child(String.valueOf(mGenre)).child(mQuestionUid).updateChildren(data);
                }
            });
        } else {
            mFavoriteButton.setVisibility(View.GONE);
            mFavoriteButton.setEnabled(false);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (mGenre == 0) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを収録する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });
    }
}