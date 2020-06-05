package com.verygood.island.entity.vo;

import com.verygood.island.entity.Letter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName LetterVo
 * @Description 信件类VO
 * @author: chenyu
 * @date: 2020/5/23 13:43
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LetterVo implements Serializable {
    /*
    信件
     */
    private Letter letter;

    /*
    发送者昵称
     */
    private String nickname;

    /*
    邮票样式
     */
    private String stampName;

    /**
     * 收件人昵称
     */
    private String receiverName;
}
