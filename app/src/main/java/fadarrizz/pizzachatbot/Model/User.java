package fadarrizz.pizzachatbot.Model;

public class User {
    private String firebaseID;
    private String username;
    private String password;
    private String email;

    public User(){}

    public User(String firebaseID, String username, String email) {
        this.firebaseID = firebaseID;
        this.username = username;
        this.email = email;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
