package org.filespace.model.intermediate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface FilespaceFileInfo {
    public Integer getFileId();
    public String getFileName();
    public Long getSize();
    public Integer getSenderId();
    public String getUsername();
    public String getDescription();
    public Integer getNumberOfDownloads();
    public LocalDateTime getAttachDateTime();
}
