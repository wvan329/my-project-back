package cn.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2025-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "word", autoResultMap = true)
public class Word implements Serializable {

    private static final long serialVersionUID = 1L;

    //汉字
    private String word;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> words;
}
