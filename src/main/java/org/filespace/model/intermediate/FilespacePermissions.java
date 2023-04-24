package org.filespace.model.intermediate;

import java.time.LocalDate;
import java.time.LocalTime;

public interface FilespacePermissions {
    public Long getId();
    public String getTitle();

    public LocalDate getJoinDate();
    public LocalTime getJoinTime();

    public Boolean getAllowDownload();
    public Boolean getAllowUpload();
    public Boolean getAllowDeletion();
    public Boolean getAllowUserManagement();
    public Boolean getAllowFilespaceManagement();
}
