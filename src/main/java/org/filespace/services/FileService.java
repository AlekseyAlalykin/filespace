package org.filespace.services;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.filespace.model.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.File;
import org.filespace.model.entities.User;
import org.filespace.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileFilespaceRelationRepository fileFilespaceRelationRepository;

    @Autowired
    private DiskStorageService diskStorageService;

    public List<File> getUserFiles(User user){
        return user.getFiles();
    }

    @Transactional
    public List<File> saveFile(HttpServletRequest request, User user) throws Exception {
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterStream = upload.getItemIterator(request);

        String comment = "";


        boolean hasFiles = false;

        List<File> files = new LinkedList<>();

        while (iterStream.hasNext()) {
            FileItemStream item = iterStream.next();
            InputStream stream = item.openStream();

            if (!item.isFormField()) {
                File file = new File();

                file.setPostDate(LocalDate.now());
                file.setPostTime(LocalTime.now());
                file.setNumberOfDownloads(0);
                file.setSender(user);
                file.setFileName(item.getName());

                String tempFileLocation = "";
                String md5 = "";

                tempFileLocation = diskStorageService.temporarySaveFile(stream);
                md5 = diskStorageService.md5FileHash(tempFileLocation);
                diskStorageService.moveToStorageDirectory(md5, tempFileLocation);

                file.setMd5Hash(md5);
                file.setSize(diskStorageService.getFileSize(md5));

                files.add(file);

                hasFiles = true;
            }
            else {
                if (item.getFieldName().equals("comment")){
                    comment = Streams.asString(stream);
                    if (comment.length() > 200)
                        comment = comment.substring(0,201);
                }
            }

            stream.close();
        }

        if (!hasFiles)
            throw new IllegalStateException("No files attached");

        for (File file: files){
            file.setComment(comment);
        }

        try {
            fileRepository.saveAllAndFlush(files);

        } catch (Exception e) {
            for (File file: files){
                diskStorageService.deleteFile(file.getMd5Hash());
            }

            throw e;
        }

        return files;
    }

    @Transactional
    public List<Object> sendFile(User user, Long fileId) throws Exception {
        Optional<File> optional = fileRepository.findById(fileId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();

        boolean hasRight = false;

        if (user.getFiles().contains(file))
            hasRight = true;

        for (UserFilespaceRelation relation: user.getUserFilespaceRelations()) {
            if (relation.getFilespace().getFiles().contains(file)) {
                hasRight = true;
                break;
            }
        }

        if (!hasRight)
            throw new IllegalAccessException("No access to the file");

        file.setNumberOfDownloads(file.getNumberOfDownloads() + 1);

        fileRepository.save(file);

        InputStreamResource resource = diskStorageService.getFile(file.getMd5Hash());
        List<Object> list = new LinkedList<>();
        list.add(file);
        list.add(resource);

        fileRepository.flush();
        return list;
    }

    public void updateFileInfo(User user, Long id, String comment, String filename) throws Exception {
        Optional<File> optional = fileRepository.findById(id);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();

        if (!user.getFiles().contains(file))
            throw new IllegalAccessException("No authority over file");

        if (comment != null) {
            if (comment.length() > 200)
                comment = comment.substring(0, 201);

            file.setComment(comment);
        }

        if (filename != null){
            if (filename.length() > 254)
                throw new IllegalArgumentException("Illegal filename length");

            Pattern pattern = Pattern.compile(
                    "^(?!^(PRN|AUX|CLOCK\\$|NUL|CON|COM\\d|LPT\\d|\\..*)(\\..+)?$)[^\\x00-\\x1f\\\\?*:\\\";|/]+$",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(filename);

            if (!matcher.matches())
                throw new IllegalArgumentException("Illegal filename");

            file.setFileName(filename);
        }

        fileRepository.saveAndFlush(file);
    }

    @Transactional
    public void deleteFile(User user, Long id) throws Exception{
        Optional<File> optional = fileRepository.findById(id);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();

        if (!user.getFiles().contains(file))
            throw new IllegalAccessException("No authority over file");

        fileFilespaceRelationRepository.deleteByFile(file);

        String md5 = file.getMd5Hash();
        fileRepository.deleteById(id);

        if (!fileRepository.existsByMd5Hash(md5))
            diskStorageService.deleteFile(md5);
    }

}
