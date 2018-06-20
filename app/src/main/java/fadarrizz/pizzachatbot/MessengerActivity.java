package fadarrizz.pizzachatbot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import fadarrizz.pizzachatbot.Adapter.PizzaRecyclerAdapter;
import fadarrizz.pizzachatbot.Helpers.Helpers;
import fadarrizz.pizzachatbot.Adapter.ToppingsRecyclerAdapter;
import fadarrizz.pizzachatbot.Model.ChatMessage;
import fadarrizz.pizzachatbot.Model.Pizza;
import fadarrizz.pizzachatbot.Model.Topping;

public class MessengerActivity extends AppCompatActivity {

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

    RelativeLayout buttonLayout;
    Button nonVegButton, vegButton, yesButton, noButton;
    Handler handler;

    RelativeLayout sendText;
    private RecyclerView recyclerSelector;
    private PizzaRecyclerAdapter pizzaAdapter;
    private List<Pizza> pizzaList;

    private ToppingsRecyclerAdapter toppingsAdapter;
    private List<Topping> toppingsList;
    ArrayList<String> selectedToppings;
    ViewGroup.LayoutParams initialParams;

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
        buttonLayout = findViewById(R.id.buttonLayout);

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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = editText.getText().toString().trim();
                sendRequestToBot(message);
            }
        });

        /**
         * Update adapter with every message.
         */
        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(messageRoot, ChatMessage.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatRecord>(options) {

            @NonNull
            @Override
            public ChatRecord onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new ChatRecord(inflater.inflate(R.layout.message_list, parent, false));
            }

            @Override
            protected void onBindViewHolder(final ChatRecord viewHolder, int position, final ChatMessage model) {
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
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
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
        if (toppingsList != null) {
            toppingsAdapter.clear();
        }
    }

    /**
     * Get type of pizza from user.
     */
    public void getPizzaType() {
        nonVegButton = findViewById(R.id.button1);
        final String nonVeg = "Non-vegetarian";
        nonVegButton.setText(getResources().getString(R.string.nonVeg));
        nonVegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityAnimation(buttonLayout, false, 150, 0);
                sendRequestToBot(nonVeg);
            }
        });
        vegButton = findViewById(R.id.button2);
        final String veg = "Vegetarian";
        vegButton.setText(getResources().getString(R.string.Veg));
        vegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityAnimation(buttonLayout, false, 150, 0);
                sendRequestToBot(veg);
            }
        });

        visibilityAnimation(buttonLayout, true, 400, 500);
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
        selectedToppings = new ArrayList<String>();

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
        pizzaAdapter.setOnItemClickListener(new PizzaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                visibilityAnimation(recyclerSelector, false, 400, 0);
                String pizzaName = pizzaList.get(position).getName();
                sendRequestToBot(pizzaName);
                Log.d("Pizza send", pizzaName);
            }
        });
    }

    /**
     * Get answer from user if any additional toppings is wanted
     */
    public void getToppingChoice() {
        yesButton = findViewById(R.id.button1);
        final String yes = getResources().getString(R.string.Yes);
        yesButton.setText(yes);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityAnimation(buttonLayout, false, 150, 0);
                sendRequestToBot(yes);
            }
        });
        noButton = findViewById(R.id.button2);
        final String no = getResources().getString(R.string.No);
        noButton.setText(no);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityAnimation(buttonLayout, false, 150, 0);
                sendRequestToBot(no);
            }
        });
        visibilityAnimation(buttonLayout, true, 400, 500);
    }

    public void setToppings() {
        sendText = findViewById(R.id.sendText);
        visibilityAnimation(sendText, true, 400, 0);

        toppingsReference = database.getReference("toppings");
//        recyclerSelector = findViewById(R.id.toppingsSelector);

        toppingsList = new ArrayList<>();
        Topping done = new Topping("Done", "");
        toppingsList.add(done);

        recyclerSelector.setLayoutManager(new LinearLayoutManager(MessengerActivity.this, LinearLayoutManager.VERTICAL, false));
        toppingsAdapter = new ToppingsRecyclerAdapter(toppingsList);
        recyclerSelector.setAdapter(toppingsAdapter);

        changeLayoutParams(recyclerSelector, 150);

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
        toppingsAdapter.setOnItemClickListener(new ToppingsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Button button, int position) {
                String toppingName = toppingsList.get(position).getName();
                if (toppingName.equals("Done")) {
                    visibilityAnimation(recyclerSelector, false, 400, 0);
                    sendRequestToBot(toppingName);
                }
                changeButtonColor(button, toppingName);
            }
        });
    }

    public void changeButtonColor(Button button, String toppingName) {
        if (!selectedToppings.contains(toppingName)) {
            selectedToppings.add(toppingName);
            button.setBackgroundResource(R.drawable.button_clicked);
        } else {
            selectedToppings.remove(toppingName);
            button.setBackgroundResource(R.drawable.button);
        }
    }

    /**
     * Change height of recyclerview
     */
    public void changeLayoutParams(RecyclerView recyclerView, int dp) {
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();

        // Save initial params
        initialParams = params;

        // Set params by converting dp to px
        params.height= Helpers.convertDpToPx(150);
    }

    /**
     * Change button color
     */
    public void changeButtonColor(Button button, final boolean isClicked) {

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

    /**
     * Sign out of Google
     */
    public void signOut() {
        mAuth.signOut();

        mGoogleSignInClient.signOut();

        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
