package com.verygood.island.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.verygood.island.entity.Friend;
import com.verygood.island.entity.User;
import com.verygood.island.entity.vo.UserVo;
import com.verygood.island.exception.bizException.BizException;
import com.verygood.island.mapper.FriendMapper;
import com.verygood.island.mapper.UserMapper;
import com.verygood.island.service.FriendService;
import com.verygood.island.util.LocationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 笔友 服务实现类
 * </p>
 *
 * @author chaos
 * @since 2020-05-04
 */
@Slf4j
@CacheConfig(cacheNames = "friend")
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Resource
    private FriendMapper friendMapper;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private LocationUtils locationUtils;

    @Override
    @Cacheable(key = "#userId")
    public Page<UserVo> listFriendsByPage(int page, int pageSize, Integer userId) {
        log.info("正在执行分页查询friend: page = {} pageSize = {} ", page, pageSize);
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        //TODO 这里需要自定义用于匹配的字段,并把wrapper传入下面的page方法
        queryWrapper.eq("user_id", userId);
        Page<Friend> result = super.page(new Page<>(page, pageSize), queryWrapper);
        if (result == null) {
            log.info("该userId={}没有笔友", userId);
            return null;
        }
        List<UserVo> users = getUserVos(userId, result.getRecords());

        log.info("分页查询friend完毕: 结果数 = {} ", result.getRecords().size());
        Page<UserVo> userPage = new Page<>();
        BeanUtil.copyProperties(result, userPage);
        userPage.setRecords(users);
        return userPage;
    }

    @Override
    public Friend getFriendById(int id) {
        log.info("正在查询friend中id为{}的数据", id);
        Friend friend = super.getById(id);
        log.info("查询id为{}的friend{}", id, (null == friend ? "无结果" : "成功"));
        return friend;
    }

    @Override
    @CacheEvict(key = "#friend.userId")
    public int insertFriend(Friend friend) {
        log.info("正在插入friend");
        if (super.save(friend)) {
            log.info("插入friend成功,id为{}", friend.getFriendId());
            return friend.getFriendId();
        } else {
            log.error("插入friend失败");
            throw new BizException("添加失败");
        }
    }

    @Override
    @CacheEvict(key = "#userId")
    public int deleteFriendById(int friendId, int userId) {
        log.info("用户id为{}正在删除id为{}的friend", userId, friendId);
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<Friend>().eq("friend_user_id", friendId).eq("user_id", userId);
        if (friendMapper.delete(queryWrapper) > 0) {
            log.info("删除id为{}的friend成功", friendId);
            return friendId;
        } else {
            log.info("删除id为{}的friend失败,用户id为{}没有该好友", friendId, userId);
            throw new BizException("删除失败[id=" + friendId + "]" + "用户没有该好友");
        }
    }

    @Override
    @CacheEvict(key = "#friend.userId")
    public int updateFriend(Friend friend) {
        log.info("正在更新id为{}的friend", friend.getFriendId());
        if (super.updateById(friend)) {
            log.info("更新d为{}的friend成功", friend.getFriendId());
            return friend.getFriendId();
        } else {
            log.error("更新id为{}的friend失败", friend.getFriendId());
            throw new BizException("更新失败[id=" + friend.getFriendId() + "]");
        }
    }

    @Override
    @Cacheable(key = "#userId")
    public List<UserVo> getUserFriend(Integer userId) {
        log.info("正在查询用户id为{}的所有笔友", userId);
        Map<String, Object> columnMap = new HashMap<>(1);
        columnMap.put("user_id", userId);
        List<Friend> friends = friendMapper.selectByMap(columnMap);
        if (friends.size() <= 0) {
            log.info("用户id为{}的所有笔友为空", userId);
            return null;
        }
        return getUserVos(userId, friends);
    }

    private List<UserVo> getUserVos(Integer userId, List<Friend> friends) {
        User self = userMapper.selectById(userId);
        List<UserVo> users = new ArrayList<>(friends.size());
        log.info("userId{}笔友数量{}", userId, friends.size());
        for (Friend friend : friends) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>().eq("user_id", friend.getFriendUserId());
            User user = userMapper.selectOne(userQueryWrapper);
            user.setPassword(null);
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVo.setDistance(locationUtils.getDistance(self.getCity(), user.getCity()));
            userVo.setMail(null);
            String distance = "";
            if (userVo.getDistance() > 1000) {
                distance = "约" + userVo.getDistance() / 1000 + "公里";
            } else {
                distance = "小于30公里";
            }
            userVo.setSendInfo("距离" + distance + "," +
                    "预计" + LetterServiceImpl.getSendTime(userVo.getDistance()) +
                    "后送达");
            users.add(userVo);
        }
        return users;
    }


}
