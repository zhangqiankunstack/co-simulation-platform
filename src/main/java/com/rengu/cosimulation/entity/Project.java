package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/2/19 10:06
 */
@Entity
@Data
public class Project implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @NotBlank(message = ApplicationMessage.PROJECT_NAME_NOT_FOUND)
    private String name;
    private String orderNum;      // 令号
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String finishTime;                 // 节点计划
    @ManyToOne
    private Users pic;                        // 项目负责人
    @ManyToOne
    private Users creator;                    // 创建者
    private boolean deleted = false;               // 项目是否删除
    private int state;                             // 项目状态：0:未进行  1:进行中  2:已完成  3:超时
    private int secretClass;                       // 项目密级
    private String description;                    // 描述

}
