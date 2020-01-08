package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Chunk;
import com.rengu.cosimulation.entity.Files;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.FileRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import com.rengu.cosimulation.utils.FileMergeUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
//import static com.rengu.cosimulation.enums.ResultCode.FILE_MD5_EXISTED_ERROR;
/**
 * Author: XYmar
 * Date: 2019/2/28 11:14
 */
@Slf4j
@Service
@Transactional
public class FileService {
    private final FileRepository fileRepository;
    int concurrentNum = 10;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // 根据Md5判断文件是否存在
    public boolean hasFileByMD5(String MD5) {
        if (StringUtils.isEmpty(MD5)) {             //为空返回false
            return false;
        }
        return fileRepository.existsByMD5(MD5);             //通过MD5值是否存在true  or  false
    }

    // 保存文件块
    public void saveChunk(Chunk chunkEntity, MultipartFile multipartFile) throws IOException {
        java.io.File chunk = new java.io.File(ApplicationConfig.CHUNKS_SAVE_PATH + java.io.File.separator + chunkEntity.getIdentifier() + java.io.File.separator + chunkEntity.getChunkNumber() + ".tmp");
        chunk.getParentFile().mkdirs();
        chunk.createNewFile();
        IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(chunk));
    }

    int switchNum = concurrentNum;
    @Autowired
    private FileMergeUtils fileMergeUtils;

    // 根据Id删除文件
    @CacheEvict(value = "File_Cache", allEntries = true)
    public Files deleteFileById(String fileId) throws IOException {
        Files files = getFileById(fileId);
        FileUtils.forceDeleteOnExit(new java.io.File(files.getLocalPath()));
        fileRepository.delete(files);
        return files;
    }

    // 检查文件块是否存在
    public boolean hasChunk(Chunk chunkEntity) {
        java.io.File chunk = new java.io.File(ApplicationConfig.CHUNKS_SAVE_PATH + java.io.File.separator + chunkEntity.getIdentifier() + java.io.File.separator + chunkEntity.getChunkNumber() + ".tmp");
        return chunk.exists() && chunkEntity.getChunkSize() == FileUtils.sizeOf(chunk);
    }

    // 根据Id判断文件是否存在
    public boolean hasFileById(String fileId) {
        if (StringUtils.isEmpty(fileId)) {
            return false;
        }
        return fileRepository.existsById(fileId);
    }

    // 根据Id查询文件
    @Cacheable(value = "File_Cache", key = "#fileId")
    public Files getFileById(String fileId) {
        if (!hasFileById(fileId)) {
            throw new ResultException(ResultCode.FILE_ID_NOT_FOUND_ERROR);
        }
        return fileRepository.findById(fileId).get();
    }

    // 根据MD5查询文件
    @Cacheable(value = "File_Cache", key = "#MD5")
    public Files getFileByMD5(String MD5) {
        if (!hasFileByMD5(MD5)) {
            throw new ResultException(ResultCode.FILE_MD5_NOT_FOUND_ERROR);
        }
        return fileRepository.findByMD5(MD5).get();
    }

    // 查询所有文件
    public Page<Files> getFiles(Pageable pageable) {
        return fileRepository.findAll(pageable);
    }


