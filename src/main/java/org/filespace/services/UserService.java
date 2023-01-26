package org.filespace.services;

import org.filespace.model.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.File;
import org.filespace.model.entities.Filespace;
import org.filespace.model.entities.User;
import org.filespace.repositories.*;
import org.filespace.security.SessionManager;
import org.filespace.threads.FileDeletingThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

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
    private SessionManager sessionManager;

    public User registerUser(String username, String password, String email){
        if (password.length() < 8)
            throw new IllegalArgumentException("Weak password");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        User user = new User(username, encoder.encode(password),email, LocalDate.now());

        if (!validationService.validate(user))
            throw new IllegalArgumentException(validationService.getConstrainsViolations(user));

        if(userRepository.existsByUsernameOrEmail(user.getUsername(),user.getEmail()))
            throw new IllegalArgumentException("Such username or email has already been taken");

        userRepository.saveAndFlush(user);

        return user;
    }

    public User getUserById(Long id){
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty())
            throw new EntityNotFoundException("No such user found");

        return optional.get();
    }

    @Transactional
    public void deleteUser(User requester, Long userId) throws Exception{
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

    public void updateUser(User requester, Long userId, String username, String password, String email) throws Exception{
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
