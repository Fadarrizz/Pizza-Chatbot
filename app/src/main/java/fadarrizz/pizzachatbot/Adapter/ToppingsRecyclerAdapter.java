package fadarrizz.pizzachatbot.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import fadarrizz.pizzachatbot.Model.Topping;
import fadarrizz.pizzachatbot.R;

public class ToppingsRecyclerAdapter extends RecyclerView.Adapter<ToppingsRecyclerAdapter.ViewHolder> {

    private List<Topping> mToppingList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Button button, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Button toppingButton;

        private ViewHolder(final View view, final OnItemClickListener listener) {
            super(view);
            toppingButton = view.findViewById(R.id.toppingButton);

            toppingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(toppingButton, position);
                        }
                    }
                }
            });
        }
    }

    public ToppingsRecyclerAdapter(List<Topping> toppingsList) {
        this.mToppingList = toppingsList;
    }

    @Override
    public void onBindViewHolder(@NonNull ToppingsRecyclerAdapter.ViewHolder holder, int position) {
        Topping topping = mToppingList.get(position);
        holder.toppingButton.setText(topping.getName());
    }

    @NonNull
    @Override
    public ToppingsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_toppings, parent, false);
        return new ToppingsRecyclerAdapter.ViewHolder(v, mListener);
    }

    public void clear() {
        final int size = getItemCount();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mToppingList.remove(0);
            }
            this.notifyItemRangeRemoved(0, size);
        }
    }

    @Override
    public int getItemCount() {
        return mToppingList.size();
    }
}
