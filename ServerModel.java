import java.util.*;


/**
 * The {@code ServerModel} is the class responsible for tracking the state of the server, including
 * its current users and the channels they are in.
 * This class is used by subclasses of {@link Command} to:
 *     1. handle commands from clients, and
 *     2. handle commands from {@link ServerBackend} to coordinate client connection/disconnection.
 */
public final class ServerModel implements ServerModelApi {
    //a treemap that contains all of the channels - using the channel class
    private Set<Channel> channels;
    //a treemap containing user IDs as the key, and their nicknames as the value
    private Map<Integer, String> users;

    /**
     * Constructs a {@code ServerModel} and initializes any collections needed for modeling the
     * server state.
     */
    public ServerModel() {
      channels = new TreeSet<Channel>();
      users = new TreeMap<Integer, String>();
    }


    //==========================================================================
    // Client connection handlers
    //==========================================================================

    /**
     * Informs the model that a client has connected to the server with the given user ID. The model
     * should update its state so that it can identify this user during later interactions. The
     * newly connected user will not yet have had the chance to set a nickname, and so the model
     * should provide a default nickname for the user.
     * Any user who is registered with the server (without being later deregistered) should appear
     * in the output of {@link #getRegisteredUsers()}.
     *
     * @param userId The unique ID created by the backend to represent this user
     * @return A {@link Broadcast} to the user with their new nickname
     */
    public Broadcast registerUser(int userId) {
        String nickname = generateUniqueNickname();
        users.put(userId, nickname);
        return Broadcast.connected(nickname);
    }

    /**
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     * @return the generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String nickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            nickname = "User" + suffix++;
        } while (existingUsers != null && existingUsers.contains(nickname));
        return nickname;
    }

    /**
     * Determines if a given nickname is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Informs the model that the client with the given user ID has disconnected from the server.
     * After a user ID is deregistered, the server backend is free to reassign this user ID to an
     * entirely different client; as such, the model should remove all state of the user associated
     * with the deregistered user ID. The behavior of this method if the given user ID is not
     * registered with the model is undefined.
     * Any user who is deregistered (without later being registered) should not appear in the output
     * of {@link #getRegisteredUsers()}.
     *
     * @param userId The unique ID of the user to deregister
     * @return A {@link Broadcast} instructing clients to remove the user from all channels
     */
    public Broadcast deregisterUser(int userId) {
        String name = users.get(userId);
        //remove the user from the user map
        users.remove(userId);
        Set<String> recipients = new TreeSet<String>();
        //remove user from all channels it is a member of
        //return all the members of the channels it was a member of
        for (Channel c: channels) {
          if (c.contains(userId)) {
            c.remove(userId);
            for (Integer id: c.getMembers()) {
              recipients.add(users.get(id));
            }
          }

        }
        return Broadcast.disconnected(name, recipients);
      }
    //==========================================================================
    // Model update functions
    //==========================================================================
    // TODO: Add functions that update your model

    //==========================================================================
    // Server model queries
    // These functions provide helpful ways to test the state of your model.
    // You may also use them in your implementation.
    //==========================================================================

    /**
     * Gets the user ID currently associated with the given nickname. The returned ID is -1 if the
     * nickname is not currently in use.
     *
     * @param nickname The nickname for which to get the associated user ID
     * @return The user ID of the user with the argued nickname if such a user exists, otherwise -1
     */
    public int getUserId(String nickname) {
        for (Integer id: users.keySet()) {
            if (users.get(id).equals(nickname)) {
               return id;
            }
        }
        return -1;
    }

