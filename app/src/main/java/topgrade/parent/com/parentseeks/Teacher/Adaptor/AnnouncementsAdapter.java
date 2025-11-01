package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Announcement_Model;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.Holder> {

    private List<Announcement_Model.Announcement> allAnnouncements;
    private List<Announcement_Model.Announcement> filteredAnnouncements;
    private String currentFilter = "all"; // "all", "news", "events"

    public AnnouncementsAdapter(List<Announcement_Model.Announcement> announcements) {
        this.allAnnouncements = announcements;
        this.filteredAnnouncements = new ArrayList<>(announcements);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcement_item_layout, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Announcement_Model.Announcement announcement = filteredAnnouncements.get(position);
        
        // Set alternating row colors
        if (position % 2 != 0) {
            holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
        } else {
            holder.row.setBackgroundColor(Color.WHITE);
        }

        // Set serial number - use position + 1 for proper numbering
        holder.serial.setText(String.valueOf(position + 1));

        // Set title
        holder.title.setText(announcement.getTitle() != null ? announcement.getTitle() : "No Title");

        if (announcement.isNews()) {
            // Show news fields: Serial, Title, Publish Date, Author
            holder.startDate.setText(announcement.getPublishDate() != null ? changeDateFormat(announcement.getPublishDate()) : "No Date");
            holder.endDate.setText(announcement.getAuthor() != null ? announcement.getAuthor() : "No Author");

        } else {
            // Show event fields: Serial, Title, Start Date, End Date
            holder.startDate.setText(announcement.getStartDate() != null ? changeDateFormat(announcement.getStartDate()) : "No Start Date");
            holder.endDate.setText(announcement.getEndDate() != null ? changeDateFormat(announcement.getEndDate()) : "No End Date");
        }
    }

    @Override
    public int getItemCount() {
        return filteredAnnouncements.size();
    }

    public void filterByType(String filterType) {
        this.currentFilter = filterType;
        filteredAnnouncements.clear();
        
        for (Announcement_Model.Announcement announcement : allAnnouncements) {
            if ("all".equals(filterType) || 
                ("news".equals(filterType) && announcement.isNews()) ||
                ("text".equals(filterType) && announcement.isNews()) ||
                ("events".equals(filterType) && announcement.isEvent())) {
                filteredAnnouncements.add(announcement);
            }
        }
        
        notifyDataSetChanged();
    }

    public void updateData(List<Announcement_Model.Announcement> newAnnouncements) {
        this.allAnnouncements = newAnnouncements;
        filterByType(currentFilter); // Reapply current filter
    }

    public int getFilteredCount() {
        return filteredAnnouncements.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView serial, title, startDate, endDate;
        LinearLayout row;

        Holder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.table_row);
            serial = itemView.findViewById(R.id.serial);
            title = itemView.findViewById(R.id.title);
            startDate = itemView.findViewById(R.id.start_date);
            endDate = itemView.findViewById(R.id.end_date);
        }
    }

    private String changeDateFormat(String time) {
        if (time == null || time.isEmpty()) {
            return "N/A";
        }
        
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd MMM, yy EEE";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        try {
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time; // Return original string if parsing fails
        }
    }
}
