package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import topgrade.parent.com.parentseeks.R;

public class SolvedComplaintsFragment extends Fragment {

    View v;
    public SolvedComplaintsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_complaints,container,false);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
