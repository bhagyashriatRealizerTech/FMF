package realizer.com.makemepopular;

import android.content.Intent;

import java.util.ArrayList;

import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.service.SendMessageService;

/**
 * Created by shree on 1/18/2017.
 */
public class Singleton {

    private static Singleton _instance;
    private static String userId;
    private static Intent messageService;

    private Singleton()
    {

    }

    public static Singleton getInstance()
    {
        if (_instance == null)
        {
            _instance = new Singleton();
        }
        return _instance;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        Singleton.userId = userId;
    }

    private static ArrayList<FriendListModel> friendListModels;

    public static ArrayList<FriendListModel> getFriendListModels() {
        return friendListModels;
    }

    public static void setFriendListModels(ArrayList<FriendListModel> friendListModels) {
        Singleton.friendListModels = friendListModels;
    }

    public static Intent getMessageService() {
        return messageService;
    }

    public static void setMessageService(Intent messageService) {
        Singleton.messageService = messageService;
    }
}
