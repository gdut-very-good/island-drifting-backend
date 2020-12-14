package com.verygood.island.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.verygood.island.entity.Post;
import com.verygood.island.exception.bizException.BizException;
import com.verygood.island.mapper.PostMapper;
import com.verygood.island.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 海岛动态 服务实现类
 * </p>
 *
 * @author chaos
 * @since 2020-05-04
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "post")
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Override
    public Page<Post> listPostsByPage(int page, int pageSize, String factor) {
        log.info("正在执行分页查询post: page = {} pageSize = {} factor = {}", page, pageSize, factor);
        QueryWrapper<Post> queryWrapper = new QueryWrapper<Post>().like("", factor);
        //TODO 这里需要自定义用于匹配的字段,并把wrapper传入下面的page方法
        Page<Post> result = super.page(new Page<>(page, pageSize));
        List<Post> posts = result.getRecords();
        for (Post post :
                posts) {
            if (post.getContent().length() > 120) {
                post.setContent(post.getContent().substring(0, 120) + "...");
            }
        }
        log.info("分页查询post完毕: 结果数 = {} ", result.getRecords().size());
        return result;
    }

    @Override
    public Post getPostById(int id) {
        log.info("正在查询post中id为{}的数据", id);
        Post post = super.getById(id);
        if (post == null) {
            log.info("没有id为{}的post", id);
            throw new BizException("没有该动态");
        }
        log.info("查询id为{}的post{}", id, "成功");
        post.setView(post.getView() + 1);
        super.updateById(post);
        return post;
    }

    @Override
    @CacheEvict(key = "#post.userId")
    public int insertPost(Post post) {
        log.info("正在插入post");
        post.setView(0);
        post.setTime(LocalDateTime.now());
        post.setContent(post.getContent().trim());
        if (post.getContent().length() > 250) {
            throw new BizException("动态内容不能超过250个字");
        }
        if (super.save(post)) {
            log.info("插入post成功,id为{}", post.getPostId());
            return post.getPostId();
        } else {
            log.error("插入post失败");
            throw new BizException("添加失败");
        }
    }

    @Override
    @CacheEvict(key = "#userId")
    public int deletePostById(int id, Integer userId) {
        log.info("正在删除id为{}的post", id);

        // 查看权限
        Post post = super.getById(id);

        if (post == null) {
            log.info("执行删除海岛动态时传输了错误的id【{}】， 该id找不到对应的海岛动态", id);
            throw new BizException("请校验对应的海岛动态id");
        }

        if (!post.getUserId().equals(userId)) {
            log.info("执行删除海岛动态时，删除人【{}】并非海岛动态属于者【{}】", userId, post.getUserId());
            throw new BizException("删除海岛动态失败！您并非该海岛动态的发送者");
        }

        // 执行删除操作
        if (super.removeById(id)) {
            log.info("删除id为{}的post成功", id);
            return id;
        } else {
            log.error("删除id为{}的post失败", id);
            throw new BizException("删除失败[id=" + id + "]");
        }
    }

    @Override
    @CacheEvict(key = "#post.userId")
    public int updatePost(Post post) {
        log.info("正在更新id为{}的post", post.getPostId());
        if (super.updateById(post)) {
            log.info("更新d为{}的post成功", post.getPostId());
            return post.getPostId();
        } else {
            log.error("更新id为{}的post失败", post.getPostId());
            throw new BizException("更新失败[id=" + post.getPostId() + "]");
        }
    }

    @Override
    @Cacheable(key = "#id")
    public List<Post> getByUserId(Integer id) {
        log.info("正在查询userId为{}的post列表数据", id);
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("user_id", id).orderByDesc("time");
        List<Post> posts = super.list(postQueryWrapper);
        if (posts == null) {
            log.info("userId为{}的没有post", id);
            return null;
        }
        for (Post post :
                posts) {
            if (post.getContent().length() > 120) {
                post.setContent(post.getContent().substring(0, 120) + "...");
            }
        }
        return posts;
    }

}
