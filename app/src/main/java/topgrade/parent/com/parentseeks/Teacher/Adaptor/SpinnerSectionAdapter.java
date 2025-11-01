package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import topgrade.parent.com.parentseeks.Teacher.Model.SectionTest;

public class SpinnerSectionAdapter extends ArrayAdapter<SectionTest> {

    // Your sent context
    private final Context context;
    // Your custom values for the spinner (SectionTest)
    private final ArrayList<SectionTest> values;

    public SpinnerSectionAdapter(Context context, int textViewResourceId,
                                 ArrayList<SectionTest> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount(){
       return values.size();
    }

    @Override
    public SectionTest getItem(int position){
       return values.get(position);
    }

    @Override
    public long getItemId(int position){
       return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (SectionTests array) and the current position
        // You can NOW reference each method you has created in your bean object (SectionTest class)
        label.setText(values.get(position).getSection());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
            ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getSection());

        return label;
    }
}