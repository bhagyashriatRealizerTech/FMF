package realizer.com.makemepopular.friendnear;

/**
 * Created by Win on 17/01/2017.
 */
public class FriendNearModel
{
    public String  FriendUserId="";
    public String FriendName="";
    public String LastUpdatedOn="";
    public String ThumbnailUrl="";
    public String Distance="";
    public String duration="";
    public String distanceInKm="";
    public String FriendsCordinates="";

    public String getFriendsCordinates() {
        return FriendsCordinates;
    }

    public void setFriendsCordinates(String friendsCordinates) {
        FriendsCordinates = friendsCordinates;
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

    public String getLastUpdatedOn() {
        return LastUpdatedOn;
    }

    public void setLastUpdatedOn(String lastUpdatedOn) {
        LastUpdatedOn = lastUpdatedOn;
    }

    public String getThumbnailUrl() {
        return ThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        ThumbnailUrl = thumbnailUrl;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
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
}
