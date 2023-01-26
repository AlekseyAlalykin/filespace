package org.filespace.services;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.filespace.model.compoundrelations.*;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.intermediate.FilespaceRole;
import org.filespace.model.entities.File;
import org.filespace.model.entities.Filespace;
import org.filespace.model.entities.User;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.filespace.repositories.*;
import org.filespace.security.SecurityUtil;
import org.filespace.security.SessionManager;
import org.filespace.threads.FileDeletingThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.InputStream;
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
    private UserRepository userRepository;

    @Autowired
    private UserFilespaceRelationRepository userFilespaceRelationRepository;

    @Autowired
    private FileFilespaceRelationRepository fileFilespaceRelationRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SessionManager sessionManager;

    public void registerUser(String username, String password, String email){

        if (password.length() < 8)
            throw new IllegalArgumentException("Weak password");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        User user = new User(username, encoder.encode(password),email, LocalDate.now());

        if (!validationService.validate(user))
            throw new IllegalArgumentException(validationService.getConstrainsViolations(user));

        if(userRepository.existsByUsernameOrEmail(user.getUsername(),user.getEmail()))
            throw new IllegalArgumentException("Such username or email has already been taken");

        userRepository.saveAndFlush(user);
    }

    public User getUserById(Long id){
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty())
            throw new EntityNotFoundException("No such user found");
        return optional.get();
    }

    @Transactional
    public void deleteUser(String requestSender, Long userId) throws Exception{
        User requester = userRepository.findUserByUsername(requestSender);

        //Проверка пользователя
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user found");

        User target = optionalUser.get();

        if (!requester.equals(target))
            throw new IllegalAccessException("No authority");

        //Удаление связей пользователя и filespace
        for (UserFilespaceRelation relation: userFilespaceRelationRepository.getByUser(target)){
            Filespace filespace = relation.getFilespace();

            userFilespaceRelationRepository.delete(relation);

            if (userFilespaceRelationRepository.countAllByFilespace(filespace) == 0){
                fileFilespaceRelationRepository.deleteAllByFilespace(filespace);

                fileFilespaceRelationRepository.flush();

                filespaceRepository.delete(filespace);
            }
        }

        List<String> md5Hashes = new LinkedList<>();
        //Удаление связей файлов пользователя и filespace
        for (File file: fileRepository.getAllBySender(target)){
            //Если файлов с данным хеш значение больше нет
            if (fileRepository.countAllByMd5Hash(file.getMd5Hash()) == 1)
                md5Hashes.add(file.getMd5Hash());
            fileFilespaceRelationRepository.deleteAllByFile(file);
        }
        //Подготовил поток для удаления файлов
        FileDeletingThread thread = new FileDeletingThread(md5Hashes);

        //Удалил записи о файлах
        fileRepository.deleteAllBySender(target);

        //Закрыл все сессии
        sessionManager.closeAllUserSessions(target);

        //Удалил самого пользователя
        userRepository.delete(target);

        userFilespaceRelationRepository.flush();
        fileFilespaceRelationRepository.flush();
        fileRepository.flush();
        userRepository.flush();

        //Очистка в другом потоке
        thread.start();
    }

    @Transactional
    public void createFilespace(String requestSender, String title){
        User user = userRepository.findUserByUsername(requestSender);
        Filespace filespace = new Filespace(title);

        if (!validationService.validate(filespace))
            throw new IllegalArgumentException(
                    validationService.getConstrainsViolations(filespace));

        filespaceRepository.save(filespace);

        UserFilespaceRelation relation = new UserFilespaceRelation();
        relation.setUser(user);
        relation.setFilespace(filespace);
        relation.setRole(Role.CREATOR);

        userFilespaceRelationRepository.saveAndFlush(relation);
        filespaceRepository.flush();
    }

    public Filespace getFilespaceById(String requestSender, Long requestedFilespaceId) throws Exception{
        User user = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optional = filespaceRepository.findById(requestedFilespaceId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such entity found");



        if (!userFilespaceRelationRepository.existsById(new UserFilespaceKey(user.getId(), requestedFilespaceId)))
            throw new IllegalAccessException("No access to this filespace");

        return optional.get();
    }

    @Transactional
    public void saveFile(HttpServletRequest request, String requestSender) throws Exception {

        User user = userRepository.findUserByUsername(requestSender);

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

                tempFileLocation = fileService.temporarySaveFile(stream);
                md5 = fileService.md5FileHash(tempFileLocation);
                fileService.moveToStorageDirectory(md5, tempFileLocation);

                file.setMd5Hash(md5);
                file.setSize(fileService.getFileSize(md5));

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
                fileService.deleteFile(file.getMd5Hash());
            }

            throw e;
        }
    }

    @Transactional
    public List<Object> sendFile(String requestSender, Long fileId) throws Exception {
        User user = userRepository.findUserByUsername(requestSender);

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

        InputStreamResource resource = fileService.getFile(file.getMd5Hash());
        List<Object> list = new LinkedList<>();
        list.add(file);
        list.add(resource);

        fileRepository.flush();
        return list;
    }

    public List<File> getUserFiles(String requestSender){
        User user = userRepository.findUserByUsername(requestSender);

        return user.getFiles();
    }

    public List<FilespaceRole> getUserFilespaces(String requestSender){
        User user = userRepository.findUserByUsername(requestSender);

        List<FilespaceRole> list = userFilespaceRelationRepository.findFilespacesAndRolesByUserId(user.getId());

        return list;
    }

    @Transactional
    public void deleteFile(String requestSender, Long id) throws Exception{
        User user = userRepository.findUserByUsername(requestSender);

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
            fileService.deleteFile(md5);
    }

    public void updateFileComment(String requestSender, Long id, String comment) throws Exception {
        User user = userRepository.findUserByUsername(requestSender);

        Optional<File> optional = fileRepository.findById(id);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such file found");

        File file = optional.get();

        if (!user.getFiles().contains(file))
            throw new IllegalAccessException("No authority over file");

        if (comment == null)
            comment = "";

        if (comment.length() > 200)
            comment = comment.substring(0,201);

        file.setComment(comment);

        fileRepository.saveAndFlush(file);

    }

    public void updateFilespaceTitle(String requestSender, Long id, String title) throws Exception{
        User user = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace found");

        Filespace filespace = optionalFilespace.get();

        Optional<UserFilespaceRelation> optionalRelation = userFilespaceRelationRepository
                .findById(new UserFilespaceKey(user.getId(),id));

        if (optionalRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation relation = optionalRelation.get();

        if (relation.getRole() != Role.CREATOR)
            throw new IllegalAccessException("No authority over filespace");

        if (title == null)
            title = "";

        if (title.length() > 30)
            title = title.substring(0,30);

        filespace.setTitle(title);

        filespaceRepository.saveAndFlush(filespace);
    }

    @Transactional
    public void deleteFilespace(String requestSender, Long id) throws Exception{
        User user = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace found");

        Filespace filespace = optionalFilespace.get();

        Optional<UserFilespaceRelation> optionalRelation = userFilespaceRelationRepository
                .findById(new UserFilespaceKey(user.getId(),id));

        if (optionalRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation relation = optionalRelation.get();

        if (relation.getRole() != Role.CREATOR)
            throw new IllegalAccessException("No authority over filespace");

        fileFilespaceRelationRepository.deleteByFilespace(filespace);

        userFilespaceRelationRepository.deleteByFilespace(filespace);

        fileFilespaceRelationRepository.flush();
        userFilespaceRelationRepository.flush();

        filespaceRepository.delete(filespace);
        filespaceRepository.flush();

    }

    public void addFileToFilespace(String requestSender, Long filespaceId, Long fileId) throws Exception{
        User user = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Filespace filespace = optionalFilespace.get();

        Optional<File> optionalFile = fileRepository.findById(fileId);

        if (optionalFile.isEmpty())
            throw new EntityNotFoundException("No such file");

        File file = optionalFile.get();

        if (!file.getSender().equals(user))
            throw new IllegalAccessException("No access to file");

        Optional<UserFilespaceRelation> optionalRelation = userFilespaceRelationRepository
                .findById(new UserFilespaceKey(user.getId(),filespaceId));

        if (optionalRelation.isEmpty())
            throw new IllegalAccessException("No access to filespace");

        Role userRole = optionalRelation.get().getRole();

        if (userRole == Role.SPECTATOR)
            throw new IllegalAccessException("Current role SPECTATOR");

        if (fileFilespaceRelationRepository.existsByFileAndFilespace(file,filespace))
            throw new IllegalStateException("File already in filespace");

        FileFilespaceRelation relation = new FileFilespaceRelation();

        relation.setAttachDate(LocalDate.now());
        relation.setAttachTime(LocalTime.now());
        relation.setFile(file);
        relation.setFilespace(filespace);

        fileFilespaceRelationRepository.saveAndFlush(relation);
    }

    public List<FilespaceFileInfo> getFilesFromFilespace(String requestSender, Long id) throws Exception{
        User user = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        if (!userFilespaceRelationRepository.existsById(
                new UserFilespaceKey(user.getId(),id)))
            throw new IllegalAccessException("No access to filespace");

        return fileFilespaceRelationRepository.getFilesFromFilespace(id);
    }

    public List<FilespaceUserInfo> getUsersOfFilespace(String requestSender, Long id) throws Exception {
        User user = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        if (!userFilespaceRelationRepository.existsById(
                new UserFilespaceKey(user.getId(),id)))
            throw new IllegalAccessException("No access to filespace");

        return userFilespaceRelationRepository.getFilespaceUsersById(id);
    }

    public void addUserToFilespace(String requestSender, Long filespaceId, Long userId, Role role) throws Exception{
        User requester = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Filespace filespace = optionalFilespace.get();

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user");

        User targetedUser = optionalUser.get();

        Optional<UserFilespaceRelation> optionalRelation = userFilespaceRelationRepository.findById(
                new UserFilespaceKey(requester.getId(),filespaceId));

        if (optionalRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        Role requesterRole = optionalRelation.get().getRole();

        if (requesterRole != Role.CREATOR)
            throw new IllegalAccessException("No authority over filespace");

        if (role == Role.CREATOR)
            throw new IllegalArgumentException("Only singular CREATOR role per filespace allowed");

        UserFilespaceRelation relation = new UserFilespaceRelation();

        relation.setUser(targetedUser);
        relation.setFilespace(filespace);
        relation.setRole(role);

        userFilespaceRelationRepository.saveAndFlush(relation);
    }

    @Transactional
    public void deleteUserFromFilespace(String requestSender, Long filespaceId, Long userId, Boolean deleteFiles) throws Exception{
        User requester = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Filespace filespace = optionalFilespace.get();

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user");

        User targetedUser = optionalUser.get();

        Optional<UserFilespaceRelation> optionalRequesterFilespaceRelation =
                userFilespaceRelationRepository.findById(new UserFilespaceKey(requester.getId(), filespaceId));

        if (optionalRequesterFilespaceRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation requesterRelation = optionalRequesterFilespaceRelation.get();

        if (requesterRelation.getRole() != Role.CREATOR && !requester.equals(targetedUser))
            throw new IllegalAccessException("No authority to remove user");

        Optional<UserFilespaceRelation> optionalTargetedUserRelation =
                userFilespaceRelationRepository.findById(new UserFilespaceKey(userId, filespaceId));

        if (optionalTargetedUserRelation.isEmpty())
            throw new IllegalArgumentException("User isn't in filespace");

        if (deleteFiles)
            fileFilespaceRelationRepository.deleteFilesFromFilespaceByUserId(userId);

        userFilespaceRelationRepository.delete(optionalTargetedUserRelation.get());

        Long usersLeft = userFilespaceRelationRepository.countAllByFilespace(filespace);

        if (usersLeft == 0) {
            fileFilespaceRelationRepository.deleteAllByFilespace(filespace);

            fileFilespaceRelationRepository.flush();

            filespaceRepository.delete(filespace);
        }

        fileFilespaceRelationRepository.flush();
        userFilespaceRelationRepository.flush();
        filespaceRepository.flush();

    }

    public void deleteFileFromFilespace(String requestSender, Long filespaceId, Long fileId) throws Exception{
        User requester = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Optional<File> optionalFile = fileRepository.findById(fileId);
        if (optionalFile.isEmpty())
            throw new EntityNotFoundException("No such file");

        File file = optionalFile.get();

        Optional<UserFilespaceRelation> optionalRequesterFilespaceRelation =
                userFilespaceRelationRepository.findById(new UserFilespaceKey(requester.getId(), filespaceId));

        if (optionalRequesterFilespaceRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation requesterRelation = optionalRequesterFilespaceRelation.get();

        Optional<FileFilespaceRelation> optionalFileFilespaceRelation =
                fileFilespaceRelationRepository.findById(new FileFilespaceKey(fileId,filespaceId));

        if (optionalFileFilespaceRelation.isEmpty())
            throw new IllegalArgumentException("No such file in filespace");

        FileFilespaceRelation fileFilespaceRelation = optionalFileFilespaceRelation.get();

        if (requesterRelation.getRole() != Role.CREATOR && !file.getSender().equals(requester))
            throw new IllegalAccessException("No authority to remove the file");

        fileFilespaceRelationRepository.delete(fileFilespaceRelation);
    }

    public void patchUserRole(String requestSender, Long filespaceId, Long userId, Role role) throws Exception {
        User requester = userRepository.findUserByUsername(requestSender);

        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user");

        User target = optionalUser.get();

        Optional<UserFilespaceRelation> optionalRequesterFilespaceRelation =
                userFilespaceRelationRepository.findById(new UserFilespaceKey(requester.getId(), filespaceId));

        if (optionalRequesterFilespaceRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation requesterRelation = optionalRequesterFilespaceRelation.get();

        if (requesterRelation.getRole().equals(Role.CONTRIBUTOR) || requesterRelation.getRole().equals(Role.SPECTATOR))
            throw new IllegalAccessException("No authority to change role");

        Optional<UserFilespaceRelation> optionalTargetedUserRelation =
                userFilespaceRelationRepository.findById(new UserFilespaceKey(userId, filespaceId));

        if (optionalTargetedUserRelation.isEmpty())
            throw new IllegalArgumentException("User isn't in filespace");

        UserFilespaceRelation targetedUserRelation = optionalTargetedUserRelation.get();

        if (targetedUserRelation.getRole().equals(Role.CREATOR))
            throw new IllegalAccessException("CREATOR role can't be changed");

        if (requester.equals(target))
            throw new IllegalArgumentException("Can't change self role");


        if (requesterRelation.getRole().equals(Role.ADMINISTRATOR) && (Role.ADMINISTRATOR.getWeight() <= role.getWeight()))
            throw new IllegalArgumentException("No authority to attach given role");

        targetedUserRelation.setRole(role);
        userFilespaceRelationRepository.saveAndFlush(targetedUserRelation);
    }

    public void updateUser(String requestSender, Long userId, String username, String password, String email) throws Exception{
        User requester = userRepository.findUserByUsername(requestSender);

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user found");

        User oldUserState = optionalUser.get();

        if (!requester.equals(oldUserState))
            throw new IllegalAccessException("No authority");

        User newUserState = new User();

        if (username == null)
            newUserState.setUsername(oldUserState.getUsername());
        else {
            if (userRepository.existsByUsername(username))
                throw new IllegalArgumentException("Username already taken");

            newUserState.setUsername(username);
        }

        if (password == null)
            newUserState.setPassword(oldUserState.getPassword());
        else {
            if (password.length() < 8)
                throw new IllegalArgumentException("Weak password");

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
            newUserState.setPassword(encoder.encode(password));
        }

        if (email == null)
            newUserState.setEmail(oldUserState.getEmail());
        else {
            if (userRepository.existsByEmail(email))
                throw new IllegalArgumentException("Email already taken");

            newUserState.setEmail(email);
        }

        newUserState.setId(oldUserState.getId());
        newUserState.setRegistrationDate(oldUserState.getRegistrationDate());

        if (!validationService.validate(newUserState))
            throw new IllegalArgumentException(validationService.getConstrainsViolations(newUserState));

        userRepository.saveAndFlush(newUserState);
    }

}
