package springbook.user.dao;

import org.springframework.dao.DataAccessException;

public class DuplicateUserIdException extends DataAccessException {
    public DuplicateUserIdException(Throwable cause){
        super(String.valueOf(cause));
    }
}
