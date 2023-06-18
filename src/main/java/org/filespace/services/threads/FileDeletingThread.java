package org.filespace.services.threads;

import org.filespace.services.util.DiskStorage;

import java.io.IOException;
import java.util.List;

public class FileDeletingThread extends CustomThread {
    private List<String> md5Hashes;

    public FileDeletingThread(List<String> md5Hashes) {
        super("File-Deleting-Thread-" + nextThreadNum());
        this.md5Hashes = md5Hashes;
    }

    @Override
    public void run() {
        for (String md5: md5Hashes){
            DiskStorage storage = new DiskStorage();
            try {
                storage.deleteFile(md5);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
