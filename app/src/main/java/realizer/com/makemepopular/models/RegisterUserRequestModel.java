package realizer.com.makemepopular.models;

/**
 * Created by shree on 1/12/2017.
 */
public class RegisterUserRequestModel {

    String emailId="";
    String fName="";
    String lName="";
    String contactNo="";
    String dob="";
    String gender="";
    String accountType="";
    String thumbnailUrl="";
    String createTs="";
    String lastCity="";
    String deviceId="";
    String fcmRegId="";

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCreateTs() {
        return createTs;
    }

    public void setCreateTs(String createTs) {
        this.createTs = createTs;
    }

    public String getLastCity() {
        return lastCity;
    }

    public void setLastCity(String lastCity) {
        this.lastCity = lastCity;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFcmRegId() {
        return fcmRegId;
    }

    public void setFcmRegId(String fcmRegId) {
        this.fcmRegId = fcmRegId;
    }
}
