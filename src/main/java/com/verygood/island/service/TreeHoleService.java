package com.verygood.island.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.verygood.island.entity.TreeHole;

/**
 * <p>
 * 树洞
 * 服务类
 * </p>
 *
 * @author chaos
 * @since 2020-05-21
 */
public interface TreeHoleService {

    /**
     * 分页查询TreeHole
     *
     * @param page     当前页数
     * @param pageSize 页的大小
     * @param factor   搜索关键词
     * @return 返回mybatis-plus的Page对象,其中records字段为符合条件的查询结果
     * @author chaos
     * @since 2020-05-21
     */
    Page<TreeHole> listTreeHolesByPage(int page, int pageSize, String factor);

    /**
     * 根据id查询TreeHole
     *
     * @param id 需要查询的TreeHole的id
     * @return 返回对应id的TreeHole对象
     * @author chaos
     * @since 2020-05-21
     */
    TreeHole getTreeHoleById(int id);

    /**
     * 插入TreeHole
     *
     * @param treeHole 需要插入的TreeHole对象
     * @return 返回插入成功之后TreeHole对象的id
     * @author chaos
     * @since 2020-05-21
     */
    int insertTreeHole(TreeHole treeHole);

    /**
     * 根据id删除TreeHole
     *
     * @param id 需要删除的TreeHole对象的id
     * @return 返回被删除的TreeHole对象的id
     * @author chaos
     * @since 2020-05-21
     */
    int deleteTreeHoleById(int id);

    /**
     * 根据id更新TreeHole
     *
     * @param treeHole 需要更新的TreeHole对象
     * @return 返回被更新的TreeHole对象的id
     * @author chaos
     * @since 2020-05-21
     */
    int updateTreeHole(TreeHole treeHole);

}
