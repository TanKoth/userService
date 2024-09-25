package org.userservice.service;

import ch.qos.logback.classic.pattern.DateConverter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.RequestEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.userservice.exception.UserAlreadyExistException;
import org.userservice.exception.UserNotFoundException;
import org.userservice.exception.WrongPasswordException;
import org.userservice.model.Session;
import org.userservice.model.User;
import org.userservice.repository.SessionRepository;
import org.userservice.repository.UserRepository;

import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService {


    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SecretKey key;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sessionRepository = sessionRepository;
        this.key = Keys.hmacShaKeyFor("SecureKeySecretKeyPrivateKeyVeryPrivatekeySuperSecretKey".getBytes(StandardCharsets.UTF_8));
    }

    public boolean signUp(String email, String password) throws UserAlreadyExistException {

        Optional<User> userPresent = userRepository.findByEmail(email);
        if(userPresent.isPresent()){
            throw new UserAlreadyExistException("User already exist with email: "+email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        userRepository.save(user);

        return true;
    }

    public String login(String email, String password) throws UserNotFoundException, WrongPasswordException, UnknownHostException {
        Optional<User> userLogin = userRepository.findByEmail(email);
        if(userLogin.isEmpty()){
            throw new UserNotFoundException("User not found by email: " +email+ "Please sign up");
        }
        boolean matches = bCryptPasswordEncoder.matches(password, userLogin.get().getPassword());

        if(matches){

            String token = createJwtTokens(userLogin.get().getId(),new ArrayList<>(),userLogin.get().getEmail());
            Session session = new Session();

            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            Date daysPlus30 = calendar.getTime();

            session.setUser(userLogin.get());
            session.setToken(token);

            InetAddress IP=InetAddress.getLocalHost();
            //System.out.println("IP of my system is := "+IP.getHostAddress());

            session.setIpAddress(IP.getHostAddress());
            session.setExpiredAt(daysPlus30);

            String userName = System.getProperty("user.name");
            session.setDeviceInfo(userName);

            sessionRepository.save(session);

            return token;
        }else{
            throw new WrongPasswordException("Wrong password for email: " +email);
        }
    }

    public boolean validate(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Date expiryAt = claims.getPayload().getExpiration();
            Long userId = claims.getPayload().get("user_id", Long.class);

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public String createJwtTokens(Long userId, List<String> roles, String email){
        HashMap<String,Object> dataInJwt = new HashMap<>();
        dataInJwt.put("user_id", userId);
        dataInJwt.put("role",roles);
        dataInJwt.put("email", email);

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date daysPlus30 = calendar.getTime();

        String token = Jwts.builder()
                        .claims(dataInJwt)
                .expiration(daysPlus30)
                .issuedAt(currentDate)
                .signWith(key)
                .compact();

        return token;

    }
}
