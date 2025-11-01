package topgrade.parent.com.parentseeks.Teacher.Model;

public class SectionModel {
    private String section_id;
    private String section_name;
    private String class_id;

    public SectionModel(String section_id, String section_name, String class_id) {
        this.section_id = section_id;
        this.section_name = section_name;
        this.class_id = class_id;
    }

    public String getSection_id() {
        return section_id;
    }

    public void setSection_id(String section_id) {
        this.section_id = section_id;
    }

    public String getSection_name() {
        return section_name;
    }

    public void setSection_name(String section_name) {
        this.section_name = section_name;
    }

    public String getClass_id() {
        return class_id;
    }

    public void setClass_id(String class_id) {
        this.class_id = class_id;
    }
}
