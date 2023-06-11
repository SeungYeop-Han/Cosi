package SeungYeop_Han.Cosi.exceptions;

import javax.naming.AuthenticationException;

public class MemberNotFoundException extends AuthenticationException {
    public MemberNotFoundException(String msg){ super(msg); }
}
