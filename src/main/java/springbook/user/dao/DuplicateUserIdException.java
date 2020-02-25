package springbook.user.dao;

public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(){}

    //생성자 선언을 해 주어야 중첩예외를 만들 수 있음
    public DuplicateUserIdException(Throwable cause){
        super(cause);
    }

}
