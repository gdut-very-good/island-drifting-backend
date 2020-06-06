package com.verygood.island.controller;


import com.verygood.island.entity.Stamp;
import com.verygood.island.entity.User;
import com.verygood.island.entity.dto.ResultBean;
import com.verygood.island.exception.bizException.BizException;
import com.verygood.island.exception.bizException.BizExceptionCodeEnum;
import com.verygood.island.service.StampService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 邮票 前端控制器
 * </p>
 *
 * @author chaos
 * @version v1.0
 * @since 2020-05-04
 */
@RestController
@RequestMapping("/island/api/v1/stamp")
public class StampController {

    @Autowired
    private StampService stampService;

    /**
     * 查询分页数据
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResultBean<?> listByPage(@RequestParam(name = "page", defaultValue = "1") int page,
                                    @RequestParam(name = "pageSize", defaultValue = "1000") int pageSize,
                                    @RequestParam(name = "factor", defaultValue = "") String factor) {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        if (null == user) {
            throw new BizException(BizExceptionCodeEnum.NO_LOGIN);
        }
        return new ResultBean<>(stampService.listStampsByPage(page, pageSize, factor, user.getUserId()));
    }


    /**
     * 根据id查询
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResultBean<?> getById(@PathVariable("id") Integer id) {
        return new ResultBean<>(stampService.getStampById(id));
    }

    /**
     * 新增
     */
    @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public ResultBean<?> insert(@RequestBody Stamp stamp) {
        return new ResultBean<>(stampService.insertStamp(stamp));
    }

    /**
     * 删除
     */
    @Transactional
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResultBean<?> deleteById(@PathVariable("id") Integer id) {
        return new ResultBean<>(stampService.deleteStampById(id));
    }

    /**
     * 修改
     */
    @Transactional
    @RequestMapping(method = RequestMethod.PUT)
    public ResultBean<?> updateById(@RequestBody Stamp stamp) {
        return new ResultBean<>(stampService.updateStamp(stamp));
    }
}
