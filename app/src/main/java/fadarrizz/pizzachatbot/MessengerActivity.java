package fadarrizz.pizzachatbot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import fadarrizz.pizzachatbot.Adapter.PizzaRecyclerAdapter;
import fadarrizz.pizzachatbot.Adapter.RecyclerItemClickListener;
import fadarrizz.pizzachatbot.Adapter.ToppingsRecyclerAdapter;
import fadarrizz.pizzachatbot.Helpers.Helpers;
import fadarrizz.pizzachatbot.Model.ChatMessage;
import fadarrizz.pizzachatbot.Model.Pizza;
import fadarrizz.pizzachatbot.Model.Topping;
import fadarrizz.pizzachatbot.ViewHolder.PizzaViewHolder;

public class MessengerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MessengerActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    FirebaseDatabase database;
    DatabaseReference chatRoot, pizzaReference, toppingsReference, messageRoot;
    String uid;

    RecyclerView recyclerView;
    RelativeLayout addButton;
    FirebaseRecyclerAdapter<ChatMessage,ChatRecord> adapter;
    private TextView editText;

    private AIRequest aiRequest;
    private AIDataService aiDataService;

    RelativeLayout buttonType, buttonToppings;
    Button nonVegButton, vegButton, yesButton, noButton;
    Handler handler;

    RelativeLayout sendText;
    private RecyclerView recyclerSelector;
    private PizzaRecyclerAdapter pizzaAdapter;
    private List<Pizza> pizzaList;

    private ToppingsRecyclerAdapter toppingsAdapter;
    private List<Topping> toppingsList;

    String action;
    String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign Out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        addButton = findViewById(R.id.addButton);
        buttonType = findViewById(R.id.buttonType);
        buttonToppings = findViewById(R.id.buttonToppings);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        database = FirebaseDatabase.getInstance();
        chatRoot = database.getReference("chat");
        chatRoot.keepSynced(true);

        // Store UID of current user
        uid = getIntent().getExtras().getString("uid");
        messageRoot = chatRoot.child(uid);

        // Dialogflow configuration
        String CLIENT_ACCESS_TOKEN = "256b5853978f475cacc91ed47c96cb60";
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(config);
        aiRequest = new AIRequest();

        addButton.setOnClickListener(this);

        /**
         * Update adapter with every message.
         */

        adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatRecord>(
                ChatMessage.class, R.layout.message_list,
                ChatRecord.class, messageRoot) {
            @Override
            protected void populateViewHolder(
                    ChatRecord viewHolder, ChatMessage model, int position) {
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
         * Scroll to bottom when new message is added.
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_log:
                chatRoot.child(uid).removeValue();
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handle general onClick and .
     */
    @Override
    public void onClick(View v){
        if (v == addButton) {
            message = editText.getText().toString().trim();
        } else if (v == nonVegButton) {
            message = "Non-vegetarian";
            visibilityAnimation(buttonType, false, 150, 0);
        } else if (v == vegButton) {
            message = "Vegetarian";
            visibilityAnimation(buttonType, false, 150, 0);
        } else if (v == yesButton) {
            message = "Yes";
            visibilityAnimation(buttonToppings, false, 150, 0);
        } else if (v == noButton) {
            message = "No";
            visibilityAnimation(buttonToppings, false, 150, 0);
        }

        sendRequestToBot(message);
    }

    /**
     * Send message as request to bot and receive response
     */
    public void sendRequestToBot(String message) {
        if (!message.equals("")) {

            // Set new message
            ChatMessage chatMessage = new ChatMessage(message, "user");
            chatRoot.child(uid).push().setValue(chatMessage);

            aiRequest.setQuery(message);
            new AsyncTask<AIRequest, Void, AIResponse>() {

                @Override
                protected AIResponse doInBackground(AIRequest... aiRequests) {
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
                        chatRoot.child(uid).push().setValue(chatMessage);

                        action = result.getAction();
                        startFunctionByAction(action);
                    }
                }
            }.execute(aiRequest);
        }
        clearViews();
    }

    /**
     * Start right function by action from Dialogflow response.
     */
    public void startFunctionByAction(String action) {
        switch (action) {
            case "pizza-type.get": getPizzaType();
                break;
            case "pizza.get": setPizzas();
                break;
            case "anyToppings.get": getToppingChoice();
                break;
            case "toppings.get": setToppings();
        }
    }

    /**
     * Clear all views
     */
    public void clearViews() {
        editText.setText("");
        if (pizzaList != null) {
            pizzaAdapter.clear();
        }
    }

    /**
     * Get type of pizza from user.
     */
    public void getPizzaType() {
        nonVegButton = findViewById(R.id.nonVegButton);
        nonVegButton.setOnClickListener(this);
        vegButton = findViewById(R.id.vegButton);
        vegButton.setOnClickListener(this);

        visibilityAnimation(buttonType, true, 400, 500);
    }

    /**
     * Create list of pizzas from Firebase.
     */
    public void setPizzas() {
        sendText = findViewById(R.id.sendText);
        visibilityAnimation(sendText, true, 400, 0);

        pizzaReference = database.getReference("pizza");

        recyclerSelector = findViewById(R.id.recyclerSelector);
        pizzaList = new ArrayList<>();

        recyclerSelector.setLayoutManager(new LinearLayoutManager(MessengerActivity.this,
                LinearLayoutManager.HORIZONTAL, false));
        pizzaAdapter = new PizzaRecyclerAdapter(pizzaList);
        recyclerSelector.setAdapter(pizzaAdapter);
        visibilityAnimation(recyclerSelector, true, 400, 0);

        pizzaReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @NonNull String s) {
                try {
                    Pizza model = dataSnapshot.getValue(Pizza.class);
                    pizzaList.add(model);
                    pizzaAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
                Toast.makeText(getApplicationContext(),
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
        onPizzaListener(recyclerSelector);
    }

    public void getToppingChoice() {
        yesButton = findViewById(R.id.yesToppings);
        noButton = findViewById(R.id.noToppings);
        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);

        visibilityAnimation(buttonToppings, true, 400, 500);
    }

    public void setToppings() {
        sendText = findViewById(R.id.sendText);
        visibilityAnimation(sendText, true, 400, 0);

        toppingsReference = database.getReference("toppings");

        toppingsList = new ArrayList<>();
        recyclerSelector.setLayoutManager(new LinearLayoutManager(MessengerActivity.this,
                LinearLayoutManager.VERTICAL, false));
        toppingsAdapter = new ToppingsRecyclerAdapter(toppingsList);
        recyclerSelector.setAdapter(toppingsAdapter);
        visibilityAnimation(recyclerSelector, true, 400, 0);

        toppingsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    Topping model = dataSnapshot.getValue(Topping.class);
                    toppingsList.add(model);
                    toppingsAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
                Toast.makeText(getApplicationContext(),
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
        onToppingsListener(recyclerSelector);

    }

    /**
     * Make onClick for all items in RecyclerView
     */
    public void onPizzaListener(final RecyclerView view) {
        view.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        visibilityAnimation(recyclerSelector, false, 400, 0);
                        TextView pizza = view.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.pizzaName);
                        String pizzaName = pizza.getText().toString();
                        sendRequestToBot(pizzaName);
                    }
                })
        );
    }

    public void onToppingsListener(final RecyclerView view) {
        view.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Button extra = view.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.toppingButton);
                        String extraName = extra.getText().toString();
                        sendRequestToBot(extraName);
                    }
                })
        );
    }

    /**
     * Animate behaviour of view.
     */
    public void visibilityAnimation(final View view, Boolean visible, final int duration, int delay) {
        final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        if (visible) {
            handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    view.setVisibility(View.VISIBLE);
                    slideUp.setDuration(duration);
                    view.startAnimation(slideUp);
                }
            }, delay);

        } else {
            handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    view.setVisibility(View.GONE);
                    slideDown.setDuration(duration);
                    view.startAnimation(slideDown);
                }
            }, delay);
        }
    }

    public void signOut() {
        mAuth.signOut();

        mGoogleSignInClient.signOut();

        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
