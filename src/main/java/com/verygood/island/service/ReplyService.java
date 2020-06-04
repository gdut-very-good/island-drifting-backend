package com.verygood.island.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.verygood.island.entity.Reply;
import com.verygood.island.entity.vo.ReplyVo;

import java.util.List;

/**
 * <p>
 * 回复 服务类
 * </p>
 *
 * @author chaos
 * @since 2020-05-04
 */
public interface ReplyService {

    /**
     * 分页查询Reply
     *
     * @param page     当前页数
     * @param pageSize 页的大小
     * @param factor   搜索关键词
     * @return 返回mybatis-plus的Page对象,其中records字段为符合条件的查询结果
     * @author chaos
     * @since 2020-05-04
     */
    Page<Reply> listReplysByPage(int page, int pageSize, String factor);

    /**
     * 根据id查询Reply
     *
     * @param id 需要查询的Reply的id
     * @return 返回对应id的Reply对象
     * @author chaos
     * @since 2020-05-04
     */
    ReplyVo getReplyById(int id);

    /**
     * 插入Reply
     *
     * @param reply 需要插入的Reply对象
     * @return 返回插入成功之后Reply对象的id
     * @author chaos
     * @since 2020-05-04
     */
    int insertReply(Reply reply);

    /**
     * 根据id删除Reply
     *
     * @param id 需要删除的Reply对象的id
     * @return 返回被删除的Reply对象的id
     * @author chaos
     * @since 2020-05-04
     */
    int deleteReplyById(int id);

    /**
     * 根据id更新Reply
     *
     * @param reply 需要更新的Reply对象
     * @return 返回被更新的Reply对象的id
     * @author chaos
     * @since 2020-05-04
     */
    int updateReply(Reply reply);

    /**
     * 根据postId查询Reply
     *
     * @param id 需要查询的Reply的postId
     * @author cy
     * @since 2020-05-26
     */
    List<ReplyVo> getByPostId(Integer id);
}
