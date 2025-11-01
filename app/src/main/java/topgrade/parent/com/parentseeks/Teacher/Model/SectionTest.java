package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SectionTest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("section")
    @Expose
    private String section;

    public SectionTest(String sectionId, String section) {
        this.sectionId = sectionId;
        this.section = section;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return section;
    }
}
