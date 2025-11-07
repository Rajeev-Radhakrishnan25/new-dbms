package com.lightweightDbms.repository;

import com.lightweightDbms.exception.UserAlreadyExistsException;
import com.lightweightDbms.model.User;

/**
 * Interface for user data access operations.
 *
 */
public interface UserRepository {
    /**
     * Adds a new user to the repository.
     *
     * @param user the user to add
     * @throws UserAlreadyExistsException if user already exists
     */
    void addUser(User user) throws UserAlreadyExistsException;

    /**
     * Retrieves a user by their user ID.
     *
     * @param userId the user ID to search for
     * @return the User object if found, null otherwise
     */
    User getUserById(String userId);

    /**
     * Checks if a user exists in the repository.
     *
     * @param userId the user ID to check
     * @return true if user exists, false otherwise
     */
    boolean userExists(String userId);

    /**
     * Gets the total number of users in the repository.
     *
     * @return the count of users
     */
    int getUserCount();

    /**
     * Persists changes to an existing user.
     *
     * @param user user to update
     */
    void updateUser(User user);
}