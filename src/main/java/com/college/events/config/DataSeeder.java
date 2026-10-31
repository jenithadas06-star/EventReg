package com.college.events.config;

import com.college.events.model.Event;
import com.college.events.model.EventEligibility;
import com.college.events.model.EventRegistration;
import com.college.events.model.EventStatus;
import com.college.events.model.Organizer;
import com.college.events.model.QuestionType;
import com.college.events.model.RegistrationAnswer;
import com.college.events.model.RegistrationQuestion;
import com.college.events.model.RegistrationStatus;
import com.college.events.model.Student;
import com.college.events.repository.EventRepository;
import com.college.events.repository.RegistrationRepository;
import com.college.events.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Loads a realistic demo dataset so every feature can be tested without a database.
 *
 * <p>Dates are relative to "today", so the seeded events always cover the
 * Upcoming / Ongoing / Completed / Draft / Full states.</p>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final EventRepository events;
    private final RegistrationRepository registrations;

    public DataSeeder(UserRepository users, EventRepository events, RegistrationRepository registrations) {
        this.users = users;
        this.events = events;
        this.registrations = registrations;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return; // demo data already loaded
        }
        seed();
    }

    private void seed() {
        // ------------------------------------------------------------------
        // Accounts (demo credentials - see the login page for these)
        // ------------------------------------------------------------------
        Student aarav = (Student) users.save(new Student(null, "Aarav Sharma",
                "aarav@college.edu", "student123", "CSE", "Second Year", "A", "21CSE001", "9876543210"));
        Student diya = (Student) users.save(new Student(null, "Diya Menon",
                "diya@college.edu", "student123", "ECE", "Second Year", "B", "21ECE014", "9876500011"));
        Student rohan = (Student) users.save(new Student(null, "Rohan Verma",
                "rohan@college.edu", "student123", "IT", "First Year", "A", "24IT102", "9876500022"));
        Student sneha = (Student) users.save(new Student(null, "Sneha Iyer",
                "sneha@college.edu", "student123", "AIDS", "Third Year", "A", "22AIDS030", "9876500033"));

        Organizer priya = (Organizer) users.save(new Organizer(null, "Dr. Priya Nair",
                "priya@college.edu", "organizer123", "Department of CSE", "Faculty Coordinator", "9845012345"));
        Organizer karthik = (Organizer) users.save(new Organizer(null, "Karthik Raj",
                "karthik@college.edu", "organizer123", "Coding Club", "Club President", "9845067890"));

        // ------------------------------------------------------------------
        // Events
        // ------------------------------------------------------------------
        Event hackfest = events.save(schedule(event("HACKFEST 2026 - 24 Hour Coding Marathon",
                "Hackathon",
                "The flagship overnight hackathon of the coding club. Teams build a working prototype "
                        + "against a surprise problem statement in 24 hours, mentored by industry engineers. "
                        + "Food, energy drinks and swag are provided for the entire duration.",
                "Innovation Centre", "Main Block", "Lab 3", 120,
                priya, "Dr. Priya Nair", "Department of CSE", "9845012345"),
                21, LocalTime.of(9, 0), 1440, -6, 15));
        restrictTo(hackfest, List.of("CSE"), List.of("Second Year", "Third Year"), List.of());

        Event technovate = events.save(schedule(event("Technovate - Project Expo",
                "Technical",
                "An open exhibition where students present their semester projects, working models and "
                        + "research prototypes to a panel of faculty and industry judges. Best projects "
                        + "receive seed funding for further development.",
                "Open Auditorium", "Main Block", null, 150,
                priya, "Dr. Priya Nair", "Department of CSE", "9845012345"),
                14, LocalTime.of(10, 0), 300, -10, 10));
        openToAll(technovate);

        Event rhythmNight = events.save(schedule(event("Rhythm Night - Annual Cultural Fest",
                "Cultural",
                "The biggest night of the year: live bands, classical dance, stand-up sets and the "
                        + "much loved inter-department group performance competition. Bring your friends.",
                "Open Air Theatre", null, null, 500,
                karthik, "Karthik Raj", "Coding Club", "9845067890"),
                30, LocalTime.of(17, 30), 240, -2, 25));
        openToAll(rhythmNight);

        Event robotics = events.save(schedule(event("Robotics Workshop - Build Your First Bot",
                "Workshop",
                "A hands-on weekend workshop where you assemble, program and test a line-following "
                        + "robot. Kits, controllers and sensors are provided. No prior experience needed.",
                "Robotics Lab", "Tech Block B", "204", 40,
                priya, "Dr. Priya Nair", "Department of CSE", "9845012345"),
                9, LocalTime.of(9, 30), 360, -12, 6));
        restrictTo(robotics, List.of("ECE", "EEE", "MECH"), List.of("First Year", "Second Year"), List.of());

        Event aimlBootcamp = events.save(schedule(event("AIML Bootcamp - Foundations of Machine Learning",
                "Workshop",
                "A three hour intensive bootcamp covering the machine learning workflow end to end: "
                        + "data preparation, model training, evaluation and deployment basics with Python.",
                "Seminar Hall 2", "AI Block", null, 80,
                priya, "Dr. Priya Nair", "Department of CSE", "9845012345"),
                35, LocalTime.of(14, 0), 180, 5, 30));
        restrictTo(aimlBootcamp, List.of("AIDS", "AIML", "CSE"), List.of("Third Year", "Fourth Year"), List.of());

        Event sportsMeet = events.save(schedule(event("Sports Meet 2026 - Inter-Department Championship",
                "Sports",
                "Track and field, cricket, football, volleyball and indoor games. Register as an "
                        + "individual or as a four member departmental team and fight for the overall trophy.",
                "Main Ground", null, null, 300,
                karthik, "Karthik Raj", "Coding Club", "9845067890"),
                18, LocalTime.of(8, 0), 480, -8, 12));
        openToAll(sportsMeet);
        sportsMeet.setTeamEvent(true);
        sportsMeet.setTeamSize(4);
        events.save(sportsMeet);

        Event designSprint = events.save(schedule(event("UI/UX Design Sprint",
                "Workshop",
                "A rapid design sprint: research, wireframe, prototype and test a campus utility app "
                        + "in a single sitting. Only two seats are available so mentoring stays personal.",
                "Design Studio", "Tech Block A", "110", 2,
                karthik, "Karthik Raj", "Coding Club", "9845067890"),
                7, LocalTime.of(11, 0), 240, -14, 4));
        openToAll(designSprint);

        Event ctf = events.save(schedule(event("Capture The Flag - Cyber Security Championship",
                "Technical",
                "A jeopardy style security competition covering web exploitation, forensics, "
                        + "cryptography and reverse engineering. Teams of two, leaderboards and prizes.",
                "Cyber Lab", "Tech Block C", "301", 60,
                priya, "Dr. Priya Nair", "Department of CSE", "9845012345"),
                -24, LocalTime.of(9, 0), 480, -40, -30));
        restrictTo(ctf, List.of("CYBER", "CSE", "IT"), List.of(), List.of());

        Event alumniTalk = events.save(schedule(event("Alumni Talk - Life After Campus",
                "Seminar",
                "Recent graduates working at product companies and research labs share honest advice "
                        + "on placements, higher studies, internships and building a portfolio.",
                "Open Auditorium", "Main Block", null, 200,
                priya, "Dr. Priya Nair", "Department of CSE", "9845012345"),
                -12, LocalTime.of(15, 0), 120, -25, -16));
        openToAll(alumniTalk);

        Event photoExhibition = events.save(schedule(event("Photography Club Exhibition",
                "Club Activity",
                "An exhibition of 120 prints selected from the annual campus photography contest, "
                        + "along with a walkthrough by the jury and a short mobile photography clinic.",
                "Art Gallery", "Main Block", null, 100,
                karthik, "Karthik Raj", "Coding Club", "9845067890"),
                -45, LocalTime.of(10, 0), 480, -60, -50));
        openToAll(photoExhibition);

        Event pitchNight = events.save(schedule(event("Startup Pitch Night",
                "Seminar",
                "Student founders pitch to a panel of angel investors and incubation mentors. "
                        + "Still being finalised - not published yet.",
                "Incubation Centre", "Admin Block", null, 60,
                karthik, "Karthik Raj", "Coding Club", "9845067890"),
                40, LocalTime.of(18, 0), 180, -5, 35));
        openToAll(pitchNight);
        pitchNight.setStatus(EventStatus.DRAFT);
        events.save(pitchNight);

        // ------------------------------------------------------------------
        // Custom registration questions
        // ------------------------------------------------------------------
        addQuestion(hackfest, "Why do you want to participate in HACKFEST?",
                QuestionType.LONG_ANSWER, true, List.of());
        addQuestion(hackfest, "Which programming languages do you know?",
                QuestionType.SHORT_ANSWER, false, List.of());
        addQuestion(hackfest, "Do you require accommodation on campus?",
                QuestionType.MULTIPLE_CHOICE, true, List.of("Yes", "No"));
        addQuestion(robotics, "Do you have prior experience with Arduino or microcontrollers?",
                QuestionType.MULTIPLE_CHOICE, true, List.of("Yes", "No", "A little"));

        // ------------------------------------------------------------------
        // Sample registrations
        // ------------------------------------------------------------------
        register(aarav, hackfest,
                "I want to build something real in a single night and work with teammates from other years.",
                "Java, Python, JavaScript", "No");
        register(aarav, technovate);
        register(aarav, rhythmNight);
        register(aarav, sportsMeet);
        register(aarav, ctf);
        register(aarav, alumniTalk);

        register(diya, robotics, "A little");
        register(diya, rhythmNight);
        register(diya, technovate);
        register(diya, ctf);

        register(rohan, designSprint);
        register(rohan, sportsMeet);
        register(rohan, photoExhibition);

        register(sneha, designSprint);
        register(sneha, sportsMeet);
    }

    // ------------------------------------------------------------------
    // Builders
    // ------------------------------------------------------------------

    private Event event(String name, String category, String description, String venue, String building,
                        String roomNumber, int capacity, Organizer organizer, String organizerName,
                        String clubOrDepartment, String contact) {
        Event event = new Event();
        event.setName(name);
        event.setCategory(category);
        event.setDescription(description);
        event.setVenue(venue);
        event.setBuilding(building);
        event.setRoomNumber(roomNumber);
        event.setMaxParticipants(capacity);
        event.setOrganizerId(organizer.getId());
        event.setOrganizerName(organizerName);
        event.setClubOrDepartment(clubOrDepartment);
        event.setOrganizerContact(contact);
        event.setStatus(EventStatus.PUBLISHED);
        event.setCreatedAt(LocalDateTime.now().minusDays(25));
        return event;
    }

    private Event schedule(Event event, int startOffsetDays, LocalTime startTime, int durationMinutes,
                           int openOffsetDays, int deadlineOffsetDays) {
        LocalDate today = LocalDate.now();
        event.setEventDate(today.plusDays(startOffsetDays));
        event.setStartTime(startTime);
        event.setDurationMinutes(durationMinutes);
        event.setRegistrationOpenDate(today.plusDays(openOffsetDays));
        event.setRegistrationDeadline(today.plusDays(deadlineOffsetDays));
        return event;
    }

    private void openToAll(Event event) {
        EventEligibility eligibility = new EventEligibility();
        eligibility.setVisibleToAll(true);
        event.setEligibility(eligibility);
    }

    private void restrictTo(Event event, List<String> departments, List<String> years, List<String> sections) {
        EventEligibility eligibility = new EventEligibility();
        eligibility.setVisibleToAll(false);
        eligibility.setDepartments(new LinkedHashSet<>(departments));
        eligibility.setYears(new LinkedHashSet<>(years));
        eligibility.setSections(new LinkedHashSet<>(sections));
        event.setEligibility(eligibility);
    }

    private void addQuestion(Event event, String text, QuestionType type, boolean required, List<String> options) {
        RegistrationQuestion question = new RegistrationQuestion(null, event.getId(), text, type,
                required, options, event.getQuestions().size());
        event.getQuestions().add(question);
        events.save(event);
    }

    private void register(Student student, Event event, String... answers) {
        EventRegistration registration = new EventRegistration();
        registration.setEventId(event.getId());
        registration.setStudentId(student.getId());
        registration.setFullName(student.getFullName());
        registration.setEmail(student.getEmail());
        registration.setDepartment(student.getDepartment());
        registration.setSection(student.getSection());
        registration.setYear(student.getYear());
        registration.setRegisterNumber(student.getRegisterNumber());
        registration.setContactNumber(student.getContactNumber());
        registration.setRegisteredAt(LocalDateTime.now().minusDays(3));
        registration.setStatus(RegistrationStatus.CONFIRMED);

        EventRegistration saved = registrations.save(registration);

        List<RegistrationQuestion> questions = event.getQuestions();
        for (int i = 0; i < questions.size() && i < answers.length; i++) {
            RegistrationQuestion question = questions.get(i);
            saved.getAnswers().add(new RegistrationAnswer(null, saved.getId(), question.getId(),
                    question.getQuestionText(), answers[i]));
        }
        registrations.save(saved);
    }
}
