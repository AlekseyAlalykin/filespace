package org.filespace.threads;

import org.filespace.services.DiskStorageService;

import java.io.IOException;
import java.util.List;

public class FileDeletingThread extends Thread {
    private static int threadInitNumber = 0;
    private List<String> md5Hashes;

    private static int nextThreadNum(){
        return threadInitNumber++;
    }

    public FileDeletingThread(List<String> md5Hashes) {
        super("File-Deleting-Thread-" + nextThreadNum());
        this.md5Hashes = md5Hashes;
    }

    @Override
    public void run() {
        for (String md5: md5Hashes){
            DiskStorageService diskStorageService = new DiskStorageService();
            try {
                diskStorageService.deleteFile(md5);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
