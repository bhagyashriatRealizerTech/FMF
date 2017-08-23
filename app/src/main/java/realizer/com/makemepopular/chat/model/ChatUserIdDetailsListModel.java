package realizer.com.makemepopular.chat.model;

/**
 * Created by Win on 11/17/2015.
 */
public class ChatUserIdDetailsListModel {

    private String fname = "";
    private String lname= "";
    private String userid = "";
    private String thumbnail = "";

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

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
