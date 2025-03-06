package com.whosalbercik.tileman.exception;

public class TileAlreadyUnlockedException extends RuntimeException {
    public TileAlreadyUnlockedException() {
        super("Tile cannot be unlocked because it has already been unlocked by a different player");
    }
}
