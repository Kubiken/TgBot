package Models;

public class User
{
    String username;
    String userTgId;
    Boolean isReady;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserTgId() {
        return userTgId;
    }

    public void setUserTgId(String userTgId) {
        this.userTgId = userTgId;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }
}
