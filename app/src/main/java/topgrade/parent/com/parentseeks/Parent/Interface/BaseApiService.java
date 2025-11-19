package topgrade.parent.com.parentseeks.Parent.Interface;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import topgrade.parent.com.parentseeks.Parent.Model.GeneralModel;
import topgrade.parent.com.parentseeks.Parent.Model.LoginResponse;
import topgrade.parent.com.parentseeks.Parent.Model.ReportModel;
import topgrade.parent.com.parentseeks.Parent.Model.SessionModel;
import topgrade.parent.com.parentseeks.Parent.Model.StatusModel;
import topgrade.parent.com.parentseeks.Parent.Model.SubjectAttendeceModel;
import topgrade.parent.com.parentseeks.Parent.Model.date_sheet.DateSheetResponse;
import topgrade.parent.com.parentseeks.Parent.Model.timetable.StudentTimetableResponse;
import topgrade.parent.com.parentseeks.Parent.Model.timetable.StudentTimetableSessionResponse;
import topgrade.parent.com.parentseeks.Teacher.Model.API.AssigntaskModel;
import topgrade.parent.com.parentseeks.Teacher.Model.API.AttendanceSubmt_;
import topgrade.parent.com.parentseeks.Teacher.Model.API.Complain_title_List;
import topgrade.parent.com.parentseeks.Teacher.Model.API.UpdateTaskModel;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamResultModel;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamSessionResponse;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamTestRespone;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ProgressReportModel;
import topgrade.parent.com.parentseeks.Teacher.Model.AdvancedSalaryModel;
import topgrade.parent.com.parentseeks.Teacher.Model.CityModel;
import topgrade.parent.com.parentseeks.Teacher.Model.Event_Model;
import topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel;
import topgrade.parent.com.parentseeks.Teacher.Model.LegderModel;
import topgrade.parent.com.parentseeks.Teacher.Model.SalaryModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StateModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListModel;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableModel;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableSessionModel;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableSmsModel;
import topgrade.parent.com.parentseeks.Teacher.Model.UpdateProfilModel;
import topgrade.parent.com.parentseeks.Teacher.Model.News_Model;
import topgrade.parent.com.parentseeks.Parent.Model.Diary;

public interface BaseApiService {

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/login")
    Call<LoginResponse> login(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/login")
    Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffLoginResponse> staffLogin(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/load_exam")
    Call<ReportModel> load_exam(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/load_exam_session")
    Call<SessionModel> load_exam_session(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_exam_session")
    Call<SessionModel> load_exam_session_teacher(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_active_exam_sessions")
    Call<ExamSessionResponse> load_active_exam_sessions(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_salary")
    Call<SalaryModel> load_salary(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_advance")
    Call<AdvancedSalaryModel> load_advance(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/logout")
    Call<ResponseBody> logout_teacher(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/logout")
    Call<ResponseBody> logout_parent(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=student/logout")
    Call<ResponseBody> logout_student(@Body RequestBody body);



    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/load_attendance_subjectwise")
    Call<SubjectAttendeceModel> load_attendance_subjectwise(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_ledger")
    Call<LegderModel> load_ledger(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_profile")
    Call<TeachModel> load_profile(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/report_progress")
    Call<ProgressReportModel> report_progress(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/assign_task")
    Call<AssigntaskModel> assign_task(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/assign_task")
    Call<UpdateTaskModel> update_task(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_students")
    Call<StudentListModel> load_students(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_exams_results")
    Call<ExamResultModel> load_exams_results(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/save_exam_results")
    Call<ExamResultModel> save_exam_results(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_exams")
    Call<ExamTestRespone> load_exams(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_timetable")
    Call<TimetableModel> load_timetable(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_timetable_session")
    Call<TimetableSessionModel> load_timetable_session(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_timetable_sms")
    Call<TimetableSmsModel> load_timetable_sms(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/getstate")
    Call<StateModel> getstate();


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/getcity")
    Call<CityModel> getcity(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/update_profile")
    Call<UpdateProfilModel> update_profile(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/update_profile")
    Call<GeneralModel> update_profile_(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_events")
    Call<Event_Model> load_events(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_news")
    Call<News_Model> load_news(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/attendence_student")
    Call<AttendanceSubmt_> attendence_student(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/attendence_student_full")
    Call<AttendanceSubmt_> attendence_student_full(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/feedback")
    Call<FeedbackModel> feedback(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/leave_applicaton")
    Call<StaffApplicationModel> leave_applicaton(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/leave_applicaton")
    Call<topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse> leave_applicaton_title(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_leave_application_categories")
    Call<topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse> load_leave_application_categories(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/complain")
    Call<StaffComplainModel> complain(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/complain")
    Call<Complain_title_List> complain_title(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/complain")
    Call<topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel> parent_complain(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/complain")
    Call<topgrade.parent.com.parentseeks.Student.Model.StudentComplaintModel> student_complain(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/create_exams")
    Call<Object> createExam(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/update_profile_student")
    Call<StatusModel> updateStudentProfile(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/load_timetable_session")
    Call<StudentTimetableSessionResponse> loadStudentTimetableSession(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/load_timetable")
    Call<StudentTimetableResponse> loadStudentTimetable(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/load_timetable_session")
    Call<StudentTimetableSessionResponse> loadStudentDateSheetSession(@Body RequestBody body);


    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/load_exam_session")
    Call<DateSheetResponse> loadStudentDateSheet(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=parent/load_diary")
    Call<Diary> load_diary(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/send_diary")
    Call<Diary> send_diary(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/get_teacher_classes")
    Call<ResponseBody> getTeacherClasses(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/get_teacher_sections")
    Call<ResponseBody> getTeacherSections(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/send_class_diary")
    Call<ResponseBody> sendClassDiary(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/get_teacher_subjects")
    Call<ResponseBody> getTeacherSubjects(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/get_teacher_students")
    Call<ResponseBody> getTeacherStudents(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/send_subject_diary")
    Call<ResponseBody> sendSubjectDiary(@Body RequestBody body);

    @Headers("Content-Type:application/json")
    @POST("api.php?page=teacher/send_diary_by_role")
    Call<ResponseBody> sendDiary(@Body RequestBody body);
}
