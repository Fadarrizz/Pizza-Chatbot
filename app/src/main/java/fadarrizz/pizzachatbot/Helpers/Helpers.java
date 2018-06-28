package fadarrizz.pizzachatbot.Helpers;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

import fadarrizz.pizzachatbot.Model.User;
import fadarrizz.pizzachatbot.R;
import fadarrizz.pizzachatbot.SignInActivity;

public class Helpers {
    public static User currentUser;
    public static Intent data;
    private static Handler handler;

    public static int convertDpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static void visibilityAnimation(final View view, Context context, Boolean visible, final int duration, int delay) {
        final Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        final Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
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
}
