package springbook.user.service;

import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static springbook.user.service.BasicUserLevelUpgradePolicy.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.BasicUserLevelUpgradePolicy.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {

    List<User> users;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    UserService userService;
    @Autowired
    UserService testUserService; // 같은 타입의 빈이 두개 존재하기 때문에 필드이름을 기준으로 주입될 빈이 결정된다. 자동 프록시 생성기에 의해 트랜잭션 부가기능이 testUserService빈에 적용됐는지 확인

    @Autowired
    UserDao userDao;

    static class TestUserServiceException extends RuntimeException {}

    static class TestUserServiceImpl extends UserServiceImpl { // 포인트컷 클래스필터에 걸리는이름으로 정의
        private String id ="madnite1"; // 테스트 픽스처의 users(3)의 id값을 고정시켰다.

        @Override
        public void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }

        @Override
        public List<User> getAll() {
            for(User user: super.getAll()) {
                super.update(user); // 읽기전용 메소드에서 강제로 쓰기 시도한다. read-only:true이므로, 예외발생해야한다.
            }
            return null;
        }
    }

    @Test(expected= TransientDataAccessResourceException.class)
    public void readOnlyTransactionAttribute() {
        testUserService.getAll(); // 예외발생해야함
    }

    @Before
    public void setUp() {
        this.users = Arrays.asList(
                new User("bumjin", "박범진", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0, "bumjin@spring.io"),
                new User("joytouch", "강명성", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "joytouch@spring.io"),
                new User("erwins", "신승한", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1, "erwins@spring.io"),
                new User("madnite1", "이상호", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "madnite1@spring.io"),
                new User("green", "오민규", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "green@spring.io")
        );
    }

    @Test
    @Transactional
    @Rollback(false) // 기본값은 true, 테스트가 끝나도 롤백되지 않게 하려면 false로 설정
    public void transactionSync() {
        userService.deleteAll();
        userService.add(users.get(0));
        userService.add(users.get(1));
    }

    @Test
    public void transactionSyncAndRollbach() {
        userDao.deleteAll();
        assertThat(userDao.getCount(), is(0));

        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

        userService.add(users.get(0));
        userService.add(users.get(1));
        assertThat(userDao.getCount(), is(2));

        transactionManager.rollback(txStatus);       // 강제롤백한다.
        assertThat(userDao.getCount(), is(0)); // 롤백이되었는지 테스트
    }

    @Test
    public void upgradeAllOrNothing() {
        userDao.deleteAll();
        for(User user: users) userDao.add(user);

        try {
            this.testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch(TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(1), false);
    }

    @Test
    public void advisorAutoProxyCreator() {
        assertThat(testUserService, is(java.lang.reflect.Proxy.class)); // 프록시로 변경된 오브젝트인지 확인한다.
    }

    @Test
    public void bean() {
       assertThat(this.userService, is(notNullValue()));
    }

    @Test
    public void upgradeLevels() throws Exception {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MailSender mockMailSender = mock(MailSender.class);

        UserLevelUpgradePolicy userLevelUpgradePolicy = new BasicUserLevelUpgradePolicy();
        ((BasicUserLevelUpgradePolicy)userLevelUpgradePolicy).setUserDao(mockUserDao);
        ((BasicUserLevelUpgradePolicy)userLevelUpgradePolicy).setMailSender(mockMailSender);

        userServiceImpl.setMailSender(mockMailSender);
        userServiceImpl.setUserLevelUpgradePolicy(new BasicUserLevelUpgradePolicy());
        userServiceImpl.setUserLevelUpgradePolicy(userLevelUpgradePolicy);
        userServiceImpl.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(), is(Level.GOLD));

        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
        assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));

    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        } else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }
}
