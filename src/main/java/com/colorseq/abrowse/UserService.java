package com.colorseq.abrowse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserService implements UserDetailsService {

    private UserEntityDao userEntityDao;

    public UserService() {
    }

    @Autowired
    public void setUserEntityDao(UserEntityDao userEntityDao) {
        this.userEntityDao = userEntityDao;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        UserEntity userEntity = this.userEntityDao.findByUsername(name);
        if (userEntity == null) {
            throw new UsernameNotFoundException("用户不存在或密码错误");
        }
        return userEntity;
    }
}
