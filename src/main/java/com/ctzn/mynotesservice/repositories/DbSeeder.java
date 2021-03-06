package com.ctzn.mynotesservice.repositories;

import com.ctzn.mynotesservice.model.note.NoteEntity;
import com.ctzn.mynotesservice.model.notebook.NotebookEntity;
import com.ctzn.mynotesservice.model.user.UserEntity;
import com.ctzn.mynotesservice.model.user.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
@Slf4j
public class DbSeeder {

    private static final String defaultUserPublicId = "36c773c8-00b0-4f94-ada1-da5b74b49de4";

    private NotebookRepository notebookRepository;
    private NoteRepository noteRepository;
    private UserRepository userRepository;

    public DbSeeder(NotebookRepository notebookRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.notebookRepository = notebookRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    private void generateRandomDb(int nbNum, int notesPerNbMin, int notesPerNbMax) {
        // create default users no db init
        if (userRepository.count() == 0) {
            // USER
            UserEntity user = new UserEntity("User", "", "user@example.com");
            user.setPassword("12345");
            user.setRolesFromList(Collections.singletonList(UserRole.USER));
            // set the fixed public user id instead of randomly generated
            // for easier debugging
            user.setUserId(defaultUserPublicId);
            userRepository.save(user);
            // ADMIN
            UserEntity admin = new UserEntity("Admin", "", "admin@example.com");
            admin.setPassword("12345");
            // set the fixed public user id instead of randomly generated
            // for easier debugging
            admin.setUserId("82e91d4a-5ba5-4854-b3d4-6977d58283b9");
            admin.setRolesFromList(Collections.singletonList(UserRole.ADMIN));
            userRepository.save(admin);
            // MULTIPLE ROLES USER
            UserEntity admin2 = new UserEntity("Multiple roles user", "", "admin2@example.com");
            admin2.setPassword("12345");
            // set the fixed public user id instead of randomly generated
            // for easier debugging
            admin2.setUserId("82e91d4a-5ba5-4854-b3d4-6977d58283ba");
            admin2.setRolesFromList(Arrays.asList(UserRole.ADMIN, UserRole.USER));
            userRepository.save(admin2);
            log.info("Default users created");
        }
        // generate random db for default user
        Optional<UserEntity> optionalUser = userRepository.findByUserId(defaultUserPublicId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            notebookRepository.deleteAll();
            Random rnd = new Random();
            long now = new Date().getTime();
            for (int i = 0; i < nbNum; i++) {
                NotebookEntity nb = new NotebookEntity("Notebook " + (i + 1), user);
                notebookRepository.save(nb);
                int notesNum = rnd.nextInt(notesPerNbMax - notesPerNbMin + 1) + notesPerNbMin;
                for (int n = 0; n < notesNum; n++) {
                    NoteEntity note = new NoteEntity(
                            "Note " + (i + 1) + "." + (n + 1),
                            NonsenseGenerator.getInstance().makeText(1),
                            nb);
                    // set random creation date within last year
                    int period = (int) Duration.ofDays(365L).getSeconds();
                    note.setLastModifiedOn(new Date(now - 1000L * rnd.nextInt(period)));
                    noteRepository.save(note);
                }
            }
        }
    }

    // method to call on the app startup
    public void seed(boolean force) {
        // put random data to db if db is empty
        if (notebookRepository.count() == 0 || force) {
            log.info("Fill database: begin");
            generateRandomDb(12, 3, 40);
            log.info("Fill database: end");
        }
    }

    @Async
    // method to call from a NIO thread
    // async execution enables to immediately receive an acknowledgment message on the client side
    public void seedAsync() {
        seed(true);
    }

}
