package topgrade.parent.com.parentseeks.Parent.Model.date_sheet;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class DateSheetResponse {
    public SharedStatus status;
    public List<Image> images;
    public List<DateSheetFile> files;
    public List<DateSheetData> data;
    public HeaderFooter header_footer;
}
