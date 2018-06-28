package fadarrizz.pizzachatbot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import fadarrizz.pizzachatbot.Adapter.ExpandableRecyclerAdapter;
import fadarrizz.pizzachatbot.Interface.OnItemLongClickListener;
import fadarrizz.pizzachatbot.Model.ChatMessage;
import fadarrizz.pizzachatbot.Model.Order;
import fadarrizz.pizzachatbot.ViewHolder.OrderViewHolder;

public class OrderFragment extends Fragment {
    private static final String TAG = "OrderFragment";
    private static final int INTERVAL = 15000;

    View mFragment;
    Context thisContext;

    RecyclerView orderRecyclerView;
    ExpandableRecyclerAdapter adapter;

    FirebaseDatabase database;
    DatabaseReference orderRoot;
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    String currentUserUID;

    private List<Order> orderList;

    Order selectedItemForDeletion;

    public OrderFragment() {
        // Required empty public constructor
    }

    public static OrderFragment newInstance() {
        return new OrderFragment();
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
        mFragment = inflater.inflate(R.layout.fragment_order, container, false);

        // Configure Google Sign Out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(thisContext, gso);

        mAuth = FirebaseAuth.getInstance();
        currentUserUID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        database = FirebaseDatabase.getInstance();
        orderRoot = database.getReference("orders").child(currentUserUID);
        orderRoot.keepSynced(true);

        getOrders();

        return mFragment;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order_menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_orders:
                deleteOrderOptions(1);
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get orders from Firebase and display in recyclerview
     */
    public void getOrders() {
        orderRecyclerView = mFragment.findViewById(R.id.orderList);
        orderRecyclerView.setHasFixedSize(true);

        orderList = new ArrayList<>();
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));

        orderRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Order model = data.getValue(Order.class);
                    orderList.add(model);
                }
                adapter = new ExpandableRecyclerAdapter(orderList);
                orderRecyclerView.setAdapter(adapter);

                adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(int position) {
                        selectedItemForDeletion = orderList.get(position);

                        new AlertDialog.Builder(getActivity())
                                .setTitle("Delete order")
                                .setMessage("Are you sure you want to delete this order?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        orderList.remove(selectedItemForDeletion);
                                        orderRoot.child(currentUserUID).child(selectedItemForDeletion.getID()).removeValue();
                                        adapter.notifyDataSetChanged();
                                    }
                                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
                Toast.makeText(getContext(),
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Create alert dialog for order deletion
     */
    public void createAlertDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete order")
                .setMessage("Are you sure you want to delete this order?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteOrderOptions(0);
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * Delete one or all options.
     * @param choice specifies that choice.
     */
    public void deleteOrderOptions(int choice) {
        switch (choice) {
            case 0: {
                orderList.remove(selectedItemForDeletion);
                orderRoot.child(selectedItemForDeletion.getID()).removeValue();
            }
            case 1:
                for (int i = 0; i < orderList.size(); i++) {
                    orderList.remove(i);
                }
                orderRoot.removeValue();
        }
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
