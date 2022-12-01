package org.filespace.services;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.filespace.model.*;
import org.filespace.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class IntegratedService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FilespaceRepository filespaceRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFilespaceRelationRepository relationRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private FileService fileService;

    public void registerUser(String username, String password, String email){

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        User user = new User(username, encoder.encode(password),email, LocalDate.now());

        if (!validationService.validate(user))
            throw new IllegalArgumentException(validationService.getConstrainsViolations(user));

        if(userRepository.existsByUsernameOrEmail(user.getUsername(),user.getEmail()))
            throw new IllegalArgumentException("Such username or email has already been taken");

        userRepository.saveAndFlush(user);
    }

    public User getUserById(String id){
        Long lId = Long.parseLong(id);

        Optional<User> optional = userRepository.findById(lId);
        if (optional.isEmpty())
            throw new EntityNotFoundException("No such user found");
        return optional.get();
    }


    public void updateUser(String requestSender, Long requestedUserId, User newState) throws Exception {
        User user = userRepository.getById(requestedUserId);

        if (requestSender != user.getUsername())
            throw new IllegalAccessException("No authority over this user");

        //Пока недостаточно данных
    }

    public void deleteUser(String requestSender, String requestedUserId) {

    }

    @Transactional
    public void createFilespace(String requestSender, String title){
        User user = userRepository.findUserByUsername(requestSender);
        Filespace filespace = new Filespace(title);

        if (!validationService.validate(filespace))
            throw new IllegalArgumentException(
                    validationService.getConstrainsViolations(filespace));

        filespaceRepository.saveAndFlush(filespace);

        UserFilespaceRelation relation = new UserFilespaceRelation();
        relation.setUser(user);
        relation.setFilespace(filespace);
        relation.setRole(Role.CREATOR);

        relationRepository.saveAndFlush(relation);
    }

    public Filespace getFilespaceById(String requestSender, Long requestedFilespaceId) throws Exception{
        User user = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optional = filespaceRepository.findById(requestedFilespaceId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such entity found");



        if (!relationRepository.existsByKey(new UserFilespaceKey(user.getId(), requestedFilespaceId)))
            throw new IllegalAccessException("No access to this filespace");

        return optional.get();
    }

    @Transactional
    public void saveFile(HttpServletRequest request, String requestSender) throws Exception {

        User user = userRepository.findUserByUsername(requestSender);
        File file = new File();
        file.setPostDate(LocalDate.now());
        file.setPostTime(LocalTime.now());
        file.setNumberOfDownloads(0);

        file.setSender(user);

        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterStream = upload.getItemIterator(request);

        String md5 = "";

        while (iterStream.hasNext()) {
            FileItemStream item = iterStream.next();
            InputStream stream = item.openStream();

            if (!item.isFormField()) {
                file.setFileName(item.getName());

                md5 = fileService.uploadFile(stream);
                file.setMd5Hash(md5);

            } else {
                if (item.getFieldName() == "comment"){
                    String comment = Streams.asString(stream);
                    if (comment.length() > 200)
                        comment = comment.substring(0,201);

                    file.setComment(comment);
                }
                    file.setComment(Streams.asString(stream));

            }

            stream.close();
        }

        file.setSize(fileService.getFileSize(md5));

        try {
            fileRepository.saveAndFlush(file);
        } catch (Exception e){
            fileService.deleteFile(md5);
            throw e;
        }
    }

    public List<Object> sendFile(String requestSender, String fileId) throws Exception {
        User user = userRepository.findUserByUsername(requestSender);

        Long id = Long.parseLong(fileId);
        Optional<File> optional = fileRepository.findById(id);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();
        System.out.println("Оригинальный файл: " + file.toString());

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

        InputStreamResource resource = fileService.getFile(file.getMd5Hash());
        List<Object> list = new LinkedList<>();
        list.add(file);
        list.add(resource);

        return list;
    }

}
