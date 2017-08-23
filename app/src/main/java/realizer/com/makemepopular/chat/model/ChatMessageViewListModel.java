package realizer.com.makemepopular.chat.model;

/**
 * Created by Win on 11/26/2015.
 */
public class ChatMessageViewListModel {

    private String messageId="";
    private String senderId = "";
    private String timeStamp="";
    private String message="";
    private String threadId="";
    private String receiverId="";
    private String SenderName="";
    private String senderThumbnail="";
    private String sendDate="";
    private String sendTime="";
    private String isNewMessage="";
    private String isRead="";
    private String isDelivered="";
    private String LastmsgTime="";

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderName() {
        return SenderName;
    }

    public void setSenderName(String senderName) {
        SenderName = senderName;
    }

    public String getSenderThumbnail() {
        return senderThumbnail;
    }

    public void setSenderThumbnail(String senderThumbnail) {
        this.senderThumbnail = senderThumbnail;
    }

    public String getIsNewMessage() {
        return isNewMessage;
    }

    public void setIsNewMessage(String isNewMessage) {
        this.isNewMessage = isNewMessage;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public String getIsDelivered() {
        return isDelivered;
    }

    public void setIsDelivered(String isDelivered) {
        this.isDelivered = isDelivered;
    }

    public String getLastmsgTime() {
        return LastmsgTime;
    }

    public void setLastmsgTime(String lastmsgTime) {
        LastmsgTime = lastmsgTime;
    }
}
