package org.filespace.services;


import org.filespace.model.compoundrelations.*;
import org.filespace.model.entities.File;
import org.filespace.model.entities.Filespace;
import org.filespace.model.entities.User;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.intermediate.FilespacePermissions;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.filespace.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class FilespaceService {

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


    public List<FilespacePermissions> getUserFilespacesByTitle(User user, String title){
        List<FilespacePermissions> list = userFilespaceRelationRepository.findFilespacesAndPermissionsByUserIdAndTitle(user.getId(), title);

        return list;
    }

    @Transactional
    public Filespace createFilespace(User user, String title){
        Filespace filespace = new Filespace(title);

        if (!validationService.validate(filespace))
            throw new IllegalArgumentException(
                    validationService.getConstrainsViolations(filespace));

        filespaceRepository.save(filespace);

        UserFilespaceRelation relation = new UserFilespaceRelation();

        relation.setUser(user);
        relation.setFilespace(filespace);

        relation.setJoinDate(LocalDate.now());
        relation.setJoinTime(LocalTime.now());

        relation.setAllowDeletion(true);
        relation.setAllowDownload(true);
        relation.setAllowUserManagement(true);
        relation.setAllowFilespaceManagement(true);
        relation.setAllowUpload(true);

        userFilespaceRelationRepository.saveAndFlush(relation);
        filespaceRepository.flush();

        return filespace;
    }

    public FilespacePermissions getFilespaceById(User user, Long filespaceId) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such entity found");

        Optional<FilespacePermissions> optionalPermissions = userFilespaceRelationRepository.findFilespaceAndPermissionsByUserIdAndFilespaceId(user.getId(),filespaceId);

        if (optionalPermissions.isEmpty())
            throw new IllegalAccessException("No access to this filespace");

        return optionalPermissions.get();
    }

    public void updateFilespace(User user, Long id, String title) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace found");

        Filespace filespace = optionalFilespace.get();

        Optional<UserFilespaceRelation> optionalRelation = userFilespaceRelationRepository
                .findById(new CompoundKey(user.getId(),id));

        if (optionalRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation relation = optionalRelation.get();

        if (!relation.allowFilespaceManagement())
            throw new IllegalAccessException("No authority over filespace");

        if (title == null)
            title = "";

        if (title.length() > 30)
            title = title.substring(0,30);

        filespace.setTitle(title);

        filespaceRepository.saveAndFlush(filespace);
    }

    @Transactional
    public void deleteFilespace(User user, Long id) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace found");

        Filespace filespace = optionalFilespace.get();

        Optional<UserFilespaceRelation> optionalRelation = userFilespaceRelationRepository
                .findById(new CompoundKey(user.getId(),id));

        if (optionalRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation relation = optionalRelation.get();

        if (!relation.allowFilespaceManagement())
            throw new IllegalAccessException("No authority over filespace");

        fileFilespaceRelationRepository.deleteByFilespace(filespace);

        userFilespaceRelationRepository.deleteByFilespace(filespace);

        fileFilespaceRelationRepository.flush();
        userFilespaceRelationRepository.flush();

        filespaceRepository.delete(filespace);
        filespaceRepository.flush();

    }

    public FileFilespaceRelation attachFileToFilespace(User user, Long filespaceId, Long fileId) throws Exception{
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
                .findById(new CompoundKey(user.getId(),filespaceId));

        if (optionalRelation.isEmpty())
            throw new IllegalAccessException("No access to filespace");

        if (!optionalRelation.get().allowUpload())
            throw new IllegalAccessException("No upload permission");

        if (fileFilespaceRelationRepository.existsByFileAndFilespace(file,filespace))
            throw new IllegalStateException("File already in filespace");

        FileFilespaceRelation relation = new FileFilespaceRelation();

        relation.setAttachDate(LocalDate.now());
        relation.setAttachTime(LocalTime.now());
        relation.setFile(file);
        relation.setFilespace(filespace);

        fileFilespaceRelationRepository.saveAndFlush(relation);

        return relation;
    }

    public List<FilespaceFileInfo> getFilesFromFilespace(User user, Long id, String query) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        if (!userFilespaceRelationRepository.existsById(
                new CompoundKey(user.getId(),id)))
            throw new IllegalAccessException("No access to filespace");

        return fileFilespaceRelationRepository.getFilesFromFilespace(id, query);
    }

    public List<FilespaceUserInfo> getUsersOfFilespace(User user, Long id, String username) throws Exception {
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        if (!userFilespaceRelationRepository.existsById(
                new CompoundKey(user.getId(),id)))
            throw new IllegalAccessException("No access to filespace");

        return userFilespaceRelationRepository.getFilespaceUsersByIdAndUsername(id, username);
    }


    public UserFilespaceRelation attachUserToFilespace(User requester, Long filespaceId, UserFilespaceRelation relation) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Filespace filespace = optionalFilespace.get();

        Optional<User> optionalUser;
        if (relation.getUser().getId() != null)
            optionalUser = userRepository.findById(relation.getUser().getId());
        else
            optionalUser = userRepository.findByUsername(relation.getUser().getUsername());

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user");

        User targetedUser = optionalUser.get();

        Optional<UserFilespaceRelation> optionalRequesterRelation = userFilespaceRelationRepository.findById(
                new CompoundKey(requester.getId(),filespaceId));

        if (optionalRequesterRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation requesterRelation = optionalRequesterRelation.get();

        if (!requesterRelation.allowUserManagement())
            throw new IllegalAccessException("No authority over filespace");

        if (!requesterRelation.allowDownload() && relation.allowDownload())
            throw new IllegalArgumentException("Can't give download permission since you don't have one");

        if (!requesterRelation.allowUpload() && relation.allowUpload())
            throw new IllegalArgumentException("Can't give upload permission since you don't have one");

        if (!requesterRelation.allowUserManagement() && relation.allowUserManagement())
            throw new IllegalArgumentException("Can't give user management permission since you don't have one");

        if (!requesterRelation.allowFilespaceManagement() && relation.allowFilespaceManagement())
            throw new IllegalArgumentException("Can't give filespace management permission since you don't have one");

        Optional<UserFilespaceRelation> optionalTargetRelation = userFilespaceRelationRepository.findById(
                new CompoundKey(targetedUser.getId(),filespaceId));
        if (optionalTargetRelation.isPresent())
            throw new IllegalArgumentException("User already attached to filespace");

        if (!requesterRelation.allowFilespaceManagement() && relation.allowFilespaceManagement().equals(true))
            throw new IllegalArgumentException("Can't give filespace management permission without having one");

        relation.setUser(targetedUser);
        relation.setFilespace(filespace);
        relation.setJoinDate(LocalDate.now());
        relation.setJoinTime(LocalTime.now());

        userFilespaceRelationRepository.saveAndFlush(relation);

        return relation;
    }

    @Transactional
    public void detachUserFromFilespace(User requester, Long filespaceId, Long userId, Boolean deleteFiles) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Filespace filespace = optionalFilespace.get();

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user");

        User targetedUser = optionalUser.get();

        Optional<UserFilespaceRelation> optionalRequesterFilespaceRelation =
                userFilespaceRelationRepository.findById(new CompoundKey(requester.getId(), filespaceId));

        if (optionalRequesterFilespaceRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        if (!optionalRequesterFilespaceRelation.get().allowUserManagement() && !requester.equals(targetedUser))
            throw new IllegalAccessException("No permission to remove user");

        Optional<UserFilespaceRelation> optionalTargetedUserRelation =
                userFilespaceRelationRepository.findById(new CompoundKey(userId, filespaceId));

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

    public void detachFileFromFilespace(User requester, Long filespaceId, Long fileId) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(filespaceId);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Optional<File> optionalFile = fileRepository.findById(fileId);

        if (optionalFile.isEmpty())
            throw new EntityNotFoundException("No such file");

        File file = optionalFile.get();

        Optional<UserFilespaceRelation> optionalRequesterFilespaceRelation =
                userFilespaceRelationRepository.findById(new CompoundKey(requester.getId(), filespaceId));

        if (optionalRequesterFilespaceRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation requesterRelation = optionalRequesterFilespaceRelation.get();

        Optional<FileFilespaceRelation> optionalFileFilespaceRelation =
                fileFilespaceRelationRepository.findById(new CompoundKey(fileId,filespaceId));

        if (optionalFileFilespaceRelation.isEmpty())
            throw new IllegalArgumentException("No such file in filespace");

        FileFilespaceRelation fileFilespaceRelation = optionalFileFilespaceRelation.get();

        if (!requesterRelation.allowDeletion() && !file.getSender().equals(requester))
            throw new IllegalAccessException("No authority to remove the file");

        fileFilespaceRelationRepository.delete(fileFilespaceRelation);
    }

    public void updateUserPermissions(User requester, UserFilespaceRelation relation) throws Exception {
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(relation.getFilespace().getId());

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        Optional<User> optionalUser = userRepository.findById(relation.getUser().getId());

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user");

        User target = optionalUser.get();

        Optional<UserFilespaceRelation> optionalRequesterFilespaceRelation =
                userFilespaceRelationRepository.findById(new CompoundKey(requester.getId(), relation.getFilespace().getId()));

        if (optionalRequesterFilespaceRelation.isEmpty())
            throw new IllegalAccessException("No authority over filespace");

        UserFilespaceRelation requesterRelation = optionalRequesterFilespaceRelation.get();

        if (!requesterRelation.allowUserManagement())
            throw new IllegalAccessException("No authority to change permissions");

        if (!requesterRelation.allowUserManagement())
            throw new IllegalAccessException("No authority over filespace");

        Optional<UserFilespaceRelation> optionalTargetedUserRelation =
                userFilespaceRelationRepository.findById(new CompoundKey(relation.getUser().getId(), relation.getFilespace().getId()));

        if (optionalTargetedUserRelation.isEmpty())
            throw new IllegalArgumentException("User isn't in filespace");

        UserFilespaceRelation targetedUserRelation = optionalTargetedUserRelation.get();

        if (!requesterRelation.allowDownload() && relation.allowDownload() && !targetedUserRelation.allowDownload())
            throw new IllegalArgumentException("Can't give download permission since you don't have one");

        if (!requesterRelation.allowUpload() && relation.allowUpload() && !targetedUserRelation.allowUpload())
            throw new IllegalArgumentException("Can't give upload permission since you don't have one");

        if (!requesterRelation.allowUserManagement() && relation.allowUserManagement() && !targetedUserRelation.allowUserManagement())
            throw new IllegalArgumentException("Can't give user management permission since you don't have one");

        if (!requesterRelation.allowFilespaceManagement() && relation.allowFilespaceManagement() && !targetedUserRelation.allowFilespaceManagement())
            throw new IllegalArgumentException("Can't give filespace management permission since you don't have one");

        if (!requesterRelation.allowDeletion() && relation.allowDeletion() && !targetedUserRelation.allowDeletion())
            throw new IllegalArgumentException("Can't give filespace management permission since you don't have one");

        if (requester.equals(target))
            throw new IllegalArgumentException("Can't change self permissions");

        if (!optionalRequesterFilespaceRelation.get().allowFilespaceManagement() && relation.allowFilespaceManagement())
            throw new IllegalArgumentException("Can't give filespace management permission without having one");

        targetedUserRelation.setAllowDownload(relation.allowDownload());
        targetedUserRelation.setAllowUpload(relation.allowUpload());
        targetedUserRelation.setAllowDeletion(relation.allowDeletion());
        targetedUserRelation.setAllowUserManagement(relation.allowUserManagement());
        targetedUserRelation.setAllowFilespaceManagement(relation.allowFilespaceManagement());

        userFilespaceRelationRepository.saveAndFlush(targetedUserRelation);
    }
}
