package topgrade.parent.com.parentseeks.Teacher.Interface;

import android.view.View;

import topgrade.parent.com.parentseeks.Teacher.Adaptor.StaffAttendanceSubmitAdapter;

public interface AttendanceSubmitInterface {

    public void StatusSubmit(View view, int position,
                             String status, StaffAttendanceSubmitAdapter.AttendanceSubmitViewHolder holder);


    public void NoteSubmit(int position,
                           String note);
}
