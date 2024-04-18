package my.edu.utar.groupassignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import my.edu.utar.groupassignment.Adapters.DiscussionAdapter;


public class past_year_discussion extends AppCompatActivity {

    DatabaseReference mDatabase;
    Button postBtn;
    EditText discussionText;
    ImageView btEmoji;
//    List<PaperDiscussion> discussionList;
//    ArrayAdapter<PaperDiscussion> adapter;
//    ListView listViewDiscussions;

    private ListView listViewDiscussions;
    private DiscussionAdapter adapter;
    private List<PaperDiscussion> discussionList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_year_discussion);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        postBtn = findViewById(R.id.postBtn);
        discussionText = findViewById(R.id.commentEditText);
        btEmoji = findViewById(R.id.bt_emoji);
        listViewDiscussions = findViewById(R.id.listViewDiscussions);

        // Assign the xml view to java

        discussionList = new ArrayList<>();
        adapter = new DiscussionAdapter(this, R.layout.activity_past_year_discussion, discussionList);
        listViewDiscussions.setAdapter(adapter);

        discussionList.add(new PaperDiscussion( "Hello World"));
        discussionList.add(new PaperDiscussion( "This is a test message"));
        adapter.notifyDataSetChanged();

        //emoji popup
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.listViewDiscussions)
        ).build(discussionText);

        btEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toggle between text and emoji
                popup.toggle();

            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Retrive and add The discussion comment
                addDiscussion();

                addEmojiText();
                retrieveDiscussions();
//
            }
        });

        // Retrieve the file name from the intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("pdfFileName")) {
            String pdfFileName = intent.getStringExtra("pdfFileName");

            // Load the PDF file based on the file name
            loadPDF(pdfFileName);
        }


    }

    private void addEmojiText(){
        String text = discussionText.getText().toString();
        if (!text.isEmpty()) {
//            EmojiTextView emojiTextView = (EmojiTextView) LayoutInflater.from(this).inflate(R.layout.emoji_text_view, listViewDiscussions, false);
//            emojiTextView.setText(text);
//            listViewDiscussions.addView(emojiTextView);
//            discussionText.getText().clear(); // Clear after adding to view
            discussionList.add(new PaperDiscussion(text));  // Assuming PaperDiscussion can be initialized this way
            adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
            discussionText.getText().clear();

        }
    }

    private void addDiscussion() {

        String comment = discussionText.getText().toString();
        if(!comment.isEmpty()){
            PaperDiscussion newDiscussion = new PaperDiscussion(comment);
            discussionList.add(newDiscussion);
            adapter.notifyDataSetChanged();

            DatabaseReference commentsRef = mDatabase.child("Comments").push();
            commentsRef.setValue(newDiscussion).addOnSuccessListener(aVoid->{
                Toast.makeText(past_year_discussion.this, "Comment added!", Toast.LENGTH_SHORT).show();
                discussionText.setText("");
            }).addOnFailureListener(e -> Toast.makeText(past_year_discussion.this, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }

    }
    private void loadPDF(String pdfName) {
        PDFView pdfView = findViewById(R.id.pdfView);
//
        pdfView.fromAsset(pdfName).load();


//        pdfView.fromUri(Uri.parse(url)).load();
//    }
    }
    private void retrieveDiscussions() {
        DatabaseReference discussionRef = mDatabase.child("Comments");
        discussionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                discussionList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PaperDiscussion discussion = snap.getValue(PaperDiscussion.class);
                    if (discussion != null) {
                        discussionList.add(discussion);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(past_year_discussion.this, "Error loading discussions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}