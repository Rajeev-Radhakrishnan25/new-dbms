package com.lightweightDbms.repository;

import com.lightweightDbms.exception.UserAlreadyExistsException;
import com.lightweightDbms.model.User;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * In-memory implementation of the user repository using a map.
 *
 */
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users;

    /**
     * Constructs a new InMemoryUserRepository.
     */
    public InMemoryUserRepository() {
        this.users = new ConcurrentHashMap<>();
    }

    /**
     * Adds a new user to the repository.
     *
     * @param user the user to add
     * @throws UserAlreadyExistsException if user already exists
     */
    @Override
    public void addUser(User user) throws UserAlreadyExistsException {
        if (users.containsKey(user.getUserId())) {
            throw new UserAlreadyExistsException("User with ID '" + user.getUserId() + "' already exists");
        }
        users.put(user.getUserId(), user);
    }

    /**
     * Retrieves a user by their user ID.
     *
     * @param userId the user ID to search for
     * @return the User object if found, null otherwise
     */
    @Override
    public User getUserById(String userId) {
        return users.get(userId);
    }

    /**
     * Checks if a user exists in the repository.
     *
     * @param userId the user ID to check
     * @return true if user exists, false otherwise
     */
    @Override
    public boolean userExists(String userId) {
        return users.containsKey(userId);
    }

    /**
     * Gets the total number of users in the repository.
     *
     * @return the count of users
     */
    @Override
    public int getUserCount() {
        return users.size();
    }

    /** {@inheritDoc} */
    @Override
    public void updateUser(User user) {
        if (user == null) return;
        users.put(user.getUserId(), user);
    }
}