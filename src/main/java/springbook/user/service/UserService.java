package springbook.user.service;

import org.springframework.transaction.annotation.Transactional;
import springbook.user.domain.User;

import java.util.List;

@Transactional
public interface UserService {
    boolean canUpgradeLevel(User user);
    void upgradeLevel(User user);

    void add(User user);

    // UserDao내의 동일 메소드와 1:1대응되지만, add()-초기레벨설정 처럼 단순위임 이상의로직을 가질수 있다.
    @Transactional(readOnly = true)
    User get(String id);
    @Transactional(readOnly = true)
    List<User> getAll();
    void deleteAll();
    void update(User user);

    void upgradeLevels();

}
