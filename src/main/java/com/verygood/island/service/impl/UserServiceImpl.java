package com.verygood.island.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.verygood.island.entity.User;
import com.verygood.island.exception.bizException.BizException;
import com.verygood.island.mapper.UserMapper;
import com.verygood.island.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author chaos
 * @since 2020-05-04
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 根据id查询User
     *
     * @param user 登陆用户
     * @return 返回登陆成功的用户
     * @author chaos
     * @since 2020-05-02
     */
    @Override
    public User login(User user) {
        return null;
    }

    @Override
    public Page<User> listUsersByPage(int page, int pageSize, String factor) {
        log.info("正在执行分页查询user: page = {} pageSize = {} factor = {}", page, pageSize, factor);
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().like("", factor);
        //TODO 这里需要自定义用于匹配的字段,并把wrapper传入下面的page方法
        Page<User> result = super.page(new Page<>(page, pageSize));
        log.info("分页查询user完毕: 结果数 = {} ", result.getRecords().size());
        return result;
    }

    @Override
    public User getUserById(int id) {
        log.info("正在查询user中id为{}的数据", id);
        User user = super.getById(id);
        log.info("查询id为{}的user{}", id, (null == user ? "无结果" : "成功"));
        return user;
    }

    @Override
    public int insertUser(User user) {
        log.info("正在插入user");
        if (super.save(user)) {
            log.info("插入user成功,id为{}", user.getUserId());
            return user.getUserId();
        } else {
            log.error("插入user失败");
            throw new BizException("添加失败");
        }
    }

    @Override
    public int deleteUserById(int id) {
        log.info("正在删除id为{}的user", id);
        if (super.removeById(id)) {
            log.info("删除id为{}的user成功", id);
            return id;
        } else {
            log.error("删除id为{}的user失败", id);
            throw new BizException("删除失败[id=" + id + "]");
        }
    }

    @Override
    public int updateUser(User user) {
        log.info("正在更新id为{}的user", user.getUserId());
        user.setWord(null);
        if (super.updateById(user)) {
            log.info("更新d为{}的user成功", user.getUserId());
            return user.getUserId();
        } else {
            log.error("更新id为{}的user失败", user.getUserId());
            throw new BizException("更新失败[id=" + user.getUserId() + "]");
        }
    }

}
