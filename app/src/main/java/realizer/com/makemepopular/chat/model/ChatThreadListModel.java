package realizer.com.makemepopular.chat.model;

import java.util.Date;

/**
 * Created by Win on 1/12/2016.
 */
public class ChatThreadListModel {

    String threadName="";
    String threadId ="";
    int unreadCount=0;
    String date="";
    String lastMessage="";
    String lastSenderName="";
    String lastSenderId="";
    Date senddate = null;
    String fromSenderName="";
    String profileImg="";
    String lastMessageId="";
    String lastMessageText="";
    String initiateId="";
    String initiateName="";
    String participentID="";
    String CustomThreadName="";
    String timeStamp="";
    String participant="";
    String isLastMsgRead="";
    String isLastMsgDeliver="";

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public String getCustomThreadName() {
        return CustomThreadName;
    }

    public void setCustomThreadName(String customThreadName) {
        CustomThreadName = customThreadName;
    }

    public String getParticipentID() {
        return participentID;
    }

    public void setParticipentID(String participentID) {
        this.participentID = participentID;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public String getInitiateId() {
        return initiateId;
    }

    public void setInitiateId(String initiateId) {
        this.initiateId = initiateId;
    }

    public String getInitiateName() {
        return initiateName;
    }

    public void setInitiateName(String initiateName) {
        this.initiateName = initiateName;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getFromSenderName() {
        return fromSenderName;
    }

    public void setFromSenderName(String fromSenderName) {
        this.fromSenderName = fromSenderName;
    }

    public String getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getLastSenderName() {
        return lastSenderName;
    }

    public void setLastSenderName(String lastSenderName) {
        this.lastSenderName = lastSenderName;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }


    public Date getSenddate() {
        return senddate;
    }

    public void setSenddate(Date senddate) {
        this.senddate = senddate;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getIsLastMsgRead() {
        return isLastMsgRead;
    }

    public void setIsLastMsgRead(String isLastMsgRead) {
        this.isLastMsgRead = isLastMsgRead;
    }

    public String getIsLastMsgDeliver() {
        return isLastMsgDeliver;
    }

    public void setIsLastMsgDeliver(String isLastMsgDeliver) {
        this.isLastMsgDeliver = isLastMsgDeliver;
    }
}
