package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: XYmar
 * Date: 2019/2/28 11:12
 * 文件块
 */
@Data
public class Chunk implements Serializable {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private int chunkNumber;            //当前chunk编号
    private int totalChunks;            //文件总块数
    private long chunkSize;             //约定文件大小
    private long totalSize;             //文件总大小
    private String identifier;          //文件MD5值
    private String filename;            //文件名称
    private String relativePath;        //文件路径
}
