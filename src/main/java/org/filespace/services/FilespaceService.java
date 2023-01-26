package org.filespace.services;

import org.filespace.model.compoundrelations.*;
import org.filespace.model.entities.File;
import org.filespace.model.entities.Filespace;
import org.filespace.model.entities.User;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.intermediate.FilespaceRole;
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

    public List<FilespaceRole> getUserFilespaces(User user){
        List<FilespaceRole> list = userFilespaceRelationRepository.findFilespacesAndRolesByUserId(user.getId());

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
        relation.setRole(Role.CREATOR);

        userFilespaceRelationRepository.saveAndFlush(relation);
        filespaceRepository.flush();

        return filespace;
    }

    public Filespace getFilespaceById(User user, Long requestedFilespaceId) throws Exception{
        Optional<Filespace> optional = filespaceRepository.findById(requestedFilespaceId);

        if (optional.isEmpty())
            throw new EntityNotFoundException("No such entity found");



        if (!userFilespaceRelationRepository.existsById(new UserFilespaceKey(user.getId(), requestedFilespaceId)))
            throw new IllegalAccessException("No access to this filespace");

        return optional.get();
    }

    public void updateFilespaceTitle(User user, Long id, String title) throws Exception{
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
    public void deleteFilespace(User user, Long id) throws Exception{
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

        return relation;
    }

    public List<FilespaceFileInfo> getFilesFromFilespace(User user, Long id) throws Exception{
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        if (!userFilespaceRelationRepository.existsById(
                new UserFilespaceKey(user.getId(),id)))
            throw new IllegalAccessException("No access to filespace");

        return fileFilespaceRelationRepository.getFilesFromFilespace(id);
    }

    public List<FilespaceUserInfo> getUsersOfFilespace(User user, Long id) throws Exception {
        Optional<Filespace> optionalFilespace = filespaceRepository.findById(id);

        if (optionalFilespace.isEmpty())
            throw new EntityNotFoundException("No such filespace");

        if (!userFilespaceRelationRepository.existsById(
                new UserFilespaceKey(user.getId(),id)))
            throw new IllegalAccessException("No access to filespace");

        return userFilespaceRelationRepository.getFilespaceUsersById(id);
    }

    public UserFilespaceRelation addUserToFilespace(User requester, Long filespaceId, Long userId, Role role) throws Exception{

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

        return relation;
    }

    @Transactional
    public void deleteUserFromFilespace(User requester, Long filespaceId, Long userId, Boolean deleteFiles) throws Exception{
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

    public void deleteFileFromFilespace(User requester, Long filespaceId, Long fileId) throws Exception{
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

    public void updateUserRole(User requester, Long filespaceId, Long userId, Role role) throws Exception {
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
}
