package realizer.com.makemepopular.notifications;

/**
 * Created by shree on 1/25/2017.
 */
public class NotificationModel {

    String notificationId,notiFromUserId,notiFromUserName,notiFromThumbnailUrl,notiToUserId,notiText,notiTime,notiType;
    boolean isReceived=false;
    boolean isRead=false;

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotiFromUserId() {
        return notiFromUserId;
    }

    public void setNotiFromUserId(String notiFromUserId) {
        this.notiFromUserId = notiFromUserId;
    }

    public String getNotiFromUserName() {
        return notiFromUserName;
    }

    public void setNotiFromUserName(String notiFromUserName) {
        this.notiFromUserName = notiFromUserName;
    }

    public String getNotiFromThumbnailUrl() {
        return notiFromThumbnailUrl;
    }

    public void setNotiFromThumbnailUrl(String notiFromThumbnailUrl) {
        this.notiFromThumbnailUrl = notiFromThumbnailUrl;
    }

    public String getNotiToUserId() {
        return notiToUserId;
    }

    public void setNotiToUserId(String notiToUserId) {
        this.notiToUserId = notiToUserId;
    }

    public String getNotiText() {
        return notiText;
    }

    public void setNotiText(String notiText) {
        this.notiText = notiText;
    }

    public String getNotiTime() {
        return notiTime;
    }

    public void setNotiTime(String notiTime) {
        this.notiTime = notiTime;
    }

    public String getNotiType() {
        return notiType;
    }

    public void setNotiType(String notiType) {
        this.notiType = notiType;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean isReceived) {
        this.isReceived = isReceived;
    }
}
