package org.filespace.model.intermediate;

import java.time.LocalDate;
import java.time.LocalTime;

public interface FilespaceUserInfo {
    public Long getId();
    public String getUsername();
    public LocalDate getJoinDate();
    public LocalTime getJoinTime();
    public Boolean getAllowDownload();
    public Boolean getAllowUpload();
    public Boolean getAllowDeletion();
    public Boolean getAllowUserManagement();
    public Boolean getAllowFilespaceManagement();
}
