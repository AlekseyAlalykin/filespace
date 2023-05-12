package org.filespace.services;

import org.filespace.model.entities.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.simplerelations.*;
import org.filespace.model.intermediate.UserInfo;
import org.filespace.repositories.*;
import org.filespace.security.SecurityUtil;
import org.filespace.security.SessionManager;
import org.filespace.services.threads.EmailThread;
import org.filespace.services.threads.FileDeletingThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private EmailServiceImpl emailService;

    @Transactional
    public User registerUser(String username, String password, String email){
        if (password.length() < 8)
            throw new IllegalArgumentException("Weak password");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        User user = new User(username, encoder.encode(password), email, LocalDate.now());

        if (!validationService.validate(user))
            throw new IllegalArgumentException(validationService.getConstrainsViolations(user));

        //Проверка если кто то зарегистрировал аккаунт но не подтвердил его
        if (userRepository.existsByEmail(email)) {
            User sameEmailUser = userRepository.findUserByEmail(email);
            if (!deleteIfNotConfirmed(sameEmailUser))
                throw new IllegalStateException("Such email has already been taken");
        }

        if (userRepository.existsByUsername(username)) {
            User sameUsernameUser = userRepository.findUserByUsername(username);
            if (!deleteIfNotConfirmed(sameUsernameUser))
                throw new IllegalStateException("Such username has already been taken");
        }

        userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken(UUID.randomUUID().toString(), user,
                LocalDate.now(), LocalTime.now(), TokenType.REGISTRATION);

        verificationTokenRepository.save(verificationToken);

        userRepository.flush();
        verificationTokenRepository.flush();


        EmailThread emailThread = new EmailThread(emailService, user.getEmail(), emailService.getOnRegistrationSubject(),
                String.format(emailService.getOnRegistrationMessage(), emailService.getDomainName(), verificationToken.getToken()));

        emailThread.start();

        return user;
    }

    public Object getUserById(Long id){
        Optional<?> optional;

        if (id.equals(securityUtil.getCurrentUserId()))
            return securityUtil.getCurrentUser();
        else {
            optional = userRepository.findUserById(id);

            if (optional.isEmpty())
                throw new EntityNotFoundException("No such user found");

            return optional.get();
        }
    }

    public List<UserInfo> getUsersList(String username, Integer limit){
        return userRepository.findUsersByUsernameWithLimit(username, limit);
    }

    @Transactional
    public void deleteUser(User requester, Long userId) throws IllegalAccessException{
        //Проверка пользователя
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user found");

        User target = optionalUser.get();

        if (!requester.equals(target))
            throw new IllegalAccessException("No authority");

        VerificationToken verificationToken = new VerificationToken(UUID.randomUUID().toString(), target,
                LocalDate.now(), LocalTime.now(), TokenType.DELETION);

        verificationTokenRepository.saveAndFlush(verificationToken);

        EmailThread emailThread = new EmailThread(emailService, target.getEmail(), emailService.getOnDeletionSubject(),
                String.format(emailService.getOnDeletionMessage(), emailService.getDomainName(), verificationToken.getToken()));

        emailThread.start();
    }

    @Transactional
    public void updateUser(User requester, Long userId, String username, String password, String email) throws IllegalAccessException{
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty())
            throw new EntityNotFoundException("No such user found");

        User oldUserState = optionalUser.get();

        if (!requester.equals(oldUserState))
            throw new IllegalAccessException("No authority");

        User newUserState = new User();

        if (username == null || oldUserState.getUsername().equals(username))
            newUserState.setUsername(oldUserState.getUsername());
        else {
            if (userRepository.existsByUsername(username)) {
                User sameUsernameUser = userRepository.findUserByUsername(username);
                if (!deleteIfNotConfirmed(sameUsernameUser))
                    throw new IllegalStateException("Such username has already been taken");
            }

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

        VerificationToken verificationToken = null;

        if (email == null || oldUserState.getEmail().equals(email))
            newUserState.setEmail(oldUserState.getEmail());
        else {
            if (userRepository.existsByEmail(email)) {
                User sameEmailUser = userRepository.findUserByEmail(email);
                if (!deleteIfNotConfirmed(sameEmailUser))
                    throw new IllegalStateException("Such email has already been taken");
            }

            verificationToken = new VerificationToken(UUID.randomUUID().toString(),
                    oldUserState, LocalDate.now(), LocalTime.now(), TokenType.EMAIL_CHANGE, email);
            verificationTokenRepository.save(verificationToken);

            newUserState.setEmail(oldUserState.getEmail());
        }

        newUserState.setId(oldUserState.getId());
        newUserState.setRegistrationDate(oldUserState.getRegistrationDate());
        newUserState.setEnabled(oldUserState.isEnabled());

        if (!validationService.validate(newUserState))
            throw new IllegalArgumentException(validationService.getConstrainsViolations(newUserState));

        //Инвалидация сессий
        sessionManager.closeAllUserSessions(oldUserState);

        userRepository.save(newUserState);

        verificationTokenRepository.flush();
        userRepository.flush();

        if ( !oldUserState.getEmail().equals(email) )
            new EmailThread(emailService, email, emailService.getOnEmailChangeSubject(),
                    String.format(emailService.getOnEmailChangeMessage(), emailService.getDomainName(), verificationToken.getToken())
            ).start();
    }

    //Удаляет пользователя если регистрация аккаунта не была подтверждена
    public boolean deleteIfNotConfirmed(User user){
        if (user.isEnabled())
            return false;

        List<VerificationToken> tokens = verificationTokenRepository.findAllByUserAndType(user, TokenType.REGISTRATION);
        for (VerificationToken token: tokens){
            if (!token.isExpired() && !token.isConfirmed()){
                return false;
            }
        }

        eraseUser(user);
        return true;
    }

    //Полностью удаляет все связанное с пользователем включая файлы
    @Transactional
    protected void eraseUser(User user){
        //Удаление связей пользователя и filespace
        for (UserFilespaceRelation relation: userFilespaceRelationRepository.getByUser(user)){
            Filespace filespace = relation.getFilespace();

            userFilespaceRelationRepository.delete(relation);

            if (userFilespaceRelationRepository.countAllByFilespace(filespace) == 0){
                fileFilespaceRelationRepository.deleteByFilespace(filespace);

                fileFilespaceRelationRepository.flush();

                filespaceRepository.delete(filespace);
            }
        }

        List<String> md5Hashes = new LinkedList<>();
        //Удаление связей файлов пользователя и filespace
        for (File file: fileRepository.getAllBySenderOrderByPostDateDescPostTimeDesc(user)){
            //Если файлов с данным хеш значение больше нет
            if (fileRepository.countAllByMd5Hash(file.getMd5Hash()) == 1)
                md5Hashes.add(file.getMd5Hash());
            fileFilespaceRelationRepository.deleteByFile(file);
        }
        //Подготовил поток для удаления файлов
        FileDeletingThread thread = new FileDeletingThread(md5Hashes);

        //Удалил записи о файлах
        fileRepository.deleteAllBySender(user);

        //Закрыл все сессии
        sessionManager.closeAllUserSessions(user);

        //Удалил все токены
        verificationTokenRepository.deleteAllByUser(user);

        //Удалил самого пользователя
        userRepository.delete(user);

        userFilespaceRelationRepository.flush();
        fileFilespaceRelationRepository.flush();
        fileRepository.flush();
        verificationTokenRepository.flush();
        userRepository.flush();

        //Очистка в другом потоке
        thread.start();
    }

    @Transactional
    public String confirmToken(String tokenValue){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue);

        if (verificationToken == null)
            throw new EntityNotFoundException("No such token");

        if (verificationToken.isExpired()) {
            verificationToken.setConfirmed(true);
            throw new IllegalArgumentException("Token expired");
        }

        if (verificationToken.isConfirmed())
            throw new IllegalArgumentException("Token already confirmed");

        String message = null;
        User user = verificationToken.getUser();

        switch (verificationToken.getType()){
            case REGISTRATION:
                user.setEnabled(true);
                verificationToken.setConfirmed(true);
                message = "Registration confirmed";
                break;
            case DELETION:
                eraseUser(user);
                message = "Deletion confirmed";
                break;
            case EMAIL_CHANGE:
                if (userRepository.existsByEmail(verificationToken.getValue())) {
                    User sameEmailUser = userRepository.findUserByEmail(verificationToken.getValue());
                    if (!deleteIfNotConfirmed(sameEmailUser)){
                        verificationToken.setConfirmed(true);
                        throw new IllegalArgumentException("Such email has already been taken");
                    }
                }

                if (!validationService.validate(user)){
                    verificationToken.setConfirmed(true);
                    throw new IllegalArgumentException(validationService.getConstrainsViolations(user));
                }

                user.setEmail(verificationToken.getValue());
                verificationToken.setConfirmed(true);

                message = "Email change confirmed";
        }

        userRepository.saveAndFlush(user);
        verificationTokenRepository.saveAndFlush(verificationToken);

        //Инвалидирую сессии
        sessionManager.closeAllUserSessions(user);

        return message;
    }

}
