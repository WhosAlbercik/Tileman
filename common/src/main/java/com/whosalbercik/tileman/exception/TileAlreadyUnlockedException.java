package com.whosalbercik.tileman.exception;

/**
 * Exception occurs when {@link com.whosalbercik.tileman.server.TileHandler} attempts to unlock tile which is already unlocked. Once tile unlocked, there is no possibility for tile to be available for unlocking again. Ownership can be transferred, however, this is not defined as unlocking
 */
public class TileAlreadyUnlockedException extends RuntimeException {
    public TileAlreadyUnlockedException() {
        super("Tile cannot be unlocked because it has already been unlocked by a different player");
    }
}
