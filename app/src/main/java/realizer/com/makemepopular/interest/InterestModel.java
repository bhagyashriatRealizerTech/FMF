package realizer.com.makemepopular.interest;

/**
 * Created by Win on 11/01/2017.
 */
public class InterestModel
{
    String InterestText="";
    String InteresticoText="";
    boolean is_selected=false;


    public String getInterestText() {
        return InterestText;
    }

    public void setInterestText(String interestText) {
        InterestText = interestText;
    }

    public String getInteresticoText() {
        return InteresticoText;
    }

    public void setInteresticoText(String interesticoText) {
        InteresticoText = interesticoText;
    }

    public boolean is_selected() {
        return is_selected;
    }

    public void setIs_selected(boolean is_selected) {
        this.is_selected = is_selected;
    }
}
