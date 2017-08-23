package realizer.com.makemepopular.chat.model;

/**
 * Created by Bhagyashri on 9/1/2016.
 */
public class AddedContactModel {

    String fname;
    String userId;
    String profileimage;
   String lname;

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileimage() {
        return profileimage;
    }


    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AddedContactModel)) {
            return false;
        }

        AddedContactModel that = (AddedContactModel) other;

        // Custom equality check here.
        return this.userId.equals(that.userId);
    }
}
