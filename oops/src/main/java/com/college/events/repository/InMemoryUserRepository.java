package com.college.events.repository;

import com.college.events.model.Role;
import com.college.events.model.User;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory {@link UserRepository} used until MySQL is wired in.
 *
 * <p>This is the <b>only</b> class that has to change when the database arrives:
 * replace it with a {@code JpaRepository<User, Long>} and the rest of the
 * application keeps working unchanged.</p>
 */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(sequence.incrementAndGet());
        } else {
            sequence.updateAndGet(current -> Math.max(current, user.getId()));
        }
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return store.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    @Override
    public List<User> findByRole(Role role) {
        return store.values().stream()
                .filter(u -> u.getRole() == role)
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            store.remove(id);
        }
    }

    @Override
    public long count() {
        return store.size();
    }
}
