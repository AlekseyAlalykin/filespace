package org.filespace.services;

import org.filespace.config.SecurityConfig;
import org.filespace.model.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.*;
import org.filespace.repositories.*;
import org.filespace.security.SecurityUtil;
import org.filespace.security.SessionManager;
import org.filespace.security.UserDetailsImpl;
import org.filespace.services.threads.EmailThread;
import org.filespace.services.threads.FileDeletingThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
            if (!deleteIfInactive(sameEmailUser))
                throw new IllegalArgumentException("Such email has already been taken");
        }

        if (userRepository.existsByUsername(username)) {
            User sameUsernameUser = userRepository.findUserByUsername(username);
            if (!deleteIfInactive(sameUsernameUser))
                throw new IllegalArgumentException("Such username has already been taken");
        }

        userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken(UUID.randomUUID().toString(), user,
                LocalDate.now(), LocalTime.now(), TokenType.REGISTRATION);

        verificationTokenRepository.save(verificationToken);

        userRepository.flush();
        verificationTokenRepository.flush();


        EmailThread emailThread = new EmailThread(user.getEmail(),EmailHandler.onRegistrationSubject,
                String.format(EmailHandler.onRegistrationMessage, EmailHandler.domainName, verificationToken.getToken()));

        emailThread.start();

        return user;
    }

    @Transactional
    public void confirmRegistration(String tokenValue){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue);

        if (verificationToken == null)
            throw new EntityNotFoundException("No such token");

        if (verificationToken.isExpired())
            throw new IllegalArgumentException("Token expired");

        if (verificationToken.isConfirmed())
            throw new IllegalArgumentException("Token already confirmed");

        if (!verificationToken.getType().equals(TokenType.REGISTRATION))
            throw new IllegalArgumentException("Wrong token type");

        User user = verificationToken.getUser();

        user.setEnabled(true);
        verificationToken.setConfirmed(true);

        userRepository.saveAndFlush(user);
        verificationTokenRepository.saveAndFlush(verificationToken);
    }

    public User getUserById(Long id){
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty())
            throw new EntityNotFoundException("No such user found");

        return optional.get();
    }

    public User getCurrentUser(){
        return securityUtil.getCurrentUser();
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

        VerificationToken verificationToken = new VerificationToken(UUID.randomUUID().toString(), target,
                LocalDate.now(), LocalTime.now(), TokenType.DELETION);

        verificationTokenRepository.saveAndFlush(verificationToken);

        EmailThread emailThread = new EmailThread(target.getEmail(), EmailHandler.onDeletionSubject,
                String.format(EmailHandler.onDeletionMessage, EmailHandler.domainName, verificationToken.getToken()));

        emailThread.start();
    }

    @Transactional
    public void confirmDeletion(String tokenValue){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue);

        if (verificationToken == null)
            throw new EntityNotFoundException("No such token");

        if (verificationToken.isExpired())
            throw new IllegalArgumentException("Token expired");

        if (verificationToken.isConfirmed())
            throw new IllegalArgumentException("Token already confirmed");

        if (!verificationToken.getType().equals(TokenType.DELETION))
            throw new IllegalArgumentException("Wrong token type");

        User target = verificationToken.getUser();

        eraseUser(target);
    }

    @Transactional
    public void updateUser(User requester, Long userId, String username, String password, String email) throws Exception{
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
                if (!deleteIfInactive(sameUsernameUser))
                    throw new IllegalArgumentException("Such username has already been taken");
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
                if (!deleteIfInactive(sameEmailUser))
                    throw new IllegalArgumentException("Such email has already been taken");
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

        userRepository.save(newUserState);

        verificationTokenRepository.flush();
        userRepository.flush();

        if ( !oldUserState.getEmail().equals(email) )
            new EmailThread(email, EmailHandler.onEmailChangeSubject,
                    String.format(EmailHandler.onEmailChangeMessage, EmailHandler.domainName, verificationToken.getToken())
            ).start();

        //Изменение данных пользователя в spring security
        sessionManager.updateUsernameForUserSessions(oldUserState.getUsername(), newUserState.getUsername());
        /*
        Collection<SimpleGrantedAuthority> authorities =
                (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext()
                        .getAuthentication().getAuthorities();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(newUserState.getUsername(), newUserState.getPassword(), authorities);

         */


        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userDetails.setUsername(newUserState.getUsername());


        /*
        Authentication request = new UsernamePasswordAuthenticationToken(newUserState.getUsername(), newUserState.getPassword());
        Authentication result = authenticationManager.authenticate(request);
        SecurityContextHolder.getContext().setAuthentication(result);
        */

        if (password != null)
            sessionManager.closeAllUserSessions(oldUserState);
    }

    //Удаляет пользователя если регистрация аккаунта не была подтверждена
    public boolean deleteIfInactive(User user){
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
                fileFilespaceRelationRepository.deleteAllByFilespace(filespace);

                fileFilespaceRelationRepository.flush();

                filespaceRepository.delete(filespace);
            }
        }

        List<String> md5Hashes = new LinkedList<>();
        //Удаление связей файлов пользователя и filespace
        for (File file: fileRepository.getAllBySender(user)){
            //Если файлов с данным хеш значение больше нет
            if (fileRepository.countAllByMd5Hash(file.getMd5Hash()) == 1)
                md5Hashes.add(file.getMd5Hash());
            fileFilespaceRelationRepository.deleteAllByFile(file);
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

    public void confirmEmailChange(String tokenValue){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue);

        if (verificationToken == null)
            throw new EntityNotFoundException("No such token");

        if (verificationToken.isExpired())
            throw new IllegalArgumentException("Token expired");

        if (verificationToken.isConfirmed())
            throw new IllegalArgumentException("Token already confirmed");

        if (!verificationToken.getType().equals(TokenType.EMAIL_CHANGE))
            throw new IllegalArgumentException("Wrong token type");

        User user = verificationToken.getUser();

        user.setEmail(verificationToken.getValue());
        verificationToken.setConfirmed(true);

        userRepository.saveAndFlush(user);
        verificationTokenRepository.saveAndFlush(verificationToken);

        sessionManager.closeAllUserSessions(user);
    }

}
