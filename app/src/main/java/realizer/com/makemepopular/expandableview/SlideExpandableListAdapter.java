package realizer.com.makemepopular.expandableview;

import android.view.View;
import android.widget.ListAdapter;

import realizer.com.makemepopular.R;


/**
 * ListAdapter that adds sliding functionality to a list.
 * Uses R.id.expandalbe_toggle_button and R.id.expandable id's if no
 * ids are given in the contructor.
 */
public class SlideExpandableListAdapter extends AbstractSlideExpandableListAdapter {
    private int toggle_button_id;
    private int expandable_view_id;

    public SlideExpandableListAdapter(ListAdapter wrapped, int toggle_button_id, int expandable_view_id) {
        super(wrapped);
        this.toggle_button_id = toggle_button_id;
        this.expandable_view_id = expandable_view_id;
    }

    public SlideExpandableListAdapter(ListAdapter wrapped) {
        this(wrapped, R.id.expandable_toggle_button, R.id.expandable);
    }

    @Override
    public View getExpandToggleButton(View parent) {
        return parent.findViewById(toggle_button_id);
    }

    @Override
    public View getExpandableView(View parent) {
        return parent.findViewById(expandable_view_id);
    }
}