//    private Files mergeChunks(java.io.File file, Chunk chunkEntity) throws IOException {
//        file.delete();
//        file.getParentFile().mkdirs();
//        file.createNewFile();
//        for (int i = 1; i <= chunkEntity.getTotalChunks(); i++) {
//            java.io.File chunk = new java.io.File(ApplicationConfig.CHUNKS_SAVE_PATH + java.io.File.separator + chunkEntity.getIdentifier() + java.io.File.separator + i + ".tmp");
//            if (chunk.exists()) {
//                FileUtils.writeByteArrayToFile(file, FileUtils.readFileToByteArray(chunk), true);
//            } else {
//                throw new ResultException(ResultCode.FILE_CHUNK_NOT_FOUND_ERROR);
//            }
//        }
//        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
//        if (!chunkEntity.getIdentifier().equals(DigestUtils.md5Hex(fileInputStream))) {
//            throw new RuntimeException("文件合并失败，请检查：" + file.getAbsolutePath() + "是否正确。");
//        }
//        return saveFile(file);
//    }

    // 合并文件块
    public Files mergeChunks(Chunk chunk) throws IOException, ExecutionException, InterruptedException {
        if (hasFileByMD5(chunk.getIdentifier())) {                  //根据MD5值判断文件是否存在，如果存在
            return getFileByMD5(chunk.getIdentifier());             //根据MD5值查询文件
        } else {
            java.io.File file = null;
            String extension = FilenameUtils.getExtension(chunk.getFilename());     //获取文件的扩展名
            //System.out.println("扩展名"+extension);
            if (StringUtils.isEmpty(extension)) {       //判断扩展名是否为空
                file = new java.io.File(ApplicationConfig.FILES_SAVE_PATH + java.io.File.separator + chunk.getFilename());//如果文件为空，保存路径+MD5值
            } else {
                file = new java.io.File(ApplicationConfig.FILES_SAVE_PATH + java.io.File.separator + chunk.getFilename());//不为空+保存路径+文件扩展名
            }
            return mergeChunks(file, chunk);
        }
    }

    // 保存文件信息
    @CacheEvict(value = "File_Cache", allEntries = true)
    public Files saveFile(java.io.File file) throws IOException {
        Files filesEntity = new Files();
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        String MD5 = DigestUtils.md5Hex(fileInputStream);
        if (hasFileByMD5(MD5)) {
            throw new ResultException(ResultCode.FILE_MD5_EXISTED_ERROR);       //todo:该文件MD5值已经存在
        }
        filesEntity.setMD5(MD5);         // MD5
        filesEntity.setPostfix(FilenameUtils.getExtension(file.getName()));                // 后缀
        filesEntity.setFileSize(FileUtils.sizeOf(file));                                   // 大小
        filesEntity.setLocalPath(file.getAbsolutePath());                                  // 路径
        return fileRepository.save(filesEntity);
    }

    private Files mergeChunks(File file, Chunk chunkEntity) throws IOException, ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();            //起始时间
        file.delete();
        file.getParentFile().mkdirs();                          //得到文件名的父类所有路径
        file.createNewFile();                                   //创建一个新的文件
        if (chunkEntity.getTotalChunks() >= switchNum) {             //todo:假如文件块数大于等于10
            int last = chunkEntity.getTotalChunks() % concurrentNum;        //用户文件总块数除以10，得到余数
            int baseStep = (chunkEntity.getTotalChunks() - last) / concurrentNum;          //计算每10兆一块，计算可以分几块shunks
            System.out.println("余数=" + baseStep);
            int startPoint = 1;
            Map<Integer, CompletableFuture<File>> fileMap = new HashMap<>();
            for (int i = 1; i <= concurrentNum; i++) {
                int step = baseStep;
                if (i == concurrentNum) {
                    step = step + last;
                }
                fileMap.put(i, fileMergeUtils.megeChunks(startPoint, startPoint + step - 1, chunkEntity));
                startPoint = startPoint + step;
            }
            FileOutputStream fileOutputStream = FileUtils.openOutputStream(file, true);             //开启连接输出流，可以发送内容
            for (int i = 1; i <= concurrentNum; i++) {
                File block = fileMap.get(i).get();
                if (block.exists()) {
                    IOUtils.copy(FileUtils.openInputStream(block), fileOutputStream);
//                    FileUtils.writeByteArrayToFile(file, FileUtils.readFileToByteArray(block), true);
                } else {
                    IOUtils.closeQuietly(fileOutputStream);
                    throw new ResultException(ResultCode.FILE_CHUNK_NOT_FOUND_ERROR);       //文件块不存在或不合法
                }
            }
            IOUtils.closeQuietly(fileOutputStream);
        } else {
            for (int i = 1; i <= chunkEntity.getTotalChunks(); i++) {
                java.io.File chunk = new java.io.File(ApplicationConfig.CHUNKS_SAVE_PATH + java.io.File.separator + chunkEntity.getIdentifier() + java.io.File.separator + i + ".tmp");
                if (chunk.exists()) {
                    FileUtils.writeByteArrayToFile(file, FileUtils.readFileToByteArray(chunk), true);
                } else {
                    throw new ResultException(ResultCode.FILE_CHUNK_NOT_FOUND_ERROR);
                }
            }
        }
        log.info(file.getAbsolutePath() + "合成完毕！总大小：" + FileUtils.sizeOf(file) + ",开始时间：" + startTime + ",耗时：" + (System.currentTimeMillis() - startTime));
        return saveFile(file);
    }
}
