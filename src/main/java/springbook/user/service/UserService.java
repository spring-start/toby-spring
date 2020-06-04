package springbook.user.service;

import springbook.user.domain.User;

import java.util.List;

public interface UserService {
    boolean canUpgradeLevel(User user);
    void upgradeLevel(User user);

    void add(User user);

    // UserDao내의 동일 메소드와 1:1대응되지만, add()-초기레벨설정 처럼 단순위임 이상의로직을 가질수 있다.
    User get(String id);
    List<User> getAll();
    void deleteAll();
    void update(User user);

    void upgradeLevels();

}
