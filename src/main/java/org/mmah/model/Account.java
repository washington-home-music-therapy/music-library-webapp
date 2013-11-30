package org.mmah.model;

import lombok.Data;

/**
 * The account may represent a caregiver, administrator, library provider, or patient.
 *
 * It is appropriate to bind this to OATH authentication information.
 */
@Data
public class Account {
    private long id;

    private String name;
    private String email;
}
