package com.whosalbercik.tileman.server;

import java.util.HashMap;
import java.util.UUID;

/**
 * Holds player friending invites
 */
public class PlayerInvites {
    private static HashMap<UUID, UUID> invites = new HashMap<>(); // first is invited, second is inviting

    /**
     * Save invite from inviting to invited
     *
     * @param inviting the inviting player
     * @param invited  the invited player
     */
    public static void saveInvite(UUID inviting , UUID invited) {
        invites.put(invited, inviting);
    }

    /**
     * Gets invite
     *
     * @param invited the invited
     * @return the inviting player
     */
    public static UUID getInvite(UUID invited) {
        return invites.get(invited);
    }
}
