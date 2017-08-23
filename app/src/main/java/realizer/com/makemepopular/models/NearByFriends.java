package realizer.com.makemepopular.models;

/**
 * Created by shree on 1/17/2017.
 */
public class NearByFriends {

    String FriendUserId="";
    String FriendName="";
    String ThumbnailUrl="";
    int Distance;
    String duration="";
    String distanceInKm="";
    String latitude="";
    String longitude="";
    String gender="";
    String age="";
    String lastupdate="";

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFriendUserId() {
        return FriendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        FriendUserId = friendUserId;
    }

    public String getFriendName() {
        return FriendName;
    }

    public void setFriendName(String friendName) {
        FriendName = friendName;
    }

    public String getThumbnailUrl() {
        return ThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        ThumbnailUrl = thumbnailUrl;
    }

    public int getDistance() {
        return Distance;
    }

    public void setDistance(int distance) {
        Distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistanceInKm() {
        return distanceInKm;
    }

    public void setDistanceInKm(String distanceInKm) {
        this.distanceInKm = distanceInKm;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
