package realizer.com.makemepopular.chat.model;

/**
 * Created by Win on 1/6/2016.
 */
public class ChatMessageSendModel {

    public int ConversationId=0;
    public String fromUser="";
    public String fromName ="";
    public String sendtoName ="";
    public String fromUserId="";
    public String sendtouserId="";
    public String text ="";
    public String sentTime ="";
    public String hassync="";

    public int getSentDate() {
        return sentDate;
    }

    public void setSentDate(int sentDate) {
        this.sentDate = sentDate;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getSendtoName() {
        return sendtoName;
    }

    public void setSendtoName(String sendtoName) {
        this.sendtoName = sendtoName;
    }

    public String getSendtouserId() {
        return sendtouserId;
    }

    public void setSendtouserId(String sendtouserId) {
        this.sendtouserId = sendtouserId;
    }

    public int sentDate = 0;
    public int getConversationId() {
        return ConversationId;
    }

    public void setConversationId(int conversationId) {
        ConversationId = conversationId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }

    public String getHassync() {
        return hassync;
    }

    public void setHassync(String hassync) {
        this.hassync = hassync;
    }
}
