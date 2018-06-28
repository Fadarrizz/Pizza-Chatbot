package fadarrizz.pizzachatbot.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fadarrizz.pizzachatbot.Interface.OnItemLongClickListener;
import fadarrizz.pizzachatbot.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView tvPizza;
    public TextView tvOrderDay;
    public TextView tvOrderTime;
    public TextView tvCategory;
    public TextView tvToppings;
    public TextView tvOrderDate;

    public OrderViewHolder(View view) {
        super(view);

        tvPizza = view.findViewById(R.id.orderPizza);
        tvOrderDay = view.findViewById(R.id.orderDay);
        tvOrderTime = view.findViewById(R.id.orderTime);
        tvCategory = view.findViewById(R.id.category);
        tvToppings = view.findViewById(R.id.toppings);
        tvOrderDate = view.findViewById(R.id.orderDate);

        RelativeLayout arrowLayout = view.findViewById(R.id.expandArrowLayout);
        LinearLayout expandableLayout = view.findViewById(R.id.expandableLayout);

//        view.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (listener != null) {
//                    int position = getAdapterPosition();
//                    if (position != RecyclerView.NO_POSITION) {
//                        listener.onItemLongClick(position);
//                    }
//                }
//                return true;
//            }
//        });
    }
}
