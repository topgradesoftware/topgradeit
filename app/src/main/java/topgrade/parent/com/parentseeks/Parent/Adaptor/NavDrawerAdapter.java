package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickDrawerItem;
import topgrade.parent.com.parentseeks.Parent.Model.NavDrawerItem;
import topgrade.parent.com.parentseeks.R;

public class NavDrawerAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<NavDrawerItem> nav_items;

    private OnClickDrawerItem click_item_listener;


    public NavDrawerAdapter(Context context, ArrayList<NavDrawerItem> nav_items) {
        this.context = context;
        this.nav_items = nav_items;
    }

    public void setOnClickDrawerItem(OnClickDrawerItem click_item_listener) {
        this.click_item_listener = click_item_listener;
    }


    @Override
    public int getCount() {
        return nav_items.size();
    }

    @Override
    public Object getItem(int position) {
        return nav_items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View single_view = convertView;
        final NavDrawerViewHolders view_holder;

        if (single_view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            single_view = inflater.inflate(R.layout.single_view_drawer_list, parent, false);

            view_holder = new NavDrawerViewHolders(single_view);

            single_view.setTag(view_holder);
        } else {
            view_holder = (NavDrawerViewHolders) single_view.getTag();
        }

        final NavDrawerItem drawer_item = nav_items.get(position);

        view_holder.setValues(drawer_item.getTitle_name());


        view_holder.rl_container_drawer_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                click_item_listener.clickDrawerItem(drawer_item.getTitle_name());

            }
        });

        return single_view;
    }


    public class NavDrawerViewHolders {
        public RelativeLayout rl_container_drawer_content;
        public ImageView iv_drawer_icon;
        public TextView tv_drawer_title;

        public NavDrawerViewHolders(View v) {
            rl_container_drawer_content = v.findViewById(R.id.rl_container_drawer_content);
            iv_drawer_icon = v.findViewById(R.id.iv_drawer_icon);
            tv_drawer_title = v.findViewById(R.id.tv_drawer_text);
        }

        public void setValues(String title) {
            tv_drawer_title.setText(title);
        }
    }

}