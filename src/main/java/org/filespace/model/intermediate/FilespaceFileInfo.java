package org.filespace.model.intermediate;

import java.time.LocalDate;
import java.time.LocalTime;

public interface FilespaceFileInfo {
    public Long getFileId();
    public String getFileName();
    public Long getSize();
    public Long getSenderId();
    public String getUsername();
    public String getDescription();
    public Integer getNumberOfDownloads();
    public LocalTime getAttachTime();
    public LocalDate getAttachDate();
}
