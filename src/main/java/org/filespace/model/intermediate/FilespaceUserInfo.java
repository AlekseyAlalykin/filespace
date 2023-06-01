package org.filespace.model.intermediate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface FilespaceUserInfo {
    public Integer getId();
    public String getUsername();
    public LocalDateTime getJoinDateTime();
    public Boolean getAllowDownload();
    public Boolean getAllowUpload();
    public Boolean getAllowDeletion();
    public Boolean getAllowUserManagement();
    public Boolean getAllowFilespaceManagement();
}
