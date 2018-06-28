package fadarrizz.pizzachatbot.Adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import fadarrizz.pizzachatbot.Model.Pizza;
import fadarrizz.pizzachatbot.R;

public class PizzaRecyclerAdapter extends RecyclerView.Adapter<PizzaRecyclerAdapter.ViewHolder> {

    private List<Pizza> mPizzaList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView pizzaImg;
        public TextView pizzaName;
        public TextView pizzaPrice;

        ViewHolder(View view, final OnItemClickListener listener) {
            super(view);
            pizzaImg = view.findViewById(R.id.pizzaImg);
            pizzaName = view.findViewById(R.id.pizzaName);
            pizzaPrice = view.findViewById(R.id.pizzaPrice);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public PizzaRecyclerAdapter(List<Pizza> pizzaList) {
        this.mPizzaList = pizzaList;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pizza pizza = mPizzaList.get(position);
        Picasso.get().load(pizza.getImg_url())
                .into(holder.pizzaImg);
        holder.pizzaName.setText(pizza.getName());
        holder.pizzaPrice.setText(pizza.getPrice());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_pizza, parent, false);
        return new ViewHolder(v, mListener);
    }

    public void clear() {
        final int size = getItemCount();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mPizzaList.remove(0);
            }
            this.notifyItemRangeRemoved(0, size);
        }
    }

    @Override
    public int getItemCount() {
        return mPizzaList.size();
    }
}
