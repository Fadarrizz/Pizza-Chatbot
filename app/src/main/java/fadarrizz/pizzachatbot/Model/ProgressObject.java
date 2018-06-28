package fadarrizz.pizzachatbot.Model;

public class ProgressObject {
    private int mId;
    private String mTitle;
    private int mProgress;

    public ProgressObject(int id, String title, int progress) {
        mId = id;
        mTitle = title;
        mProgress = progress;
    }

    int getId() {
        return mId;
    }

    void setId(int id) {
        mId = id;
    }

    String getTitle() {
        return mTitle;
    }

    void setTitle(String title) {
        mTitle = title;
    }

    int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }
}
