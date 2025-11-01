package topgrade.parent.com.parentseeks.Parent.Model;

public class MonthModel {

    private String id;
    private String month;

    public MonthModel(String id, String month) {
        this.id = id;
        this.month = month;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
