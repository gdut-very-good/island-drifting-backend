package com.verygood.island.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.verygood.island.constant.Constants;
import com.verygood.island.entity.Friend;
import com.verygood.island.entity.Notice;
import com.verygood.island.entity.User;
import com.verygood.island.entity.vo.UserVo;
import com.verygood.island.exception.bizException.BizException;
import com.verygood.island.exception.bizException.BizExceptionCodeEnum;
import com.verygood.island.mapper.FriendMapper;
import com.verygood.island.mapper.NoticeMapper;
import com.verygood.island.mapper.UserMapper;
import com.verygood.island.service.StampService;
import com.verygood.island.service.UserService;
import com.verygood.island.util.ImageUtils;
import com.verygood.island.util.LocationUtils;
import com.verygood.island.util.Md5Util;
import com.verygood.island.util.UploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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


    private static List<User> users = null;
    /**
     * 用户名的正则表达式：4-16位的字母数字组合（可包含其中一种）
     */
    private final String NAME_PATTERN = "^[a-zA-Z0-9]{4,16}$";
    /**
     * 密码的正则表达式：6-16位的字母数字组合（必须包含字母，数字）
     */
    private final String PASSWORD_PATTERN = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
    @Resource
    LocationUtils locationUtils;
    @Resource
    StampService stampService;
    @Autowired
    NoticeMapper noticeMapper;
    @Autowired
    FriendMapper friendMapper;

    public static List<User> getUserList() {
        if (null == users) {
            cacheUsers();
        }
        return users;
    }

    @Scheduled(cron = "0 15 * * * ?")
    public static void cacheUsers() {
        UserMapper userMapper = com.verygood.island.util.BeanUtils.getBean(UserMapper.class);
        log.info("正在清空缓存的用户数据");
        if (users != null) {
            users.clear();
        }
        log.info("正在缓存用户");
        users = userMapper.selectList(new QueryWrapper<>());
        log.info("缓存成功：{}条用户数据", users.size());
    }

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
        log.info("正在执行登录操作：user:【{}】", user);
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            log.info("执行登录操作时未传输账号或者密码");
            throw new BizException("请输入正确的账号或者密码");
        }
        // 查询数据库中该账户的账号密码
        User userPo = super.getOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (userPo == null) {
            log.info("执行登录操作时传输了错误的账号：[{}]", user.getUsername());
            throw new BizException("该账号未进行注册");
        }
        // 校验密码
        if (!userPo.getPassword().equals(Md5Util.getMd5String(user.getPassword()))) {
            log.info("执行登录操作时密码错误，账号信息为：【{}】", user);
            throw new BizException("密码错误！");
        }

        // 密码进行隐藏
        userPo.setPassword("");

        return userPo;
    }

    /**
     * 分页查询User
     *
     * @param page     当前页数
     * @param pageSize 页的大小
     * @param factor   搜索关键词
     * @return 返回mybatis-plus的Page对象,其中records字段为符合条件的查询结果
     * @author chaos
     * @since 2020-05-04
     */
    @Override
    public Page<UserVo> searchUsersByPage(int page, int pageSize, String factor) {
        factor = factor.trim();
        log.info("正在执行分页查询user: page = {} pageSize = {} factor = {}", page, pageSize, factor);
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().like("nickname", factor)
                .or().likeLeft("nickname", factor).or().likeRight("nickname", factor);
        Page<User> result = super.page(new Page<>(page, pageSize), queryWrapper);
        log.info("分页查询user完毕: 结果数 = {} ", result.getRecords().size());

        // 查看自己的信息
        User self = (User) SecurityUtils.getSubject().getPrincipal();
        if (null == self) {
            throw new BizException(BizExceptionCodeEnum.NO_LOGIN);
        }
        self = super.getById(self.getUserId());

        //转成vo类型
        List<User> records = result.getRecords();
        List<UserVo> voRecords = new LinkedList<>();
        for (User user : records) {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVo.setDistance(locationUtils.getDistance(self.getCity(), user.getCity()));
            voRecords.add(userVo);
        }

        Page<UserVo> userVoPage = new Page<>();
        BeanUtils.copyProperties(result, userVoPage);
        userVoPage.setRecords(voRecords);
        return userVoPage;
    }

    @Override
    public UserVo getUserById(int id) {
        log.info("正在查询user中id为{}的数据", id);
        User user = super.getById(id);
        log.info("查询id为{}的user{}", id, (null == user ? "无结果" : "成功"));
        if (null == user) {
            return null;
        }
        // 隐藏密码和邮箱
        user.setPassword("").setMail("");

        // 查看自己的信息
        User self = (User) SecurityUtils.getSubject().getPrincipal();
        self = super.getById(self.getUserId());

        // 新建一个返回的vo
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);

        // 查看是否是查询自己的信息
        if (self.getUserId() == id) {
            userVo.setDistance(0L);
        } else {
            //查看别人的海岛随机获得邮票和时间胶囊
            int random = RandomUtil.randomInt(0, 20);
            switch (random) {
                case 0:
                    stampService.addStamp(self.getUserId(), Constants.STAMP_PAIR);
                    sendNotice("你浏览别人的海岛时获得随机奖励: " + Constants.STAMP_PAIR + " 样式的邮票一张", self.getUserId());
                    break;
                case 1:
                    stampService.addStamp(self.getUserId(), Constants.STAMP_ROSE);
                    sendNotice("你浏览别人的海岛时获得随机奖励: " + Constants.STAMP_ROSE + " 样式的邮票一张", self.getUserId());
                    break;
                case 2:
                    stampService.addStamp(self.getUserId(), Constants.STAMP_MAN);
                    sendNotice("你浏览别人的海岛时获得随机奖励: " + Constants.STAMP_MAN + " 样式的邮票一张", self.getUserId());
                    break;
                case 3:
                    stampService.addStamp(self.getUserId(), Constants.STAMP_PEN);
                    sendNotice("你浏览别人的海岛时获得随机奖励: " + Constants.STAMP_PEN + " 样式的邮票一张", self.getUserId());
                    break;
                case 4:
                    stampService.addStamp(self.getUserId(), Constants.STAMP_BLOOM);
                    sendNotice("你浏览别人的海岛时获得随机奖励: " + Constants.STAMP_BLOOM + " 样式的邮票一张", self.getUserId());
                    break;
                case 5:
                    self.setCapsule(self.getCapsule() + 1);
                    sendNotice("你浏览别人的海岛时获得随机奖励: 一个时间胶囊", self.getUserId());
                    super.updateById(self);
                    break;
                default:
                    break;
            }

            userVo.setDistance(locationUtils.getDistance(self.getCity(), user.getCity()));
        }

        return userVo;


    }

    /**
     * 发送通知
     */
    private void sendNotice(String content, Integer userId) {
        Notice notice = new Notice();
        notice.setTitle("获得奖励通知");
        notice.setContent(content);
        notice.setUserId(userId);
        noticeMapper.insert(notice);
        log.info("发送notice成功，内容为{}", content);
    }

    /**
     * 随机获取用户
     *
     * @return 返回一个用户
     * @notice none
     * @author <a href="mailto:kobe524348@gmail.com">黄钰朝</a>
     * @date 2020-05-23
     */
    @Override
    public UserVo getUserByRandom() {
        if (null == users) {
            cacheUsers();
        }
        //随机获取一个用户
        User user = users.get(new Random().nextInt(users.size()));
        // 查看自己的信息
        User self = (User) SecurityUtils.getSubject().getPrincipal();
        self = super.getById(self.getUserId());
        //转成vo
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        userVo.setDistance(locationUtils.getDistance(user.getCity(), self.getCity()));
        return userVo;
    }

    @Override
    public int insertUser(User user) {
        log.info("正在插入user");

        // 校验账号和密码参数
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            log.info("插入用户时传输空参数：账号或者密码为空");
            throw new BizException("账号或者密码不能为空");
        }

        // 查看账户格式是否正确
        if (!user.getUsername().matches(NAME_PATTERN)) {
            log.info("用户进行注册时未传输正确的用户名格式：【{}】", user.getUsername());
            throw new BizException("用户名格式错误！应为：4-16位的字母数字组合");
        }

        if (!user.getPassword().matches(PASSWORD_PATTERN)) {
            log.info("用户进行注册时未传输正确的密码格式：【{}】", user.getPassword());
            throw new BizException("密码格式错误！应为：6-16位的字母数字组合");
        }

        // 查看该用户是否已经存在
        if (super.getOne(new QueryWrapper<User>().eq("username", user.getUsername())) != null) {
            log.info("插入数据时检测到账号【{}】已经存在！插入失败", user.getUsername());
            throw new BizException("该账号已经存在");
        }

        // 设置三个时间胶囊
        user.setCapsule(Constants.INIT_CAPSULE_NUMBER);


        //设置默认头像
        int random = RandomUtil.randomInt(0, 1);
        if (random == 0) {
            user.setPhoto(Constants.DEFAULT_PHOTO_MAN);
        } else {
            user.setPhoto(Constants.DEFAULT_PHOTO_WOMEN);
        }

        // 对密码进行加密
        user.setPassword(Md5Util.getMd5String(user.getPassword()));

        // 进行插库操作
        if (super.save(user)) {
            log.info("插入user成功,id为{}", user.getUserId());
        } else {
            log.error("插入user失败");
            throw new BizException("添加失败");
        }

        //添加开发者笔友
        User islandDeveloper = super.getOne(new QueryWrapper<User>().eq("username", "island"));
        addFriend(user.getUserId(), islandDeveloper);

        // 进行邮票的增加,初始化送 5 张 “中国” 类型邮票
        stampService.addStamp(user.getUserId(), Constants.STAMP_PEN);
        stampService.addStamp(user.getUserId(), Constants.STAMP_BLOOM);
        stampService.addStamp(user.getUserId(), Constants.STAMP_MAN);
        stampService.addStamp(user.getUserId(), Constants.STAMP_LADY);
        stampService.addStamp(user.getUserId(), Constants.STAMP_ROSE);

        return user.getUserId();
    }

    /**
     * 加好友
     *
     * @param userId     用户id
     * @param friendUser 朋友
     */
    private void addFriend(Integer userId, User friendUser) {
        if (friendUser != null) {
            Friend friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendUserId(friendUser.getUserId());
            friendMapper.insert(friend);
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
        user.setPhoto(null);
        user.setSendLetter(null);
        user.setReceiveLetter(null);
        user.setPassword(null);
        if (user.getCity() != null && !locationUtils.isValidLocation(user.getCity())) {
            throw new BizException("您填写的地址无法识别: " + user.getCity());
        }
        if (super.updateById(user)) {
            log.info("更新d为{}的user成功", user.getUserId());
            return user.getUserId();
        } else {
            log.error("更新id为{}的user失败", user.getUserId());
            throw new BizException("更新失败[id=" + user.getUserId() + "]");
        }
    }

    @Override
    public String uploadIcon(MultipartFile file, Integer userId) {

        log.info("正在执行上传用户头像操作");

        File result = checkAndUploadFile(file);

        // 进行数据库的更新
        User userPo = getOne(new QueryWrapper<User>().eq("user_id", userId));

        // 删除旧的头像地址
        tryDeleteOldImage(userPo.getPhoto());

        // 设置图片地址
        userPo.setPhoto(result.getName());

        updateById(userPo);

        return result.getName();
    }

    /**
     * 尝试删除旧图
     *
     * @param photo
     */
    private void tryDeleteOldImage(String photo) {
        if (!StringUtils.isEmpty(photo)) {
            if (!Constants.DEFAULT_PHOTO_MAN.equalsIgnoreCase(photo) &&
                    !Constants.DEFAULT_PHOTO_WOMEN.equalsIgnoreCase(photo) &&
                    !Constants.DEFAULT_BACKGROUND.equalsIgnoreCase(photo)) {
                UploadUtils.deleteFile(photo);
            }
        }
    }

    @Override
    public String uploadBackground(MultipartFile file, Integer userId) {
        log.info("正在执行上传用户海岛背景操作");

        File result = checkAndUploadFile(file);

        // 进行数据库的更新
        User userPo = getOne(new QueryWrapper<User>().eq("user_id", userId));

        // 删除旧的背景地址
        tryDeleteOldImage(userPo.getBackground());

        // 设置图片地址
        userPo.setBackground(result.getName());

        updateById(userPo);

        return result.getName();
    }

    /**
     * 检查文件格式并且上传到服务器
     *
     * @param file 文件内容
     * @return 上传后的文件
     */
    private File checkAndUploadFile(MultipartFile file) {
        if (file == null || file.getSize() == 0) {
            log.info("上传的文件为空！");
            throw new BizException("请选择正确的文件！");
        }

        // 对上传的文件进行判断
        boolean isImage = false;
        try {
            isImage = ImageUtils.isImage(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!isImage) {
            log.info("用户上传的文件不是图片：【{}】", file.getOriginalFilename());
            throw new BizException("上传海岛背景失败！该文件非图片类型");
        }

        log.info("上传的文件名称：【{}】", file.getOriginalFilename());
        return UploadUtils.upload(file, UploadUtils.getFileName(file.getOriginalFilename()));
    }
}
