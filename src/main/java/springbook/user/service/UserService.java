package springbook.user.service;

import springbook.user.domain.User;

public interface UserService {
    boolean canUpgradeLevel(User user);
    void upgradeLevel(User user);
    void add(User user);
    void upgradeLevels();

}
