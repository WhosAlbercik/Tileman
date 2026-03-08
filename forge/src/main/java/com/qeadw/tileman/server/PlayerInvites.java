package com.qeadw.tileman.server;

import java.util.HashMap;
import java.util.UUID;

public class PlayerInvites {
    private static final HashMap<UUID, UUID> invites = new HashMap<>();

    public static void saveInvite(UUID inviting, UUID invited) {
        invites.put(invited, inviting);
    }

    public static UUID getInvite(UUID invited) {
        return invites.get(invited);
    }
}
