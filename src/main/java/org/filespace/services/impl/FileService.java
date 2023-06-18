package org.filespace.services.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.filespace.model.entities.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.simplerelations.File;
import org.filespace.model.entities.simplerelations.User;
import org.filespace.repositories.*;
import org.filespace.services.util.DiskStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.time.LocalDateTime;
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
    private FileFilespaceRelationRepository fileFilespaceRelationRepository;

    @Autowired
    private DiskStorage storage;

    public List<File> getUserFilesByFilename(User user, String filename){
        return fileRepository.getAllBySenderAndFileNameIgnoreCaseStartingWithOrderByPostDateTimeDesc(user, filename);
    }

    @Transactional
    public List<File> saveFileFromUser(HttpServletRequest request, User user) throws Exception {
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterStream = upload.getItemIterator(request);

        String description = "";


        boolean hasFiles = false;

        List<File> files = new LinkedList<>();

        while (iterStream.hasNext()) {
            FileItemStream item = iterStream.next();
            InputStream stream = item.openStream();

            if (!item.isFormField()) {
                File file = new File();

                file.setPostDateTime(LocalDateTime.now());
                file.setNumberOfDownloads(0);
                file.setSender(user);
                file.setFileName(item.getName());

                String tempFileName = "";
                String md5 = "";


                try {
                    tempFileName = storage.temporarySaveFile(stream);

                    md5 = storage.md5FileHash(tempFileName);
                    file.setSize(storage.getFileSize(tempFileName));

                    List<File> filesList;

                    boolean flag = false;

                    do {
                        filesList = fileRepository.getByMd5Hash(md5);

                        if (filesList.size() == 0)
                            flag = true;

                        for (File fileItem: filesList){
                            if (fileItem.getSize().equals(file.getSize())){
                                flag = true;
                                break;
                            }
                        }

                        if (!flag)
                            md5 = DigestUtils.md5Hex(md5);
                    } while (!flag);

                } catch (Exception e){
                    e.printStackTrace();

                    throw new FileUploadException(e.getMessage());
                }

                try {
                    storage.moveToStorageDirectory(md5, tempFileName);
                } catch (Exception e){
                    throw new Exception(e.getMessage());
                }

                file.setMd5Hash(md5);

                files.add(file);

                hasFiles = true;
            }
            else {
                if (item.getFieldName().equals("description")){
                    description = Streams.asString(stream, "UTF-8");
                    if (description.length() > 400)
                        description = description.substring(0,401);
                }
            }

            stream.close();
        }

        if (!hasFiles)
            throw new IllegalStateException("No files attached");

        for (File file: files){
            file.setDescription(description);
        }

        try {
            fileRepository.saveAllAndFlush(files);

        } catch (Exception e) {
            for (File file: files){
                storage.deleteFile(file.getMd5Hash());
            }

            throw e;
        }

        return files;
    }

    public File copyFile(User user, Integer fileId) throws Exception {
        Optional<File> optional = fileRepository.findById(fileId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File originalFile = optional.get();

        if (user.getFiles().contains(originalFile))
            throw new IllegalAccessException("Can't copy own file");

        boolean hasRight = false;

        for (UserFilespaceRelation relation: user.getUserFilespaceRelations()) {
            if (relation.getFilespace().getFiles().contains(originalFile)) {
                hasRight = true;
                break;
            }
        }

        if (!hasRight)
            throw new IllegalAccessException("No access to the file");

        File copy = new File(user,
                originalFile.getFileName(),
                originalFile.getSize(),
                LocalDateTime.now(),
                0,
                originalFile.getDescription(),
                originalFile.getMd5Hash());

        fileRepository.saveAndFlush(copy);

        return copy;
    }

    @Transactional
    public List<Object> sendFileToUser(User user, Integer fileId) throws Exception {
        Optional<File> optional = fileRepository.findById(fileId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();

        boolean hasRight = false;

        if (user.getFiles().contains(file))
            hasRight = true;

        for (UserFilespaceRelation relation: user.getUserFilespaceRelations()) {
            if (relation.getFilespace().getFiles().contains(file) && relation.allowDownload()) {
                hasRight = true;
                break;
            }
        }

        if (!hasRight)
            throw new IllegalAccessException("No access to the file");

        file.setNumberOfDownloads(file.getNumberOfDownloads() + 1);

        fileRepository.save(file);

        byte[] resource = storage.getFile(file.getMd5Hash());
        List<Object> list = new LinkedList<>();
        list.add(file);
        list.add(resource);

        fileRepository.flush();
        return list;
    }

    public File getFileJSON(User user, Integer fileId) throws Exception{
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

        return file;
    }

    public void updateFileInfo(User user, Integer fileId, String description, String filename) throws IllegalAccessException {
        Optional<File> optional = fileRepository.findById(fileId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();

        if (!user.getFiles().contains(file))
            throw new IllegalAccessException("No authority over file");

        if (description != null) {
            if (description.length() > 400)
                description = description.substring(0, 401);

            file.setDescription(description);
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
    public void deleteFile(User user, Integer fileId) throws Exception{
        Optional<File> optional = fileRepository.findById(fileId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();

        if (!user.getFiles().contains(file))
            throw new IllegalAccessException("No authority over file");

        fileFilespaceRelationRepository.deleteByFile(file);

        String md5 = file.getMd5Hash();
        fileRepository.deleteById(fileId);

        if (!fileRepository.existsByMd5Hash(md5))
            storage.deleteFile(md5);
    }

}
