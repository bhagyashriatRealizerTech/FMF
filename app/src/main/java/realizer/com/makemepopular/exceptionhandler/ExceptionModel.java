package realizer.com.makemepopular.exceptionhandler;

/**
 * Created by Bhagyashri on 10/5/2016.
 */
public class ExceptionModel {

    String UserId;
    String ExceptionDetails;
    String DeviceModel;
    String AndroidVersion;
    String ApplicationSource;
    String DeviceBrand;
    int ExceptionID;


    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getExceptionDetails() {
        return ExceptionDetails;
    }

    public void setExceptionDetails(String exceptionDetails) {
        ExceptionDetails = exceptionDetails;
    }

    public String getDeviceModel() {
        return DeviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        DeviceModel = deviceModel;
    }

    public String getAndroidVersion() {
        return AndroidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        AndroidVersion = androidVersion;
    }

    public String getDeviceBrand() {
        return DeviceBrand;
    }

    public void setDeviceBrand(String deviceBrand) {
        DeviceBrand = deviceBrand;
    }

    public String getApplicationSource() {
        return ApplicationSource;
    }

    public void setApplicationSource(String applicationSource) {
        ApplicationSource = applicationSource;
    }

    public int getExceptionID() {
        return ExceptionID;
    }

    public void setExceptionID(int exceptionID) {
        ExceptionID = exceptionID;
    }
}
