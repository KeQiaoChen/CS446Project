package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;

/**
 * Created by Qian on 2018-02-23.
 */

public class ChooseSessionActivity extends AppCompatActivity {

    private Context applicationContext;
    private TextView waitMessage;
    private IntentFilter chooseSessionIntentFilter;
    private BroadcastReceiver chooseSessionActivityReceiver;
    private ArrayAdapter<String> sessionListAdapter;
    private ArrayList<String> sessionNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_session);
        final ListView sessionList = findViewById(R.id.listViewSessionsList);
        waitMessage = findViewById(R.id.textViewWaitForSessionNames);
        applicationContext = getApplicationContext();
        //sessionNames.add("Demo");
        sessionListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sessionNames);
        sessionList.setAdapter(sessionListAdapter);
        //waitMessage.setVisibility(View.INVISIBLE);
        Button buttonJoin = findViewById(R.id.buttonJoin);
        buttonJoin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String selectedSessionName = (String) sessionList.getItemAtPosition(sessionList.getCheckedItemPosition());
                // Broadcast a targeted Intent to create ParticipantMusicPlayerActivity (screen 3 in
                // mockup). This Intent contains the name of the session the user has chosen to
                // join.
                Intent chooseSessionIntent = new Intent(ChooseSessionActivity.this,
                                ParticipantMusicPlayerActivity.class);
                chooseSessionIntent
                        .putExtra(applicationContext.getString(R.string.session_name),
                                selectedSessionName);
                startActivity(chooseSessionIntent);
            }

        });
    }

    protected void onStart() {
        super.onStart();
        chooseSessionIntentFilter = new IntentFilter();
        // Whenever a new session is found, display the session name in the list of available
        // sessions so the user can choose to join it.
        chooseSessionIntentFilter
                .addAction(applicationContext.getString(R.string.find_session_return));
        chooseSessionActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.find_session_return))) {
                    sessionNames.clear();
                    sessionNames.add(intent.getStringExtra(applicationContext.getString(R.string
                            .available_sessions_key)));
                    sessionListAdapter.notifyDataSetChanged();
                    waitMessage.setVisibility(View.INVISIBLE);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(chooseSessionActivityReceiver,
                chooseSessionIntentFilter);
        // Broadcast an Intent to tell the app that the user is looking for a session to join.
        broadcastIntentWithoutExtras(
                applicationContext.getString(R.string.find_session_message),this);
    }

    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(chooseSessionActivityReceiver);
        chooseSessionIntentFilter = null;
        chooseSessionActivityReceiver = null;
    }

}
