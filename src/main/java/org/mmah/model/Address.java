package org.mmah.model;

import lombok.Data;

/**
 * Abstraction layer for pluggable address processing. This is a hard problem and we
 * might need to switch libraries.
 *
 * Best effort first pass.
 */
@Data
public class Address {
    private String addressee; // John Jones
    private String thoroughfareNumber; // 123
    private String thoroughfareName; // 1st Avenue South
    private String unitDescription; // Suite 101
    private String localityName; // New York, New York
    private String countryName; // United States of America
    private String postalCode; // 10007
}
