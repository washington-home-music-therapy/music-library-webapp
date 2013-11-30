package org.mmah.model;

import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
public class Library {
    private long id;

    private String name;

    private Account administrator;
    private List<Account> librarians;
    private Address address;
}
