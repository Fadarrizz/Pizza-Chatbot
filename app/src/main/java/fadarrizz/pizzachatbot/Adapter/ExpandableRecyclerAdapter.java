package fadarrizz.pizzachatbot.Adapter;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import fadarrizz.pizzachatbot.Helpers.Helpers;
import fadarrizz.pizzachatbot.Interface.OnItemLongClickListener;
import fadarrizz.pizzachatbot.Model.Order;
import fadarrizz.pizzachatbot.R;

public class ExpandableRecyclerAdapter extends RecyclerView.Adapter<ExpandableRecyclerAdapter.ViewHolder> {

    private List<Order> orders;
    private SparseBooleanArray expandState = new SparseBooleanArray();
    private Context context;
    private OnItemLongClickListener mListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mListener = listener;
    }

    public ExpandableRecyclerAdapter(List<Order> orders) {
        this.orders = orders;

        // Set initial expanded state to false
        for (int i = 0; i < orders.size(); i++) {
            expandState.append(i, false);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPizza, tvCategory, tvToppings, tvOrderDay, tvOrderTime, tvOrderDate;
        public RelativeLayout arrowLayout;
        public LinearLayout expandableLayout;
        public ImageView arrow;

        ViewHolder(View view, final OnItemLongClickListener listener) {
            super(view);

            tvPizza = view.findViewById(R.id.orderPizza);
            tvOrderDay = view.findViewById(R.id.orderDay);
            tvOrderTime = view.findViewById(R.id.orderTime);
            tvCategory = view.findViewById(R.id.category);
            tvToppings = view.findViewById(R.id.toppings);
            tvOrderDate = view.findViewById(R.id.orderDate);

            arrowLayout = view.findViewById(R.id.expandArrowLayout);
            expandableLayout = view.findViewById(R.id.expandableLayout);

            arrow = view.findViewById(R.id.expandArrow);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemLongClick(position);
                        }
                    }
                    return true;
                }
            });
        }
    }

    @NonNull
    @Override
    public ExpandableRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_parent, viewGroup, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ExpandableRecyclerAdapter.ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        Order order = orders.get(position);

        holder.tvPizza.setText(order.getPizza());

        if (order.getOrderDate() != null) {
            Date date = order.getOrderDate();

            SimpleDateFormat weekDay = new SimpleDateFormat("EEE d-M");

            holder.tvOrderDay.setText(weekDay.format(date));

            SimpleDateFormat time = new SimpleDateFormat("HH:mm");
            holder.tvOrderTime.setText(time.format(date));

            SimpleDateFormat dateFormat = new SimpleDateFormat("d-M-yyyy");
            holder.tvOrderDate.setText(dateFormat.format(date));
        }

        holder.tvCategory.setText(order.getCategory());
        holder.tvToppings.setText("");
        if (order.getToppings() != null) {
            for (int j = 0; j < order.getToppings().size(); j++) {
                holder.tvToppings.append(order.getToppings().get(j) + ", ");
            }
        }

        final boolean isExpanded = expandState.get(position);
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.arrowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButton(holder.expandableLayout, holder.arrow, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void onClickButton(final LinearLayout expandableLayout, final ImageView imageView, final int i) {

        if (expandableLayout.getVisibility() == View.VISIBLE) {
            createRotateAnimator(imageView, 180f, 0f).start();
            Helpers.visibilityAnimation(expandableLayout, context, false, 500, 0);
            expandableLayout.setVisibility(View.GONE);
            expandState.put(i, false);
        } else {
            createRotateAnimator(imageView, 0f, 180f).start();
            expandableLayout.setVisibility(View.VISIBLE);
            expandState.put(i, true);
        }
    }

    private ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(160);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }
}