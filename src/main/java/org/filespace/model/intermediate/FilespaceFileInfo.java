package org.filespace.model.intermediate;

import java.time.LocalDate;
import java.time.LocalTime;

public interface FilespaceFileInfo {
    public Long getId();
    public String getFileName();
    public Long getSize();
    public Long getSenderId();
    public String getComment();
    public Integer getNumberOfDownloads();
    public LocalTime getAttachTime();
    public LocalDate getAttachDate();
}
