package SeungYeop_Han.Cosi.DTOs;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {

    private final String name;
    private final String email;
    private final String password;
}
