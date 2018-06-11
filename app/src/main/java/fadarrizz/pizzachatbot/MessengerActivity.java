package fadarrizz.pizzachatbot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIOutputContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import fadarrizz.pizzachatbot.Adapter.MessageAdapter;
import fadarrizz.pizzachatbot.Model.ChatMessage;

public class MessengerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MessengerActivity";

    RecyclerView recyclerView;
    RelativeLayout addButton;
    DatabaseReference databaseReference;
    FirebaseRecyclerAdapter<ChatMessage,ChatRecord> adapter;
    private TextView editText;

    private AIRequest aiRequest;
    private AIDataService aiDataService;

    RelativeLayout buttonLayout;
    Button nonVegButton;
    Button vegButton;


    AIOutputContext context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        addButton = findViewById(R.id.addButton);
        buttonLayout = findViewById(R.id.buttonLayout);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        // Empty chat node
        databaseReference.child("chat").removeValue();

        // Dialogflow token
        String CLIENT_ACCESS_TOKEN = "256b5853978f475cacc91ed47c96cb60";
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(config);
        aiRequest = new AIRequest();

        addButton.setOnClickListener(this);

        /**
         * Create new adapter
         */
        adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatRecord>(
                ChatMessage.class, R.layout.message_list,
                ChatRecord.class, databaseReference.child("chat")) {
            @Override
            protected void populateViewHolder(ChatRecord viewHolder, ChatMessage model, int position) {
                if (model.getMessageUser().equals("user")) {

                    viewHolder.rightText.setText(model.getMessageText());

                    viewHolder.rightText.setVisibility(View.VISIBLE);
                    viewHolder.leftText.setVisibility(View.GONE);
                } else {
                    viewHolder.leftText.setText(model.getMessageText());

                    viewHolder.leftText.setVisibility(View.VISIBLE);
                    viewHolder.rightText.setVisibility(View.GONE);
                }
            }
        };

        /**
         * Scroll to bottom when new message is added
         */
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();

                if (lastVisiblePosition == -1 || (positionStart >= (messageCount - 1) &&
                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        String message = "";

        if (v == addButton) {
            message = editText.getText().toString().trim();
        } else if (v == nonVegButton) {
            message = "Non-vegetarian";
        } else if (v == vegButton) {
            message = "Vegetarian";
        }

        if (!message.equals("")) {

            // Set new message
            ChatMessage chatMessage = new ChatMessage(message, "user");
            databaseReference.child("chat").push().setValue(chatMessage);

            aiRequest.setQuery(message);
            new AsyncTask<AIRequest, Void, AIResponse>() {

                @Override
                protected AIResponse doInBackground(AIRequest... aiRequests) {
                    final AIRequest request = aiRequests[0];
                    try {
                        final AIResponse response = aiDataService.request(aiRequest);
                        return response;
                    } catch (AIServiceException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(AIResponse response) {
                    if (response != null) {
                        Result result = response.getResult();
                        String reply = result.getFulfillment().getSpeech();
                        ChatMessage chatMessage = new ChatMessage(reply, "bot");
                        databaseReference.child("chat").push().setValue(chatMessage);

                        context = result.getContext("awaiting_pizza_type");
                        if (context != null) {
                            getPizzaType();
                        }

                    }
                }
            }.execute(aiRequest);
        }
        // Empty textView
        editText.setText("");

        buttonLayout.setVisibility(View.GONE);
    }

    public void getPizzaType() {

        buttonLayout.setVisibility(View.VISIBLE);

        nonVegButton = findViewById(R.id.nonVegButton);
        nonVegButton.setOnClickListener(this);
        vegButton = findViewById(R.id.vegButton);
        vegButton.setOnClickListener(this);
    }
}
