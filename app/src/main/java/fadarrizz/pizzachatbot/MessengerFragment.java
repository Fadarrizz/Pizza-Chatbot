package fadarrizz.pizzachatbot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import fadarrizz.pizzachatbot.Adapter.PizzaRecyclerAdapter;
import fadarrizz.pizzachatbot.Adapter.ToppingsRecyclerAdapter;
import fadarrizz.pizzachatbot.Helpers.Helpers;
import fadarrizz.pizzachatbot.Model.ChatMessage;
import fadarrizz.pizzachatbot.Model.Order;
import fadarrizz.pizzachatbot.Model.Pizza;
import fadarrizz.pizzachatbot.Model.Topping;
import fadarrizz.pizzachatbot.ViewHolder.ChatRecord;

public class MessengerFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "MessengerFragment";

    private View mFragment;
    private Context thisContext;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseDatabase database;
    private DatabaseReference chatRoot, pizzaReference, toppingsReference, messageRoot;
    private String uid;

    private TextView emptyView;
    private RecyclerView recyclerView;
    private RelativeLayout addButton;
    private FirebaseRecyclerAdapter<ChatMessage,ChatRecord> adapter;
    private TextView editText;

    private AIRequest aiRequest;
    private AIDataService aiDataService;
    private AIService aiService;

    private RelativeLayout buttonLayout;
    private Button nonVegButton, vegButton, yesButton, noButton;
    private Handler handler;

    private RelativeLayout sendText;
    private Boolean flagFab = true;
    private RecyclerView recyclerSelector;
    private PizzaRecyclerAdapter pizzaAdapter;
    private List<Pizza> pizzaList;
    private Query pizzaTypeQuery;

    private ToppingsRecyclerAdapter toppingsAdapter;
    private List<Topping> toppingsList;
    private ArrayList<String> selectedToppings;
    private ViewGroup.LayoutParams initialParams;

    private Order order;

    private String action;
    private String message = "";

    public MessengerFragment() {
        // Required empty public constructor
    }

    public static MessengerFragment newInstance() {
        return new MessengerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        thisContext = Objects.requireNonNull(getActivity()).getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragment = inflater.inflate(R.layout.fragment_messenger, container, false);

        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign Out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(thisContext, gso);

        recyclerView = mFragment.findViewById(R.id.recyclerView);
        editText = mFragment.findViewById(R.id.editText);
        addButton = mFragment.findViewById(R.id.addButton);
        buttonLayout = mFragment.findViewById(R.id.buttonLayout);
        emptyView = mFragment.findViewById(R.id.empty_view);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        database = FirebaseDatabase.getInstance();
        chatRoot = database.getReference("chat");
        chatRoot.keepSynced(true);

        uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        messageRoot = chatRoot.child(uid);

        setHelpText();

        // Dialogflow configuration
        String CLIENT_ACCESS_TOKEN = "256b5853978f475cacc91ed47c96cb60";
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(config);
        aiRequest = new AIRequest();
        aiService = AIService.getService(thisContext, config);
        aiService.setListener(new AIListener() {
            @Override
            public void onResult(AIResponse result) {
                sendRequestToBot(result.getResult().getResolvedQuery());
            }
            @Override
            public void onError(AIError error) {

            }
            @Override
            public void onAudioLevel(float level) {

            }
            @Override
            public void onListeningStarted() {

            }
            @Override
            public void onListeningCanceled() {

            }
            @Override
            public void onListeningFinished() {

            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = editText.getText().toString().trim();
                sendRequestToBot(message);
            }
        });

        listenForTextChange();

        /**
         * Update view with every message.
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
            protected void onBindViewHolder(@NonNull final ChatRecord viewHolder, int position, @NonNull final ChatMessage model) {
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

        return mFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messenger_menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_log:
                chatRoot.child(uid).removeValue();
                setHelpText();
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
    @SuppressLint("StaticFieldLeak")
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
        } else {
            aiService.startListening();
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
                break;
            case "pizza.done": orderPizza();
                break;
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
        order = new Order();

        pizzaReference = database.getReference("pizza");

        nonVegButton = mFragment.findViewById(R.id.button1);
        final String nonVeg = "Non-vegetarian";
        nonVegButton.setText(getResources().getString(R.string.nonVeg));
        nonVegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityAnimation(buttonLayout, false, 150, 0);
                sendRequestToBot(nonVeg);
                order.setCategory(nonVeg);
                pizzaTypeQuery = pizzaReference.orderByChild("type").equalTo("non-veg");
            }
        });
        vegButton = mFragment.findViewById(R.id.button2);
        final String veg = "Vegetarian";
        vegButton.setText(getResources().getString(R.string.Veg));
        vegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityAnimation(buttonLayout, false, 150, 0);
                sendRequestToBot(veg);
                order.setCategory(veg);
                pizzaTypeQuery = pizzaReference.orderByChild("type").equalTo("veg");
            }
        });
        visibilityAnimation(buttonLayout, true, 400, 500);
    }

    /**
     * Create list of pizzas from Firebase.
     */
    public void setPizzas() {
        sendText = mFragment.findViewById(R.id.sendText);
        visibilityAnimation(sendText, true, 400, 0);

        recyclerSelector = mFragment.findViewById(R.id.recyclerSelector);
        pizzaList = new ArrayList<>();
        selectedToppings = new ArrayList<String>();

        recyclerSelector.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        pizzaAdapter = new PizzaRecyclerAdapter(pizzaList);
        recyclerSelector.setAdapter(pizzaAdapter);
        visibilityAnimation(recyclerSelector, true, 400, 0);

        pizzaTypeQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
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
                Toast.makeText(getContext(),
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
                order.setPizza(pizzaName);
            }
        });
    }

    /**
     * Ask user for additional toppings
     */
    public void getToppingChoice() {
        yesButton = mFragment.findViewById(R.id.button1);
        final String yes = getResources().getString(R.string.yes);
        yesButton.setText(yes);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityAnimation(buttonLayout, false, 150, 0);
                sendRequestToBot(yes);
            }
        });
        noButton = mFragment.findViewById(R.id.button2);
        final String no = getResources().getString(R.string.no);
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

    /**
     * Get toppings from Firebase and set recyclerview
     */
    public void setToppings() {
        sendText = mFragment.findViewById(R.id.sendText);
        visibilityAnimation(sendText, true, 400, 0);

        toppingsReference = database.getReference("toppings");

        toppingsList = new ArrayList<>();
        Topping done = new Topping("Done", "");
        toppingsList.add(done);

        recyclerSelector.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
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
                Toast.makeText(getContext(),
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
                    order.setToppings(selectedToppings);
                    order.setOrderDate(new Date());
                    order.setOrderTime((int) System.currentTimeMillis());
                } else {
                    changeButtonColor(button, toppingName);
                }
            }
        });
    }

    /**
     * Store order in Firebase
     */
    public void orderPizza() {
        order.setID(database.getReference("orders")
                .child(uid)
                .push().getKey());
        database.getReference("orders")
                .child(uid)
                .push()
                .setValue(order);
    }

    /**
     * Change button color
     */
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
     * Change height of recycler view
     */
    public void changeLayoutParams(RecyclerView recyclerView, int dp) {
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();

        // Save initial params
        initialParams = params;

        // Set params by converting dp to px
        params.height= Helpers.convertDpToPx(150);
    }

    /**
     * Animate behaviour of view.
     */
    public void visibilityAnimation(final View view, Boolean visible, final int duration, int delay) {
        final Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        final Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
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
     * Listen changes in textview and update button image
     */
    public void listenForTextChange() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = mFragment.findViewById(R.id.fab_img);
                String text = s.toString().trim();

                if (!text.isEmpty() && flagFab) {
                    ImageViewAnimatedChange(getActivity(), fab_img);
                    flagFab = false;
                } else if (text.isEmpty()) {
                    ImageViewAnimatedChange(getActivity(), fab_img);
                    flagFab = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Animate button image change
     * @param c Context
     * @param v ImageView
     */
    public void ImageViewAnimatedChange(Context c, final ImageView v) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (flagFab) {
                    v.setImageResource(R.drawable.ic_mic_white_24dp);
                } else {
                    v.setImageResource(R.drawable.ic_send_white_24dp);
                }
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        v.startAnimation(anim_out);
    }

    /**
     * Show text when chat log is empty
     * to assist user in finding the text input.
     */
    public void setHelpText() {
        messageRoot.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (!dataSnapshot.exists()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
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

            }
        });
    }

    /**
     * Sign out of Google
     */
    public void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        startActivity(intent);
    }
}
