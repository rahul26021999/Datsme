package me.dats.com.datsme.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.dats.com.datsme.Adapters.BlockListAdapter;
import me.dats.com.datsme.Models.BlockUsers;
import me.dats.com.datsme.R;

public class BlocklistActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_blocklist)
    Toolbar toolbar;
    @BindView(R.id.blocklist_list)
    RecyclerView recyclerView;
    @BindView(R.id.EmptyBlockList)
    TextView textView;
    private BlockListAdapter adapter;
    private ArrayList<BlockUsers> blockedusersList = new ArrayList<>();
    private String TAG = "BlockListActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list_);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        adapter = new BlockListAdapter(blockedusersList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Blocklist").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if (dsp.child("blocked").exists()){
                        DatabaseReference userref = FirebaseDatabase.getInstance().getReference().child("Users").child(dsp.getKey());
                    userref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                BlockUsers user = new BlockUsers(
                                        dataSnapshot.child("name").getValue().toString()
                                        , dsp.getKey().toString()
                                        , dataSnapshot.child("thumb_image").getValue().toString()
                                        , dsp.child("blocked").getValue().toString());

                                blockedusersList.add(user);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        }

        @Override
        public void onCancelled (@NonNull DatabaseError databaseError){

        }
    });


}

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}



