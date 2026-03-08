package com.whosalbercik.tileman.server;

import java.util.HashMap;
import java.util.UUID;

public class PlayerInvites {
    private static HashMap<UUID, UUID> invites = new HashMap<>(); // first is invited, second is inviting

    public static void saveInvite(UUID inviting , UUID invited) {
        invites.put(invited, inviting);
    }

    public static UUID getInvite(UUID invited) {
        return invites.get(invited);
    }
}
