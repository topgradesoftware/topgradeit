package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.R;

public class ChildListAdaptor extends RecyclerView.Adapter<ChildListAdaptor.Holder> {
    
    private Context context;
    private List<SharedStudent> students;
    private OnClickListener onClickListener;

    public ChildListAdaptor(Context context, List<SharedStudent> students, OnClickListener onClickListener) {
        this.context = context;
        this.students = students;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.child_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SharedStudent student = students.get(position);
        holder.textView.setText(student.getFullName());

        // Load actual profile picture
        loadStudentImage(holder.imageView, student);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onItemClick(v, position);
                }
            }
        });
    }

    private void loadStudentImage(ImageView imageView, SharedStudent student) {
        try {
            if (student.getPicture() != null && !student.getPicture().isEmpty()) {
                String imageUrl = API.image_base_url + student.getPicture();
                Log.d("ChildListAdaptor", "Loading student image from: " + imageUrl);
                
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.man_brown)
                        .error(R.drawable.man_brown)
                        .into(imageView);
            } else {
                Log.d("ChildListAdaptor", "No picture found for student: " + student.getFullName() + ", using default image");
                imageView.setImageResource(R.drawable.man_brown);
            }
        } catch (Exception e) {
            Log.e("ChildListAdaptor", "Error loading student image for: " + student.getFullName(), e);
            imageView.setImageResource(R.drawable.man_brown);
        }
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.name_wanted);
            imageView = itemView.findViewById(R.id.perfect);
        }
    }
}