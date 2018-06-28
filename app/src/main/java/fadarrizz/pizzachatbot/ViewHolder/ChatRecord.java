package fadarrizz.pizzachatbot.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import fadarrizz.pizzachatbot.R;

public class ChatRecord extends RecyclerView.ViewHolder {

    public TextView leftText, rightText;

    public ChatRecord(View itemView) {
        super(itemView);

        leftText = itemView.findViewById(R.id.message_bot);
        rightText = itemView.findViewById(R.id.message_user);
    }
}
