package org.app.f1.entities;

import jakarta.persistence.*;

import lombok.*;

import java.util.Locale;
import java.util.Objects;

@Entity
@Table(
        name = "drivers",
        uniqueConstraints =
                @UniqueConstraint(columnNames = "normalized_name")

)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "broadcast_name", length = 50)
    private String broadcastName;

    @Column(name = "headshot_url", length = 512)
    private String headshotUrl;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "full_name", length = 100, unique = true)
    private String fullName;

    @Column(name = "normalized_name", length = 100, unique = true)
    private String normalizedName;

    public static String normalize(String fullName) {
        return (fullName).replace(" ", "_").toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Driver driver = (Driver) o;
        if (normalizedName == null) normalizedName = normalize(fullName);
        return normalizedName.equals(driver.normalizedName);
    }

    @Override
    public int hashCode() { return Objects.hash(normalizedName); }
}
