package realizer.com.makemepopular.interest;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.utils.FontManager;

/**
 * Created by Win on 11/01/2017.
 */
public class InterestAdapter extends BaseAdapter {

    private static ArrayList<InterestModel> interestList;
    private LayoutInflater interestinflater;
    private Context context1;
    View convrtview;

    public InterestAdapter(Context context1,ArrayList<InterestModel> list) {
        interestList=list;
        this.context1 = context1;
        interestinflater=LayoutInflater.from(context1);
    }

    @Override
    public int getCount() {
        return interestList.size();
    }

    @Override
    public Object getItem(int position) {
        return interestList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return interestList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        convrtview = convertView;
        if (convertView == null) {
            convertView = interestinflater.inflate(R.layout.interest_activity_adapter, null);
            holder = new ViewHolder();
            holder.interest_text= (TextView) convertView.findViewById(R.id.interest_text);
            holder.interest_ico= (TextView) convertView.findViewById(R.id.interest_ico_text);
            holder.outer= (LinearLayout) convertView.findViewById(R.id.interest_adapter_outer);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.interest_ico.setText(interestList.get(position).getInteresticoText());
        if(interestList.get(position).getInterestText().equalsIgnoreCase("Trekking/Outing"))
            holder.interest_text.setText("Hiking");
        else
            holder.interest_text.setText(interestList.get(position).getInterestText());
        holder.interest_ico.setTypeface(FontManager.getTypeface(context1, FontManager.FONTAWESOME));

        if (interestList.get(position).is_selected())
        {
            holder.outer.setBackground(context1.getResources().getDrawable(R.drawable.square_line_solid));
            holder.interest_ico.setTextColor(Color.WHITE);
            holder.interest_text.setTextColor(Color.WHITE);
        }
        else if (!interestList.get(position).is_selected())
        {
            holder.outer.setBackground(context1.getResources().getDrawable(R.drawable.square_line));
            holder.interest_ico.setTextColor(context1.getResources().getColor(R.color.colorPrimary));
            holder.interest_text.setTextColor(context1.getResources().getColor(R.color.colorPrimary));
        }


        return convertView;
    }
    static class ViewHolder {

        TextView interest_text,interest_ico;
        LinearLayout outer;
    }
}