    /**
     * Gets the nickname currently associated with the given user ID. The returned nickname is
     * null if the user ID is not currently in use.
     *
     * @param userId The user ID for which to get the associated nickname
     * @return The nickname of the user with the argued user ID if such a user exists, otherwise
     *          null
     */
    public String getNickname(int userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        }
        else { return null;}
    }
    /**
     * Changes the nickname of a given user
     *
     * @param the integer user ID
     * @param the String new nickname
     *
     * @return void
     */
    public void changeNickname(int id, String nickname) {
      users.put(id, nickname);
    }
    /**
     * Determines all of the users that are in common channels as the input user
     *
     * @param the integer user ID
     *
     * @return a collection of all of the users in common channels as the input user
     */
    public Collection<String> getRelevant(int id) {
      Set<String> s = new TreeSet<String>();
      for (Channel c: channels) {
          if (c.contains(id)) {
              for (Integer m: c.getMembers()) {
                  s.add(users.get(m));
              }
          }
      }
      return s;
    }
    /**
     * Gets a collection of the nicknames of all users who are registered with the server. Changes
     * to the returned collection should not affect the server state.
     *
     * This method is provided for testing.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        Set<String> u = new TreeSet<String>();
        for (Integer id: users.keySet()) {
            u.add(users.get(id));
        }
        return u;
    }

    /**
     * Gets a collection of the names of all the channels that are present on the server. Changes to
     * the returned collection should not affect the server state.
     *
     * This method is provided for testing.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        Set<String> s = new TreeSet<String>();
        for (Channel c: channels) {
            s.add(c.getTitle());
        }
        return s;

    }
    /**
     * Determines whether or not a certain channel exists in the server
     *
     * @param the string channel name
     *
     * @return boolean that is true when the channel is present and false otherwise
     */
    public boolean channelExists(String name) {
        for (Channel c: channels) {
            if (c.getTitle().equals(name)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Adds a channel to the server
     *
     * @param the string owner nickname
     * @param the string channel name
     * @param boolean whether the channel is invite only
     *
     * @return void
     */
    public void addChannel(int owner, String name, boolean inviteOnly) {
        channels.add(new Channel(owner, name, inviteOnly));
    }
    /**
     * Adds a user to a channel
     *
     * @param the string channel name
     * @param the integer user ID
     *
     * @return void
     */
    public void addUser(int u, String channelName) {
        for (Channel c: channels) {
            if (c.getTitle().equals(channelName)) {
                c.add(u);
            }
        }
    }
    /**
     * Removes a user from a channel
     *
     * @param the integer user ID of the user being removed
     * @param the string channel name
     *
     * @return void
     */
    public void removeUser(int u, String channel) {
        for (Channel c: channels) {
           if (c.getTitle().equals(channel)) {
               c.remove(u);
           }
        }
    }
    /**
     * Determines whether or not a user is present in a certain channel
     *
     * @param the integer user ID of the user in question
     * @param the string channel name
     *
     * @return boolean that is true when the user is present and false otherwise
     */
    public boolean userContained(int u, String channel) {
        for (Channel c: channels) {
            if (c.getTitle().equals(channel)) {
                return c.contains(u);
            }
        }
        return false;
    }
    /**
     * Removes everyone from a given channel
     *
     * @param the string channel name
     *
     * @return void
     */
    public void removeEveryone(String channel) {
      for (Channel c: channels) {
        if (c.getTitle().equals(channel)){
          for (Integer id: c.getMembers()) {
            c.remove(id);
          }
        }
      }
    }
    /**
     * Removes a channel from the server
     *
     * @param the string channel name
     *
     * @return void
     */
    public void removeChannel(String channel) {
      for (Channel c: channels) {
        if (c.getTitle().equals(channel)) {
          channels.remove(c);
        }
      }
    }
    /**
     * Determines whether or not a certain channel in invite only
     *
     * @param the string channel name
     *
     * @return boolean that is true when the channel is invite only and false otherwise
     */
    public boolean isInviteOnly(String channel) {
        for (Channel c: channels) {
            if (c.getTitle().equals(channel)) {
                return c.getPriv();
            }
        }
        return false;
    }
    /**
     * Determines whether or not a certain user is in the server
     *
     * @param the integer user ID
     *
     * @return boolean that is true when the user is present and false otherwise
     */
    public boolean userPresentInServer(String u) {
        for (Integer val: users.keySet()) {
            if (users.get(val).equals(u)) {return true;}
        }
        return false;
    }
    /**
     * Gets a collection of the nicknames of all the users in a given channel. The collection is
     * empty if no channel with the given name exists. Modifications to the returned collection
     * should not affect the server state.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get member nicknames
     * @return The collection of user nicknames in the argued channel
     */
    public Collection<String> getUsers(String channelName) {
        Set<String> s = new TreeSet<String>();
        for (Channel c: channels) {
            if (c.getTitle().compareTo(channelName) == 0) {
                for (Integer id: c.getMembers()){
                  s.add(users.get(id));
                }
            }
        }
        return s;
    }

    /**
     * Gets the nickname of the owner of the given channel. The result is {@code null} if no
     * channel with the given name exists.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel exists, othewrise null
     */
    public String getOwner(String channelName) {
        int id = 0;
        for (Channel c: channels) {
           if (c.getTitle().compareTo(channelName) == 0) {
               id = c.getOwner();
           }
        }
        return users.get(id);
    }
}
