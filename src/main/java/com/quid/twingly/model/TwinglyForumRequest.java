package com.quid.twingly.model;

import java.io.Serial;
import java.io.Serializable;

public record TwinglyForumRequest(long from, long to, Country country, int offset) implements Serializable {
    @Serial
    private static final long serialVersionUID = 3384489958952035601L;
}
